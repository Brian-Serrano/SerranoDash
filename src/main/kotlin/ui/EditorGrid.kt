package ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import classes.EditorState
import controller.EditorController

@Composable
fun EditorGrid(
    images: List<ImageBitmap>,
    ids: List<Int>,
    editorState: EditorState,
    editorController: EditorController
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
            if (images.isNotEmpty() && ids.isNotEmpty()) {
                val imagesOne = images.subList(0, if (images.size >= 12) 12 else images.size)
                val idsOne = ids.subList(0, if (ids.size >= 12) 12 else ids.size)

                imagesOne.forEachIndexed { index, imageBitmap ->
                    Button(
                        onClick = { editorController.updateEditorState(editorState.copy(selectedItem = idsOne[index])) },
                        modifier = Modifier.fillMaxHeight().width(70.dp).padding(5.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (editorState.selectedItem == idsOne[index]) Color.DarkGray else Color.Gray
                        )
                    ) {
                        Image(
                            bitmap = imageBitmap,
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
            if (images.size > 12 && ids.size > 12) {
                val imagesTwo = images.subList(12, if (images.size >= 24) 24 else images.size)
                val idsTwo = ids.subList(12, if (ids.size >= 24) 24 else ids.size)

                imagesTwo.forEachIndexed { index, imageBitmap ->
                    Button(
                        onClick = { editorController.updateEditorState(editorState.copy(selectedItem = idsTwo[index])) },
                        modifier = Modifier.fillMaxHeight().width(70.dp).padding(5.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (editorState.selectedItem == idsTwo[index]) Color.DarkGray else Color.Gray
                        )
                    ) {
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = null,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }
            }
        }
    }
}