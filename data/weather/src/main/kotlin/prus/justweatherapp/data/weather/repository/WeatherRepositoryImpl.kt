package prus.justweatherapp.data.weather.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import prus.justweatherapp.core.common.result.RequestResult
import prus.justweatherapp.core.common.result.map
import prus.justweatherapp.core.common.result.toRequestResult
import prus.justweatherapp.data.weather.mapper.mapToDBO
import prus.justweatherapp.data.weather.mapper.mapToDomainModel
import prus.justweatherapp.data.weather.mergestrategy.RequestResultMergeStrategy
import prus.justweatherapp.domain.weather.model.Weather
import prus.justweatherapp.domain.weather.repository.WeatherRepository
import prus.justweatherapp.local.db.dao.LocationsDao
import prus.justweatherapp.local.db.dao.WeatherDao
import prus.justweatherapp.local.db.entity.WeatherEntity
import prus.justweatherapp.remote.datasource.WeatherDataSource
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val weatherDataSource: WeatherDataSource,
    private val locationsDao: LocationsDao,
    private val weatherDao: WeatherDao,
) : WeatherRepository {

    private val currentWeatherDataRefreshTimeMinutes = 30
    private val forecastWeatherListMaxSize = 40

    override fun getCurrentWeatherByLocationId(
        locationId: String
    ): Flow<RequestResult<Weather?>> = flow {
        val mergeStrategy = RequestResultMergeStrategy<Weather?>()

        getCurrentWeatherFromDb(locationId)
            .onEach { dbRequestResult ->
                when (dbRequestResult) {
                    is RequestResult.Error -> {
                        emitAll(
                            flowOf(dbRequestResult)
                                .combine(
                                    getCurrentWeatherFromServer(locationId),
                                    mergeStrategy::merge
                                )
                        )
                    }

                    else -> {
                        emit(dbRequestResult)
                    }
                }
            }.collect()
    }

    private fun getCurrentWeatherFromDb(
        locationId: String
    ): Flow<RequestResult<Weather?>> = flow {
        val weatherEntity = weatherDao.getCurrentWeatherByLocationId(
            locationId = locationId,
            dateFrom = Clock.System.now()
                .plus(-currentWeatherDataRefreshTimeMinutes, DateTimeUnit.MINUTE)
                .toLocalDateTime(TimeZone.currentSystemDefault())
        )
        if (weatherEntity == null) {
            emit(
                RequestResult.Error(
                    error = Throwable("No current weather data in the database")
                )
            )
            return@flow
        }

        emit(RequestResult.Success(weatherEntity.mapToDomainModel()))
    }.onStart { emit(RequestResult.Loading()) }

    private suspend fun getCurrentWeatherFromServer(
        locationId: String
    ): Flow<RequestResult<Weather?>> = flow {
        val location = locationsDao.getLocationById(
            locationId = locationId
        )
        if (location == null) {
            emit(
                RequestResult.Error(
                    error = Throwable("Cannot find the location in the database")
                )
            )
            return@flow
        }

        weatherDataSource.getCurrentWeatherData(location.lat, location.lng)
            .toRequestResult()
            .also { apiRequestResult ->
                if (apiRequestResult is RequestResult.Success) {
                    val dbo = listOf(checkNotNull(apiRequestResult.data).mapToDBO(locationId))
                    saveDbosToDb(
                        response = dbo
                    )
                }
                emit(apiRequestResult.map { it.mapToDomainModel(locationId) })
            }
    }.onStart { emit(RequestResult.Loading()) }

    private suspend fun saveDbosToDb(response: List<WeatherEntity>) {
        weatherDao.insertAll(response)
    }

    override fun getForecastWeatherByLocationId(
        locationId: String,
    ): Flow<RequestResult<List<Weather>>> = flow {
        val mergeStrategy = RequestResultMergeStrategy<List<Weather>>()

        getForecastWeatherFromDb(locationId)
            .onEach { dbRequestResult ->
                if (dbRequestResult is RequestResult.Success &&
                    checkNotNull(dbRequestResult.data).size < forecastWeatherListMaxSize
                ) {
                    emitAll(
                        flowOf(dbRequestResult)
                            .combine(
                                getForecastWeatherFromServer(locationId),
                                mergeStrategy::merge
                            )
                    )
                } else {
                    emit(dbRequestResult)
                }
            }.collect()
    }

    private fun getForecastWeatherFromDb(
        locationId: String
    ): Flow<RequestResult<List<Weather>>> = flow {
        emit(
            RequestResult.Success(
                weatherDao.getForecastWeatherByLocationId(
                    locationId = locationId,
                    limit = forecastWeatherListMaxSize
                ).map { it.mapToDomainModel() }
            )
        )
    }.onStart { RequestResult.Loading(data = null) }

    private suspend fun getForecastWeatherFromServer(
        locationId: String
    ): Flow<RequestResult<List<Weather>>> = flow {
        val location = locationsDao.getLocationById(
            locationId = locationId
        )
        if (location == null) {
            emit(
                RequestResult.Error(
                    error = Throwable("Cannot find the location in the database")
                )
            )
            return@flow
        }

        weatherDataSource.getForecastWeatherData(location.lat, location.lng)
            .toRequestResult()
            .also { apiRequestResult ->
                if (apiRequestResult is RequestResult.Success) {
                    val data = checkNotNull(apiRequestResult.data)
                    val dbos = data.list.map { it.mapToDBO(locationId, data.city) }
                    saveDbosToDb(
                        response = dbos
                    )
                }
                emit(apiRequestResult.map { it.mapToDomainModel(locationId) })
            }
    }.onStart { emit(RequestResult.Loading()) }
}