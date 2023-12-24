package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun EditorArrow(
    action: () -> Unit,
    image: ImageVector
) {
    IconButton(
        onClick = action,
        modifier = Modifier.padding(5.dp).clip(RoundedCornerShape(10.dp)).background(color = Color.Green)
    ) {
        Icon(imageVector = image, contentDescription = null, modifier = Modifier.size(50.dp))
    }
}