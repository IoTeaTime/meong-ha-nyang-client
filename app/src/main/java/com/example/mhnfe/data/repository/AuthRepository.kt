package com.example.mhnfe.data.repository

import android.util.Log
import com.example.mhnfe.data.api.ApiService
import com.example.mhnfe.data.api.AuthApi
import com.example.mhnfe.data.model.ApiResponse
import com.example.mhnfe.data.model.EmailRequest
import com.example.mhnfe.data.model.Result
import com.example.mhnfe.data.model.SignUpRequest
import com.google.gson.Gson
import retrofit2.HttpException

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
    ): ApiResponse {
        val request = SignUpRequest(email, password, passwordConfirm, nickname)
        return api.signUp(request)
    }

    suspend fun checkEmailDuplicate(email: String): ApiResponse {
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
            ApiResponse(errorResponse.result, errorResponse.data)
        } catch (e: Exception) {
            Log.e("AuthRepository", "checkEmailDuplicate API 호출 실패: ${e.message}", e)
            throw e
        }
    }

    private fun parseErrorResponse(errorBody: String?): ApiResponse {
        return try {
            if (!errorBody.isNullOrEmpty()) {
                Gson().fromJson(errorBody, ApiResponse::class.java)
            } else {
                ApiResponse(com.example.mhnfe.data.model.Result(-1, "Unknown Error"), null)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error parsing response: ${e.message}", e)
            ApiResponse(Result(-1, "Parsing Error"), null)
        }
    }
}