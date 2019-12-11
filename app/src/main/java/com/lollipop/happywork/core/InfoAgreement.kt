package com.lollipop.happywork.core

import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets

/**
 * @author lollipop
 * @date 2019-12-10 21:30
 * 内容协议
 */
object InfoAgreement {

    const val TYPE_NOTING = 0
    const val TYPE_STRING = 1
    const val TYPE_PROTOBUF = 2

    var defCharset = StandardCharsets.UTF_8

    fun read(inputStream: InputStream): Info {
        val valueLength = readInt(inputStream)
        if (valueLength == -1) {
            return Info(TYPE_NOTING)
        }
        val infoType = inputStream.read()
        if (valueLength > 0) {
            val bytes = ByteArray(valueLength)
            val dataLength = inputStream.read(bytes)
            if (dataLength < 0) {
                return Info(TYPE_NOTING)
            }
            val info = Info(infoType)
            when (infoType) {
                TYPE_STRING -> {
                    info.value = String(bytes, 0, dataLength, defCharset)
                }
                TYPE_PROTOBUF -> {
                    info.bytes = bytes
                }
            }
            return info
        }
        return Info(TYPE_NOTING)
    }

    fun write(outputStream: OutputStream, info: Info) {
        val type = info.type
        var dataArray: ByteArray
        try {
            dataArray = when (type) {
                TYPE_STRING -> {
                    info.value.toByteArray(defCharset)
                }
                TYPE_PROTOBUF -> {
                    info.bytes
                }
                else -> {
                    Info.EMPTY_ARRAY
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            dataArray = Info.EMPTY_ARRAY
        }
        writeInt(outputStream, dataArray.size)
        outputStream.write(type)
        outputStream.write(dataArray)
        outputStream.flush()
    }

    private fun readInt(inputStream: InputStream): Int {
        var result = 0
        for (i in 0..3) {
            val b: Int = inputStream.read()
            if (b == -1) {
                return -1
            }
            result = result shl 8
            result += b
        }
        return result
    }

    private fun writeInt(outputStream: OutputStream, value: Int) {
        for (i in 3 downTo 0) {
            outputStream.write(value shr (8 * i))
        }
    }

}