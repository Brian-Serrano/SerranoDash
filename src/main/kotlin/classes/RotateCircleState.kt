package classes

import androidx.compose.ui.geometry.Offset

data class RotateCircleState(
    val angle: Double,
    val handle: Offset,
    val position: Offset
)