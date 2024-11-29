//package com.example.mhnfe.data.remote.api
//
//import com.example.mhnfe.data.remote.request.CCTVRequest
//import com.example.mhnfe.data.remote.response.CCTVResponse
//import retrofit2.http.Body
//import retrofit2.http.POST
//
//interface QRApi {
//    @POST("/open-api/cctv")
//    suspend fun createCCTV(
//        @Body request: CCTVRequest
//    ): CCTVResponse
//
//}