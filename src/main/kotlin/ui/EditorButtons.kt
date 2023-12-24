package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun EditorButtons(
    texts: List<String>,
    actions: List<() -> Unit>,
    conditions: List<Boolean>
) {
    Column(modifier = Modifier.fillMaxHeight().width(150.dp)) {
        texts.forEachIndexed { idx, text ->
            Button(
                onClick = actions[idx],
                modifier = Modifier.fillMaxSize().padding(5.dp).weight(1F),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (conditions[idx]) Color(0xFF006600) else Color.Green
                )
            ) {
                Text(text)
            }
        }
    }
}