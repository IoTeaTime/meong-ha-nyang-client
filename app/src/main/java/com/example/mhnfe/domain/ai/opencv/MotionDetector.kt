package com.example.mhnfe.domain.ai.opencv


import kotlinx.coroutines.*
import android.util.Log
import org.opencv.core.*
import org.opencv.imgproc.Imgproc


class MotionDetector {

    private var previousFrame: Mat? = null
    private val minArea = 1300 // 최소 움직임 영역 크기
    private val tag = "MotionDetector"

    fun detectMotion(currentFrame: Mat): List<Rect> {
        val grayFrame = Mat()
        val motionAreas = mutableListOf<Rect>()

        runBlocking {
            launch(Dispatchers.Default) {
                try {
                    // 컬러 이미지를 흑백으로 변환(CV_8UC4 -> CV_8UC1)
                    Imgproc.cvtColor(currentFrame, grayFrame, Imgproc.COLOR_RGBA2GRAY)

                    // 프레임 저장
                    if (previousFrame == null) {
                        previousFrame = grayFrame.clone()
                        return@launch
                    }

                    // 프레임 간 차이 계산
                    val diffFrame = Mat()
                    Core.absdiff(previousFrame, grayFrame, diffFrame)

                    // 가우시안 블러 적용
                    val blurredFrame = Mat()
                    Imgproc.GaussianBlur(diffFrame, blurredFrame, Size(31.0, 31.0), 0.0)

                    // 차이를 Threshold로 처리(이진화)
                    val threshFrame = Mat()
                    Imgproc.threshold(blurredFrame, threshFrame, 50.0, 255.0, Imgproc.THRESH_BINARY)

                    // 모폴로지 연산(침식 후 팽창)
                    Imgproc.dilate(threshFrame, threshFrame, Mat(), Point(-1.0, -1.0), 2)
                    Imgproc.erode(threshFrame, threshFrame, Mat(), Point(-1.0, -1.0), 1)

                    // Contour를 찾아 움직임 영역 감지
                    val contours = mutableListOf<MatOfPoint>()
                    Imgproc.findContours(
                        threshFrame,
                        contours,
                        Mat(),
                        Imgproc.RETR_EXTERNAL,
                        Imgproc.CHAIN_APPROX_SIMPLE
                    )

                    for (contour in contours) {
                        val area = Imgproc.contourArea(contour)
                        if (area > minArea) { // 최소 영역 필터링
                            val boundingRect = Imgproc.boundingRect(contour)
                            if (boundingRect.width >= 50 && boundingRect.height >= 50) { // 최소 ROI 필터링
                                motionAreas.add(boundingRect)
                            }
                        }
                    }

                    // 현재 프레임을 이전 프레임으로 갱신
                    previousFrame?.release()
                    previousFrame = grayFrame.clone()

                    // 리소스 해제
                    diffFrame.release()
                    blurredFrame.release()
                    threshFrame.release()

                } catch (e: Exception) {
                    Log.e(tag, "Error in motion detection: ${e.message}", e)
                } finally {
                    grayFrame.release()
                }
            }
        }
        return motionAreas
    }
}