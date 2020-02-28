/*
 * Copyright (C) 2020 Xizhi Zhu
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

package me.xizzhu.android.joshua.reading

import androidx.annotation.IntDef
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.infra.activity.BaseSettingsViewModel
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.infra.arch.filterOnSuccess
import me.xizzhu.android.joshua.infra.arch.toViewData
import me.xizzhu.android.joshua.utils.currentTimeMillis

data class ChapterListViewData(val currentVerseIndex: VerseIndex, val bookNames: List<String>)

data class VerseDetailRequest(val verseIndex: VerseIndex, @Content val content: Int) {
    companion object {
        const val VERSES = 0
        const val NOTE = 1
        const val STRONG_NUMBER = 2

        @IntDef(VERSES, NOTE, STRONG_NUMBER)
        @Retention(AnnotationRetention.SOURCE)
        annotation class Content
    }
}

data class VerseUpdate(val verseIndex: VerseIndex, @Operation val operation: Int, val data: Any? = null) {
    companion object {
        const val VERSE_SELECTED = 1
        const val VERSE_DESELECTED = 2
        const val NOTE_ADDED = 3
        const val NOTE_REMOVED = 4
        const val BOOKMARK_ADDED = 5
        const val BOOKMARK_REMOVED = 6
        const val HIGHLIGHT_UPDATED = 7

        @IntDef(VERSE_SELECTED, VERSE_DESELECTED, NOTE_ADDED, NOTE_REMOVED,
                BOOKMARK_ADDED, BOOKMARK_REMOVED, HIGHLIGHT_UPDATED)
        @Retention(AnnotationRetention.SOURCE)
        annotation class Operation
    }
}

class ReadingViewModel(
        private val bibleReadingManager: BibleReadingManager, private val readingProgressManager: ReadingProgressManager,
        private val translationManager: TranslationManager, private val bookmarkManager: VerseAnnotationManager<Bookmark>,
        private val highlightManager: VerseAnnotationManager<Highlight>, private val noteManager: VerseAnnotationManager<Note>,
        private val strongNumberManager: StrongNumberManager, settingsManager: SettingsManager
) : BaseSettingsViewModel(settingsManager) {
    // TODO migrate when https://github.com/Kotlin/kotlinx.coroutines/issues/1082 is done
    private val verseDetailRequest: BroadcastChannel<VerseDetailRequest> = ConflatedBroadcastChannel()
    private val verseUpdates: BroadcastChannel<VerseUpdate> = ConflatedBroadcastChannel()

    fun downloadedTranslations(): Flow<ViewData<List<TranslationInfo>>> = translationManager.downloadedTranslations()
            .distinctUntilChanged()
            .toViewData()

    fun currentTranslation(): Flow<ViewData<String>> = _currentTranslation().toViewData()

    // TODO
    private fun _currentTranslation(): Flow<String> = bibleReadingManager.currentTranslation().filter { it.isNotEmpty() }

    suspend fun saveCurrentTranslation(translationShortName: String) {
        bibleReadingManager.saveCurrentTranslation(translationShortName)
    }

    fun parallelTranslations(): Flow<ViewData<List<String>>> = bibleReadingManager.parallelTranslations().toViewData()

    suspend fun requestParallelTranslation(translationShortName: String) {
        bibleReadingManager.requestParallelTranslation(translationShortName)
    }

    suspend fun removeParallelTranslation(translationShortName: String) {
        bibleReadingManager.removeParallelTranslation(translationShortName)
    }

    suspend fun clearParallelTranslation() {
        bibleReadingManager.clearParallelTranslation()
    }

    fun currentVerseIndex(): Flow<ViewData<VerseIndex>> = _currentVerseIndex().toViewData()

    // TODO
    private fun _currentVerseIndex(): Flow<VerseIndex> = bibleReadingManager.currentVerseIndex().filter { it.isValid() }

    suspend fun saveCurrentVerseIndex(verseIndex: VerseIndex) {
        bibleReadingManager.saveCurrentVerseIndex(verseIndex)
    }

    fun chapterListViewData(): Flow<ViewData<ChapterListViewData>> =
            _currentVerseIndex().combine(bookNames()) { currentVerseIndex, bookNames ->
                ChapterListViewData(currentVerseIndex, bookNames)
            }.toViewData()

    private fun bookNames(): Flow<List<String>> = _currentTranslation()
            .map { bibleReadingManager.readBookNames(it) }

    suspend fun readBookNames(translationShortName: String): List<String> =
            bibleReadingManager.readBookNames(translationShortName)

    fun bookShortNames(): Flow<ViewData<List<String>>> = currentTranslation()
            .filterOnSuccess()
            .map { ViewData.success(bibleReadingManager.readBookShortNames(it)) }

    suspend fun readVerses(translationShortName: String, bookIndex: Int, chapterIndex: Int): List<Verse> =
            bibleReadingManager.readVerses(translationShortName, bookIndex, chapterIndex)

    suspend fun readVerses(translationShortName: String, parallelTranslations: List<String>,
                           bookIndex: Int, chapterIndex: Int): List<Verse> =
            bibleReadingManager.readVerses(translationShortName, parallelTranslations, bookIndex, chapterIndex)

    suspend fun readBookmarks(bookIndex: Int, chapterIndex: Int): List<Bookmark> = bookmarkManager.read(bookIndex, chapterIndex)

    suspend fun readBookmark(verseIndex: VerseIndex): Bookmark = bookmarkManager.read(verseIndex)

    suspend fun saveBookmark(verseIndex: VerseIndex, hasBookmark: Boolean) {
        if (hasBookmark) {
            bookmarkManager.remove(verseIndex)
            verseUpdates.offer(VerseUpdate(verseIndex, VerseUpdate.BOOKMARK_REMOVED))
        } else {
            bookmarkManager.save(Bookmark(verseIndex, currentTimeMillis()))
            verseUpdates.offer(VerseUpdate(verseIndex, VerseUpdate.BOOKMARK_ADDED))
        }
    }

    suspend fun readHighlights(bookIndex: Int, chapterIndex: Int): List<Highlight> = highlightManager.read(bookIndex, chapterIndex)

    suspend fun readHighlight(verseIndex: VerseIndex): Highlight = highlightManager.read(verseIndex)

    suspend fun saveHighlight(verseIndex: VerseIndex, @Highlight.Companion.AvailableColor color: Int) {
        if (color == Highlight.COLOR_NONE) {
            highlightManager.remove(verseIndex)
            verseUpdates.offer(VerseUpdate(verseIndex, VerseUpdate.HIGHLIGHT_UPDATED, Highlight.COLOR_NONE))
        } else {
            highlightManager.save(Highlight(verseIndex, color, currentTimeMillis()))
            verseUpdates.offer(VerseUpdate(verseIndex, VerseUpdate.HIGHLIGHT_UPDATED, color))
        }
    }

    suspend fun readNotes(bookIndex: Int, chapterIndex: Int): List<Note> = noteManager.read(bookIndex, chapterIndex)

    suspend fun readNote(verseIndex: VerseIndex): Note = noteManager.read(verseIndex)

    suspend fun saveNote(verseIndex: VerseIndex, note: String) {
        if (note.isEmpty()) {
            noteManager.remove(verseIndex)
            verseUpdates.offer(VerseUpdate(verseIndex, VerseUpdate.NOTE_REMOVED))
        } else {
            noteManager.save(Note(verseIndex, note, currentTimeMillis()))
            verseUpdates.offer(VerseUpdate(verseIndex, VerseUpdate.NOTE_ADDED))
        }
    }

    suspend fun readStrongNumber(verseIndex: VerseIndex): List<StrongNumber> = strongNumberManager.readStrongNumber(verseIndex)

    fun downloadStrongNumber(): Flow<ViewData<Int>> =
            strongNumberManager.download()
                    .map { progress ->
                        if (progress <= 100) {
                            ViewData.loading(progress)
                        } else {
                            // Ideally, we should use onCompletion() to handle this. However, it doesn't
                            // distinguish between a successful completion and a cancellation.
                            // See https://github.com/Kotlin/kotlinx.coroutines/issues/1693
                            ViewData.success(-1)
                        }
                    }
                    .catch { cause ->
                        // TODO Log.e(tag, "Failed to download Strong number", cause)
                        emit(ViewData.error(exception = cause))
                    }

    fun startTracking() {
        readingProgressManager.startTracking()
    }

    fun stopTracking() {
        // uses GlobalScope to make sure this will be executed without being canceled
        // uses Dispatchers.Main.immediate to make sure this will be executed immediately
        GlobalScope.launch(Dispatchers.Main.immediate) { readingProgressManager.stopTracking() }
    }

    fun verseUpdates(): Flow<VerseUpdate> = verseUpdates.asFlow()

    fun verseDetailRequest(): Flow<VerseDetailRequest> = verseDetailRequest.asFlow()

    fun requestVerseDetail(request: VerseDetailRequest) {
        verseDetailRequest.offer(request)
    }

    fun showNoteInVerseDetail() {
        viewModelScope.launch {
            bibleReadingManager.currentVerseIndex().first().let { verseIndex ->
                if (verseIndex.isValid()) {
                    // NOTE It's a hack here, because the only thing needed by verse interactor is to select the verse
                    verseUpdates.offer(VerseUpdate(verseIndex, VerseUpdate.VERSE_SELECTED))
                    verseDetailRequest.offer(VerseDetailRequest(verseIndex, VerseDetailRequest.NOTE))
                }
            }
        }
    }

    fun closeVerseDetail(verseIndex: VerseIndex) {
        // NOTE It's a hack here, because the only thing needed by the other end (verse interactor) is to deselect the verse
        verseUpdates.offer(VerseUpdate(verseIndex, VerseUpdate.VERSE_DESELECTED))
    }
}
