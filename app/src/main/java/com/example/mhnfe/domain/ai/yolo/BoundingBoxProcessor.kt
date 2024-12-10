package com.example.mhnfe.domain.ai.yolo

data class BoundingBox(
    val x1: Int,
    val y1: Int,
    val x2: Int,
    val y2: Int,
    val w: Int,   // 너비
    val h: Int,   // 높이
    val cnf: Float,   // confidence
    val idxNum: Int,   // 탐지된 번호
    val objectName: String
)

object BoundingBoxProcessor {

    // 상수 선언
    private const val CONFIDENCE_THRESHOLD = 0.3F
    private const val IOU_THRESHOLD = 0.5F
    private const val TAG = "BoundingBoxProcessor"
    private const val SCREEN_WIDTH = 1280
    private const val SCREEN_HEIGHT = 720

    // 콜백 -> 탐지된 객체 정보를 처리하도록 수정
    fun bestBoxes(
        array: FloatArray,
        labels: List<String>,
        numElements: Int,
        numChannel: Int,
        callback: (List<BoundingBox>) -> Unit
    ) {
        val boundingBoxes = mutableListOf<BoundingBox>()

        for (c in 0 until numElements) {
            var maxConf = CONFIDENCE_THRESHOLD
            var maxIdx = -1
            var j = 4
            var arrayIdx = c + numElements * j
            while (j < numChannel) {
                if (array[arrayIdx] > maxConf) {
                    maxConf = array[arrayIdx]
                    maxIdx = j - 4
                }
                j++
                arrayIdx += numElements
            }

            if (maxConf > CONFIDENCE_THRESHOLD) {
                val clsName = labels[maxIdx]
                val cx = array[c] * SCREEN_WIDTH
                val cy = array[c + numElements] * SCREEN_HEIGHT
                val w = array[c + numElements * 2] * SCREEN_WIDTH
                val h = array[c + numElements * 3] * SCREEN_HEIGHT
                val x1 = (cx - (w / 2F)).toInt()
                val y1 = (cy - (h / 2F)).toInt()
                val x2 = (cx + (w / 2F)).toInt()
                val y2 = (cy + (h / 2F)).toInt()
                if (x1 < 0 || x1 > SCREEN_WIDTH) continue
                if (y1 < 0 || y1 > SCREEN_HEIGHT) continue
                if (x2 < 0 || x2 > SCREEN_WIDTH) continue
                if (y2 < 0 || y2 > SCREEN_HEIGHT) continue

                boundingBoxes.add(
                    BoundingBox(
                        x1 = x1, y1 = y1, x2 = x2, y2 = y2,
                        w = w.toInt(), h = h.toInt(),
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

        callback(filteredBoxes)
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
        val intersectionArea = maxOf(0F, (x2 - x1).toFloat()) * maxOf(0F, (y2 - y1).toFloat())
        val box1Area = box1.w * box1.h
        val box2Area = box2.w * box2.h
        return intersectionArea / (box1Area + box2Area - intersectionArea)
    }
}