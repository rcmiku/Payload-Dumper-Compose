package com.rcmiku.payload.dumper.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.rcmiku.payload.dumper.compose.ui.PayloadDumperApp
import com.rcmiku.payload.dumper.compose.ui.theme.PayloadDumperComposeTheme
import com.rcmiku.payload.dumper.compose.utils.AppContextUtil
import com.rcmiku.payload.dumper.compose.viewModel.PayloadDumperViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false
        AppContextUtil.init(this)
        val payloadDumperViewModel: PayloadDumperViewModel by viewModels()
        setContent {
            PayloadDumperComposeTheme {
                PayloadDumperApp(viewModel = payloadDumperViewModel)
            }
        }
    }

}