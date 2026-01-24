package es.didaktikapp.gernikapp.network

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import es.didaktikapp.gernikapp.BuildConfig
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private var retrofit: Retrofit? = null

    /**
     * Instancia única de Moshi para toda la aplicación
     * (reutilizable para parsing de JSON en diferentes partes del código)
     */
    val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    fun getInstance(context: Context): Retrofit {
        if (retrofit == null) {
            synchronized(this) {
                if (retrofit == null) {
                    retrofit = createRetrofit(context)
                }
            }
        }
        return retrofit!!
    }

    private fun createRetrofit(context: Context): Retrofit {
        val tokenManager = TokenManager(context)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(Constants.Network.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(Constants.Network.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(Constants.Network.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    fun getApiService(context: Context): ApiService {
        return getInstance(context).create(ApiService::class.java)
    }
}