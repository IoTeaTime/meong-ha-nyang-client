package com.example.mhnfe.domain.ai

import com.example.mhnfe.domain.ai.yolo.BoundingBox

object BoundingBoxUtils {

    // 객체 이름과 신뢰도를 각각 반환하는 메서드
    fun getTypeAndConfidence(boundingBoxes: List<BoundingBox>): Pair<String, Float> {
        return if (boundingBoxes.isNotEmpty()) {
            val box = boundingBoxes.first()
            box.objectName to box.cnf
        } else {
            "unknown" to 0.0f
        }
    }

    // 각 박스의 좌표를 반환하는 메서드
    fun getCoordinates(boundingBoxes: List<BoundingBox>): List<Map<String, Int>> {
        return boundingBoxes.map { box ->
            mapOf(
                "x1" to box.x1,
                "y1" to box.y1,
                "x2" to box.x2,
                "y2" to box.y2,
                "x3" to box.x1,
                "y3" to box.y2,
                "x4" to box.x2,
                "y4" to box.y1
            )
        }
    }

    // 이벤트 발생 여부를 결정하는 메서드
    fun shouldTriggerEvent(lastEventTime: Long, eventDelayMillis: Long): Boolean =
        (System.currentTimeMillis() - lastEventTime) >= eventDelayMillis
}
