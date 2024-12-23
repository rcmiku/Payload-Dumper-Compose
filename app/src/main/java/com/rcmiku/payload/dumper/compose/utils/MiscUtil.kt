package com.rcmiku.payload.dumper.compose.utils

import com.google.protobuf.ByteString
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream

object MiscUtil {

    fun String.isValidUrl(): Boolean {
        return this.toHttpUrlOrNull() != null
    }

    fun ByteString.toHexString(): String {
        return joinToString("") { "%02x".format(it.toInt() and 0xFF) }
    }

    fun ByteArray.toLong(): Long {
        var result = 0L
        for (i in this.indices) {
            result = result shl 8
            result = result or (this[i].toLong() and 0xFF)
        }
        return result
    }

    fun ByteArray.toInt(): Int {
        return this.toLong().toInt()
    }

    fun ByteArray.toBufferedInputStream(): BufferedInputStream {
        val byteArrayInputStream = ByteArrayInputStream(this)
        return BufferedInputStream(byteArrayInputStream)
    }

    fun Long.sizeIn(): String {
        return when {
            this < 1000 -> "%d B".format(this)
            this < 1000 * 1000 -> "%d KB".format(this / 1024)
            this < 1000 * 1000 * 1000 -> "%d MB".format(this / (1024 * 1024))
            else -> "%.2f GB".format(this / (1024.0 * 1024 * 1024))
        }
    }
}