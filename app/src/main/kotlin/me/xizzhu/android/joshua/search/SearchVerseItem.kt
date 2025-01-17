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

package me.xizzhu.android.joshua.search

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.ui.*
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.BaseViewHolder
import java.util.*

class SearchVerseItem(val verseIndex: VerseIndex, private val bookShortName: String,
                      private val text: String, private val query: String,
                      @ColorInt private val highlightColor: Int)
    : BaseItem(R.layout.item_search_verse, { inflater, parent -> SearchVerseItemViewHolder(inflater, parent) }) {
    companion object {
        // We don't expect users to change locale that frequently.
        @SuppressLint("ConstantLocale")
        private val DEFAULT_LOCALE = Locale.getDefault()

        private val BOOK_NAME_SIZE_SPAN = createTitleSizeSpan()
        private val BOOK_NAME_STYLE_SPAN = createTitleStyleSpan()
        private val KEYWORD_SIZE_SPAN = createKeywordSizeSpan()
        private val KEYWORD_STYLE_SPAN = createKeywordStyleSpan()
        private val SPANNABLE_STRING_BUILDER = SpannableStringBuilder()
    }

    interface Callback {
        fun openVerse(verseToOpen: VerseIndex)
    }

    val textForDisplay: CharSequence by lazy {
        // format:
        // <short book name> <chapter verseIndex>:<verse verseIndex>
        // <verse text>
        SPANNABLE_STRING_BUILDER.clearAll()
                .append(bookShortName)
                .append(' ')
                .append(verseIndex.chapterIndex + 1).append(':').append(verseIndex.verseIndex + 1)
                .setSpan(BOOK_NAME_SIZE_SPAN, BOOK_NAME_STYLE_SPAN)
                .append('\n')
                .append(text)

        // highlights the keywords
        val textStartIndex = SPANNABLE_STRING_BUILDER.length - text.length
        val lowerCase = SPANNABLE_STRING_BUILDER.toString().lowercase(DEFAULT_LOCALE)
        for ((index, keyword) in query.trim().replace("\\s+", " ").split(" ").withIndex()) {
            val start = lowerCase.indexOf(keyword.lowercase(DEFAULT_LOCALE), textStartIndex)
            if (start > 0) {
                SPANNABLE_STRING_BUILDER.setSpan(
                        if (index == 0) KEYWORD_SIZE_SPAN else createKeywordSizeSpan(),
                        if (index == 0) KEYWORD_STYLE_SPAN else createKeywordStyleSpan(),
                        start, start + keyword.length)
            }
        }

        // highlights the verse if needed
        if (highlightColor != Highlight.COLOR_NONE) {
            SPANNABLE_STRING_BUILDER.setSpan(
                    BackgroundColorSpan(highlightColor),
                    ForegroundColorSpan(if (highlightColor == Highlight.COLOR_BLUE) Color.WHITE else Color.BLACK),
                    SPANNABLE_STRING_BUILDER.length - text.length, SPANNABLE_STRING_BUILDER.length
            )
        }

        return@lazy SPANNABLE_STRING_BUILDER.toCharSequence()
    }
}

private class SearchVerseItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<SearchVerseItem>(inflater.inflate(R.layout.item_search_verse, parent, false)) {
    private val text = itemView as TextView

    init {
        itemView.setOnClickListener {
            item?.let { item ->
                (itemView.activity as? SearchVerseItem.Callback)?.openVerse(item.verseIndex)
                        ?: throw IllegalStateException("Attached activity [${itemView.activity.javaClass.name}] does not implement SearchVerseItem.Callback")
            }
        }
    }

    override fun bind(settings: Settings, item: SearchVerseItem, payloads: List<Any>) {
        with(text) {
            updateSettingsWithPrimaryText(settings)
            text = item.textForDisplay
        }
    }
}
