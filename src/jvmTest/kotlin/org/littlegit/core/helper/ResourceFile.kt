package org.littlegit.core.helper

import org.junit.rules.ExternalResource
import java.io.File
import java.io.IOException
import java.io.InputStream

actual open class ResourceFile actual constructor(private var res: String) : ExternalResource() {
    private var file: File? = null
    private var inputStream: InputStream? = null

    actual val content: List<String>
        @Throws(IOException::class)
        get() {
            val lineList = mutableListOf<String>()
            File(res).inputStream().bufferedReader().useLines { lines -> lines.forEach { lineList.add(it)} }

            try {
                inputStream?.close()
            } catch (e: IOException) {
                // ignore
            }

            if (file != null) {
                file!!.delete()
            }

            return lineList
        }
}