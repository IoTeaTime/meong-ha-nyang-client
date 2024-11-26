package com.example.mhnfe.data.repository

import android.util.Log
import com.example.mhnfe.data.remote.api.ApiService
import com.example.mhnfe.data.remote.api.AuthApi
import com.example.mhnfe.data.remote.request.EmailRequest
import com.example.mhnfe.data.remote.request.LoginRequest
import com.example.mhnfe.data.remote.request.RefreshFcmTokenRequest
import com.example.mhnfe.data.remote.response.Result
import com.google.gson.Gson
import retrofit2.HttpException
import com.example.mhnfe.data.remote.response.LoginResponse
import com.example.mhnfe.data.remote.request.SignUpRequest
import com.example.mhnfe.data.remote.response.CheckEmailResponse
import com.example.mhnfe.data.remote.response.FCMResponse
import com.example.mhnfe.data.remote.response.SignUpResponse

class AuthRepository {
    private val api: AuthApi

    init {
        api = ApiService.createApiService(AuthApi::class.java)
    }

    suspend fun signUp(
        email: String,
        password: String,
        passwordConfirm: String,
        nickname: String
    ): SignUpResponse {
        val request = SignUpRequest(email, password, passwordConfirm, nickname)
        return api.signUp(request)
    }

    suspend fun checkEmailDuplicate(email: String): CheckEmailResponse {
        val emailRequest = EmailRequest(email)
//        Log.d("AuthRepository", "checkEmailDuplicate 요청: $emailRequest")
        return try {
            val response = api.checkEmailDuplicate(emailRequest)
//            Log.d("AuthRepository", "checkEmailDuplicate 응답2: $response")
            response
        } catch (e: HttpException) {
//            Log.e("AuthRepository", "checkEmailDuplicate HTTP 예외1: ${e.code()}", e)
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = parseErrorResponse(errorBody)
            CheckEmailResponse(errorResponse.result, errorResponse.data)
        } catch (e: Exception) {
            Log.e("AuthRepository", "checkEmailDuplicate API 호출 실패: ${e.message}", e)
            throw e
        }
    }

    private fun parseErrorResponse(errorBody: String?): CheckEmailResponse {
        return try {
            if (!errorBody.isNullOrEmpty()) {
                Gson().fromJson(errorBody, CheckEmailResponse::class.java)
            } else {
                CheckEmailResponse(Result(-1, "Unknown Error"), null)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error parsing response: ${e.message}", e)
            CheckEmailResponse(Result(-1, "Parsing Error"), null)
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): LoginResponse {
        val request = LoginRequest(email, password)
        return api.login(request)
    }

    suspend fun refreshFcmToken(jwtToken: String, fcmToken: String): FCMResponse {
        val request = RefreshFcmTokenRequest(fcmToken)
        return api.refreshFcmToken(jwtToken, request)
    }
}