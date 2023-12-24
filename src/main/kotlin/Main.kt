import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ui.Editor
import utils.Constants.WINDOW_HEIGHT
import utils.Constants.WINDOW_WIDTH
import javax.swing.JFrame

@Composable
@Preview
fun App() {
    MaterialTheme {
        Editor()
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(position = WindowPosition(Alignment.Center), size = DpSize(WINDOW_WIDTH.dp, WINDOW_HEIGHT.dp)),
        resizable = false,
        title = "Serrano Dash"
    ) {
        App()
    }
}
