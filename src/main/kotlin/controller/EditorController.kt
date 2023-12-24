package controller

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import classes.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.produceIn
import org.jetbrains.skia.Image
import utils.Constants
import utils.Constants.TILE_SIZE
import utils.FileUtils
import utils.HelperFunctions
import java.io.File
import kotlin.math.*

class EditorController {

    private val _objects = MutableStateFlow(FileUtils.read("src/main/save/Test.txt"))
    val objects: StateFlow<List<DashObject>> = _objects.asStateFlow()

    private val _camera = MutableStateFlow(Camera(0, 0))
    val camera: StateFlow<Camera> = _camera.asStateFlow()

    private val _gridCamera = MutableStateFlow(Camera(0, 0))
    val gridCamera: StateFlow<Camera> = _gridCamera.asStateFlow()

    private val _editorState = MutableStateFlow(
        EditorState(
            selectedItem = 0,
            tabIndex = 0,
            highlightedItems = emptySet(),
            isSwipe = false,
            isRotate = false,
            isFreeMove = false,
            isSnap = false,
            isUndoEnabled = false,
            isRedoEnabled = false,
            blocksTabIndex = 0,
            platformTabIndex = 0,
            otherTabIndex = 0,
            pitTabIndex = 0
        )
    )
    val editorState: StateFlow<EditorState> = _editorState.asStateFlow()

    private val _highlighter = MutableStateFlow(Highlighter(false, 0f, 0f, 0f, 0f))
    val highlighter: StateFlow<Highlighter> = _highlighter.asStateFlow()

    private val _rotateCircleState = MutableStateFlow(RotateCircleState(0.0, Offset.Zero, Offset.Zero))
    val rotateCircleState: StateFlow<RotateCircleState> = _rotateCircleState.asStateFlow()

    private val undoProcesses = mutableListOf<ProcessBlock>()

    private val redoProcesses = mutableListOf<ProcessBlock>()

    private var prevPositionObjects = emptyList<DashObject>()

    private var prevRotationObjects = emptyList<DashObject>()

    private var swipeOffset = Offset.Zero

    private var isRotating = false

    private fun updateObjects(newObject: List<DashObject>) {
        _objects.value = newObject
    }

    private fun updateCamera(newCamera: Camera) {
        _camera.value = newCamera
    }

    private fun updateGridCamera(newCamera: Camera) {
        _gridCamera.value = newCamera
    }

    fun updateEditorState(newState: EditorState) {
        _editorState.value = newState
    }

    private fun updateHighlighter(newHighlighter: Highlighter) {
        _highlighter.value = newHighlighter
    }

    fun updateRotateCircle(newState: RotateCircleState) {
        _rotateCircleState.value = newState
    }

    private fun placeObject(offset: Offset) {
        val x = HelperFunctions.snap(offset.x + _camera.value.x)
        val y = HelperFunctions.snap(offset.y + _camera.value.y)

        // Check if a block not exist in the grid
        if (_objects.value.none { it.x == x && it.y == y }) {
            val id = HelperFunctions.findEmptyIdSlot(_objects.value)
            val obj = DashObject(id, _editorState.value.selectedItem, x, y, 0)
            val prevHighlight = _editorState.value.highlightedItems

            // Highlight the newly placed object
            updateEditorState(_editorState.value.copy(highlightedItems = setOf(id)))

            // Place the object
            updateObjects(_objects.value.plus(obj))

            // Add to process
            addProcess(ProcessType.BUILD, emptyList(), listOf(obj), prevHighlight, _editorState.value.highlightedItems)
        }
    }

    private fun isPointingToHighlightedObject(): Boolean {
        return selectHighlightedObjects()
            .any { (it.x + TILE_SIZE) - _camera.value.x >= swipeOffset.x && it.x - _camera.value.x <= swipeOffset.x && (it.y + TILE_SIZE) - _camera.value.y >= swipeOffset.y && it.y - _camera.value.y <= swipeOffset.y }
    }

