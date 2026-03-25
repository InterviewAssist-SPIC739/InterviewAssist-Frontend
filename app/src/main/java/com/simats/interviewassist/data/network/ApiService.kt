package com.simats.interviewassist.data.network

import com.simats.interviewassist.data.models.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.DELETE
import retrofit2.http.Headers
import com.google.gson.annotations.SerializedName

interface ApiService {
    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("complete-profile")
    suspend fun completeProfile(@Body request: ProfileRequest): Response<ProfileResponse>

    @POST("skip-profile")
    suspend fun skipProfile(@Body request: Map<String, Int>): Response<Void>

    @POST("forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ForgotPasswordResponse>

    @POST("verify-otp")
    suspend fun verifyOTP(@Body request: VerifyOTPRequest): Response<VerifyOTPResponse>

    @POST("reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ResetPasswordResponse>

    @retrofit2.http.GET("profile/{user_id}")
    suspend fun getUserProfile(@retrofit2.http.Path("user_id") userId: Int): Response<UserProfileResponse>

    @POST("change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ChangePasswordResponse>

    @POST("toggle-2fa")
    suspend fun toggle2FA(@Body request: Toggle2FARequest): Response<Toggle2FAResponse>

    @POST("verify-login-otp")
    suspend fun verifyLoginOtp(@Body request: VerifyLoginOtpRequest): Response<LoginResponse>

    @retrofit2.http.DELETE("delete-account")
    suspend fun deleteAccount(): Response<com.simats.interviewassist.data.models.DeleteAccountResponse>

    @POST("companies")
    suspend fun addCompany(@Body request: AddCompanyRequest): Response<AddCompanyResponse>

    @retrofit2.http.GET("companies")
    suspend fun getCompanies(): Response<List<CompanyResponse>>

    @retrofit2.http.GET("companies/{company_id}")
    suspend fun getCompanyDetail(@retrofit2.http.Path("company_id") companyId: Int): Response<CompanyResponse>

    @POST("companies/{company_id}/follow")
    suspend fun toggleFollowCompany(@retrofit2.http.Path("company_id") companyId: Int): Response<ToggleFollowResponse>

    @GET("experiences/{experience_id}")
    suspend fun getExperienceDetail(@Path("experience_id") experienceId: Int): Response<InterviewExperienceResponse>

    @POST("companies/{company_id}/questions")
    suspend fun askQuestion(@retrofit2.http.Path("company_id") companyId: Int, @Body request: Map<String, String>): Response<Any>

    @POST("questions/{question_id}/answers")
    suspend fun answerQuestion(@retrofit2.http.Path("question_id") questionId: Int, @Body request: Map<String, String>): Response<Any>

    @GET("alumni/assist-questions")
    suspend fun getAssistQuestions(): Response<List<AssistQuestionResponse>>

    @GET("my-questions")
    suspend fun getMyQuestions(): Response<List<AssistQuestionResponse>>

    @retrofit2.http.PUT("companies/{company_id}")
    suspend fun updateCompany(@retrofit2.http.Path("company_id") companyId: Int, @Body request: AddCompanyRequest): Response<AddCompanyResponse>

    @DELETE("companies/{company_id}")
    suspend fun deleteCompany(@Path("company_id") companyId: Int): Response<Void>

    @GET("admin/pending-experiences")
    suspend fun getPendingExperiences(): Response<List<InterviewExperienceResponse>>

    @PUT("experiences/{experience_id}/review")
    suspend fun reviewExperience(@Path("experience_id") experienceId: Int, @Body request: ReviewExperienceRequest): Response<Void>

    @GET("my-experiences")
    suspend fun getMyExperiences(): Response<List<InterviewExperienceResponse>>

    @GET("admin/users")
    suspend fun getUsers(): Response<List<UserData>>

    @PUT("admin/users/{user_id}/suspend")
    suspend fun suspendUser(@Path("user_id") userId: Int): Response<Map<String, String>>

    @PUT("admin/users/{user_id}/unsuspend")
    suspend fun unsuspendUser(@Path("user_id") userId: Int): Response<Map<String, String>>

    @DELETE("admin/users/{user_id}")
    suspend fun deleteUser(@Path("user_id") userId: Int): Response<Map<String, String>>

    @GET("admin/pending-alumni")
    suspend fun getPendingAlumni(): Response<List<UserData>>

    @PUT("admin/users/{user_id}/approve")
    suspend fun approveUser(@Path("user_id") userId: Int): Response<Map<String, String>>

    @DELETE("admin/users/{user_id}/reject")
    suspend fun rejectUser(@Path("user_id") userId: Int): Response<Map<String, String>>

    @POST("request-alumni-upgrade")
    suspend fun requestAlumniUpgrade(@Body request: ProfileRequest): Response<Map<String, String>>

    @GET("admin/pending-upgrades")
    suspend fun getPendingUpgrades(): Response<List<UserData>>

    @Headers("Cache-Control: no-cache")
    @GET("admin/notifications/")
    suspend fun getAdminNotifications(): Response<List<NotificationResponse>>

    @POST("admin/notifications/mark-read/")
    suspend fun markAdminNotificationsRead(@Body request: MarkReadRequest): Response<Map<String, String>>

    @DELETE("admin/notifications/{notification_id}")
    suspend fun deleteAdminNotification(@Path("notification_id") notificationId: Int): Response<Map<String, String>>

    @Headers("Cache-Control: no-cache")
    @GET("notifications/")
    suspend fun getNotifications(): Response<List<NotificationResponse>>

    @POST("notifications/mark-read/")
    suspend fun markNotificationsRead(@Body request: MarkReadRequest): Response<Map<String, String>>

    @DELETE("notifications/{notification_id}")
    suspend fun deleteNotification(@Path("notification_id") notificationId: Int): Response<Map<String, String>>

    @Headers("Cache-Control: no-cache")
    @GET("admin/dashboard-stats")
    suspend fun getDashboardStats(): Response<DashboardStatsResponse>

    @POST("experiences")
    suspend fun submitExperience(@Body request: ExperienceRequest): Response<Map<String, Any>>

    @DELETE("experiences/{experience_id}")
    suspend fun deleteExperience(@Path("experience_id") experienceId: Int): Response<Map<String, String>>

    @PUT("experiences/{experience_id}")
    suspend fun updateExperience(@Path("experience_id") experienceId: Int, @Body request: ExperienceRequest): Response<Map<String, Any>>

    @POST("experiences/{experience_id}/helpful")
    suspend fun toggleHelpful(@Path("experience_id") experienceId: Int): Response<ToggleHelpfulResponse>

    @GET("saved-experiences")
    suspend fun getSavedExperiences(): Response<List<InterviewExperienceResponse>>

    @POST("experiences/{experience_id}/save")
    suspend fun toggleSave(@Path("experience_id") experienceId: Int): Response<ToggleSaveResponse>

    @POST("experiences/{experience_id}/report")
    suspend fun reportExperience(@Path("experience_id") experienceId: Int, @Body request: Map<String, String>): Response<Map<String, String>>

    @GET("admin/reports")
    suspend fun getReports(): Response<List<ReportedContentResponse>>

    @POST("admin/reports/{report_id}/keep")
    suspend fun keepContent(@Path("report_id") reportId: Int): Response<Map<String, String>>

    @POST("admin/reports/{report_id}/remove")
    suspend fun removeContent(@Path("report_id") reportId: Int): Response<Map<String, String>>
    @DELETE("questions/{question_id}")
    suspend fun deleteQuestion(@Path("question_id") questionId: Int): Response<Map<String, String>>

    @DELETE("answers/{answer_id}")
    suspend fun deleteAnswer(@Path("answer_id") answerId: Int): Response<Map<String, String>>

    @POST("questions/{question_id}/report")
    suspend fun reportQuestion(@Path("question_id") questionId: Int, @Body request: Map<String, String>): Response<Map<String, String>>

    @POST("answers/{answer_id}/report")
    suspend fun reportAnswer(@Path("answer_id") answerId: Int, @Body request: Map<String, String>): Response<Map<String, String>>

    @POST("admin/create-admin")
    suspend fun createAdmin(@Body request: CreateAdminRequest): Response<Map<String, String>>

    @POST("admin/update-admin-password")
    suspend fun updateAdminPassword(@Body request: UpdateAdminPasswordRequest): Response<Map<String, String>>
}

data class AssistQuestionResponse(
    val id: Int,
    @SerializedName("user_id") val userId: Int?,
    @SerializedName("company_id") val companyId: Int,
    @SerializedName("company_name") val companyName: String,
    @SerializedName("question_text") val questionText: String,
    @SerializedName("asked_by") val askedBy: String,
    val date: String,
    val answers: List<QuestionAnswerResponse>
)

data class QuestionAnswerResponse(
    val id: Int,
    @SerializedName("answerer_name") val answererName: String,
    @SerializedName("answerer_role") val answererRole: String,
    @SerializedName("is_verified_alumni") val isVerifiedAlumni: Boolean,
    @SerializedName("answer_text") val answerText: String,
    val date: String,
    @SerializedName("current_company") val currentCompany: String? = null
)

data class ReportedContentResponse(
    val id: Int,
    @SerializedName("content_type") val contentType: String,
    @SerializedName("experience_id") val experienceId: Int,
    @SerializedName("question_id") val questionId: Int?,
    @SerializedName("answer_id") val answerId: Int?,
    @SerializedName("user_id") val userId: Int,
    val reason: String,
    @SerializedName("status") val status: String,
    @SerializedName("reported_by") val reportedBy: String,
    @SerializedName("experience_title") val experienceTitle: String,
    @SerializedName("experience_snippet") val experienceSnippet: String,
    @SerializedName("content_creator") val contentCreator: String,
    @SerializedName("time_ago") val timeAgo: String
)

data class ToggleHelpfulResponse(
    val message: String,
    @SerializedName("is_helpful") val isHelpful: Boolean,
    @SerializedName("helpful_count") val helpfulCount: Int
)
