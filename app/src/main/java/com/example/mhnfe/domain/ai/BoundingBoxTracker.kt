package com.example.mhnfe.domain.ai

import android.graphics.Rect

// todo BoundingBoxUtils을 불러 옴(추후에 Yolo Bounding Box의 값을 받아와야 함)
// 임시로 코드 수정 해 놨음

class BoundingBoxTracker {
    private var lastBoundingBox: BoundingBoxCoordinates? = null // 이전 bounding box 저장

    // 이전 bounding box와 현재 bounding box의 차이를 계산하는 함수
    fun trackBoundingBox(): Boolean {
        val currentBoundingBox = BoundingBoxUtils.boundingBoxCoordinates()
        val isMovementDetected = isMovement(lastBoundingBox, currentBoundingBox)

        // 이전 bounding box를 현재 값으로 업데이트
        lastBoundingBox = currentBoundingBox

        return isMovementDetected
    }

    // 좌표 변화가 10 이상인지 확인하는 함수
    private fun isMovement(lastBoundingBox: BoundingBoxCoordinates?, currentBoundingBox: BoundingBoxCoordinates): Boolean {
        if (lastBoundingBox == null) return false // 초기 상태는 이동 없음

        // BoundingBoxUtils에서 좌표 변환 메서드 활용
        /*
        val lastCoordinates = BoundingBoxUtils.boundingBoxCoordinatesFromRect(lastBoundingBox)
        val currentCoordinates = BoundingBoxUtils.boundingBoxCoordinatesFromRect(currentBoundingBox)

        // x, y 좌표 변화 계산
        val xDiff = listOf(
            Math.abs(lastCoordinates.x1 - currentCoordinates.x1),
            Math.abs(lastCoordinates.x2 - currentCoordinates.x2),
            Math.abs(lastCoordinates.x3 - currentCoordinates.x3),
            Math.abs(lastCoordinates.x4 - currentCoordinates.x4)
        ).maxOrNull() ?: 0

        val yDiff = listOf(
            Math.abs(lastCoordinates.y1 - currentCoordinates.y1),
            Math.abs(lastCoordinates.y2 - currentCoordinates.y2),
            Math.abs(lastCoordinates.y3 - currentCoordinates.y3),
            Math.abs(lastCoordinates.y4 - currentCoordinates.y4)
        ).maxOrNull() ?: 0
        */
        val xDiff = listOf(
            Math.abs(lastBoundingBox.x1 - currentBoundingBox.x1),
            Math.abs(lastBoundingBox.x2 - currentBoundingBox.x2),
            Math.abs(lastBoundingBox.x3 - currentBoundingBox.x3),
            Math.abs(lastBoundingBox.x4 - currentBoundingBox.x4)
        ).maxOrNull() ?: 0

        val yDiff = listOf(
            Math.abs(lastBoundingBox.y1 - currentBoundingBox.y1),
            Math.abs(lastBoundingBox.y2 - currentBoundingBox.y2),
            Math.abs(lastBoundingBox.y3 - currentBoundingBox.y3),
            Math.abs(lastBoundingBox.y4 - currentBoundingBox.y4)
        ).maxOrNull() ?: 0

        // x 또는 y 좌표 차이가 10 이상인 경우 행동 변화로 판단
        return xDiff >= 10 || yDiff >= 10
    }
}