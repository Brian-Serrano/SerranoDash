package utils

import classes.DashObject
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object FileUtils {

    fun read(fileName: String): List<DashObject> {
        val file = File(fileName)
        if (file.exists()) {
            return readData(fileName)
        }
        else {
            file.createNewFile()
            return readData(fileName)
        }
    }

    private fun readData(fileName: String): List<DashObject> {
        return FileInputStream(fileName)
            .bufferedReader()
            .use { br -> Regex(";[^;]+;")
                .findAll(br.readLine() ?: "")
                .map { HelperFunctions.mapToDashObject(it.value) }
            }.toList()
    }

    fun write(fileName: String, objects: List<DashObject>) {
        FileOutputStream(fileName)
            .bufferedWriter()
            .use { bw -> objects.forEach { bw.write(";${it.id},${it.blockId},${it.x},${it.y},${it.rotation};") } }
    }
}