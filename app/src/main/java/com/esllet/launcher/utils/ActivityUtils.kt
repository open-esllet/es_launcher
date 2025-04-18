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

package com.esllet.launcher.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Parcelable
import android.view.View
import android.view.WindowInsets
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.esllet.launcher.R
import java.io.Serializable


object ActivityUtils {
    private val TAG = ActivityUtils::class.java.simpleName

    val PKG_WEREAD = "com.tencent.weread.eink"
    val PKG_LOCAL_READ = "com.flyersoft.moonreader"
    val PKG_KINDLE = "com.amazon.kindle"
    val PKG_LEARN = "cn.bcbook.whyx"
    val PKG_USB_DETAILS = "com.android.settings"
    val CLS_USB_DETAILS = "com.android.settings.Settings\$UsbDetailsActivity"
    val PKG_OTA = "android.rockchip.update.service"
    val CLS_OTA = "android.rockchip.update.service.CheckForNewVersionActivity"
    val PKG_SEETING = "com.android.settings"
    val CLS_SEETING = "com.android.settings.wifi.WifiPickerActivity"
    val PKG_NOTE = "com.es.note"
    val CLS_NOTE = "com.es.note.NoteActivity"
    val PKG_FACTORY = "com.es.factorytools"
    val CLS_FACTORY = "com.es.factorytools.MainActivity"

    fun hasLearn(context: Context): Boolean {
        val intent =  context.packageManager.getLaunchIntentForPackage(PKG_LEARN)
        return intent != null
    }

    fun hasWxReader(context: Context): Boolean {
        val intent =  context.packageManager.getLaunchIntentForPackage(PKG_WEREAD)
        return intent != null
    }

    fun hasLocalReader(context: Context): Boolean {
        val intent =  context.packageManager.getLaunchIntentForPackage(PKG_LOCAL_READ)
        return intent != null
    }

    fun jumpApp(
        context: Context,
        packageName: String,
        activityClassName: String? = null,
        newTask: Boolean = false,
        vararg extras: Pair<String, Any?> = emptyArray()
    ) {
        if (activityClassName == null) {
            val intent =  context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                if (newTask)  intent.setFlags(intent.flags or Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                Toast.makeText(context, context.getString(R.string.toast_app_not_installed), Toast.LENGTH_SHORT).show()
            }
        } else {
            try {
                val intent = Intent().apply {
                    setComponent(ComponentName(packageName, activityClassName))
                    if (newTask)  setFlags(flags or Intent.FLAG_ACTIVITY_NEW_TASK)
                    extras.forEach { (key, value) ->
                        when (value) {
                            is String -> putExtra(key, value)
                            is Int -> putExtra(key, value)
                            is Boolean -> putExtra(key, value)
                            is Array<*> -> putExtra(key, value as Array<Parcelable>)
                            is Serializable -> putExtra(key, value)
                            else -> LogUtil.d(TAG, "jumpApp() Unsupported type: ${value}")
                        }
                    }
//                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, context.getString(R.string.toast_app_cannot_open), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setFullscreenEnabled(activity: Activity, hideStatus: Boolean, hideNavigation: Boolean) {
        val windowInsetsController =
            WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        if (hideStatus || hideNavigation) {
            WindowCompat.setDecorFitsSystemWindows(activity.window, false)
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            activity.window.decorView.setOnApplyWindowInsetsListener { view: View, windowInsets: WindowInsets ->
                if (hideStatus && hideNavigation) {
                    if (windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars())
                        || windowInsets.isVisible(WindowInsetsCompat.Type.statusBars())
                    ) {
                        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
                    }
                } else if (hideStatus) {
                    if (windowInsets.isVisible(WindowInsetsCompat.Type.statusBars())) {
                        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
                    }
                } else {
                    if (windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars())) {
                        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
                    }
                }
                view.onApplyWindowInsets(windowInsets)
                windowInsets
            }

            if (!hideStatus) {
                windowInsetsController.show(WindowInsetsCompat.Type.statusBars())
            } else if (!hideNavigation) {
                windowInsetsController.show(WindowInsetsCompat.Type.navigationBars())
            }
        } else {
            WindowCompat.setDecorFitsSystemWindows(activity.window, true)
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            activity.window.decorView.setOnApplyWindowInsetsListener(null)
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    fun setWhiteNavigationBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity.getWindow()
            window.setNavigationBarColor(ContextCompat.getColor(activity, android.R.color.white))


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val decorView = window.getDecorView()
                var flags = decorView.getSystemUiVisibility()

                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

                decorView.setSystemUiVisibility(flags)
            }
        }
    }
}