package prus.justweatherapp.feature.weather.location.current

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import prus.justweatherapp.core.ui.UiText
import prus.justweatherapp.core.ui.components.JwaLabeledText
import prus.justweatherapp.core.ui.shimmer.ShimmerRectangle
import prus.justweatherapp.feature.weather.R
import prus.justweatherapp.theme.AppTheme
import prus.justweatherapp.theme.contentPaddings

@Composable
fun CurrentWeatherUI(
    modifier: Modifier,
    locationId: String,
    viewModel: CurrentWeatherViewModel = hiltViewModel<CurrentWeatherViewModel,
            CurrentWeatherViewModel.ViewModelFactory>
        (key = locationId) { factory ->
        factory.create(locationId)
    },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    CurrentWeatherUI(
        modifier = modifier,
        state = state
    )
}

@Composable
private fun CurrentWeatherUI(
    modifier: Modifier = Modifier,
    state: CurrentWeatherUiState
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        shape = RoundedCornerShape(25.dp),
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            disabledContentColor = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.5f),
        )
    )
    {
        val context = LocalContext.current

        if (state is CurrentWeatherUiState.Error) {
            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
        }

        val weather: CurrentWeatherUiModel? =
            if (state is CurrentWeatherUiState.Success) state.weather
            else null

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .contentPaddings(),
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    if (weather != null) {
                        Text(
                            text = weather.dateTime,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = weather.temp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 56.sp,
                        )
                        Text(
                            text = weather.weatherConditions.asString(),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            text = weather.feelsLike,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    } else {
                        ShimmerRectangle(
                            modifier = Modifier
                                .width(140.dp)
                                .height(16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ShimmerRectangle(
                            modifier = Modifier
                                .width(130.dp)
                                .height(70.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ShimmerRectangle(
                            modifier = Modifier
                                .width(90.dp)
                                .height(18.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ShimmerRectangle(
                            modifier = Modifier
                                .width(120.dp)
                                .height(18.dp)
                        )
                    }
                }

                if (weather != null) {
                    Image(
                        modifier = Modifier
                            .size(130.dp),
                        painter = painterResource(id = weather.conditionImageResId),
                        contentDescription = weather.weatherConditions.asString()
                    )
                } else {
                    ShimmerRectangle(
                        modifier = Modifier
                            .size(130.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            )  {
                if (weather != null) {
                    JwaLabeledText(
                        label = stringResource(id = R.string.sunrise),
                        text = weather.sunrise
                    )
                    JwaLabeledText(
                        label = stringResource(id = R.string.daylight),
                        text = weather.daylight
                    )
                    JwaLabeledText(
                        label = stringResource(id = R.string.sunset),
                        text = weather.sunset
                    )
                } else {
                    ShimmerRectangle(
                        modifier = Modifier
                            .width(70.dp)
                            .height(40.dp)
                    )
                    ShimmerRectangle(
                        modifier = Modifier
                            .width(70.dp)
                            .height(40.dp)
                    )
                    ShimmerRectangle(
                        modifier = Modifier
                            .width(70.dp)
                            .height(40.dp)
                    )
                }
            }

        }

    }
}

@PreviewLightDark
@Composable
private fun CurrentWeatherUILoadingPreview(
) {
    AppTheme {
        Surface {
            CurrentWeatherUI(
                state = CurrentWeatherUiState.Loading
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun CurrentWeatherUISuccessPreview(
) {
    AppTheme {
        Surface {
            CurrentWeatherUI(
                state = CurrentWeatherUiState.Success(
                    weather = CurrentWeatherUiModel(
                        dateTime = "Mon, 18 March, 15:40",
                        temp = "15ºC",
                        feelsLike = "Feels like 14ºC",
                        tempMinMax = "↓6º ↑18º",
                        weatherConditions = UiText.DynamicString("Mostly cloudy"),
                        conditionImageResId = prus.justweatherapp.core.ui.R.drawable.mostlycloudy,
                        sunrise = "07:07",
                        daylight = "12:01",
                        sunset = "19:08",
                    )
                )
            )
        }
    }
}