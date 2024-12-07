package com.example.mhnfe.domain.ai

import com.example.mhnfe.domain.ai.yolo.BoundingBox
import org.json.JSONObject

// 좌표 데이터 클래스
data class BoundingBoxCoordinates(
    val x1: Int, val y1: Int,
    val x2: Int, val y2: Int,
    val x3: Int, val y3: Int,
    val x4: Int, val y4: Int
)

object BoundingBoxUtils {

    // BoundingBox 객체의 좌표를 BoundingBoxCoordinates 객체로 변환
    private fun boundingBoxCoordinates(box: BoundingBox): BoundingBoxCoordinates {
        return BoundingBoxCoordinates(
            x1 = box.x1.toInt(),  // 좌상단 x
            y1 = box.y1.toInt(),  // 좌상단 y
            x2 = box.x2.toInt(),  // 우상단 x
            y2 = box.y1.toInt(),  // 우상단 y (같은 y값)
            x3 = box.x1.toInt(),  // 좌하단 x (같은 x값)
            y3 = box.y2.toInt(),  // 좌하단 y
            x4 = box.x2.toInt(),  // 우하단 x (같은 x값)
            y4 = box.y2.toInt()   // 우하단 y
        )
    }

    // 각 박스의 좌표를 JSON 형식으로 반환
    fun generateCoordinatesJson(boundingBoxes: List<BoundingBox>): String {
        return JSONObject().apply {
            boundingBoxes.forEachIndexed { index, box ->
                val coordinates = boundingBoxCoordinates(box)
                put("box_$index", JSONObject().apply {
                    put("x1", coordinates.x1)
                    put("y1", coordinates.y1)
                    put("x2", coordinates.x2)
                    put("y2", coordinates.y2)
                    put("x3", coordinates.x3)
                    put("y3", coordinates.y3)
                    put("x4", coordinates.x4)
                    put("y4", coordinates.y4)
                })
            }
        }.toString()
    }

    fun generateObjectNameJson(boundingBoxes: List<BoundingBox>): String =
        boundingBoxes.joinToString(prefix = "[", postfix = "]") { box ->
            "{\"object_name\":\"${box.objectName}\"}"
        }

    fun generateConfidenceJson(boundingBoxes: List<BoundingBox>): String =
        boundingBoxes.joinToString(prefix = "[", postfix = "]") { box ->
            "{\"confidence\":${box.cnf}}"
        }

    fun shouldTriggerEvent(lastEventTime: Long, eventDelayMillis: Long): Boolean =
        (System.currentTimeMillis() - lastEventTime) >= eventDelayMillis
}