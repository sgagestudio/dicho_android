package com.sgagestudio.dicho.data.camera

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.util.UUID

object ReceiptImageStore {
    fun createTempImageFile(context: Context): File {
        val directory = File(context.cacheDir, "receipts").apply { mkdirs() }
        return File(
            directory,
            "receipt_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg",
        )
    }

    fun deleteImageUri(context: Context, uri: Uri) {
        runCatching {
            when (uri.scheme) {
                "file", null -> {
                    val path = uri.path ?: return@runCatching
                    File(path).delete()
                }
                "content" -> {
                    context.contentResolver.delete(uri, null, null)
                }
            }
        }.onFailure { error ->
            Log.w("ReceiptImageStore", "No se pudo borrar imagen temporal.", error)
        }
    }
}
