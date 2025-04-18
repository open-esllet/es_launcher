/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2025, pat733
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.esllet.launcher.vm

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.storage.StorageManager
import android.view.WindowManager
import androidx.lifecycle.ViewModel
import com.esllet.launcher.utils.LogUtil
import com.esllet.launcher.utils.SysPropHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import kotlin.math.pow

class SettingsVm private constructor(): ViewModel() {
    data class SettingsStatus(val devName: String = "",
                              val language: String = "",
                              val screenSize: String = "",
                              val width: Int = 0,
                              val height: Int = 0,
                              val ram: Double = 0.0,
                              val storage: String = "",
                              val systemVersion: String = "1.0",
                              val androidVersion: String = "",
                              val hardwareVersion: String = "Ver1",
                              val model: String = "",
                              val serial: String = "",
                              val wifiMac: String = "",
                              val battery: Int = 0)

    companion object {
        val TAG = SettingsVm::class.java.simpleName

        private var instance: SettingsVm? = null

        fun getInstance() : SettingsVm {
            return instance ?: synchronized(this) {
                instance ?: SettingsVm().also { instance = it }
            }
        }

        fun destroy() {
            instance = null
        }
    }

    val status = MutableStateFlow(SettingsStatus())

    fun updateStatus(context: Context) {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        val metrics = windowManager.currentWindowMetrics

        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val memGb = memoryInfo.totalMem / 1024.0.pow(3.0)

        status.update { status ->
            status.copy(SysPropHelper.getDevName(), context.resources.configuration.locales[0].language, "10.3", metrics.bounds.width(), metrics.bounds.height(),
                memGb, getStorageInfo(context), SysPropHelper.getSysVersion2(),
                Build.VERSION.RELEASE,
                SysPropHelper.getBoard(), Build.MODEL,
                SysPropHelper.getSerial(), getMacAddress(), getBatteryCapacity())
        }
    }

    private fun formatSize(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var i = 0

        while (size >= 1024 && i < units.size - 1) {
            size /= 1024
            i++
        }

        return "%.2f %s".format(size, units[i])
    }

    private fun getStorageInfo(context: Context) : String {
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val storageVolumes = storageManager.storageVolumes

        var totalSpace: Long = 0
        var usedSpace: Long = 0
        storageVolumes.forEach { volume ->
            if (volume.isPrimary) {
                val v = File(volume.directory!!.absolutePath)
                totalSpace = v.totalSpace
                usedSpace = totalSpace - v.freeSpace
            }
        }

        return "${formatSize(usedSpace)}/${formatSize(totalSpace)}"
    }

    private fun getMacAddress(): String {
        val interfaces = listOf("wlan0")
        interfaces.forEach { interfaceName ->
            val file = File("/sys/class/net/$interfaceName/address")
            if (file.exists()) {
                return file.readText().trim()
            }
        }
        return ""
    }

    private fun getBatteryCapacity(): Int {
        try {
            return 2200
        } catch (e: Exception) {
            LogUtil.d(TAG, "getBatteryCapacity() failed!", e)
        }
        return 0
    }
}
