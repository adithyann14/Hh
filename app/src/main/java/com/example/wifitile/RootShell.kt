package com.example.wifitile

import android.util.Log
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

object RootShell {

    private const val TAG = "RootShell"
    private const val TIMEOUT_SECONDS = 10L

    private val SU_PATHS = arrayOf(
        "/data/adb/ksu/bin/su",
        "/data/adb/ap/bin/su",
        "/data/adb/magisk/su",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su"
    )

    private fun isSuBinaryPresent(): Boolean {
        if (SU_PATHS.any { File(it).exists() }) return true
        return try {
            val p = ProcessBuilder("which", "su").start()
            p.waitFor(3, TimeUnit.SECONDS)
            val line = p.inputStream.bufferedReader().readLine()
            p.destroy()
            !line.isNullOrBlank()
        } catch (e: Exception) {
            false
        }
    }

    fun hasRoot(): Boolean {
        if (!isSuBinaryPresent()) {
            Log.e(TAG, "No su binary found on device")
            return false
        }
        return try {
            exec("id").contains("uid=0")
        } catch (e: Exception) {
            Log.e(TAG, "Root check failed", e)
            false
        }
    }

    @Throws(IOException::class, InterruptedException::class)
    fun exec(command: String): String {
        val process = ProcessBuilder("su", "-c", command)
            .redirectErrorStream(false)
            .start()

        val finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        val out = process.inputStream.bufferedReader().readText()
        val err = process.errorStream.bufferedReader().readText()
        if (err.isNotBlank()) Log.w(TAG, "stderr [$command]: $err")
        process.destroy()

        if (!finished) throw IOException("Command timed out: $command")
        return out.trim()
    }
}
