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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.esllet.launcher.R
import com.esllet.launcher.ds.NoteCpDs
import com.esllet.launcher.repo.NoteCpRepo
import com.esllet.launcher.utils.LogUtil
import com.esllet.launcher.entity.NoteEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class NoteVm private constructor(): ViewModel() {

    private val TAG = NoteVm::class.java.simpleName

    data class NoteItem(val id: Long, val pageNo: Int, val imgPaths: String?, val title: String,
                        val saving: Boolean, val updateTime: Long)
    data class NoteStatus(val items: SnapshotStateList<NoteItem>)
    data class DialogStatus(val selId: Long?, val state: DLG_STATE)

    companion object {
        val ID_CREATED = -1L
        val DEFAULT_FOLDER_ID = 0

        enum class DLG_STATE {
            DISMISS,
            OPTIONS,
            RENAME
        }

        private var instance: NoteVm? = null

        private fun getEmptyItem(context: Context) : NoteItem {
            return NoteItem(ID_CREATED,
                0,
                null,
                context.getString(R.string.note_add_note), false, 0)
        }

        fun init(context: Context) : NoteVm {
            return instance ?: synchronized(this) {
                instance ?: NoteVm().also {
                    instance = it
                    it.repo = NoteCpRepo(NoteCpDs)
                    it.noteStatus.value.items.add(getEmptyItem(context))
                }
            }
        }

        fun destroy(context: Context) {
            instance = null
        }

        fun getInstance() : NoteVm? {
            return instance
        }
    }

    lateinit var repo : NoteCpRepo

    val noteStatus = MutableStateFlow(NoteStatus(mutableStateListOf()))
    val dlgStatus = MutableStateFlow(DialogStatus(null, DLG_STATE.DISMISS))

    fun setDialogState(selId: Long?, state: DLG_STATE) {
        dlgStatus.update { status -> status.copy(selId, state) }
    }

    suspend fun reloadNotes() {
        val notes = repo.getNoteStream()
        noteStatus.value.items.apply {
            removeIf { it.id != ID_CREATED }

            for (n in notes) {
                val item = NoteItem(n.id, n.pageNo,n.imgPaths, n.name,
                    n.saving != 0, n.updatedTime)
                add(item)
                LogUtil.d(TAG, "reloadNotes() item = $item")
            }
        }
    }

    private suspend fun reloadNote(id: Long): NoteItem? {
        var item: NoteItem? = null
        noteStatus.value.items.also {
            var itemIndex: Int = -1
            for ((i,v) in it.withIndex()) {
                if (v.id == id) {
                    itemIndex = i
                    break
                }
            }
            if (itemIndex == -1) return@also

            repo.getNote(id)?.also { e ->
                item = NoteItem(e.id, e.pageNo, e.imgPaths, e.name,
                    e.saving != 0, e.updatedTime)
                it[itemIndex] = item!!
                LogUtil.d(TAG, "reloadNote() item = $item")
            }
        }
        return item
    }

    fun getNoteItemFromStatus(id: Long) : NoteItem? {
        var item: NoteItem? = null
        noteStatus.value.items.also {
            var itemIndex: Int = -1

            for ((i, v) in it.withIndex()) {
                if (v.id == id) {
                    itemIndex = i
                    break
                }
            }

            if (itemIndex == -1) return@also
            item = noteStatus.value.items[itemIndex]
        }

        return item
    }

    suspend fun reloadNoteUntilSaveCompleted(id: Long) {
        var spentMillis = 0L
        while (true) {
            val delayMillis = calcReloadDelayMills(id, spentMillis)
            LogUtil.d(TAG, "reloadNoteUntilSaveCompleted() id=$id, delayMillis=$delayMillis, spentMillis=$spentMillis")
            if (delayMillis == 0L) break // load done

            delay(delayMillis)
            spentMillis += delayMillis
        }
    }

    suspend fun calcReloadDelayMills(id: Long, spentMillis: Long): Long {
        val maxDelay = 20000 // the maximum time spent loading 9 pages
        if (spentMillis > maxDelay) {
            return 0
        }

        if (spentMillis == 0L) {
            val item = reloadNote(id)
            if (item == null) {
                LogUtil.d(TAG, "calcReloadDelayMills() item not in state yet, reload in 500ms")
                return 500L
            }

            return 2000L
        } else {
            if (isNoteSaving(id)) {
                return 1000L
            }
        }

        return 0
    }

    suspend fun isNoteSaving(id: Long) : Boolean {
        val item = reloadNote(id)
        val saving = item != null && item.saving
        LogUtil.d(TAG, "isNoteSaving($id) = $saving")

        return saving
    }

    suspend fun getNoteEntity(id: Long) : NoteEntity? {
        return repo.getNote(id)
    }

    suspend fun deleteNote(id: Long) {
        repo.deleteNote(id)
        noteStatus.value.items.apply {
            removeIf { it.id == id }
            LogUtil.d(TAG, "deleteNote() item.id = $id")
        }
    }

    suspend fun updateNote(id: Long, newItem: NoteEntity) {
        repo.updateNote(id, newItem)
        noteStatus.value.items.apply {
            val index = indexOfFirst { it.id == id }
            removeAt(index)
            add(index, NoteItem(newItem.id, newItem.pageNo, newItem.imgPaths,
                newItem.name, newItem.saving != 0, System.currentTimeMillis()))
            LogUtil.d(TAG, "updateNote() item.id = $id")
        }
    }
}