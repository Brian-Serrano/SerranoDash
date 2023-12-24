package classes

enum class ProcessType {
    BUILD, DELETE, MOVE, ROTATE, SELECT
}

class ProcessBlock(
    val type: ProcessType,
    val prevObj: List<DashObject>,
    val nextObj: List<DashObject>,
    val prevHighlight: Set<Int>,
    val nextHighlight: Set<Int>
)