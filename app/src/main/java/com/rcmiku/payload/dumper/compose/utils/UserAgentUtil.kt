package com.rcmiku.payload.dumper.compose.utils

import android.os.Build

class UserAgentUtil {

    companion object {

        val DEFAULT_USER_AGENT: String = buildString {
            val validRelease = Build.VERSION.RELEASE_OR_CODENAME.isNotEmpty()
            val validId = !Build.ID.isNullOrEmpty()
            val includeModel = "REL" == Build.VERSION.CODENAME && !Build.MODEL.isNullOrEmpty()

            append("AndroidDownloadManager")
            if (validRelease) {
                append("/").append(Build.VERSION.RELEASE_OR_CODENAME)
            }

            append(" (Linux; U; Android")
            if (validRelease) {
                append(" ").append(Build.VERSION.RELEASE_OR_CODENAME)
            }

            if (includeModel || validId) {
                append(";")
                if (includeModel) {
                    append(" ").append(Build.MODEL)
                }
                if (validId) {
                    append(" Build/").append(Build.ID)
                }
            }

            append(")")
        }
    }
}