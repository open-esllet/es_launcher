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

package com.esllet.launcher.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.esllet.launcher.R
import com.esllet.launcher.vm.WifiVm
import android.content.Intent

object WifiWidget {
    @Composable
    fun Content() {
        val context = LocalContext.current
        val wifiStatus = WifiVm.getInstance()!!.status.collectAsState()
        val drawableId = if (!wifiStatus.value.interfaceOn) {
            R.drawable.ic_wifi_off
        } else {
            if (wifiStatus.value.bssid == "NULL") R.drawable.ic_wifi_on1
            else when (wifiStatus.value.rssi) {
                in Int.MIN_VALUE .. -90 -> R.drawable.ic_wifi_on1
                in -90 until -70 -> R.drawable.ic_wifi_on2
                in -70 until -50 -> R.drawable.ic_wifi_on3
                else -> R.drawable.ic_wifi_on4
            }
        }

        Image(
            painter = painterResource(id = drawableId),
            contentDescription = "WIFI",
            modifier = Modifier
                .fillMaxHeight()
                .padding(6.dp)
                .clickable {
                    Intent().also {
                        it.setClassName(
                            "com.android.settings",
                            "com.android.settings.wifi.WifiPickerActivity"
                        )
                        context.startActivity(it)
                    }
                })
    }
}