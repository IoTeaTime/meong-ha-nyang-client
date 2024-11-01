package com.example.mhnfe.data.model

data class CCTV(
    val id: String,
    val name: String,
)
val emptyCCTVList = emptyList<CCTV>()

val sampleCCTVList = listOf(
    CCTV(
        id = "1",
        name = "주방"
    ),
    CCTV(
        id = "2",
        name = "거실"
    ),
    CCTV(
        id = "3",
        name = "방1"
    )
)
//원래는 도메인 모델에 넣어야함
enum class UserType {
    MASTER,
    VIEWER,
    CCTV;

    companion object {
        fun fromString(type: String?): UserType {
            return when (type?.lowercase()) {
                "master" -> MASTER
                "viewer" -> VIEWER
                "cctv" -> CCTV
                else -> VIEWER
            }
        }
    }
}

enum class QRGenerateType {
    CCTV,
    VIEWER;

    companion object {
        fun fromString(type: String?): QRGenerateType {
            return when (type?.lowercase()) {
                "cctv" -> CCTV
                "viewer" -> VIEWER
                else -> CCTV // 기본값
            }
        }

        fun fromUserType(userType: UserType): QRGenerateType {
            return when (userType) {
                UserType.CCTV -> CCTV
                UserType.VIEWER -> VIEWER
                else -> throw IllegalArgumentException("Invalid type for QR generation")
            }
        }
    }

    fun toTitle(): String = when (this) {
        CCTV -> "CCTV 등록"
        VIEWER -> "뷰어 등록"
    }

    fun toMessage(): String = when (this) {
        CCTV -> "CCTV로 사용할 기기에서\nQR 인증을 해주세요"
        VIEWER -> "뷰어로 사용할 기기에서\nQR 인증을 해주세요"
    }
}
