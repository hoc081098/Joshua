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

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.utils.currentTimeMillis
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ReadingViewModelTest : BaseUnitTest() {
    @Mock
    private lateinit var bibleReadingManager: BibleReadingManager
    @Mock
    private lateinit var readingProgressManager: ReadingProgressManager
    @Mock
    private lateinit var translationManager: TranslationManager
    @Mock
    private lateinit var bookmarkManager: VerseAnnotationManager<Bookmark>
    @Mock
    private lateinit var highlightManager: VerseAnnotationManager<Highlight>
    @Mock
    private lateinit var noteManager: VerseAnnotationManager<Note>
    @Mock
    private lateinit var strongNumberManager: StrongNumberManager
    @Mock
    private lateinit var settingsManager: SettingsManager

    private lateinit var readingViewModel: ReadingViewModel

    @BeforeTest
    override fun setup() {
        super.setup()

        readingViewModel = ReadingViewModel(bibleReadingManager, readingProgressManager, translationManager,
                bookmarkManager, highlightManager, noteManager, strongNumberManager, settingsManager)
    }

    @Test
    fun testDownloadedTranslations() = testDispatcher.runBlockingTest {
        `when`(translationManager.downloadedTranslations()).thenReturn(
                flowOf(
                        emptyList(),
                        emptyList(),
                        listOf(MockContents.kjvTranslationInfo, MockContents.bbeTranslationInfo),
                        listOf(MockContents.kjvTranslationInfo, MockContents.bbeTranslationInfo),
                        listOf(MockContents.bbeTranslationInfo)
                )
        )

        assertEquals(
                listOf(
                        ViewData.success(emptyList()),
                        ViewData.success(listOf(MockContents.kjvTranslationInfo, MockContents.bbeTranslationInfo)),
                        ViewData.success(listOf(MockContents.bbeTranslationInfo))
                ),
                readingViewModel.downloadedTranslations().toList()
        )
    }

    @Test
    fun testCurrentTranslation() = testDispatcher.runBlockingTest {
        `when`(bibleReadingManager.currentTranslation()).thenReturn(flowOf("", MockContents.kjvShortName, "", ""))

        assertEquals(
                listOf(ViewData.success(MockContents.kjvShortName)),
                readingViewModel.currentTranslation().toList()
        )
    }

    @Test
    fun testCurrentVerseIndex() = testDispatcher.runBlockingTest {
        `when`(bibleReadingManager.currentVerseIndex()).thenReturn(
                flowOf(VerseIndex.INVALID, VerseIndex(1, 2, 3), VerseIndex.INVALID, VerseIndex.INVALID)
        )

        assertEquals(
                listOf(ViewData.success(VerseIndex(1, 2, 3))),
                readingViewModel.currentVerseIndex().toList()
        )
    }

    @Test
    fun testChapterListViewData() = testDispatcher.runBlockingTest {
        `when`(bibleReadingManager.currentVerseIndex()).thenReturn(flowOf(VerseIndex(0, 0, 0)))
        `when`(bibleReadingManager.currentTranslation()).thenReturn(flowOf("", MockContents.kjvShortName, "", ""))
        `when`(bibleReadingManager.readBookNames(MockContents.kjvShortName)).thenReturn(MockContents.kjvBookNames)

        assertEquals(
                listOf(ViewData.success(ChapterListViewData(VerseIndex(0, 0, 0), MockContents.kjvBookNames))),
                readingViewModel.chapterListViewData().toList()
        )
    }

    @Test
    fun testBookShortNames() = testDispatcher.runBlockingTest {
        `when`(bibleReadingManager.currentTranslation()).thenReturn(flowOf("", MockContents.kjvShortName, "", ""))
        `when`(bibleReadingManager.readBookShortNames(MockContents.kjvShortName)).thenReturn(MockContents.kjvBookShortNames)

        assertEquals(
                listOf(ViewData.success(MockContents.kjvBookShortNames)),
                readingViewModel.bookShortNames().toList()
        )
    }

    @Test
    fun testSaveBookmark() = testDispatcher.runBlockingTest {
        currentTimeMillis = 1234L
        val verseUpdates = async { readingViewModel.verseUpdates().take(2).toList() }

        readingViewModel.saveBookmark(VerseIndex(1, 2, 3), true)
        readingViewModel.saveBookmark(VerseIndex(4, 5, 6), false)

        with(inOrder(bookmarkManager)) {
            verify(bookmarkManager, times(1)).remove(VerseIndex(1, 2, 3))
            verify(bookmarkManager, times(1)).save(Bookmark(VerseIndex(4, 5, 6), 1234L))
        }
        assertEquals(
                listOf(
                        VerseUpdate(VerseIndex(1, 2, 3), VerseUpdate.BOOKMARK_REMOVED),
                        VerseUpdate(VerseIndex(4, 5, 6), VerseUpdate.BOOKMARK_ADDED)
                ),
                verseUpdates.await()
        )
    }

    @Test
    fun testSaveHighlight() = testDispatcher.runBlockingTest {
        currentTimeMillis = 1234L
        val verseUpdates = async { readingViewModel.verseUpdates().take(2).toList() }

        readingViewModel.saveHighlight(VerseIndex(1, 2, 3), Highlight.COLOR_BLUE)
        readingViewModel.saveHighlight(VerseIndex(4, 5, 6), Highlight.COLOR_NONE)

        with(inOrder(highlightManager)) {
            verify(highlightManager, times(1)).save(Highlight(VerseIndex(1, 2, 3), Highlight.COLOR_BLUE, 1234L))
            verify(highlightManager, times(1)).remove(VerseIndex(4, 5, 6))
        }
        assertEquals(
                listOf(
                        VerseUpdate(VerseIndex(1, 2, 3), VerseUpdate.HIGHLIGHT_UPDATED, Highlight.COLOR_BLUE),
                        VerseUpdate(VerseIndex(4, 5, 6), VerseUpdate.HIGHLIGHT_UPDATED, Highlight.COLOR_NONE)
                ),
                verseUpdates.await()
        )
    }

    @Test
    fun testSaveNote() = testDispatcher.runBlockingTest {
        currentTimeMillis = 1234L
        val verseUpdates = async { readingViewModel.verseUpdates().take(2).toList() }

        readingViewModel.saveNote(VerseIndex(1, 2, 3), "random notes")
        readingViewModel.saveNote(VerseIndex(4, 5, 6), "")

        with(inOrder(noteManager)) {
            verify(noteManager, times(1)).save(Note(VerseIndex(1, 2, 3), "random notes", 1234L))
            verify(noteManager, times(1)).remove(VerseIndex(4, 5, 6))
        }
        assertEquals(
                listOf(
                        VerseUpdate(VerseIndex(1, 2, 3), VerseUpdate.NOTE_ADDED),
                        VerseUpdate(VerseIndex(4, 5, 6), VerseUpdate.NOTE_REMOVED)
                ),
                verseUpdates.await()
        )
    }
}
