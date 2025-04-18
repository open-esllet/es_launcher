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

package com.esllet.launcher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.esllet.launcher.utils.ActivityUtils
import com.esllet.launcher.utils.LogUtil
import com.esllet.launcher.vm.BatteryVm.BatteryStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BatteryStatusReceiver(private val onBatteryStatusChanged: (BatteryStatus) -> Unit) : BroadcastReceiver() {
    private val TAG = BatteryStatusReceiver::class.java.simpleName

    var pluggedState = -1

    override fun onReceive(context: Context, intent: Intent) {
        LogUtil.d(TAG, "onReceive() ${intent.action}")
        when (intent.action) {
            Intent.ACTION_BATTERY_CHANGED -> {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                pluggedState = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
                val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
                onBatteryStatusChanged(BatteryStatus(level, pluggedState, health, temp))

                LogUtil.d(TAG, "onReceive() level=$level, status=$status, plugged=$pluggedState, health=$health")
            }

            Intent.ACTION_POWER_CONNECTED -> {
                CoroutineScope(Dispatchers.IO).launch {
                    delay(1000)
                    if (pluggedState != BatteryManager.BATTERY_PLUGGED_USB) return@launch

                    withContext(Dispatchers.Main) {
                        ActivityUtils.jumpApp(
                            context,
                            ActivityUtils.PKG_USB_DETAILS,
                            ActivityUtils.CLS_USB_DETAILS,
                            true
                        )
                    }
                }
            }

            Intent.ACTION_POWER_DISCONNECTED -> {

            }
        }
    }

    fun register(context: Context) {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        context.registerReceiver(this, filter)
    }

    fun unregister(context: Context) {
        context.unregisterReceiver(this)
    }
}
