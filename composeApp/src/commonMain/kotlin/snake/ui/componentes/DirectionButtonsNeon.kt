package snake.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import snake.composeapp.generated.resources.Res
import snake.composeapp.generated.resources.cd_down
import snake.composeapp.generated.resources.cd_left
import snake.composeapp.generated.resources.cd_play_pause
import snake.composeapp.generated.resources.cd_right
import snake.composeapp.generated.resources.cd_up
import snake.composeapp.generated.resources.ic_down
import snake.composeapp.generated.resources.ic_left
import snake.composeapp.generated.resources.ic_pause
import snake.composeapp.generated.resources.ic_play
import snake.composeapp.generated.resources.ic_right
import snake.composeapp.generated.resources.ic_up
import snake.model.Direction
import snake.ui.SnakeViewModel

// ========================
// Controls neon
// ========================
@Composable
fun DirectionButtonsNeon(viewModel: SnakeViewModel, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        NeonIconButton(
            resId = Res.drawable.ic_up,
            contentDescription = stringResource(Res.string.cd_up),
            onClick = { viewModel.onDirectionChanged(Direction.UP) }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NeonIconButton(
                resId = Res.drawable.ic_left,
                contentDescription = stringResource(Res.string.cd_left),
                onClick = { viewModel.onDirectionChanged(Direction.LEFT) }
            )

            NeonIconButton(
                resId = if (viewModel.uiState.value.isPaused) Res.drawable.ic_play else Res.drawable.ic_pause,
                contentDescription = stringResource(Res.string.cd_play_pause),
                highlight = true,
                onClick = { viewModel.togglePause() }
            )

            NeonIconButton(
                resId = Res.drawable.ic_right,
                contentDescription = stringResource(Res.string.cd_right),
                onClick = { viewModel.onDirectionChanged(Direction.RIGHT) }
            )
        }

        NeonIconButton(
            resId = Res.drawable.ic_down,
            contentDescription = stringResource(Res.string.cd_down),
            onClick = { viewModel.onDirectionChanged(Direction.DOWN) }
        )
    }
}