    fun onDragStart(offset: Offset) {
        if (_editorState.value.isRotate) {
            // Save object initial rotation
            prevRotationObjects = selectHighlightedObjects()

            isRotating = true
        }
        else if (_editorState.value.isSwipe) {
            when (_editorState.value.tabIndex) {
                // Add offset for swipe placing blocks when in build tab
                0 -> swipeOffset = offset
                // Enable highlighter when in edit or delete tab
                1, 2 -> updateHighlighter(
                    _highlighter.value.copy(
                        isEnabled = true,
                        x = offset.x,
                        y = offset.y,
                        width = offset.x,
                        height = offset.y
                    )
                )
            }
        }
        else if (_editorState.value.isFreeMove) {
            // Add offset for moving blocks freely
            swipeOffset = offset

            // Check if cursor not points to highlighted block and if it is highlight the pointed block or ignore if don't point any block
            if (!isPointingToHighlightedObject()) {
                val prevHighlight = _editorState.value.highlightedItems
                updateHighlights(offset)

                // Check if there are highlight changes if there is add it to process
                if (!(prevHighlight.size == _editorState.value.highlightedItems.size && prevHighlight == _editorState.value.highlightedItems)) {
                    addProcess(ProcessType.SELECT, emptyList(), emptyList(), prevHighlight, _editorState.value.highlightedItems)
                }
            }
            // If cursor pointing to highlighted object save their initial positions
            if (isPointingToHighlightedObject()) {
                prevPositionObjects = selectHighlightedObjects()
            }
        }
    }

    fun onDragEnd() {
        if (_editorState.value.isRotate) {
            // Add to process
            addProcess(ProcessType.ROTATE, prevRotationObjects, selectHighlightedObjects(), _editorState.value.highlightedItems, _editorState.value.highlightedItems)

            // Remove previous object rotation
            prevRotationObjects = emptyList()

            isRotating = false
        }
        else if (_editorState.value.isSwipe) {
            when (_editorState.value.tabIndex) {
                // Reset the offset added for build tab
                0 -> swipeOffset = Offset.Zero

                // Highlight/Select the items and disable and reset highlighter for edit or delete tab
                1, 2 -> {
                    val left = min(_highlighter.value.x, _highlighter.value.width)
                    val right = max(_highlighter.value.x, _highlighter.value.width)
                    val top = min(_highlighter.value.y, _highlighter.value.height)
                    val bottom = max(_highlighter.value.y, _highlighter.value.height)

                    val prevHighlight = _editorState.value.highlightedItems

                    // Highlight/Select the items
                    updateEditorState(
                        _editorState.value.copy(
                            highlightedItems = _editorState.value.highlightedItems + _objects.value.filter { (it.x + TILE_SIZE) - _camera.value.x >= left && it.x - _camera.value.x <= right && (it.y + TILE_SIZE) - _camera.value.y >= top && it.y - _camera.value.y <= bottom }.map { it.id }.toSet()
                        )
                    )

                    // Add to process
                    addProcess(ProcessType.SELECT, emptyList(), emptyList(), prevHighlight, _editorState.value.highlightedItems)

                    // Disable and reset highlighter
                    updateHighlighter(
                        _highlighter.value.copy(
                            isEnabled = false,
                            x = 0f,
                            y = 0f,
                            width = 0f,
                            height = 0f
                        )
                    )
                }
            }
        }
        else if (_editorState.value.isFreeMove) {
            // If snap is enabled snap the blocks to the grid base on the pointed block
            if (_editorState.value.isSnap && isPointingToHighlightedObject()) {
                val obj = selectHighlightedObjects()
                    .first { (it.x + TILE_SIZE) - _camera.value.x >= swipeOffset.x && it.x - _camera.value.x <= swipeOffset.x && (it.y + TILE_SIZE) - _camera.value.y >= swipeOffset.y && it.y - _camera.value.y <= swipeOffset.y }
                val xOffset = HelperFunctions.computeSnapDistance(obj.x)
                val yOffset = HelperFunctions.computeSnapDistance(obj.y)
                onMove(ProcessType.MOVE, -xOffset, -yOffset, prevPositionObjects)

                // If snap disabled only reset the offset added
                swipeOffset = Offset.Zero

                // Reset object previous position
                prevPositionObjects = emptyList()
            }
            else {
                // Add to process
                addProcess(ProcessType.MOVE, prevPositionObjects, selectHighlightedObjects(), _editorState.value.highlightedItems, _editorState.value.highlightedItems)

                // If snap disabled only reset the offset added
                swipeOffset = Offset.Zero

                // Reset object previous position
                prevPositionObjects = emptyList()
            }
        }
    }

