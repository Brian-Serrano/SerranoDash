package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import classes.ProcessType
import controller.EditorController
import controller.ImageLoader
import utils.Constants.ROTATE_CIRCLE_RADIUS
import utils.Constants.TILE_SIZE
import kotlin.math.ceil

@Preview
@Composable
fun Editor(
    editorController: EditorController = remember { EditorController() },
    imageLoader: ImageLoader = remember { ImageLoader() }
) {

    val objects by editorController.objects.collectAsState()
    val camera by editorController.camera.collectAsState()
    val gridCamera by editorController.gridCamera.collectAsState()
    val editorState by editorController.editorState.collectAsState()
    val highlighter by editorController.highlighter.collectAsState()
    val rotateCircleState by editorController.rotateCircleState.collectAsState()

    val platform1Img = imageLoader.images.subList(0, 24)
    val platform2Img = imageLoader.images.subList(24, 48)
    val platform3Img = imageLoader.images.subList(48, 72)
    val platform4Img = imageLoader.images.subList(72, 84)
    val other1Img = imageLoader.images.subList(84, 108)
    val other2Img = imageLoader.images.subList(108, 114)
    val pit1Img = imageLoader.images.subList(114, 138)
    val pit2Img = imageLoader.images.subList(138, 139)
    val outlineImg = imageLoader.images.subList(139, 158)
    val spikeImg = imageLoader.images.subList(158, 173)
    val transportImg = imageLoader.images.subList(173, 186)

    val platform1Idx = imageLoader.indices.subList(0, 24)
    val platform2Idx = imageLoader.indices.subList(24, 48)
    val platform3Idx = imageLoader.indices.subList(48, 72)
    val platform4Idx = imageLoader.indices.subList(72, 84)
    val other1Idx = imageLoader.indices.subList(84, 108)
    val other2Idx = imageLoader.indices.subList(108, 114)
    val pit1Idx = imageLoader.indices.subList(114, 138)
    val pit2Idx = imageLoader.indices.subList(138, 139)
    val outlineIdx = imageLoader.indices.subList(139, 158)
    val spikeIdx = imageLoader.indices.subList(158, 173)
    val transportIdx = imageLoader.indices.subList(173, 186)

    val icons = listOf(
        Icons.Filled.KeyboardArrowDown,
        Icons.Filled.KeyboardArrowUp,
        Icons.Filled.KeyboardArrowRight,
        Icons.Filled.KeyboardArrowLeft,
        Icons.Filled.KeyboardDoubleArrowDown,
        Icons.Filled.KeyboardDoubleArrowUp,
        Icons.Filled.KeyboardDoubleArrowRight,
        Icons.Filled.KeyboardDoubleArrowLeft,
        Icons.Filled.RotateRight,
        Icons.Filled.RotateLeft,
        Icons.Filled.Rotate90DegreesCw,
        Icons.Filled.Rotate90DegreesCcw
    )
    val actions = listOf(
        { editorController.onMove(ProcessType.MOVE, 0f, 4f) },
        { editorController.onMove(ProcessType.MOVE, 0f, -4f) },
        { editorController.onMove(ProcessType.MOVE, 4f, 0f) },
        { editorController.onMove(ProcessType.MOVE, -4f, 0f) },
        { editorController.onMove(ProcessType.MOVE, 0f, 32f) },
        { editorController.onMove(ProcessType.MOVE, 0f, -32f) },
        { editorController.onMove(ProcessType.MOVE, 32f, 0f) },
        { editorController.onMove(ProcessType.MOVE, -32f, 0f) },
        { editorController.onRotate(ProcessType.ROTATE, 90) },
        { editorController.onRotate(ProcessType.ROTATE, -90) },
        { editorController.onRotate(ProcessType.ROTATE, 45) },
        { editorController.onRotate(ProcessType.ROTATE, -45) }
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { editorController.onDragStart(it) },
                            onDragEnd = { editorController.onDragEnd() },
                            onDragCancel = { editorController.onDragEnd() },
                            onDrag = { change, dragAmount ->
                                editorController.onDrag(dragAmount)
                                change.consume()
                            }
                        )
                    }.pointerInput(PointerEventType.Press) {
                        detectTapGestures { offset ->
                            editorController.onClick(offset)
                        }
                    }
            ) {
                val horizontalGrids = ceil(size.width / TILE_SIZE).toInt()
                val verticalGrids = ceil(size.height / TILE_SIZE).toInt()
                for (row in 0..verticalGrids) {
                    for (col in 0..horizontalGrids) {
                        drawPath(
                            path = Path().apply {
                                val xOffset = (col * TILE_SIZE).toFloat() - camera.x + gridCamera.x
                                val yOffset = (row * TILE_SIZE).toFloat() - camera.y + gridCamera.y
                                moveTo(xOffset, yOffset)
                                lineTo(xOffset + TILE_SIZE, yOffset)
                                lineTo(xOffset + TILE_SIZE, yOffset + TILE_SIZE)
                                lineTo(xOffset, yOffset + TILE_SIZE)
                            },
                            color = Color.Black,
                            style = Stroke(0.5F)
                        )
                    }
                }
                objects.forEach { obj ->
                    rotate(degrees = obj.rotation.toFloat(), pivot = Offset(obj.x - camera.x + 16, obj.y - camera.y + 16)) {
                        translate(obj.x - camera.x, obj.y - camera.y) {
                            drawImage(
                                image = imageLoader.images[obj.blockId],
                                dstOffset = IntOffset.Zero,
                                dstSize = IntSize(TILE_SIZE, TILE_SIZE),
                                colorFilter = if (editorState.highlightedItems.any { it == obj.id }) ColorFilter.tint(Color(0x8800CC00), BlendMode.Color) else null
                            )
                        }
                    }
                }
                if (highlighter.isEnabled) {
                    drawRect(
                        color = Color(0x8800CC00),
                        topLeft = Offset(
                            if (highlighter.width >= highlighter.x) highlighter.x else highlighter.width,
                            if (highlighter.height >= highlighter.y) highlighter.y else highlighter.height
                        ),
                        size = Size(
                            if (highlighter.width >= highlighter.x) highlighter.width - highlighter.x else highlighter.x - highlighter.width,
                            if (highlighter.height >= highlighter.y) highlighter.height - highlighter.y else highlighter.y - highlighter.height
                        )
                    )
                }
                if (editorState.isRotate && editorState.highlightedItems.isNotEmpty()) {

                    editorController.updateRotateCircle(rotateCircleState.copy(position = editorController.computeHighlightedCenterPivotWithRotateLock()))

                    editorController.updateRotateHandle()

                    drawCircle(
                        color = Color.Black.copy(alpha = 0.10f),
                        style = Stroke(10f),
                        radius = ROTATE_CIRCLE_RADIUS,
                        center = Offset((rotateCircleState.position.x + 16) - camera.x, (rotateCircleState.position.y + 16) - camera.y)
                    )
                    drawArc(
                        color = Color.Cyan,
                        startAngle = 0f,
                        sweepAngle = rotateCircleState.angle.toFloat(),
                        useCenter = false,
                        style = Stroke(10f),
                        size = Size(ROTATE_CIRCLE_RADIUS * 2, ROTATE_CIRCLE_RADIUS * 2),
                        topLeft = Offset(((rotateCircleState.position.x + 16) - camera.x) - ROTATE_CIRCLE_RADIUS, ((rotateCircleState.position.y + 16) - camera.y) - ROTATE_CIRCLE_RADIUS)
                    )

                    drawCircle(
                        color = Color.Green,
                        center = Offset(rotateCircleState.handle.x - camera.x, rotateCircleState.handle.y - camera.y),
                        radius = 15f
                    )
                }
            }
            Column(modifier = Modifier.align(Alignment.TopStart)) {
                Button(
                    onClick = { editorController.undo() },
                    modifier = Modifier.size(70.dp).padding(5.dp).clip(RoundedCornerShape(10.dp)),
                    enabled = editorState.isUndoEnabled,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
                ) {
                    Icon(imageVector = Icons.Filled.Undo, contentDescription = null, modifier = Modifier.size(50.dp))
                }
                Button(
                    onClick = { editorController.redo() },
                    modifier = Modifier.size(70.dp).padding(5.dp).clip(RoundedCornerShape(10.dp)),
                    enabled = editorState.isRedoEnabled,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
                ) {
                    Icon(imageVector = Icons.Filled.Redo, contentDescription = null, modifier = Modifier.size(50.dp))
                }
                Button(
                    onClick = { editorController.deselect() },
                    modifier = Modifier.size(70.dp).padding(5.dp).clip(RoundedCornerShape(10.dp)),
                    enabled = editorState.highlightedItems.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
                ) {
                    Icon(imageVector = Icons.Filled.Deselect, contentDescription = null, modifier = Modifier.size(50.dp))
                }
            }
            if (editorState.tabIndex == 0) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 100.dp)
                ) {
                    EditorButtons2(
                        listOf(imageLoader.images[0], imageLoader.images[84], imageLoader.images[114], imageLoader.images[139], imageLoader.images[158], imageLoader.images[173]),
                        (0..5).map { { editorController.updateEditorState(editorState.copy(blocksTabIndex = it)) } },
                        (0..5).map { editorState.blocksTabIndex == it }
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxSize().weight(0.3f).background(color = Color(0x88000000)),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            EditorButtons(
                texts = listOf("Save", "Build", "Edit", "Delete"),
                actions = listOf(
                    { editorController.save() },
                    { editorController.updateEditorState(editorState.copy(tabIndex = 0)) },
                    { editorController.updateEditorState(editorState.copy(tabIndex = 1)) },
                    { editorController.updateEditorState(editorState.copy(tabIndex = 2)) }
                ),
                conditions = listOf(false, editorState.tabIndex == 0, editorState.tabIndex == 1, editorState.tabIndex == 2)
            )
            Row(modifier = Modifier.fillMaxHeight().width(980.dp)) {
                Row(
                    modifier = Modifier.fillMaxHeight().width(50.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (editorState.tabIndex == 0) {
                        when (editorState.blocksTabIndex) {
                            0 -> {
                                if (editorState.platformTabIndex in 1..3) {
                                    EditorArrow(
                                        action = { editorController.updateEditorState(editorState.copy(platformTabIndex = editorState.platformTabIndex - 1)) },
                                        image = Icons.Filled.KeyboardArrowLeft
                                    )
                                }
                            }
                            1 -> {
                                if (editorState.otherTabIndex == 1) {
                                    EditorArrow(
                                        action = { editorController.updateEditorState(editorState.copy(otherTabIndex = 0)) },
                                        image = Icons.Filled.KeyboardArrowLeft
                                    )
                                }
                            }
                            2 -> {
                                if (editorState.pitTabIndex == 1) {
                                    EditorArrow(
                                        action = { editorController.updateEditorState(editorState.copy(pitTabIndex = 0)) },
                                        image = Icons.Filled.KeyboardArrowLeft
                                    )
                                }
                            }
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxHeight().width(880.dp)) {
                    when (editorState.tabIndex) {
                        0 -> {
                            when (editorState.blocksTabIndex) {
                                0 -> {
                                    when (editorState.platformTabIndex) {
                                        0 -> EditorGrid(platform1Img, platform1Idx, editorState, editorController)
                                        1 -> EditorGrid(platform2Img, platform2Idx, editorState, editorController)
                                        2 -> EditorGrid(platform3Img, platform3Idx, editorState, editorController)
                                        3 -> EditorGrid(platform4Img, platform4Idx, editorState, editorController)
                                    }
                                }
                                1 -> {
                                    when (editorState.otherTabIndex) {
                                        0 -> EditorGrid(other1Img, other1Idx, editorState, editorController)
                                        1 -> EditorGrid(other2Img, other2Idx, editorState, editorController)
                                    }
                                }
                                2 -> {
                                    when (editorState.pitTabIndex) {
                                        0 -> EditorGrid(pit1Img, pit1Idx, editorState, editorController)
                                        1 -> EditorGrid(pit2Img, pit2Idx, editorState, editorController)
                                    }
                                }
                                3 -> EditorGrid(outlineImg, outlineIdx, editorState, editorController)
                                4 -> EditorGrid(spikeImg, spikeIdx, editorState, editorController)
                                5 -> EditorGrid(transportImg, transportIdx, editorState, editorController)
                            }
                        }
                        1 -> EditorGrid2(icons, actions)
                        2 -> EditorGrid2(listOf(Icons.Filled.Delete), listOf { editorController.onDelete() })
                    }
                }
                Row(
                    modifier = Modifier.fillMaxHeight().width(50.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (editorState.tabIndex == 0) {
                        when (editorState.blocksTabIndex) {
                            0 -> {
                                if (editorState.platformTabIndex in 0..2) {
                                    EditorArrow(
                                        action = { editorController.updateEditorState(editorState.copy(platformTabIndex = editorState.platformTabIndex + 1)) },
                                        image = Icons.Filled.KeyboardArrowRight
                                    )
                                }
                            }
                            1 -> {
                                if (editorState.otherTabIndex == 0) {
                                    EditorArrow(
                                        action = { editorController.updateEditorState(editorState.copy(otherTabIndex = 1)) },
                                        image = Icons.Filled.KeyboardArrowRight
                                    )
                                }
                            }
                            2 -> {
                                if (editorState.pitTabIndex == 0) {
                                    EditorArrow(
                                        action = { editorController.updateEditorState(editorState.copy(pitTabIndex = 1)) },
                                        image = Icons.Filled.KeyboardArrowRight
                                    )
                                }
                            }
                        }
                    }
                }
            }
            EditorButtons(
                texts = listOf("Swipe", "Rotate", "Free Move", "Snap"),
                actions = listOf(
                    { editorController.updateEditorState(editorState.copy(isSwipe = !editorState.isSwipe)) },
                    { editorController.updateEditorState(editorState.copy(isRotate = !editorState.isRotate)) },
                    { editorController.updateEditorState(editorState.copy(isFreeMove = !editorState.isFreeMove)) },
                    { editorController.updateEditorState(editorState.copy(isSnap = !editorState.isSnap)) }
                ),
                conditions = listOf(editorState.isSwipe, editorState.isRotate, editorState.isFreeMove, editorState.isSnap)
            )
        }
    }
}