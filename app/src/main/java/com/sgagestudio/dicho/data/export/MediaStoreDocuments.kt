package com.sgagestudio.dicho.data.export

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore

fun saveBytesToPublicDocuments(
    context: Context,
    fileName: String,
    mimeType: String,
    bytes: ByteArray,
): Uri? {
    val resolver = context.contentResolver
    val collection = MediaStore.Files.getContentUri("external")
    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        } else {
            // TODO: Android 9 y anteriores pueden requerir WRITE_EXTERNAL_STORAGE o usar SAF.
        }
    }

    val uri = resolver.insert(collection, values) ?: return null
    return runCatching {
        resolver.openOutputStream(uri)?.use { output ->
            output.write(bytes)
            output.flush()
        } ?: return null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val pendingValues = ContentValues().apply {
                put(MediaStore.MediaColumns.IS_PENDING, 0)
            }
            resolver.update(uri, pendingValues, null, null)
        }
        uri
    }.getOrElse {
        resolver.delete(uri, null, null)
        null
    }
}

fun describeSavedLocation(context: Context, uri: Uri): String {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val projection = arrayOf(
            MediaStore.MediaColumns.RELATIVE_PATH,
            MediaStore.MediaColumns.DISPLAY_NAME,
        )
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val relativePathIndex = cursor.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH)
                val displayNameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                if (relativePathIndex >= 0 && displayNameIndex >= 0) {
                    val relativePath = cursor.getString(relativePathIndex) ?: ""
                    val displayName = cursor.getString(displayNameIndex) ?: ""
                    val combined = "$relativePath$displayName"
                    if (combined.isNotBlank()) {
                        return combined
                    }
                }
            }
        }
    }
    return uri.toString()
}
