package com.example.wifitile

import android.service.quicksettings.TileService
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "WifiTileService"

class WifiTileService : TileService() {

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onClick() {
        super.onClick()
        scope.launch {
            try {
                val wifiOn = withContext(Dispatchers.IO) {
                    RootShell.exec("settings get global wifi_on").trim() == "1"
                }

                if (wifiOn) {
                    // Second tap — just turn WiFi off
                    withContext(Dispatchers.IO) {
                        RootShell.exec("svc wifi disable")
                    }
                } else {
                    // First tap — full sequence
                    withContext(Dispatchers.IO) {
                        RootShell.exec("settings put secure location_mode 3")
                    }
                    delay(1000L)
                    withContext(Dispatchers.IO) {
                        RootShell.exec("svc wifi enable")
                    }
                    delay(5_000L)
                    withContext(Dispatchers.IO) {
                        RootShell.exec("settings put secure location_mode 0")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sequence failed", e)
            }
        }
    }

    override fun onDestroy() {
        scope.coroutineContext[Job]?.cancel()
        super.onDestroy()
    }
}
