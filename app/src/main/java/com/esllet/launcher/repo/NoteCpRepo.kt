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

package com.esllet.launcher.repo

import com.esllet.launcher.ds.NoteContract
import com.esllet.launcher.ds.NoteCpDs
import com.esllet.launcher.entity.FolderEntity
import com.esllet.launcher.entity.NoteEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NoteCpRepo(private val ds: NoteCpDs) : NoteRepo {
    override suspend fun getAllFoldersStream(): StateFlow<List<FolderEntity>> {
        // TODO
        return MutableStateFlow<List<FolderEntity>>(arrayListOf())
    }

    override suspend fun getFolderStream(id: Long): StateFlow<FolderEntity?> {
        // TODO
        return MutableStateFlow(null)
    }

    override suspend fun insertFolder(item: FolderEntity) {
        // TODO
    }

    override suspend fun deleteFolder(item: FolderEntity) {
        // TODO
    }

    override suspend fun updateFolder(item: FolderEntity) {
        // TODO
    }

    override suspend fun getNoteStream(): List<NoteEntity> {
        return ds.getNoteStream("${NoteContract.Note.UPDATED_TIME} DESC")
    }

    override suspend fun getNote(id:Long): NoteEntity? {
        return ds.getNote(id)
    }

    override suspend fun insertNote(item: NoteEntity): Long {
        return ds.insertNote(item)
    }

    override suspend fun deleteNote(id: Long) {
        return ds.deleteNote(id)
    }

    override suspend fun updateNote(id: Long, newItem: NoteEntity) {
        return ds.updateNote(id, newItem)
    }
}