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

    fun MatToBitmap(mat: Mat): Bitmap {
        val bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bitmap)
        return bitmap
    }
}
