/*
 * Copyright (C) 2019 Xizhi Zhu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.xizzhu.android.joshua.annotated.notes.list

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.xizzhu.android.joshua.annotated.BaseAnnotatedVersesInteractor
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.infra.arch.viewData

class NotesListInteractor(private val noteManager: VerseAnnotationManager<Note>,
                          bibleReadingManager: BibleReadingManager,
                          settingsManager: SettingsManager,
                          dispatcher: CoroutineDispatcher = Dispatchers.Default)
    : BaseAnnotatedVersesInteractor<Note>(bibleReadingManager, settingsManager, dispatcher) {
    override fun sortOrder(): Flow<ViewData<Int>> = noteManager.observeSortOrder().map { ViewData.success(it) }

    override suspend fun verseAnnotations(@Constants.SortOrder sortOrder: Int): ViewData<List<Note>> =
            viewData { noteManager.read(sortOrder) }
}
