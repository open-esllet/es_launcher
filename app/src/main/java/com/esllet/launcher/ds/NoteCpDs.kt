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

package com.esllet.launcher.ds

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import androidx.core.net.toUri
import com.esllet.launcher.utils.LogUtil
import com.esllet.launcher.vm.NoteVm
import com.esllet.launcher.entity.FolderEntity
import com.esllet.launcher.entity.NoteEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object NoteCpDs {

    private val TAG = NoteCpDs::class.java.simpleName

    lateinit var resolver : ContentResolver

    fun init(context: Context) {
        resolver = context.contentResolver
    }

    fun getAllFoldersStream(): StateFlow<List<FolderEntity>> {
        // TODO
        return MutableStateFlow<List<FolderEntity>>(arrayListOf())
    }

    fun getFolderStream(id: Long): StateFlow<FolderEntity?> {
        // TODO
        return MutableStateFlow<FolderEntity?>(null)
    }

    fun insertFolder(item: FolderEntity) {
        // TODO
    }

    fun deleteFolder(item: FolderEntity) {
        // TODO
    }

    fun updateFolder(item: FolderEntity) {
        // TODO
    }

    @SuppressLint("Range")
    fun getNoteStream(): List<NoteEntity> {
        val list = mutableListOf<NoteEntity>()
        val c = resolver.query(NoteContract.CU_NOTE, null, null, null, null) ?: return list

        while (c.moveToNext()) {
            val note = NoteEntity(c.getLong(c.getColumnIndex(NoteContract.Note.ID)),
                c.getLong(c.getColumnIndex(NoteContract.Note.FOLDER_ID)),
                c.getString(c.getColumnIndex(NoteContract.Note.NAME)),
                c.getInt(c.getColumnIndex(NoteContract.Note.SAVING)),
                c.getInt(c.getColumnIndex(NoteContract.Note.PAGE_NO)),
                c.getString(c.getColumnIndex(NoteContract.Note.IMG_PATHS)),
                c.getString(c.getColumnIndex(NoteContract.Note.BG_PATHS)),
                c.getLong(c.getColumnIndex(NoteContract.Note.CREATED_TIME)),
                c.getLong(c.getColumnIndex(NoteContract.Note.UPDATED_TIME)),
                c.getLong(c.getColumnIndex(NoteContract.Note.ACCESS_TIME)))
            list.add(note)
        }
        c.close()

        return list
    }

    @SuppressLint("Range")
    fun getNote(id: Long): NoteEntity? {
        var note : NoteEntity? = null
        val c = resolver.query(NoteContract.CU_NOTE, null,
            "${NoteContract.Note.ID} = ?",
            arrayOf("$id"), null)
        if (c == null) return note

        while (c.moveToNext()) {
            note = NoteEntity(c.getLong(c.getColumnIndex(NoteContract.Note.ID)),
                c.getLong(c.getColumnIndex(NoteContract.Note.FOLDER_ID)),
                c.getString(c.getColumnIndex(NoteContract.Note.NAME)),
                c.getInt(c.getColumnIndex(NoteContract.Note.SAVING)),
                c.getInt(c.getColumnIndex(NoteContract.Note.PAGE_NO)),
                c.getString(c.getColumnIndex(NoteContract.Note.IMG_PATHS)),
                c.getString(c.getColumnIndex(NoteContract.Note.BG_PATHS)),
                c.getLong(c.getColumnIndex(NoteContract.Note.CREATED_TIME)),
                c.getLong(c.getColumnIndex(NoteContract.Note.UPDATED_TIME)),
                c.getLong(c.getColumnIndex(NoteContract.Note.ACCESS_TIME)))
        }
        c.close()

        return note
    }

    fun insertNote(item: NoteEntity): Long {
        val values = ContentValues().apply {
            put(NoteContract.Note.FOLDER_ID, NoteVm.DEFAULT_FOLDER_ID)
            put(NoteContract.Note.NAME, item.name)
            put(NoteContract.Note.SAVING, item.saving)
            put(NoteContract.Note.IMG_PATHS, item.imgPaths)
            put(NoteContract.Note.BG_PATHS, item.backgroundPath)
            put(NoteContract.Note.CREATED_TIME, item.createdTime)
            put(NoteContract.Note.UPDATED_TIME, item.updatedTime)
            put(NoteContract.Note.ACCESS_TIME, item.accessTime)
        }

        val newUri = resolver.insert(NoteContract.CU_NOTE, values)
        if (newUri != null) {
            val newId = ContentUris.parseId(newUri)
            LogUtil.d(TAG, "insertNote() newId: $newId")
        }

        return -1
    }

    fun deleteNote(id: Long) {
        val deletedRows = resolver.delete(
            "${NoteContract.CU_NOTE}/$id".toUri(),
            null,
            null
        )

        LogUtil.d(TAG, "deleteNote() rows = $deletedRows")
    }

    fun updateNote(id: Long, newItem: NoteEntity) {
        val values = ContentValues().apply {
            put(NoteContract.Note.FOLDER_ID, newItem.folderId)
            put(NoteContract.Note.NAME, newItem.name)
            put(NoteContract.Note.SAVING, newItem.saving)
            put(NoteContract.Note.IMG_PATHS, newItem.imgPaths)
            put(NoteContract.Note.BG_PATHS, newItem.backgroundPath)
            put(NoteContract.Note.CREATED_TIME, newItem.createdTime)
            put(NoteContract.Note.UPDATED_TIME, newItem.updatedTime)
            put(NoteContract.Note.ACCESS_TIME, newItem.accessTime)
        }

        val updatedRows = resolver.update(
            "${NoteContract.CU_NOTE}/$id".toUri(),
            values,
            null,
            null)

        LogUtil.d(TAG, "updateNote() updateRows = $updatedRows")
    }
}