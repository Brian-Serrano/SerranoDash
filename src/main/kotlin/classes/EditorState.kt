package classes

data class EditorState(
    val selectedItem: Int,
    val tabIndex: Int,
    val highlightedItems: Set<Int>,
    val isSwipe: Boolean,
    val isRotate: Boolean,
    val isFreeMove: Boolean,
    val isSnap: Boolean,
    val isUndoEnabled: Boolean,
    val isRedoEnabled: Boolean,
    val blocksTabIndex: Int,
    val platformTabIndex: Int,
    val otherTabIndex: Int,
    val pitTabIndex: Int
)