package ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp

@Composable
fun RowScope.EditorButtons2(
    images: List<ImageBitmap>,
    actions: List<() -> Unit>,
    conditions: List<Boolean>
) {
    images.forEachIndexed { idx, img ->
        Button(
            onClick = actions[idx],
            modifier = Modifier.size(60.dp, 40.dp).padding(top = 10.dp, start = 10.dp, end = 10.dp).weight(1F),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = if (conditions[idx]) Color(0xBB000000) else Color(0x88000000)
            ),
            shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
        ) {
            Image(
                bitmap = img,
                contentDescription = null,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}