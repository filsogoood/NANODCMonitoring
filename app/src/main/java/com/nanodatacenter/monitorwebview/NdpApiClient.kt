package com.nanodatacenter.monitorwebview

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * NDP API 클라이언트
 */
class NdpApiClient {
    
    companion object {
        // 서버 URL (웹 프로젝트와 동일한 서버)
        private const val BASE_URL = "http://192.168.100.102:8080"
        
        @Volatile
        private var INSTANCE: NdpApiClient? = null
        
        fun getInstance(): NdpApiClient {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NdpApiClient().also { INSTANCE = it }
            }
        }
    }
    
    private val apiService: NdpApiService
    
    init {
        // HTTP 로깅 인터셉터 설정 (더 상세한 로그)
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("NANODP_HTTP", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        // OkHttp 클라이언트 설정
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)  // 연결 타임아웃 증가
            .readTimeout(60, TimeUnit.SECONDS)     // 읽기 타임아웃 증가
            .writeTimeout(60, TimeUnit.SECONDS)    // 쓰기 타임아웃 증가
            .build()
        
        // Retrofit 설정
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        apiService = retrofit.create(NdpApiService::class.java)
        
        Log.i("NANODP_API", "🔧 API 클라이언트 초기화 완료")
        Log.i("NANODP_API", "🌐 서버 URL: $BASE_URL")
    }
    
    /**
     * 로그인 수행
     */
    suspend fun login(userId: String, password: String): Result<LoginResponse> {
        return try {
            Log.d("NANODP_API", "🌐 로그인 API 호출 시작")
            Log.d("NANODP_API", "📡 요청 URL: $BASE_URL/api/users/login")
            Log.d("NANODP_API", "👤 사용자 ID: $userId")
            
            val loginRequest = LoginRequest(userId, password)
            val response = apiService.login(loginRequest)
            
            Log.d("NANODP_API", "📨 응답 코드: ${response.code()}")
            
            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    Log.d("NANODP_API", "✅ 로그인 API 응답 성공")
                    Log.d("NANODP_API", "🔑 토큰 수신: ${loginResponse.token.take(15)}...")
                    Result.success(loginResponse)
                } ?: Result.failure(Exception("응답 본문이 비어있습니다."))
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "아이디 또는 비밀번호가 올바르지 않습니다."
                    else -> "서버에서 오류가 발생했습니다. (${response.code()})"
                }
                Log.e("NANODP_API", "❌ 로그인 API 실패: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: java.net.ConnectException) {
            Log.e("NANODP_API", "💥 연결 실패: 서버에 연결할 수 없습니다.")
            Log.e("NANODP_API", "🔍 서버 URL: $BASE_URL")
            Log.e("NANODP_API", "💡 서버가 실행 중인지 확인해주세요.")
            Result.failure(Exception("서버에 연결할 수 없습니다. 서버가 실행 중인지 확인해주세요."))
        } catch (e: java.net.SocketTimeoutException) {
            Log.e("NANODP_API", "💥 연결 타임아웃: 서버 응답이 너무 늦습니다.")
            Result.failure(Exception("서버 응답 시간이 초과되었습니다."))
        } catch (e: java.net.UnknownHostException) {
            Log.e("NANODP_API", "💥 호스트를 찾을 수 없음: ${e.message}")
            Result.failure(Exception("네트워크 연결을 확인해주세요."))
        } catch (e: Exception) {
            Log.e("NANODP_API", "💥 로그인 API 예외 발생: ${e.javaClass.simpleName}")
            Log.e("NANODP_API", "📝 상세 메시지: ${e.message}")
            Result.failure(Exception("네트워크 연결을 확인해주세요: ${e.message}"))
        }
    }
    
    /**
     * 사용자 데이터 조회 (GET 방식)
     */
    suspend fun getUserData(token: String): Result<ApiResponse> {
        return try {
            Log.d("NANODP_API", "🌐 사용자 데이터 API 호출 (GET)")
            Log.d("NANODP_API", "📡 요청 URL: $BASE_URL/api/users/data")
            Log.d("NANODP_API", "🔑 토큰: ${token.take(20)}...")
            
            val response = apiService.getUserData("Bearer $token")
            
            Log.d("NANODP_API", "📨 응답 코드: ${response.code()}")
            
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    Log.d("NANODP_API", "✅ 사용자 데이터 API 응답 성공 (GET)")
                    Log.d("NANODP_API", "📊 점수 데이터 개수: ${apiResponse.allScores?.size ?: 0}")
                    Result.success(apiResponse)
                } ?: Result.failure(Exception("응답 본문이 비어있습니다."))
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "인증이 만료되었거나 잘못되었습니다."
                    403 -> "접근이 거부되었습니다."
                    else -> "서버에서 오류가 발생했습니다. (${response.code()})"
                }
                Log.e("NANODP_API", "❌ 사용자 데이터 API 실패 (GET): $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: java.net.ConnectException) {
            Log.e("NANODP_API", "💥 연결 실패 (GET): 서버에 연결할 수 없습니다.")
            Result.failure(Exception("서버에 연결할 수 없습니다."))
        } catch (e: java.net.SocketTimeoutException) {
            Log.e("NANODP_API", "💥 연결 타임아웃 (GET): 서버 응답이 너무 늦습니다.")
            Result.failure(Exception("서버 응답 시간이 초과되었습니다."))
        } catch (e: Exception) {
            Log.e("NANODP_API", "💥 사용자 데이터 API 예외 (GET): ${e.javaClass.simpleName}")
            Log.e("NANODP_API", "📝 상세 메시지: ${e.message}")
            Result.failure(Exception("네트워크 연결을 확인해주세요: ${e.message}"))
        }
    }
    
    /**
     * 사용자 데이터 조회 (POST 방식)
     */
    suspend fun getUserDataPost(token: String): Result<ApiResponse> {
        return try {
            Log.d("NANODP_API", "🌐 사용자 데이터 API 호출 (POST)")
            Log.d("NANODP_API", "📡 요청 URL: $BASE_URL/api/users/data")
            Log.d("NANODP_API", "🔑 토큰: ${token.take(20)}...")
            
            val response = apiService.getUserDataPost("Bearer $token")
            
            Log.d("NANODP_API", "📨 응답 코드: ${response.code()}")
            
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    Log.d("NANODP_API", "✅ 사용자 데이터 API 응답 성공 (POST)")
                    Log.d("NANODP_API", "📊 점수 데이터 개수: ${apiResponse.allScores?.size ?: 0}")
                    Result.success(apiResponse)
                } ?: Result.failure(Exception("응답 본문이 비어있습니다."))
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "인증이 만료되었거나 잘못되었습니다."
                    403 -> "접근이 거부되었습니다."
                    else -> "서버에서 오류가 발생했습니다. (${response.code()})"
                }
                Log.e("NANODP_API", "❌ 사용자 데이터 API 실패 (POST): $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: java.net.ConnectException) {
            Log.e("NANODP_API", "💥 연결 실패 (POST): 서버에 연결할 수 없습니다.")
            Result.failure(Exception("서버에 연결할 수 없습니다."))
        } catch (e: java.net.SocketTimeoutException) {
            Log.e("NANODP_API", "💥 연결 타임아웃 (POST): 서버 응답이 너무 늦습니다.")
            Result.failure(Exception("서버 응답 시간이 초과되었습니다."))
        } catch (e: Exception) {
            Log.e("NANODP_API", "💥 사용자 데이터 API 예외 (POST): ${e.javaClass.simpleName}")
            Log.e("NANODP_API", "📝 상세 메시지: ${e.message}")
            Result.failure(Exception("네트워크 연결을 확인해주세요: ${e.message}"))
        }
    }
    
    /**
     * 노드 데이터 조회
     */
    suspend fun getNodesData(token: String): Result<ApiResponse> {
        return try {
            val response = apiService.getNodesData("Bearer $token")
            
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    Result.success(apiResponse)
                } ?: Result.failure(Exception("응답 본문이 비어있습니다."))
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "인증이 만료되었거나 잘못되었습니다."
                    403 -> "접근이 거부되었습니다."
                    else -> "서버에서 오류가 발생했습니다. (${response.code()})"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("네트워크 연결을 확인해주세요: ${e.message}"))
        }
    }
}