    fun onDrag(dragAmount: Offset) {
        if (_editorState.value.isRotate && _editorState.value.highlightedItems.isNotEmpty()) {

            // Compute Delta X and Delta Y
            val prevAngle = _rotateCircleState.value.angle
            val (dx, dy) = (_rotateCircleState.value.handle + dragAmount) - (computeHighlightedCenterPivotWithRotateLock() + Offset(16f, 16f))

            // Update rotate circle angle
            updateRotateCircle(_rotateCircleState.value.copy(angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())) % 360))

            // Rotate highlighted objects
            rotate((_rotateCircleState.value.angle - prevAngle).toInt())

            println(_rotateCircleState.value)
        }
        else if (_editorState.value.isSwipe) {
            when (_editorState.value.tabIndex) {
                // Place objects by swiping then update the offset
                0 -> {
                    placeObject(
                        Offset(
                            swipeOffset.x + dragAmount.x,
                            swipeOffset.y + dragAmount.y
                        )
                    )
                    swipeOffset = swipeOffset.copy(
                        x = swipeOffset.x + dragAmount.x,
                        y = swipeOffset.y + dragAmount.y
                    )
                }
                // Update/Resize the highlighter
                1, 2 -> {
                    updateHighlighter(
                        _highlighter.value.copy(
                            width = _highlighter.value.width + dragAmount.x,
                            height = _highlighter.value.height + dragAmount.y
                        )
                    )
                }
            }
        }
        else if (_editorState.value.isFreeMove) {
            // Check if cursor points to object then if it is move the block freely
            if (isPointingToHighlightedObject()) {
                move(dragAmount.x, dragAmount.y)
                swipeOffset = swipeOffset.copy(
                    x = swipeOffset.x + dragAmount.x,
                    y = swipeOffset.y + dragAmount.y
                )
            }
            // Else move the screen
            else {
                updateCameras(dragAmount)
            }
        }
        // Move the screen
        else {
            updateCameras(dragAmount)
        }
    }

    private fun updateCameras(dragAmount: Offset) {
        updateCamera(
            _camera.value.copy(
                x = (_camera.value.x - dragAmount.x.toInt()).coerceAtLeast(0).coerceAtMost(2000),
                y = (_camera.value.y - dragAmount.y.toInt()).coerceAtLeast(0).coerceAtMost(1200)
            )
        )
        updateGridCamera(
            _gridCamera.value.copy(x = HelperFunctions.snap(_camera.value.x), y = HelperFunctions.snap(_camera.value.y))
        )
    }

    fun onClick(offset: Offset) {
        when (_editorState.value.tabIndex) {
            // Place one object in build tab
            0 -> {
                placeObject(offset)
            }
            // Highlight one object in edit or delete tab
            1, 2 -> {
                val prevHighlight = _editorState.value.highlightedItems
                updateHighlights(offset)

                // Check if there are highlight changes if there is add it to process
                if (!(prevHighlight.size == _editorState.value.highlightedItems.size && prevHighlight == _editorState.value.highlightedItems)) {
                    addProcess(ProcessType.SELECT, emptyList(), emptyList(), prevHighlight, _editorState.value.highlightedItems)
                }
            }
        }
    }

    fun save() {
        FileUtils.write(fileName = "src/main/save/Test.txt", objects = _objects.value)
    }

    fun onDelete() {
        // Add to process
        addProcess(ProcessType.DELETE, selectHighlightedObjects(), emptyList(), _editorState.value.highlightedItems, emptySet())

        // Remove objects
        updateObjects(_objects.value.filter { obj -> _editorState.value.highlightedItems.none { it == obj.id } })

        // Remove highlights
        updateEditorState(_editorState.value.copy(highlightedItems = emptySet()))
    }

    fun onMove(
        type: ProcessType,
        xValue: Float,
        yValue: Float,
        initialPos: List<DashObject> = selectHighlightedObjects()
    ) {
        move(xValue, yValue)

        // Add to process
        addProcess(type, initialPos, selectHighlightedObjects(), _editorState.value.highlightedItems, _editorState.value.highlightedItems)
    }

    private fun move(
        xValue: Float,
        yValue: Float
    ) {
        // Move objects
        updateObjects(
            _objects.value.map { obj ->
                // If object is highlighted make a movement
                if (_editorState.value.highlightedItems.any { it == obj.id }) {
                    obj.copy(
                        x = obj.x + xValue,
                        y = obj.y + yValue
                    )
                }
                else {
                    obj
                }
            }
        )
    }

    fun onRotate(
        type: ProcessType,
        angle: Int,
        initialPos: List<DashObject> = selectHighlightedObjects()
    ) {
        rotate(angle)

        addProcess(type, initialPos, selectHighlightedObjects(), _editorState.value.highlightedItems, _editorState.value.highlightedItems)
    }

    private fun rotate(rotation: Int) {
        // Rotate objects
        val pivot = computeHighlightedCenterPivotWithRotateLock()
        updateObjects(
            _objects.value.map { obj ->
                // If object is highlighted make a movement
                if (_editorState.value.highlightedItems.any { it == obj.id }) {
                    val offset = calculateRotation(obj.x - pivot.x, obj.y - pivot.y, Math.toRadians(rotation.toDouble()))
                    obj.copy(
                        x = offset.first.toFloat() + pivot.x,
                        y = offset.second.toFloat() + pivot.y,
                        rotation = (obj.rotation + rotation) % 360
                    )
                }
                else {
                    obj
                }
            }
        )
    }

    private fun selectHighlightedObjects(): List<DashObject> {
        return _objects.value.filter { obj -> _editorState.value.highlightedItems.any { it == obj.id } }
    }

    private fun updateHighlights(offset: Offset) {
        updateEditorState(
            _editorState.value.copy(
                highlightedItems = try {
                    setOf(_objects.value.first { offset.x.toInt() >= it.x - _camera.value.x && offset.x.toInt() <= (it.x + TILE_SIZE) - _camera.value.x && offset.y.toInt() >= it.y - _camera.value.y && offset.y.toInt() <= (it.y + TILE_SIZE) - _camera.value.y }.id)
                }
                catch (e: NoSuchElementException) {
                    _editorState.value.highlightedItems
                }
            )
        )
    }

    fun undo() {
        if (undoProcesses.isNotEmpty()) {
            val process = undoProcesses.removeLast()
            when (process.type) {
                ProcessType.BUILD -> {
                    updateObjects(undoDelete(process.nextObj))
                    updateEditorState(_editorState.value.copy(highlightedItems = process.prevHighlight))
                }
                ProcessType.DELETE -> {
                    updateObjects(_objects.value + process.prevObj)
                    updateEditorState(_editorState.value.copy(highlightedItems = process.prevHighlight))
                }
                ProcessType.MOVE -> {
                    updateObjects(undoMove(process.prevObj))
                }
                ProcessType.ROTATE -> {
                    updateObjects(undoMove(process.prevObj))
                }
                ProcessType.SELECT -> {
                    updateEditorState(_editorState.value.copy(highlightedItems = process.prevHighlight))
                }
            }
            if (redoProcesses.isEmpty()) {
                updateEditorState(_editorState.value.copy(isRedoEnabled = true))
            }

            if (undoProcesses.isEmpty()) {
                updateEditorState(_editorState.value.copy(isUndoEnabled = false))
            }

            redoProcesses.addLast(process)
        }
    }

    fun redo() {
        if (redoProcesses.isNotEmpty()) {
            val process = redoProcesses.removeLast()
            when (process.type) {
                ProcessType.BUILD -> {
                    updateObjects(_objects.value + process.nextObj)
                    updateEditorState(_editorState.value.copy(highlightedItems = process.nextHighlight))
                }
                ProcessType.DELETE -> {
                    updateObjects(undoDelete(process.prevObj))
                    updateEditorState(_editorState.value.copy(highlightedItems = process.nextHighlight))
                }
                ProcessType.MOVE -> {
                    updateObjects(undoMove(process.nextObj))
                }
                ProcessType.ROTATE -> {
                    updateObjects(undoMove(process.nextObj))
                }
                ProcessType.SELECT -> {
                    updateEditorState(_editorState.value.copy(highlightedItems = process.nextHighlight))
                }
            }
            if (undoProcesses.isEmpty()) {
                updateEditorState(_editorState.value.copy(isUndoEnabled = true))
            }

            if (redoProcesses.isEmpty()) {
                updateEditorState(_editorState.value.copy(isRedoEnabled = false))
            }

            undoProcesses.addLast(process)
        }
    }

    private fun undoDelete(objects: List<DashObject>): List<DashObject> {
        return _objects.value.filter { obj -> objects.none { it == obj } }
    }

    private fun undoMove(objects: List<DashObject>): List<DashObject> {
        return _objects.value.map { obj ->
            try {
                objects.first { it.id == obj.id }
            }
            catch (e: NoSuchElementException) {
                obj
            }
        }
    }

    private fun clearRedo() {
        if (redoProcesses.isNotEmpty()) {
            redoProcesses.clear()
            updateEditorState(_editorState.value.copy(isRedoEnabled = false))
        }
    }

    private fun addProcess(
        type: ProcessType,
        prevObj: List<DashObject>,
        nextObj: List<DashObject>,
        prevHighlight: Set<Int>,
        nextHighlight: Set<Int>
    ) {
        // Enable undo if its empty or disabled previously
        if (undoProcesses.isEmpty()) {
            updateEditorState(_editorState.value.copy(isUndoEnabled = true))
        }

        // Add to process
        undoProcesses.addLast(ProcessBlock(type, prevObj, nextObj, prevHighlight, nextHighlight))

        // Clear redo stack
        clearRedo()
    }

    private fun calculateRotation(x: Float, y: Float, angleRadians: Double): Pair<Double, Double> {
        return Pair(
            x * cos(angleRadians) - y * sin(angleRadians),
            x * sin(angleRadians) + y * cos(angleRadians)
        )
    }

    fun computeHighlightedCenterPivotWithRotateLock(): Offset {
        return if (isRotating) _rotateCircleState.value.position else computeHighlightedCenterPivot()
    }

    private fun computeHighlightedCenterPivot(): Offset {
        val highlighted = selectHighlightedObjects()
        val minX = highlighted.minOf { it.x }
        val minY = highlighted.minOf { it.y }
        val maxX = highlighted.maxOf { it.x }
        val maxY = highlighted.maxOf { it.y }
        return Offset((minX + (maxX)) / 2, (minY + (maxY)) / 2)
    }

    fun updateRotateHandle() {
        val x = (_rotateCircleState.value.position.x + 16 + cos(Math.toRadians(_rotateCircleState.value.angle)) * Constants.ROTATE_CIRCLE_RADIUS).toFloat()
        val y = (_rotateCircleState.value.position.y + 16 + sin(Math.toRadians(_rotateCircleState.value.angle)) * Constants.ROTATE_CIRCLE_RADIUS).toFloat()

        updateRotateCircle(_rotateCircleState.value.copy(handle = Offset(x, y)))
    }

    fun deselect() {
        val prevHighlight = _editorState.value.highlightedItems
        updateEditorState(_editorState.value.copy(highlightedItems = emptySet()))
        addProcess(ProcessType.SELECT, emptyList(), emptyList(), prevHighlight, _editorState.value.highlightedItems)
    }
}