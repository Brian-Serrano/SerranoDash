package ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun EditorGrid2(
    images: List<ImageVector>,
    actions: List<() -> Unit>
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize().weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (images.isNotEmpty() && actions.isNotEmpty()) {
                val imagesOne = images.subList(0, if (images.size >= 12) 12 else images.size)
                val actionsOne = actions.subList(0, if (actions.size >= 12) 12 else actions.size)

                imagesOne.forEachIndexed { index, imageVector ->
                    Button(
                        onClick = actionsOne[index],
                        modifier = Modifier.fillMaxHeight().width(70.dp).padding(5.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Green
                        )
                    ) {
                        Icon(
                            imageVector = imageVector,
                            contentDescription = null,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxSize().weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (images.size > 12 && actions.size > 12) {
                val imagesTwo = images.subList(12, if (images.size >= 24) 24 else images.size)
                val actionsTwo = actions.subList(12, if (actions.size >= 24) 24 else actions.size)

                imagesTwo.forEachIndexed { index, imageVector ->
                    Button(
                        onClick = actionsTwo[index],
                        modifier = Modifier.fillMaxHeight().width(70.dp).padding(5.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Green
                        )
                    ) {
                        Icon(
                            imageVector = imageVector,
                            contentDescription = null,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }
            }
        }
    }
}