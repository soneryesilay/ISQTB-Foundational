package com.istqb.examsimulator.util

import android.content.Context
import java.io.IOException

object AssetLoader {
    fun loadAssetAsString(context: Context, fileName: String): String? {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            null
        }
    }

    fun getAssetFileNames(context: Context): List<String> {
        return try {
            context.assets.list("")?.toList() ?: emptyList()
        } catch (e: IOException) {
            emptyList()
        }
    }
}

