package com.example.gpstracker.core.geo

import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine

internal suspend fun OkHttpClient.execute(request: Request): String {
    val call = newCall(request)
    return suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isActive) continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                response.use {
                    val body = it.body?.string().orEmpty()
                    if (!it.isSuccessful) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(HttpStatusException(it.code, body.take(180)))
                        }
                    } else if (continuation.isActive) {
                        continuation.resume(body)
                    }
                }
            }
        })
    }
}

internal class HttpStatusException(val code: Int, val bodySnippet: String) : IOException("HTTP $code")

internal suspend fun <T> retrying(block: suspend () -> T): T {
    var delayMs = 400L
    var last: Exception? = null
    repeat(MAX_ATTEMPTS) { attempt ->
        try {
            return block()
        } catch (e: HttpStatusException) {
            last = e
            if (e.code != 429 && e.code !in 500..599) throw e
        } catch (e: IOException) {
            last = e
        }
        if (attempt < MAX_ATTEMPTS - 1) {
            delay(delayMs)
            delayMs *= 2
        }
    }
    throw last ?: IOException("request failed")
}

private const val MAX_ATTEMPTS = 3
