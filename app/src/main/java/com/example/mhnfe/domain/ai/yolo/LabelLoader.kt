package com.example.mhnfe.domain.ai.yolo

import android.content.Context
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object LabelLoader {
    fun loadLabels(context: Context, labelPath: String): MutableList<String> {
        val labels = mutableListOf<String>()
        try {
            val inputStream = context.assets.open(labelPath)
            val reader = BufferedReader(InputStreamReader(inputStream))

            var line: String? = reader.readLine()
            while (line != null && line != "") {
                labels.add(line)
                line = reader.readLine()
            }

            reader.close()
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return labels
    }
}
