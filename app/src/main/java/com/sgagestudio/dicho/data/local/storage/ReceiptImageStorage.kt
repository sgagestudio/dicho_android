package com.sgagestudio.dicho.data.local.storage

import android.content.Context
import android.net.Uri
import java.io.File
import java.time.Instant

class ReceiptImageStorage(private val context: Context) {
    fun createImageFile(): File {
        val directory = File(context.filesDir, "receipts").apply { mkdirs() }
        return File(directory, "receipt_${Instant.now().toEpochMilli()}.jpg")
    }

    fun deleteImage(uri: String) {
        val file = File(Uri.parse(uri).path.orEmpty())
        if (file.exists()) {
            file.delete()
        }
    }
}
