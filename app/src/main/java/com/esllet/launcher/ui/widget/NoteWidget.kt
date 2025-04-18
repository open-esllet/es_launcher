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

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.esllet.launcher.R
import com.esllet.launcher.utils.ActivityUtils
import com.esllet.launcher.utils.LogUtil
import com.esllet.launcher.utils.NodeUtils
import com.esllet.launcher.utils.ToastUtils
import com.esllet.launcher.vm.NoteVm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NoteWidget {

    private val TAG = NoteWidget::class.java.simpleName

    private val semaphore = Semaphore(1)

    private fun startNoteActivity(context: Context, id: Long, resultLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
        if (!semaphore.tryAcquire()) {
            LogUtil.d(TAG, "semaphore.tryAcquire() false")
            return
        }
        LogUtil.d(TAG, "semaphore.tryAcquire() true")

        CoroutineScope(Dispatchers.IO).launch {
            val saving = if (id != NoteVm.ID_CREATED) NoteVm.getInstance()!!.isNoteSaving(id) else false
            withContext(Dispatchers.Main) {
                if (saving) {
                    ToastUtils.showText(R.string.toast_note_saving)
                    semaphore.release()
                    return@withContext
                }

                val intent = Intent().apply {
                    setComponent(ComponentName(ActivityUtils.PKG_NOTE, ActivityUtils.CLS_NOTE))
                    if (id != NoteVm.ID_CREATED) {
                        putExtra("id", id)
                    }
//                    setFlags(flags or Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                try {
                    resultLauncher.launch(intent)
                } catch (e: ActivityNotFoundException) {
                    ToastUtils.showText(R.string.toast_app_not_installed)
                } finally {
                    withContext(Dispatchers.IO) {
                        try {
                            delay(2000)
                        } finally {
                            semaphore.release()
                        }
                        LogUtil.d(TAG, "startNoteActivity()")
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun Item(id: Long, noteUrl: String?, title: String, updateMillis: Long, resultLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
        val context = LocalContext.current
        val IMG_WIDTH = 150.dp
        val IMG_HEIGHT = 222.dp

        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()

        LogUtil.d(TAG, "Item() noteUrl=$noteUrl")

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.wrapContentHeight()
                .background(if (isPressed) Color.LightGray else Color.Transparent)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        startNoteActivity(context, id, resultLauncher)
                    },
                    onLongClick = {
                        if (id == NoteVm.ID_CREATED) return@combinedClickable
                        runBlocking {
                            if (NoteVm.getInstance()!!.isNoteSaving(id)) return@runBlocking
                            NoteVm.getInstance()?.setDialogState(id, NoteVm.Companion.DLG_STATE.OPTIONS)
                        }
                    },
                )
        ) {
            if (id == NoteVm.ID_CREATED) {
                Image(painter = painterResource(R.drawable.ic_note_empty),
                    contentDescription = title,
                    modifier = Modifier.width(IMG_WIDTH).height(IMG_HEIGHT).padding(vertical = 10.dp)
                        .border(width = 1.dp, color = Color.Black)
                )
            } else {
                GlideWidget.GlideImage(noteUrl, title, ColorPainter(Color.White), ColorPainter(Color.Black),
                    modifier = Modifier.width(IMG_WIDTH).height(IMG_HEIGHT).padding(vertical = 10.dp)
                        .border(width = 1.dp, color = Color.Black))
            }

            Text(text = title, modifier = Modifier.padding(bottom = 6.dp))
            Date(updateMillis).also {
                if (updateMillis != 0L) {
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(it).also { text ->
                        Text(text = text, modifier = Modifier.padding(bottom = 10.dp))
                    }
                } else {
                    Text(text = " ", modifier = Modifier.padding(bottom = 10.dp))
                }
            }
        }
    }

    @Composable
    fun Content() {
        val nStatus = NoteVm.getInstance()!!.noteStatus.collectAsState()
        val dStatus = NoteVm.getInstance()!!.dlgStatus.collectAsState()
        val scope = rememberCoroutineScope()
        val resultLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.also {
                    val noteId = it.getLongExtra("saving_id", -1L)
                    LogUtil.d(TAG, "Content() activity result noteId = $noteId")
                    if (noteId != -1L) {
                        scope.launch {
                            NoteVm.getInstance()!!.reloadNoteUntilSaveCompleted(noteId)
                        }
                    }
                }
            }
        }

        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        LogUtil.d(TAG, "Content() ON_RESUME...")
                        scope.launch {
                            NoteVm.getInstance()!!.reloadNotes()
                        }
                    }
                    Lifecycle.Event.ON_DESTROY -> {
                        LogUtil.d(TAG, "Content() ON_DESTROY...")
                    }
                    else -> {}
                }
            }

            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(3),
            verticalItemSpacing = 20.dp,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(start = 40.dp, top = 80.dp, end = 40.dp)
        ) {
            LogUtil.d(TAG, "LazyVerticalStaggeredGrid()")
            items(nStatus.value.items.size, key = { i -> nStatus.value.items[i].updateTime }) { index ->
                val item = nStatus.value.items[index]
                var pageIndex = if (item.pageNo - 1 < 0) 0 else item.pageNo - 1
                val paths = item.imgPaths?.split(";")
                var noteUrl: String? = null
                if (paths != null) {
                    if (pageIndex >= paths.size) {
                        LogUtil.d(TAG, "LazyVerticalStaggeredGrid() pageIndex($pageIndex) > paths.size(${paths.size}) !!")
                        pageIndex = 0
                    }
                    noteUrl = paths[pageIndex]
                }
                Item(item.id, noteUrl, item.title, item.updateTime, resultLauncher)
            }
        }

        when (dStatus.value.state) {
            NoteVm.Companion.DLG_STATE.OPTIONS -> {
                DialogWidget.OptionsDialog(
                    onDismiss = {
                        NoteVm.getInstance()?.setDialogState(null, NoteVm.Companion.DLG_STATE.DISMISS)
                    },
                    onRename = {
                        scope.launch {
                            NoteVm.getInstance()?.setDialogState(dStatus.value.selId!!, NoteVm.Companion.DLG_STATE.RENAME)
                        }
                    },
                    onDelete = {
                        scope.launch {
                            NoteVm.getInstance()?.deleteNote(dStatus.value.selId!!)
                            NoteVm.getInstance()?.setDialogState(null, NoteVm.Companion.DLG_STATE.DISMISS)
                        }
                    }
                )
            }
            NoteVm.Companion.DLG_STATE.RENAME -> {
                val item = NoteVm.getInstance()?.getNoteItemFromStatus(dStatus.value.selId!!)
                DialogWidget.RenameDialog(item!!.title,
                    onDismiss = {
                        NoteVm.getInstance()?.setDialogState(null, NoteVm.Companion.DLG_STATE.DISMISS)
                    },
                    onConfirm = { newName ->
                        scope.launch {
                            NoteVm.getInstance()?.also {
                                val entity = it.getNoteEntity(item!!.id)!!
                                entity.name = newName
                                entity.updatedTime = System.currentTimeMillis()
                                it.updateNote(entity.id, entity)
                                delay(300)
                                NodeUtils.fullRefresh(context)
                            }
                        }
                    })
            }
            else -> {

            }
        }
    }
}