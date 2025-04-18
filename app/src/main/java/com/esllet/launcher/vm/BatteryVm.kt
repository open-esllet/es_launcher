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

import android.content.Context
import androidx.lifecycle.ViewModel
import com.esllet.launcher.receiver.BatteryStatusReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class BatteryVm private constructor(): ViewModel() {

    data class BatteryStatus(var level: Int, var plugged: Int, var health: Int, var temp: Int)

    companion object {
        private var instance: BatteryVm? = null

        fun init(context: Context) : BatteryVm {
            return instance ?: synchronized(this) {
                instance ?: BatteryVm().also {
                    it.receiver = BatteryStatusReceiver { current ->
                        it.batteryStatus.update { current }
                    }
                    it.receiver?.register(context)
                    instance = it
                }
            }
        }

        fun destroy(context: Context) {
            instance?.receiver?.unregister(context)
            instance?.receiver = null
            instance = null
        }

        fun getInstance() : BatteryVm? {
            return instance
        }
    }

    private var receiver: BatteryStatusReceiver? = null
    val batteryStatus = MutableStateFlow(BatteryStatus(0, 0, 0, 0))
}