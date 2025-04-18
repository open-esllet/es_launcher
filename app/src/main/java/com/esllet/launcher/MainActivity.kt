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

package com.esllet.launcher

import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.esllet.launcher.ui.config.UiConfig.Companion.SIDEBAR_WIDTH
import com.esllet.launcher.ui.config.UiConfig.Companion.STATUSBAR_HEIGHT
import com.esllet.launcher.ui.theme.EsLauncherTheme
import com.esllet.launcher.ui.widget.BatteryWidget
import com.esllet.launcher.ui.widget.ClockWidget
import com.esllet.launcher.ui.widget.NoteWidget
import com.esllet.launcher.ui.widget.SettingsWidget
import com.esllet.launcher.ui.widget.WifiWidget
import com.esllet.launcher.utils.ActivityUtils
import com.esllet.launcher.utils.DialogUtils
import com.esllet.launcher.utils.LogUtil
import com.esllet.launcher.vm.MainVm

class MainActivity : ComponentActivity() {

    companion object {

        private val TAG = MainActivity::class.java.simpleName

        fun getDrawableByIndex(index: Int): Int {
            return when (index) {
                MainVm.SB_ITEM_WRITE -> R.drawable.ic_sb_write
                MainVm.SB_ITEM_READER_WX -> R.drawable.ic_sb_read
                MainVm.SB_ITEM_READER_LOCAL -> R.drawable.ic_sb_read
                MainVm.SB_ITEM_LEARN -> R.drawable.ic_sb_read
                MainVm.SB_ITEM_SETTINGS -> R.drawable.ic_sb_setting
                else -> R.drawable.ic_sb_home
            }
        }

        @Composable
        fun SidebarItem(index: Int, label: String, drawable: Int, selected: Boolean,
                        onSelected: (Int)->Unit, modifier: Modifier = Modifier) {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = modifier
                    .height(110.dp)
                    .width(SIDEBAR_WIDTH)
                    .background(/*if (selected) Color.LightGray else Color.Transparent*/
                        if (isPressed) Color.LightGray else Color.Transparent)
                    .clickable ( interactionSource = interactionSource,
                        indication = null, onClick = { onSelected(index)} )
            ) {
                Image(
                    painter = painterResource(id = drawable),
                    contentDescription = label,
                    modifier = Modifier.fillMaxWidth())
                Text(
                    text = label,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
            }
        }

        @Composable
        fun Sidebar(modifier: Modifier = Modifier) {
            val context = LocalContext.current
            var selectedItem by remember { mutableIntStateOf(0) }
            val items = listOf(
                    stringResource(R.string.sidebar_item_write),
                    stringResource(R.string.sidebar_item_reader_wx),
                    stringResource(R.string.sidebar_item_reader_local),
                    stringResource(R.string.sidebar_item_learn),
                    stringResource(R.string.sidebar_item_settings))

            Column(modifier = modifier
                .width(SIDEBAR_WIDTH)
                .fillMaxHeight()) {
                ClockWidget.Content()
                items.forEachIndexed { index, item ->
                    if (!ActivityUtils.hasLearn(context) && index == MainVm.SB_ITEM_LEARN) return@forEachIndexed
                    if (!ActivityUtils.hasWxReader(context) && index == MainVm.SB_ITEM_READER_WX) return@forEachIndexed
                    if (!ActivityUtils.hasLocalReader(context) && index == MainVm.SB_ITEM_READER_LOCAL) return@forEachIndexed

                    SidebarItem(
                        index = index,
                        label = item,
                        drawable = getDrawableByIndex(index),
                        selected = index == selectedItem,
                        onSelected = { i ->
                            selectedItem = i
                            when(i) {
                                MainVm.SB_ITEM_WRITE -> {
                                    MainVm.getInstance()?.updateMainStatus(i)
                                }
                                MainVm.SB_ITEM_READER_WX -> Intent().also {
                                    MainVm.getInstance()?.updateMainStatus(MainVm.SB_ITEM_WRITE)
                                    val wifiManager = context.getSystemService(WIFI_SERVICE) as WifiManager
                                    val wifiInfo = wifiManager.connectionInfo
                                    val wifiConnected = wifiInfo != null && wifiInfo.frequency != -1
                                    LogUtil.d(TAG, "Sidebar() WiFi: $wifiConnected")
                                    if (wifiConnected)
                                        ActivityUtils.jumpApp(context, ActivityUtils.PKG_WEREAD)
                                    else
                                        DialogUtils.promptConnectWifi(context)
                                }
                                MainVm.SB_ITEM_READER_LOCAL -> Intent().also {
                                    MainVm.getInstance()?.updateMainStatus(MainVm.SB_ITEM_WRITE)
                                    ActivityUtils.jumpApp(context, ActivityUtils.PKG_LOCAL_READ)
                                }
                                MainVm.SB_ITEM_LEARN -> Intent().also {
                                    MainVm.getInstance()?.updateMainStatus(MainVm.SB_ITEM_WRITE)
                                    ActivityUtils.jumpApp(context, ActivityUtils.PKG_LEARN)
                                }
                                MainVm.SB_ITEM_SETTINGS -> Intent().also {
                                    MainVm.getInstance()?.updateMainStatus(i)
                                }
                            }
                        }
                    )
                }
            }
        }

        @Composable
        fun StatusBar(modifier: Modifier = Modifier) {
            Row(horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(STATUSBAR_HEIGHT)
                    .padding(end = 10.dp)
            ) {
                WifiWidget.Content()
                BatteryWidget.Content()
            }
        }

        @Composable
        fun Background(modifier: Modifier = Modifier) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val startX = SIDEBAR_WIDTH.toPx()
                val startY = STATUSBAR_HEIGHT.toPx()

                drawLine(
                    color = Color.Black,
                    start = Offset(startX, startY),
                    end = Offset(size.width, startY),
                    strokeWidth = 2.dp.toPx())

                drawLine(
                    color = Color.Black,
                    start = Offset(startX, 0f),
                    end = Offset(startX, size.height),
                    strokeWidth = 2.dp.toPx())
            }
        }

        @Composable
        fun ShowRoom() {
            val status = MainVm.getInstance()!!.mainStatus.collectAsState()

            Box(modifier = Modifier.fillMaxSize().padding(start = SIDEBAR_WIDTH, top = STATUSBAR_HEIGHT)) {
                when (status.value.sidebarItem) {
                    MainVm.SB_ITEM_WRITE -> {
                        NoteWidget.Content()
                    }
                    MainVm.SB_ITEM_SETTINGS -> {
                        SettingsWidget.Content()
                    }
                }
            }
        }

        @Composable
        fun Root() {
            EsLauncherTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Background()
                    Sidebar()
                    StatusBar()
                    ShowRoom()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ActivityUtils.setFullscreenEnabled(this, true, false)
        ActivityUtils.setWhiteNavigationBar(this)
        setContent {
            Root()
        }
    }

    override fun onBackPressed() {
        // Disable Return
        return
        super.onBackPressed()
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MainActivity.Root()
}