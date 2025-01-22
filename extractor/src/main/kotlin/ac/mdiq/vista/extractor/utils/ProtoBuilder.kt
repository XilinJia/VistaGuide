package ac.mdiq.vista.extractor.utils

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64
import kotlin.experimental.or


class ProtoBuilder {
    var byteBuffer: ByteArrayOutputStream

    init {
        this.byteBuffer = ByteArrayOutputStream()
    }

    fun toBytes(): ByteArray {
        return byteBuffer.toByteArray()
    }

    fun toUrlencodedBase64(): String {
        val b64: String = Base64.getUrlEncoder().encodeToString(toBytes())
        return URLEncoder.encode(b64, StandardCharsets.UTF_8)
    }

    private fun writeVarint(`val`: Long) {
        try {
            if (`val` == 0L) byteBuffer.write(byteArrayOf(0.toByte()))
            else {
                var v = `val`
                while (v != 0L) {
                    var b = (v and 0x7fL).toByte()
                    v = v shr 7

                    if (v != 0L) b = b or 0x80.toByte()
                    byteBuffer.write(byteArrayOf(b))
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private fun field(field: Int, wire: Byte) {
        val fbits = (field.toLong()) shl 3
        val wbits = (wire.toLong()) and 0x07L
        val `val` = fbits or wbits
        writeVarint(`val`)
    }

    fun varint(field: Int, `val`: Long) {
        field(field, 0.toByte())
        writeVarint(`val`)
    }

    fun string(field: Int, string: String) {
        val strBts: ByteArray = string.toByteArray(StandardCharsets.UTF_8)
        bytes(field, strBts)
    }

    fun bytes(field: Int, bytes: ByteArray) {
        field(field, 2.toByte())
        writeVarint(bytes.size.toLong())
        try {
            byteBuffer.write(bytes)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}
