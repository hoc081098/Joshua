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

package me.xizzhu.android.joshua.ui.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.ui.updateSettingsWithPrimaryText

data class TextItem(val title: CharSequence)
    : BaseItem(R.layout.item_text, { inflater, parent -> TextItemViewHolder(inflater, parent) })

private class TextItemViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : BaseViewHolder<TextItem>(inflater.inflate(R.layout.item_text, parent, false)) {
    private val title: TextView = itemView.findViewById(R.id.title)

    override fun bind(settings: Settings, item: TextItem, payloads: List<Any>) {
        with(title) {
            updateSettingsWithPrimaryText(settings)
            text = item.title
        }
    }
}
