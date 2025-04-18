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

import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esllet.launcher.R
import com.esllet.launcher.vm.SettingsVm

class SettingsWidget {
    companion object {
        private var serialClickCount = 0

        @Composable
        fun Content() {
            val status = SettingsVm.getInstance().status.collectAsState()
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                SettingsVm.getInstance().updateStatus(context)
                serialClickCount = 0
            }

            Column(modifier = Modifier.fillMaxSize()) {
                Item(stringResource(R.string.info_dev_name), status.value.devName) {}
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).height(1.dp).background(Color.Gray))
                Item(stringResource(R.string.info_languages), status.value.language) {
                    val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                    context.startActivity(intent)
                }
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).height(1.dp).background(Color.Gray))
                Item(stringResource(R.string.info_date_time), "") {
                    val intent = Intent(Settings.ACTION_DATE_SETTINGS)
                    context.startActivity(intent)
                }
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).height(1.dp).background(Color.Gray))
                Item(stringResource(R.string.info_screen_lock), "") {
                    Intent().apply {
                        setComponent(ComponentName("com.android.settings", "com.android.settings.SubSettings"))
                        putExtra(":settings:show_fragment", "com.android.settings.password.ChooseLockGeneric\$ChooseLockGenericFragment")
                        context.startActivity(this)
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).height(1.dp).background(Color.Gray))
                Item(stringResource(R.string.info_screen_size), status.value.screenSize) {}
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).height(1.dp).background(Color.Gray))
                Item(stringResource(R.string.info_resolution), "${status.value.width} x ${status.value.height}") {}
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).height(1.dp).background(Color.Gray))
                Item(stringResource(R.string.info_ram), "${"%.2f".format(status.value.ram)} GB") {}
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).height(1.dp).background(Color.Gray))
                Item(stringResource(R.string.info_storage), status.value.storage) {}
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).height(1.dp).background(Color.Gray))
                Item(stringResource(R.string.info_system_version), status.value.systemVersion) {
                }
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).height(1.dp).background(Color.Gray))
                Item(stringResource(R.string.info_android_version), "Android ${status.value.androidVersion}") {}
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).height(1.dp).background(Color.Gray))
                Item(stringResource(R.string.info_hardware_version), status.value.hardwareVersion) {}
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).height(1.dp).background(Color.Gray))
                Item(stringResource(R.string.info_model), status.value.model) {}
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).height(1.dp).background(Color.Gray))
                Item(stringResource(R.string.info_serial), status.value.serial) {
                }
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).height(1.dp).background(Color.Gray))
                Item(stringResource(R.string.info_wifi_mac), if (!status.value.wifiMac.isEmpty()) status.value.wifiMac else stringResource(R.string.info_unavailable)) {}
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).height(1.dp).background(Color.Gray))
                Item(stringResource(R.string.info_battery), "${status.value.battery} mAh") {}
            }
        }

        @Composable
        fun Item(key: String, value: String, clickListener: () -> Unit) {
            Row(horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().height(60.dp).clickable(onClick = clickListener)) {
                Text(text = key, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(start = 20.dp))
                Text(text = value, fontSize = 18.sp, color = Color.Gray, modifier = Modifier.padding(end = 20.dp))
            }
        }
    }
}