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

package me.xizzhu.android.joshua.reading

import dagger.Module
import dagger.Provides
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.TranslationManager
import me.xizzhu.android.joshua.reading.chapter.ChapterListPresenter
import me.xizzhu.android.joshua.reading.toolbar.ToolbarPresenter
import me.xizzhu.android.joshua.reading.verse.VersePresenter
import me.xizzhu.android.joshua.ActivityScope

@Module
class ReadingModule {
    @ActivityScope
    @Provides
    fun provideReadingViewController(readingActivity: ReadingActivity,
                                     bibleReadingManager: BibleReadingManager,
                                     translationManager: TranslationManager): ReadingViewController =
            ReadingViewController(readingActivity, bibleReadingManager, translationManager)

    @Provides
    fun provideReadingDrawerPresenter(readingViewController: ReadingViewController): ReadingDrawerPresenter =
            ReadingDrawerPresenter(readingViewController)

    @Provides
    fun provideToolbarPresenter(readingViewController: ReadingViewController): ToolbarPresenter =
            ToolbarPresenter(readingViewController)

    @Provides
    fun provideChapterListPresenter(readingViewController: ReadingViewController): ChapterListPresenter =
            ChapterListPresenter(readingViewController)

    @Provides
    fun provideVersePresenter(readingViewController: ReadingViewController): VersePresenter =
            VersePresenter(readingViewController)

    @Provides
    fun provideSearchButtonPresenter(readingViewController: ReadingViewController): SearchButtonPresenter =
            SearchButtonPresenter(readingViewController)
}
