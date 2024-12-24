package com.rcmiku.payload.dumper.compose

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.rcmiku.payload.dumper.compose.ui.PayloadDumperApp
import com.rcmiku.payload.dumper.compose.ui.component.RequestPermissionDialog
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
        val isGranted = isManageExternalStoragePermissionGranted()
        setContent {
            PayloadDumperComposeTheme {
                PayloadDumperApp(viewModel = payloadDumperViewModel)
                RequestPermissionDialog(!isGranted, onConfirm = {
                    openManageExternalStoragePermissionSettings()
                })
            }
        }
    }

    private fun isManageExternalStoragePermissionGranted(): Boolean {
        return Environment.isExternalStorageManager()
    }

    private val manageFilesPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

            if (isManageExternalStoragePermissionGranted()) {
                Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.permission_not_granted),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun openManageExternalStoragePermissionSettings() {
        val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri)
        manageFilesPermissionLauncher.launch(intent)
    }

}