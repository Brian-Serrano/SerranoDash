package utils

import classes.DashObject
import utils.Constants.TILE_SIZE
import java.util.Objects
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round

object HelperFunctions {

    fun mapToDashObject(input: String): DashObject {
        val result = input.substring(1, input.length - 1).split(",")
        return DashObject(result[0].toInt(), result[1].toInt(), result[2].toFloat(), result[3].toFloat(), result[4].toInt())
    }

    fun snap(input: Float): Float {
        return floor(input / TILE_SIZE) * TILE_SIZE
    }

    fun snap(input: Int): Int {
        return (floor(input.toDouble() / TILE_SIZE) * TILE_SIZE).toInt()
    }

    fun computeSnapDistance(input: Float): Float {
        return input - round(input / TILE_SIZE) * TILE_SIZE
    }

    fun findEmptyIdSlot(objects: List<DashObject>): Int {
        var counter = 0
        while (objects.any { it.id == counter }) {
            counter += 1
        }
        return counter
    }
}