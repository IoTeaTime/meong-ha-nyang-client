package com.example.mhnfe.domain.ai.opencv

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Mat

object BitmapToMatConverter {
    fun BitToMat(bitmap: Bitmap): Mat {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)
        return mat
    }
}
