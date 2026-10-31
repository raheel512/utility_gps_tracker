package com.example.gpstracker.core.location

import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Awaits a Play Services [Task] without adding kotlinx-coroutines-play-services.
 */
suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { result ->
        if (cont.isActive) cont.resume(result)
    }
    addOnFailureListener { error ->
        if (cont.isActive) cont.resumeWithException(error)
    }
    addOnCanceledListener {
        cont.cancel()
    }
}

suspend fun <T> Task<T>.awaitTaskOrNull(): T? = try {
    awaitTask()
} catch (_: Exception) {
    null
}

fun newCancellationTokenSource(): CancellationTokenSource = CancellationTokenSource()
