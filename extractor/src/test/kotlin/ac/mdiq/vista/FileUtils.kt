package ac.mdiq.vista

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonWriter
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Util class to write file to disk
 *
 *
 * Can be used to debug and test, for example writing a service's JSON response
 * (especially useful if the response provided by the service is not documented)
 */
object FileUtils {
    @Throws(IOException::class)
    fun createFile(path: String, content: JsonObject?) {
        createFile(path, jsonObjToString(content))
    }

    @Throws(IOException::class)
    fun createFile(path: String, array: JsonArray?) {
        createFile(path, jsonArrayToString(array))
    }

    /**
     * Create a file given a path and its content. Create subdirectories if needed
     *
     * @param path    the path to write the file, including the filename (and its extension)
     * @param content the content to write
     * @throws IOException
     */
    @Throws(IOException::class)
    fun createFile(path: String, content: String) {
        val dirs = path.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (dirs.size > 1) {
            val pathWithoutFileName = path.replace(dirs[dirs.size - 1], "")
            if (!Files.exists(Paths.get(pathWithoutFileName))) { //create dirs if they don't exist
                if (!File(pathWithoutFileName).mkdirs()) throw IOException("An error occurred while creating directories")
            }
        }
        writeFile(path, content)
    }

    /**
     * Write a file to disk
     *
     * @param filename the file name (and its extension if wanted)
     * @param content  the content to write
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun writeFile(filename: String, content: String) {
        val writer = BufferedWriter(FileWriter(filename))
        writer.write(content)
        writer.flush()
        writer.close()
    }

    /**
     * Resolves the test resource file based on its filename. Looks in
     * `extractor/src/test/resources/` and `src/test/resources/`
     * @param filename the resource filename
     * @return the resource file
     */

    fun resolveTestResource(filename: String): File {
        val file = File("extractor/src/test/resources/$filename")
        return if (file.exists()) file else  File("src/test/resources/$filename")
    }

    /**
     * Convert a JSON object to String
     * toString() does not produce a valid JSON string
     */
    fun jsonObjToString(`object`: JsonObject?): String {
        return JsonWriter.string(`object`)
    }

    /**
     * Convert a JSON array to String
     * toString() does not produce a valid JSON string
     */
    fun jsonArrayToString(array: JsonArray?): String {
        return JsonWriter.string(array)
    }
}
