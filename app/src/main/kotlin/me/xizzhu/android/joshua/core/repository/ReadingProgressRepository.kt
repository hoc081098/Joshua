/*
 * Copyright (C) 2021 Xizhi Zhu
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

package me.xizzhu.android.joshua.core.repository

import me.xizzhu.android.joshua.core.ReadingProgress
import me.xizzhu.android.joshua.core.repository.local.LocalReadingProgressStorage

class ReadingProgressRepository(private val localReadingProgressStorage: LocalReadingProgressStorage) {
    suspend fun trackReadingProgress(bookIndex: Int, chapterIndex: Int, timeSpentInMills: Long, timestamp: Long) {
        localReadingProgressStorage.trackReadingProgress(bookIndex, chapterIndex, timeSpentInMills, timestamp)
    }

    suspend fun read(): ReadingProgress = localReadingProgressStorage.read()

    suspend fun save(readingProgress: ReadingProgress) {
        localReadingProgressStorage.save(readingProgress)
    }
}
