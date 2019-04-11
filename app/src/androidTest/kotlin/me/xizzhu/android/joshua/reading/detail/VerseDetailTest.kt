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

package me.xizzhu.android.joshua.reading.detail

import me.xizzhu.android.joshua.core.Note
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.Test
import kotlin.test.assertEquals

class VerseDetailTest : BaseUnitTest() {
    @Test
    fun testGetStringForDisplay() {
        val expected = "KJV, Genesis 1:1\nIn the beginning God created the heaven and the earth."
        val actual = VerseDetail(MockContents.kjvVerses[0], false, "").textForDisplay.toString()
        assertEquals(expected, actual)
    }

    @Test
    fun testGetStringForDisplayWithParallelTranslation() {
        val expected = "KJV, Genesis 1:1\nIn the beginning God created the heaven and the earth.\n\n中文和合本, 创世记 1:1\n起初神创造天地。"
        val actual = VerseDetail(MockContents.kjvVersesWithCuvParallel[0], false, "").textForDisplay.toString()
        assertEquals(expected, actual)
    }
}
