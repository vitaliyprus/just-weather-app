package prus.justweatherapp.local.db.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import prus.justweatherapp.local.db.AppDatabase
import prus.justweatherapp.local.db.entity.LocationEntity
import prus.justweatherapp.local.db.entity.WeatherEntity
import prus.justweatherapp.local.db.model.MainWeatherDataDBO
import prus.justweatherapp.local.db.model.WindDBO
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
@SmallTest
class WeatherDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var weatherDao: WeatherDao
    private lateinit var locationsDao: LocationsDao

    private val dbLocations = initDbLocations()

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            context = ApplicationProvider.getApplicationContext(),
            klass = AppDatabase::class.java
        ).allowMainThreadQueries().build()

        weatherDao = database.weatherDao()
        locationsDao = database.locationsDao()

        runBlocking {
            locationsDao.insertAll(dbLocations)
        }
    }

    private fun initDbLocations(): List<LocationEntity> {
        val locations = mutableListOf<LocationEntity>()
        for (i in 1..30) {
            locations.add(
                LocationEntity(
                    id = "id_$i",
                    city = "City$i",
                    adminName = "Admin$i",
                    country = "Country$i",
                )
            )
        }
        return locations
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAll() = runTest {
        val locationId1 = "id_1"
        val locationId2 = "id_2"
        val time1 = getDateTime()
        val time2 = getDateTime(1)

        val list = listOf(
            createWeatherEntity(locationId1, time1),
            createWeatherEntity(locationId1, time2),
            createWeatherEntity(locationId2, time1),
            createWeatherEntity(locationId2, time2),
            createWeatherEntity(locationId2, time2),
        )

        val countBefore = weatherDao.getWeatherCount()
        weatherDao.insertAll(list)

        assert(weatherDao.getWeatherCount() == countBefore + 4)
    }

    @Test
    fun getWeatherByLocationId() = runTest {
        val locationId1 = "id_1"
        val locationId2 = "id_2"
        val time1 = getDateTime()
        val time2 = getDateTime(-2)
        val time3 = getDateTime(-5)

        val list = listOf(
            createWeatherEntity(locationId1, time1),
            createWeatherEntity(locationId1, time2),
            createWeatherEntity(locationId1, time3),
            createWeatherEntity(locationId2, time1),
            createWeatherEntity(locationId2, time2),
            createWeatherEntity(locationId2, time3),
        )
        weatherDao.insertAll(list)

        val data = weatherDao.getCurrentWeatherByLocationId(locationId1)

        assert(
            data != null && abs(
                data.dateTime.toInstant(TimeZone.currentSystemDefault())
                    .toEpochMilliseconds() -
                        time1.toInstant(TimeZone.currentSystemDefault())
                            .toEpochMilliseconds()
            ) < 1000
        )
    }

    @Test
    fun deleteOutdatedEntities() = runTest {
        val locationId1 = "id_1"
        val locationId2 = "id_2"
        val time1 = getDateTime()
        val time2 = getDateTime(-2)
        val time3 = getDateTime(-5)

        val list = listOf(
            createWeatherEntity(locationId1, time1),
            createWeatherEntity(locationId1, time2),
            createWeatherEntity(locationId1, time3),
            createWeatherEntity(locationId2, time1),
            createWeatherEntity(locationId2, time2),
            createWeatherEntity(locationId2, time3),
        )
        weatherDao.insertAll(list)

        val countBefore = weatherDao.getWeatherCount()

        weatherDao.deleteOutdatedEntities()

        assert(weatherDao.getWeatherCount() == countBefore - 2)
    }

    private fun getDateTime(shiftHours: Int = 0): LocalDateTime {
        return Clock.System.now()
            .plus(shiftHours, DateTimeUnit.HOUR)
            .toLocalDateTime(TimeZone.currentSystemDefault())
    }

    private fun createWeatherEntity(locationId: String, dateTime: LocalDateTime): WeatherEntity {
        return WeatherEntity(
            locationId = locationId,
            dateTime = dateTime,
            temp = 290.0,
            feelsLike = 290.0,
            humidity = 100.0,
            pop = 0.0,
            rain = 0.0,
            showers = 0.0,
            snowfall = 0.0,
            weatherCode= 0,
            pressure = 1000.0,
            cloudCover = 100.0,
            visibility = 10000.0,
            windSpeed = 100.0,
            windDirection = 0.0,
            windGusts = 10.0,
            uvi = 1.0,
            timestamp = dateTime,
        )
    }

    private fun createMainWeatherData(): MainWeatherDataDBO {
        return MainWeatherDataDBO(
            temp = 290.0,
            feelsLike = 290.0,
            tempMin = 290.0,
            tempMax = 290.0,
            pressure = 1000.0,
            humidity = 100.0,
        )
    }

    private fun createWind(): WindDBO {
        return WindDBO(
            speed = 10.0,
            gust = 10.0,
            degree = 0.0
        )
    }
}