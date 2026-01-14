package es.didaktikapp.gernikapp.network

import es.didaktikapp.gernikapp.ApiConfig
import es.didaktikapp.gernikapp.data.models.LoginRequest
import es.didaktikapp.gernikapp.data.models.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST(ApiConfig.AUTH_LOGIN)
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>
}