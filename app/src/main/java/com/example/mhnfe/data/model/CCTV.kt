package com.example.mhnfe.data.model

data class CCTV(
    val id: String,
    val name: String,
)

//아무것도 없는거 테스트 할때
val emptyCCTVList = emptyList<CCTV>()

//CCTV 예시 데이터
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
