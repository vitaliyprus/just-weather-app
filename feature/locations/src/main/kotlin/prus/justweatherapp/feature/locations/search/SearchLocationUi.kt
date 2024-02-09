package prus.justweatherapp.feature.locations.search

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import prus.justweatherapp.core.presentation.ui.components.MessageScreen
import prus.justweatherapp.feature.locations.R
import prus.justweatherapp.theme.AppTheme

@Composable
internal fun SearchLocationsListUi(
    state: SearchLocationScreenState
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = ColorPainter(MaterialTheme.colorScheme.background),
                contentScale = ContentScale.FillBounds
            ),
        contentAlignment = Alignment.Center
    )
    {
        when (state) {

            is SearchLocationScreenState.Error -> {
//                TODO()
            }

            SearchLocationScreenState.Loading -> {
//                TODO()
            }

            SearchLocationScreenState.Empty -> {
                MessageScreen(
                    title = stringResource(id = R.string.nothing_found),
                    subtitle = stringResource(id = R.string.nothing_found_hint)
                )
            }

            is SearchLocationScreenState.Success -> {
                val locations = state.locations.collectAsLazyPagingItems()
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        count = locations.itemCount,
                        key = locations.itemKey { it.id }
                    ) { index ->
                        locations[index]?.let { location ->
                            SearchLocationListItem(location)
                        }
                    }
                }
            }
        }
    }
}

@Preview(
    name = "Empty search locations",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SearchLocationsListUiPreview() {
    AppTheme {
        Surface {
            SearchLocationsListUi(
                state = SearchLocationScreenState.Empty
            )
        }
    }
}