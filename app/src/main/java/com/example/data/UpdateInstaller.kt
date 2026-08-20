package com.example.data

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

data class InstallPermissionRequest(
    val version: String,
    val apkFile: File
)

object UpdateInstaller {
    private const val PREFS_NAME = "update_prefs"
    const val KEY_OPTED_OUT_AUTO_INSTALL = "opted_out_auto_install"

    private var activeDownloadId: Long = -1L
    private var activeVersion: String = ""
    private var pendingApkFile: File? = null
    private var isReceiverRegistered = false

    private val _permissionRequest = MutableStateFlow<InstallPermissionRequest?>(null)
    val permissionRequest: StateFlow<InstallPermissionRequest?> = _permissionRequest.asStateFlow()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isOptedOut(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_OPTED_OUT_AUTO_INSTALL, false)
    }

    fun setOptedOut(context: Context, optedOut: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_OPTED_OUT_AUTO_INSTALL, optedOut).apply()
    }

    fun canRequestPackageInstalls(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun startDownload(context: Context, updateInfo: UpdateInfo) {
        try {
            val fileName = "Finance-Tracker_${updateInfo.version}.apk"
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            if (file.exists()) {
                file.delete()
            }

            val cleanNewVersion = if (updateInfo.version.startsWith("v", ignoreCase = true)) {
                updateInfo.version
            } else {
                "v${updateInfo.version}"
            }

            val request = DownloadManager.Request(Uri.parse(updateInfo.downloadUrl)).apply {
                setTitle("Finance Tracker Update")
                setDescription("Downloading $cleanNewVersion...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                setMimeType("application/vnd.android.package-archive")
            }

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            activeDownloadId = downloadManager.enqueue(request)
            activeVersion = updateInfo.version
            registerReceiverIfNeeded(context.applicationContext)

            Toast.makeText(context, "Download started in background", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("UpdateInstaller", "Download start error: ${e.message}", e)
            Toast.makeText(context, "Could not start download: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                if (id == activeDownloadId && id != -1L) {
                    handleDownloadCompleted(context, id, activeVersion)
                }
            }
        }
    }

    private fun registerReceiverIfNeeded(appContext: Context) {
        if (!isReceiverRegistered) {
            try {
                val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                ContextCompat.registerReceiver(
                    appContext,
                    downloadReceiver,
                    filter,
                    ContextCompat.RECEIVER_EXPORTED
                )
                isReceiverRegistered = true
            } catch (e: Exception) {
                Log.e("UpdateInstaller", "Error registering receiver: ${e.message}", e)
            }
        }
    }

    private fun handleDownloadCompleted(context: Context, downloadId: Long, version: String) {
        val fileName = "Finance-Tracker_${version}.apk"
        val apkFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
        
        if (!apkFile.exists()) {
            Log.w("UpdateInstaller", "Downloaded file does not exist at: ${apkFile.absolutePath}")
            return
        }

        if (canRequestPackageInstalls(context)) {
            installApk(context, apkFile)
        } else {
            if (isOptedOut(context)) {
                Log.i("UpdateInstaller", "User opted out of auto-install permission prompt. Skipping prompt.")
            } else {
                pendingApkFile = apkFile
                _permissionRequest.value = InstallPermissionRequest(version = version, apkFile = apkFile)
            }
        }
    }

    fun onResumeCheck(context: Context) {
        val pending = pendingApkFile
        if (pending != null && pending.exists() && canRequestPackageInstalls(context)) {
            pendingApkFile = null
            _permissionRequest.value = null
            installApk(context, pending)
        }
    }

    fun onPermissionAllow(context: Context) {
        _permissionRequest.value = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } else {
            val pending = pendingApkFile
            if (pending != null && pending.exists()) {
                pendingApkFile = null
                installApk(context, pending)
            }
        }
    }

    fun onPermissionCancel(context: Context, dontAskAgain: Boolean) {
        if (dontAskAgain) {
            setOptedOut(context, true)
        }
        pendingApkFile = null
        _permissionRequest.value = null
    }

    fun installApk(context: Context, apkFile: File) {
        try {
            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            Log.e("UpdateInstaller", "Error launching installer: ${e.message}", e)
            Toast.makeText(context, "Could not open installer: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
