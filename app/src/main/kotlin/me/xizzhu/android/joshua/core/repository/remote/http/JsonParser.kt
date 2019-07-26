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

package me.xizzhu.android.joshua.core.repository.remote.http

import android.util.JsonReader
import androidx.annotation.VisibleForTesting
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationInfo
import me.xizzhu.android.logger.Log

private const val TAG = "JsonParser"

fun JsonReader.readListJson(): List<RemoteTranslationInfo> {
    beginObject()
    while (hasNext()) {
        when (nextName()) {
            "translations" -> {
                return readTranslationsArray()
            }
            else -> {
                skipValue()
                Log.w(TAG, "Unsupported JSON format", RuntimeException("Unsupported JSON format in list.json"))
            }
        }
    }
    endObject()
    Log.w(TAG, "Unsupported JSON format", RuntimeException("Missing 'translations' in list.json"))
    return emptyList()
}

@VisibleForTesting
fun JsonReader.readTranslationsArray(): List<RemoteTranslationInfo> {
    val remoteTranslations = ArrayList<RemoteTranslationInfo>()
    beginArray()
    while (hasNext()) {
        readTranslation()?.let { remoteTranslations.add(it) }
    }
    endArray()
    return remoteTranslations
}

@VisibleForTesting
fun JsonReader.readTranslation(): RemoteTranslationInfo? {
    var shortName: String? = null
    var name: String? = null
    var language: String? = null
    var size: Long? = null
    beginObject()
    while (hasNext()) {
        when (nextName()) {
            "shortName" -> shortName = nextString()
            "name" -> name = nextString()
            "language" -> language = nextString()
            "size" -> size = nextLong()
            else -> {
                skipValue()
                Log.w(TAG, "Unsupported JSON format", RuntimeException("Unsupported JSON format in list.json"))
            }
        }
    }
    endObject()
    if (shortName == null || name == null || language == null || size == null) {
        Log.w(TAG, "Unsupported JSON format", RuntimeException("Illegal 'translation' in list.json - short name: $shortName"))
        return null
    }
    return RemoteTranslationInfo(shortName, name, language, size)
}

fun JsonReader.readBooksJson(): Pair<List<String>, List<String>> {
    var bookNames: List<String>? = null
    var bookShortNames: List<String>? = null
    beginObject()
    while (hasNext()) {
        when (nextName()) {
            "name" -> skipValue()
            "shortName" -> skipValue()
            "language" -> skipValue()
            "bookNames" -> bookNames = readStringsArray()
            "bookShortNames" -> bookShortNames = readStringsArray()
            else -> {
                skipValue()
                Log.w(TAG, "Unsupported JSON format", RuntimeException("Unsupported JSON format in books.json"))
            }
        }
    }
    endObject()
    if (bookNames?.size == Bible.BOOK_COUNT && bookShortNames?.size == Bible.BOOK_COUNT) {
        return Pair(bookNames, bookShortNames)
    }
    throw RuntimeException("Illegal JSON format in books.json")
}

@VisibleForTesting
fun JsonReader.readStringsArray(): List<String> {
    val strings = ArrayList<String>()
    beginArray()
    while (hasNext()) {
        strings.add(nextString())
    }
    endArray()
    return strings
}

fun JsonReader.readChapterJson(): List<String> {
    beginObject()
    while (hasNext()) {
        when (nextName()) {
            "verses" -> return readStringsArray().apply {
                if (isEmpty()) {
                    throw RuntimeException("Empty verses array in chapter JSON")
                }
            }
            else -> {
                skipValue()
                Log.w(TAG, "Unsupported JSON format", RuntimeException("Unsupported format in chapter JSON"))
            }
        }
    }
    endObject()
    throw RuntimeException("Illegal format in chapter JSON")
}
