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

package me.xizzhu.android.joshua.notes

import android.os.Bundle
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.ui.LoadingSpinner
import me.xizzhu.android.joshua.ui.LoadingSpinnerPresenter
import me.xizzhu.android.joshua.ui.bindView
import me.xizzhu.android.joshua.utils.BaseSettingsActivity
import javax.inject.Inject

class NotesActivity : BaseSettingsActivity() {
    @Inject
    lateinit var notesInteractor: NotesInteractor

    @Inject
    lateinit var loadingSpinnerPresenter: LoadingSpinnerPresenter

    @Inject
    lateinit var notesPresenter: NotesPresenter

    private val loadingSpinner: LoadingSpinner by bindView(R.id.loading_spinner)
    private val notesListView: NotesListView by bindView(R.id.notes)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_notes)
        notesListView.setPresenter(notesPresenter)

        observeSettings(notesInteractor)
    }

    override fun onStart() {
        super.onStart()

        loadingSpinnerPresenter.attachView(loadingSpinner)
        notesPresenter.attachView(notesListView)
    }

    override fun onStop() {
        loadingSpinnerPresenter.detachView()
        notesPresenter.detachView()

        super.onStop()
    }
}