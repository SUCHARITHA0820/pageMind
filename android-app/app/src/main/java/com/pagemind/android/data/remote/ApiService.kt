package com.pagemind.android.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// --- Auth DTOs ---
data class SignupRequest(
    val name: String,
    val email: String,
    val password: String,
    val preferredLanguage: String? = "en"
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String?,
    val userId: Long?,
    val name: String?,
    val email: String?,
    val dob: String?,
    val phoneNumber: String?,
    val gender: String?,
    val preferredLanguage: String?,
    val profilePicUrl: String?
)

data class ForgotPasswordRequest(
    val email: String
)

data class VerifyCodeRequest(
    val email: String,
    val code: String
)

data class ResetPasswordRequest(
    val email: String,
    val code: String,
    val newPassword: String
)

data class ApiResponse(
    val success: Boolean,
    val message: String?,
    val devFallbackCode: String? = null
)

// --- User Profile DTO ---
data class UserProfileDto(
    val id: Long?,
    val name: String?,
    val email: String?,
    val dob: String?,
    val phoneNumber: String?,
    val gender: String?,
    val preferredLanguage: String?,
    val profilePicUrl: String?
)

// --- Book DTOs ---
data class BookDto(
    val id: Long,
    val title: String,
    val author: String,
    val genre: String?,
    val description: String?,
    @SerializedName("coverUrl", alternate = ["cover_url"]) val coverUrl: String?,
    @SerializedName("rating", alternate = ["averageRating"]) val averageRating: Double?,
    @SerializedName("publishedYear", alternate = ["publicationYear", "published_year"]) val publicationYear: Int?
)

data class PageResponse<T>(
    val content: List<T>,
    @SerializedName("currentPage", alternate = ["pageNumber"]) val pageNumber: Int? = 0,
    val pageSize: Int? = 20,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean? = false
)

// --- Chatbot DTOs ---
data class ChatRequest(
    val message: String,
    val userId: Long? = null,
    val sessionId: String? = null,
    @SerializedName("session_id") val session_id: String? = null
)

data class ChatResponse(
    val success: Boolean,
    val message: String?,
    val detectedGenre: String?,
    @SerializedName("recommendedBooks", alternate = ["books"]) val recommendedBooks: List<BookDto>? = null,
    val historyId: Long?,
    val sessionId: String?
)

data class ChatMessageDto(
    val id: String?,
    val role: String,
    val content: String,
    val timestamp: String?
)

// --- Retrofit API Service Interface ---
interface ApiService {

    // Auth endpoints
    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ApiResponse>

    @POST("auth/verify-code")
    suspend fun verifyCode(@Body request: VerifyCodeRequest): Response<ApiResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiResponse>

    // User Profile endpoints
    @GET("user/profile")
    suspend fun getProfile(): Response<UserProfileDto>

    @PUT("user/profile")
    suspend fun updateProfile(@Body request: UserProfileDto): Response<UserProfileDto>

    // Books endpoints
    @GET("books")
    suspend fun getBooks(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
        @Query("genre") genre: String? = null,
        @Query("search") search: String? = null
    ): Response<PageResponse<BookDto>>

    @GET("books/{id}")
    suspend fun getBookById(@Path("id") id: Long): Response<BookDto>

    // User Likes endpoints
    @POST("user/likes/{bookId}")
    suspend fun likeBook(@Path("bookId") bookId: Long): Response<ApiResponse>

    @DELETE("user/likes/{bookId}")
    suspend fun unlikeBook(@Path("bookId") bookId: Long): Response<ApiResponse>

    @GET("user/likes")
    suspend fun getUserLikes(): Response<List<BookDto>>

    // Chatbot endpoint
    @POST("chat")
    suspend fun chat(@Body request: ChatRequest): Response<ChatResponse>
}
