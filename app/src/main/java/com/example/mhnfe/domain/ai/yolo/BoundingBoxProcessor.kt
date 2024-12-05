package com.example.mhnfe.domain.ai.yolo

import android.util.Log

data class BoundingBox(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val w: Float,   // 너비
    val h: Float,   // 높이
    val cnf: Float,   // confidence
    val idxNum: Int,   // 탐지된 번호
    val objectName: String
)

object BoundingBoxProcessor {

    // 상수 선언
    private const val CONFIDENCE_THRESHOLD = 0.3F
    private const val IOU_THRESHOLD = 0.5F
    private const val TAG = "BoundingBoxProcessor"

    fun bestBoxes(
        array: FloatArray,
        labels: List<String>,
        numElements: Int,
        numChannel: Int
    ): List<BoundingBox> {
        val boundingBoxes = mutableListOf<BoundingBox>()

        for (c in 0 until numElements) {
            var maxConf = CONFIDENCE_THRESHOLD
            var maxIdx = -1
            var j = 4
            var arrayIdx = c + numElements * j
            while (j < numChannel){
                if (array[arrayIdx] > maxConf) {
                    maxConf = array[arrayIdx]
                    maxIdx = j - 4
                }
                j++
                arrayIdx += numElements
            }

            if (maxConf > CONFIDENCE_THRESHOLD) {
                val clsName = labels[maxIdx]
                val cx = array[c]
                val cy = array[c + numElements]
                val w = array[c + numElements * 2]
                val h = array[c + numElements * 3]
                val x1 = cx - (w / 2F)
                val y1 = cy - (h / 2F)
                val x2 = cx + (w / 2F)
                val y2 = cy + (h / 2F)
                if (x1 < 0F || x1 > 1F) continue
                if (y1 < 0F || y1 > 1F) continue
                if (x2 < 0F || x2 > 1F) continue
                if (y2 < 0F || y2 > 1F) continue

                boundingBoxes.add(
                    BoundingBox(
                        x1 = x1, y1 = y1, x2 = x2, y2 = y2,
                        w = w, h = h,
                        cnf = maxConf, idxNum = maxIdx, objectName = clsName
                    )
                )
            }
        }

        // Non-Maximum Suppression (NMS) 적용
        val selectedBoxes = applyNMS(boundingBoxes)

        // 필터링
        val filteredBoxes = selectedBoxes.filter {
            it.objectName in listOf("dog", "cat", "person")
        }

        filteredBoxes.forEach { box ->
            Log.d(TAG, "Detected ${box.objectName} with confidence: ${box.cnf}")
        }

        return filteredBoxes
    }

    private fun applyNMS(boxes: List<BoundingBox>): MutableList<BoundingBox> {
        val sortedBoxes = boxes.sortedByDescending { it.cnf }.toMutableList()
        val selectedBoxes = mutableListOf<BoundingBox>()

        while (sortedBoxes.isNotEmpty()) {
            val first = sortedBoxes.first()
            selectedBoxes.add(first)
            sortedBoxes.remove(first)

            val iterator = sortedBoxes.iterator()
            while (iterator.hasNext()) {
                val nextBox = iterator.next()
                val iou = calculateIoU(first, nextBox)
                if (iou >= IOU_THRESHOLD) {
                    iterator.remove()
                }
            }
        }

        return selectedBoxes
    }

    private fun calculateIoU(box1: BoundingBox, box2: BoundingBox): Float {
        val x1 = maxOf(box1.x1, box2.x1)
        val y1 = maxOf(box1.y1, box2.y1)
        val x2 = minOf(box1.x2, box2.x2)
        val y2 = minOf(box1.y2, box2.y2)
        val intersectionArea = maxOf(0F, x2 - x1) * maxOf(0F, y2 - y1)
        val box1Area = box1.w * box1.h
        val box2Area = box2.w * box2.h
        return intersectionArea / (box1Area + box2Area - intersectionArea)
    }
}
