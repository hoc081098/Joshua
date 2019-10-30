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

package me.xizzhu.android.joshua.infra.arch

import androidx.annotation.IntDef
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

data class ViewData<T> private constructor(@Status val status: Int, val data: T?, val exception: Throwable?) {
    companion object {
        const val STATUS_SUCCESS = 0
        const val STATUS_ERROR = 1
        const val STATUS_LOADING = 2

        @IntDef(STATUS_SUCCESS, STATUS_ERROR, STATUS_LOADING)
        @Retention(AnnotationRetention.SOURCE)
        annotation class Status

        fun <T> success(data: T): ViewData<T> = ViewData(STATUS_SUCCESS, data, null)
        fun <T> error(data: T? = null, exception: Throwable? = null): ViewData<T> = ViewData(STATUS_ERROR, data, exception)
        fun <T> loading(data: T? = null): ViewData<T> = ViewData(STATUS_LOADING, data, null)
    }
}

fun <T> ViewData<T>.toNothing(): ViewData<Nothing?> = when (status) {
    ViewData.STATUS_SUCCESS -> ViewData.success(null)
    ViewData.STATUS_ERROR -> ViewData.error(exception = exception)
    ViewData.STATUS_LOADING -> ViewData.loading()
    else -> throw IllegalStateException("Unsupported view data status: $status")
}

inline fun <R> viewData(block: () -> R): ViewData<R> = try {
    ViewData.success(block())
} catch (e: Exception) {
    ViewData.error(exception = e)
}

fun <T> ViewData<T>.dataOnSuccessOrThrow(errorMessage: String): T =
        if (ViewData.STATUS_SUCCESS == status) {
            data!!
        } else {
            throw IllegalStateException(errorMessage, exception)
        }

suspend inline fun <T> Flow<ViewData<T>>.collect(
        crossinline onLoading: suspend (value: T?) -> Unit,
        crossinline onSuccess: suspend (value: T) -> Unit,
        crossinline onError: suspend (value: T?, exception: Throwable?) -> Unit): Unit = collect { viewData ->
    when (viewData.status) {
        ViewData.STATUS_LOADING -> onLoading(viewData.data)
        ViewData.STATUS_SUCCESS -> onSuccess(viewData.data!!)
        ViewData.STATUS_ERROR -> onError(viewData.data, viewData.exception)
        else -> throw IllegalStateException("Unsupported status: ${viewData.status}")
    }
}

suspend inline fun <T> Flow<ViewData<T>>.collectOnSuccess(crossinline action: suspend (value: T) -> Unit): Unit = collect { viewData ->
    if (viewData.status == ViewData.STATUS_SUCCESS) action(viewData.data!!)
}
