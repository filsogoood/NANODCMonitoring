package com.nanodatacenter.monitorwebview

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        private const val BASE_URL = "http://211.176.180.172:8080"
        
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
     * 기본 네트워크 연결 테스트 (디버깅용)
     */
    suspend fun testBasicConnectivity(): Boolean {
        Log.d("NANODP_API", "🔧 기본 네트워크 연결 테스트 시작")
        
        return withContext(Dispatchers.IO) {
            try {
                // OkHttp를 사용한 연결 테스트 (백그라운드 스레드에서 실행)
                val testClient = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .build()
                
                val request = okhttp3.Request.Builder()
                    .url("$BASE_URL/")
                    .head() // HEAD 요청으로 빠른 연결 테스트
                    .build()
                
                Log.d("NANODP_API", "🔌 OkHttp 연결 테스트 중...")
                val response = testClient.newCall(request).execute()
                
                val isConnected = response.isSuccessful || response.code in 400..499
                Log.d("NANODP_API", "✅ 연결 테스트 성공: $isConnected (응답코드: ${response.code})")
                
                response.close()
                isConnected
                
            } catch (e: Exception) {
                Log.e("NANODP_API", "❌ 연결 테스트 실패: ${e.javaClass.simpleName} - ${e.message}")
                false
            }
        }
    }

    /**
     * 로그인 수행
     */
    suspend fun login(userId: String, password: String): Result<LoginResponse> {
        return try {
            // 서버 연결 상태 사전 확인
            Log.d("NANODP_API", "🌐 서버 연결 상태 확인 중...")
            val isConnected = checkServerConnection()
            if (!isConnected) {
                Log.e("NANODP_API", "❌ 서버 연결 불가 - 로그인 중단")
                return Result.failure(Exception("서버에 연결할 수 없습니다. 네트워크 상태를 확인해주세요."))
            }
            
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
                    500 -> "서버 내부 오류가 발생했습니다."
                    503 -> "서버가 일시적으로 사용할 수 없습니다."
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
     * 서버 연결 상태 확인
     */
    private suspend fun checkServerConnection(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("NANODP_API", "🔍 서버 연결 테스트 시작")
                Log.d("NANODP_API", "📡 테스트 URL: $BASE_URL")
                
                // GET 요청으로 서버 응답 확인 (백그라운드 스레드에서 실행)
                val testClient = OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)  // 타임아웃 증가
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .build()
                
                // 간단한 GET 요청 (서버 루트 또는 health check 엔드포인트)
                val request = okhttp3.Request.Builder()
                    .url("$BASE_URL/")  // 루트 경로로 테스트
                    .get()
                    .build()
                
                Log.d("NANODP_API", "🌐 연결 시도 중...")
                val response = testClient.newCall(request).execute()
                val responseCode = response.code
                val isConnected = response.isSuccessful || responseCode in 400..499 // 400대 오류도 연결은 됨
                
                Log.d("NANODP_API", "📡 연결 테스트 응답 코드: $responseCode")
                Log.d("NANODP_API", "📝 응답 메시지: ${response.message}")
                Log.d("NANODP_API", "✅ 서버 연결 상태: ${if (isConnected) "정상" else "실패"}")
                
                // 응답 본문 일부 로그 (디버깅용)
                if (response.isSuccessful) {
                    try {
                        val responseBody = response.body?.string()?.take(100) ?: "응답 본문 없음"
                        Log.d("NANODP_API", "📄 응답 본문 (처음 100자): $responseBody")
                    } catch (e: Exception) {
                        Log.d("NANODP_API", "⚠️ 응답 본문 읽기 실패: ${e.message}")
                    }
                }
                
                response.close()
                isConnected
                
            } catch (e: java.net.ConnectException) {
                Log.e("NANODP_API", "❌ 연결 거부: 서버가 연결을 거부했습니다")
                Log.e("NANODP_API", "🔍 확인 사항: 서버가 실행 중인지, 포트가 열려있는지 확인")
                Log.e("NANODP_API", "📝 상세 오류: ${e.javaClass.simpleName} - ${e.message}")
                false
            } catch (e: java.net.SocketTimeoutException) {
                Log.e("NANODP_API", "❌ 연결 타임아웃: 서버 응답 시간 초과")
                Log.e("NANODP_API", "🔍 확인 사항: 네트워크 상태, 서버 응답 속도 확인")
                Log.e("NANODP_API", "📝 상세 오류: ${e.javaClass.simpleName} - ${e.message}")
                false
            } catch (e: java.net.UnknownHostException) {
                Log.e("NANODP_API", "❌ 호스트를 찾을 수 없음: DNS 해석 실패")
                Log.e("NANODP_API", "🔍 확인 사항: 인터넷 연결, DNS 설정 확인")
                Log.e("NANODP_API", "📝 상세 오류: ${e.javaClass.simpleName} - ${e.message}")
                false
            } catch (e: java.net.ProtocolException) {
                Log.e("NANODP_API", "❌ 프로토콜 오류: HTTP 프로토콜 문제")
                Log.e("NANODP_API", "📝 상세 오류: ${e.javaClass.simpleName} - ${e.message}")
                false
            } catch (e: java.security.cert.CertificateException) {
                Log.e("NANODP_API", "❌ 인증서 오류: SSL/TLS 인증서 문제")
                Log.e("NANODP_API", "📝 상세 오류: ${e.javaClass.simpleName} - ${e.message}")
                false
            } catch (e: javax.net.ssl.SSLException) {
                Log.e("NANODP_API", "❌ SSL 오류: SSL/TLS 연결 문제")
                Log.e("NANODP_API", "📝 상세 오류: ${e.javaClass.simpleName} - ${e.message}")
                false
            } catch (e: java.io.IOException) {
                Log.e("NANODP_API", "❌ IO 오류: 네트워크 입출력 문제")
                Log.e("NANODP_API", "🔍 확인 사항: 네트워크 연결, 방화벽 설정 확인")
                Log.e("NANODP_API", "📝 상세 오류: ${e.javaClass.simpleName} - ${e.message}")
                false
            } catch (e: Exception) {
                Log.e("NANODP_API", "❌ 예상치 못한 오류: ${e.javaClass.simpleName}")
                Log.e("NANODP_API", "📝 상세 메시지: ${e.message}")
                Log.e("NANODP_API", "📚 스택 트레이스: ${e.stackTrace.take(3).joinToString(" | ")}")
                false
            }
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
