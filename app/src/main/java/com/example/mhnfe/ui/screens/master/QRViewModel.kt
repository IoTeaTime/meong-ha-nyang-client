package com.example.mhnfe.ui.screens.master

import android.graphics.Bitmap
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.util.UUID

class QRViewModel : ViewModel() {
    private val _qrContent = mutableStateOf(UUID.randomUUID().toString())
    val qrContent: State<String> = _qrContent

    fun generateNewQRCode() {
        _qrContent.value = UUID.randomUUID().toString()
    }

    // QR 코드 생성 로직
    fun generateQRBitmap(size: Int): Bitmap {
        val hints = hashMapOf<EncodeHintType, Any>().apply {
            put(EncodeHintType.MARGIN, 1)
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
        }

        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(
                _qrContent.value,  // 현재 UUID 값 사용
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
            )

            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)

            canvas.drawColor(android.graphics.Color.WHITE)

            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
            }

            for (x in 0 until width) {
                for (y in 0 until height) {
                    if (bitMatrix.get(x, y)) {
                        canvas.drawRect(
                            x.toFloat(),
                            y.toFloat(),
                            (x + 1).toFloat(),
                            (y + 1).toFloat(),
                            paint
                        )
                    }
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}