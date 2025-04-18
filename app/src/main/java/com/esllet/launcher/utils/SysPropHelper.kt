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

import android.annotation.SuppressLint;
import java.lang.reflect.Method

@SuppressLint("PrivateApi", "DiscouragedPrivateApi")
object SysPropHelper {
    private const val TAG = "SysPropHelper"

    private const val HWC_COMMIT_DISABLED = "persist.sys.hwc_commit_disabled"
    private const val RO_VID = "ro.vendor.vid"
    private const val RO_BOARD = "ro.vendor.board"
    private const val RO_VERSION = "ro.vendor.version"
    private const val RO_MODEL = "ro.product.model"
    private const val RO_SERIAL = "ro.serialno"
    private const val RO_MCODE = "ro.vendor.mcode"

    private var getMethod: Method
    private var setMethod: Method

    init {
        val sysPropClz = Class.forName("android.os.SystemProperties")
        getMethod = sysPropClz.getDeclaredMethod("get", String:: class.java)
        setMethod = sysPropClz.getDeclaredMethod("set", String:: class.java, String:: class.java)
    }

    fun getDevName(): String {
        val stringValue = getMethod.invoke(null, RO_MCODE) as String
        return stringValue
    }

    fun getSerial(): String {
        val stringValue = getMethod.invoke(null, RO_SERIAL) as String
        return stringValue
    }

    fun getSysVersion(): String {
        var productName = getMethod.invoke(null, RO_MODEL) as String
        if (productName.contains(" ")) {
            productName = productName.replace(" ", "")
        }
        var vid = getMethod.invoke(null, RO_VID) as String
        if (vid.contains(" ")) {
            vid = vid.replace(" ", "")
        }

        var version = getMethod.invoke(null, RO_VERSION);

        return "$productName-$vid $version"
    }

    fun getSysVersion2(): String {
        val version = getMethod.invoke(null, RO_VERSION);

        return "$version"
    }

    fun getBoard(): String {
        val board = getMethod.invoke(null, RO_BOARD)
        return "$board"
    }

    fun setHwcCommitDisabled(disabled: Boolean) {
        try {
            setMethod.invoke(null, HWC_COMMIT_DISABLED, if (disabled) "1" else "0")
        } catch (e: RuntimeException) {
            LogUtil.d(TAG, "setHwcCommitDisabled($disabled) failed", e)
        }
    }
}