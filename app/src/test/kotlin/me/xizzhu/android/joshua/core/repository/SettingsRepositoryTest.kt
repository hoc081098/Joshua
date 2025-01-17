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

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.repository.local.LocalSettingsStorage
import me.xizzhu.android.joshua.tests.BaseUnitTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsRepositoryTest : BaseUnitTest() {
    private lateinit var localSettingsStorage: LocalSettingsStorage

    private lateinit var settingsRepository: SettingsRepository

    @BeforeTest
    override fun setup() {
        super.setup()

        localSettingsStorage = mockk()
        coEvery { localSettingsStorage.readSettings() } returns Settings.DEFAULT
        coEvery { localSettingsStorage.saveSettings(any()) } returns Unit

        settingsRepository = SettingsRepository(localSettingsStorage, testDispatcher)
    }

    @Test
    fun `test observe settings within constructor`() = runBlocking {
        val settings = Settings(false, true, 3, false, false, true, Highlight.COLOR_PINK)
        coEvery { localSettingsStorage.readSettings() } returns settings

        settingsRepository = SettingsRepository(localSettingsStorage)

        assertEquals(settings, settingsRepository.settings.first())
    }

    @Test
    fun `test observe settings within constructor with exception`() = runBlocking {
        coEvery { localSettingsStorage.readSettings() } throws RuntimeException("Random exception")
        settingsRepository = SettingsRepository(localSettingsStorage)

        assertEquals(Settings.DEFAULT, settingsRepository.settings.first())
    }

    @Test
    fun `test saveSettings()`() = runBlocking {
        val settings = Settings(false, true, 1, true, true, true, Highlight.COLOR_BLUE)
        settingsRepository.saveSettings(settings)
        assertEquals(settings, settingsRepository.settings.first())
    }

    @Test
    fun `test saveSettings() does nothing if setting remains unchanged`() = runBlocking {
        settingsRepository.saveSettings(Settings.DEFAULT)
        settingsRepository.saveSettings(Settings.DEFAULT)
        coVerify(exactly = 0) { localSettingsStorage.saveSettings((any())) }
    }

    @Test
    fun `test saveSettings() for font size scale`() = runBlocking {
        saveUpdatedSettingsAndVerifySaveSettingsIsCalled(Settings.DEFAULT.copy(fontSizeScale = 1))
    }

    private suspend fun saveUpdatedSettingsAndVerifySaveSettingsIsCalled(updatedSettings: Settings) {
        settingsRepository.saveSettings(updatedSettings)
        coVerify(exactly = 1) { localSettingsStorage.saveSettings(updatedSettings) }
    }

    @Test
    fun `test saveSettings() for keeping screen on`() = runBlocking {
        saveUpdatedSettingsAndVerifySaveSettingsIsCalled(Settings.DEFAULT.copy(keepScreenOn = false))
    }

    @Test
    fun `test saveSettings() for night mode on`() = runBlocking {
        saveUpdatedSettingsAndVerifySaveSettingsIsCalled(Settings.DEFAULT.copy(nightModeOn = true))
    }

    @Test
    fun `test saveSettings() for for simple reading mode on`() = runBlocking {
        saveUpdatedSettingsAndVerifySaveSettingsIsCalled(Settings.DEFAULT.copy(simpleReadingModeOn = true))
    }

    @Test
    fun `test saveSettings() for hiding search button`() = runBlocking {
        saveUpdatedSettingsAndVerifySaveSettingsIsCalled(Settings.DEFAULT.copy(hideSearchButton = true))
    }

    @Test
    fun `test saveSettings() for for consolidating verses for sharing`() = runBlocking {
        saveUpdatedSettingsAndVerifySaveSettingsIsCalled(Settings.DEFAULT.copy(consolidateVersesForSharing = true))
    }

    @Test
    fun `test saveSettings() for highlight color`() = runBlocking {
        saveUpdatedSettingsAndVerifySaveSettingsIsCalled(Settings.DEFAULT.copy(defaultHighlightColor = Highlight.COLOR_PURPLE))
    }
}
