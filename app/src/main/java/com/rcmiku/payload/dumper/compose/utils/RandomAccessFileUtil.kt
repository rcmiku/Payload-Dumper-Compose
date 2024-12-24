package com.rcmiku.payload.dumper.compose.utils

import java.io.RandomAccessFile

object RandomAccessFileUtil {

    lateinit var raf: RandomAccessFile

    fun init(raf: RandomAccessFile) {
        this.raf = raf
    }
}