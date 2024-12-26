package com.rcmiku.payload.dumper.compose.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import java.io.IOException
import java.net.URI
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

object HttpUtil {

    private lateinit var url: String
    private lateinit var fileName: String
    private var fileLength: Long = 0
    private var position: Long = 0
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(Interceptor { chain ->
            val originalRequest = chain.request()
            val newRequest = originalRequest.newBuilder()
                .header(
                    "User-Agent",
                    if (PreferencesUtil().perfGetBoolean("isCustomUserAgentEnabled") == true)
                        PreferencesUtil().perfGet("customUserAgent")
                            ?: UserAgentUtil.DEFAULT_USER_AGENT else UserAgentUtil.DEFAULT_USER_AGENT
                )
                .build()
            chain.proceed(newRequest)
        })
        .build()

    @Throws(IOException::class)
    suspend fun init(link: String) = withContext(Dispatchers.IO) {
        url = link
        runCatching {
            val request = Request.Builder()
                .url(link)
                .addHeader("Range", "bytes=0-0")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val contentRange = response.header("Content-Range")
                    fileLength = contentRange?.split("/")?.get(1)?.toLong() ?: 0L
                    fileName = getFileNameFromHeaders(response.headers)
                } else {
                    throw IOException("Failed to initialize HTTP request: ${response.message}")
                }
            }
        }.onFailure { exception ->
            throw IOException("Failed to initialize HTTP request", exception)
        }
    }


    fun length(): Long {
        return fileLength
    }

    fun position(): Long {
        return position
    }

    fun getFileName(): String {
        return fileName
    }

    suspend fun read(byteArray: ByteArray): Int = withContext(Dispatchers.IO) {
        val buffer = ByteArray(4 * 1024)
        var bytesRead: Int
        val requestBuilder = Request.Builder().url(url)
        var currentPosition = position
        var totalBytesRead = 0

        val rangeHeader = "bytes=$position-${position + byteArray.size - 1}"
        val request = requestBuilder.addHeader("Range", rangeHeader).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val body: ResponseBody? = response.body
            if (body != null) {
                val inputStream = body.byteStream()
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    if (totalBytesRead + bytesRead > byteArray.size) {
                        val remainingSpace = byteArray.size - totalBytesRead
                        System.arraycopy(buffer, 0, byteArray, totalBytesRead, remainingSpace)
                        totalBytesRead += remainingSpace
                        break
                    } else {
                        System.arraycopy(buffer, 0, byteArray, totalBytesRead, bytesRead)
                        totalBytesRead += bytesRead
                    }
                }
                currentPosition += totalBytesRead
            }
        }
        position = currentPosition
        return@withContext totalBytesRead
    }

    fun seek(bytePosition: Long) {
        if (bytePosition in 0 until fileLength) {
            position = bytePosition
        } else {
            throw IllegalArgumentException("Invalid seek position")
        }
    }

    private fun getFileNameFromHeaders(headers: Headers): String {
        val contentDisposition = headers["Content-Disposition"]
        if (!contentDisposition.isNullOrEmpty()) {
            val dispositionParts = contentDisposition.split(";")
            for (part in dispositionParts) {
                if (part.trim().startsWith("filename=")) {
                    return part.trim().substringAfter("=").replace("\"", "")
                }
            }
        }
        return Paths.get(URI(url).path).fileName.toString()
    }
}
