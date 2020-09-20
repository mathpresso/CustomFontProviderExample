package com.mathpresso.customfontproviderexample

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.provider.FontsContractCompat
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException

class FontsProvider : ContentProvider() {

    private val client: OkHttpClient = OkHttpClient.Builder().build()

    override fun onCreate(): Boolean {
        Log.d(TAG, "onCreate")
        return true
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException()
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        Log.d(TAG, "query $uri")

        downloadFont()

        val cursor = MatrixCursor(
            arrayOf(
                FontsContractCompat.Columns._ID,
                FontsContractCompat.Columns.FILE_ID,
                FontsContractCompat.Columns.TTC_INDEX,
                FontsContractCompat.Columns.VARIATION_SETTINGS,
                FontsContractCompat.Columns.WEIGHT,
                FontsContractCompat.Columns.ITALIC,
                FontsContractCompat.Columns.RESULT_CODE
            )
        )
        cursor.addRow(arrayOf(1, 1, 0, 1, 400, 0, 0))

        return cursor
    }

    private fun downloadFont() {
        val fontFile = File(
            requireNotNull(context).getDir("fonts", Context.MODE_PRIVATE),
            "Lobster-Regular.ttf"
        )

        if (fontFile.exists()) {
            return
        }

        val request = Request.Builder()
            .get()
            .url(FONT_DOWNLOAD_URL)
            .build()

        try {
            val response = client.newCall(request).execute()
            Log.e("Test", "${response.code}")
            response.body?.source()?.use { source ->
                fontFile.sink().buffer().use { sink ->
                    sink.writeAll(source)
                    sink.flush()
                }
            }
        } catch (e: IOException) {
            if (fontFile.exists()) {
                fontFile.delete()
            }
        }
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        throw UnsupportedOperationException()
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException()
    }

    override fun getType(uri: Uri): String? {
        throw UnsupportedOperationException()
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        Log.d(TAG, "openFile $uri $mode")
        val fontFile = File(
            requireNotNull(context).getDir("fonts", Context.MODE_PRIVATE),
            "Lobster-Regular.ttf"
        )
        return ParcelFileDescriptor.open(fontFile, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    companion object {
        private const val TAG = "FontsProvider"

        private const val FONT_DOWNLOAD_URL =
            "https://github.com/mathpresso/CustomFontProviderExample/raw/master/fonts/Lobster-Regular.ttf"
    }
}