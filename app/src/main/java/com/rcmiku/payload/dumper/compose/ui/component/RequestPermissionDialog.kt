package com.rcmiku.payload.dumper.compose.ui.component

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rcmiku.payload.dumper.compose.BuildConfig
import com.rcmiku.payload.dumper.compose.R
import com.rcmiku.payload.dumper.compose.utils.MiscUtil.isManageExternalStoragePermissionGranted
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.utils.MiuixPopupUtil.Companion.dismissDialog

@Composable
fun RequestPermissionDialog(showDialog: MutableState<Boolean>) {

    val context = LocalContext.current

    val manageFilesPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            if (isManageExternalStoragePermissionGranted()) {
                Toast.makeText(
                    context,
                    context.getString(R.string.permission_granted),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.permission_not_granted),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    fun openManageExternalStoragePermissionSettings() {
        val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri)
        manageFilesPermissionLauncher.launch(intent)
    }

    SuperDialog(
        title = stringResource(R.string.storage_permission_required),
        summary = stringResource(R.string.permission_summary),
        show = showDialog,
        onDismissRequest = {
            dismissDialog(showDialog)
        },
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                text = stringResource(R.string.cancel),
                onClick = {
                    dismissDialog(showDialog)
                },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(20.dp))
            TextButton(
                text = stringResource(R.string.confirm),
                onClick = {
                    openManageExternalStoragePermissionSettings()
                    dismissDialog(showDialog)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColorsPrimary()
            )
        }
    }
}