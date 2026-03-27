package com.simats.interviewassist.data.models

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val email: String,
    val password: String,
    val role: String = "Student"
)

data class LoginRequest(
    val email: String,
    val password: String,
    val role: String? = null
)

data class LoginResponse(
    val message: String? = null,
    val error: String? = null,
    val user: UserData? = null,
    @SerializedName("requires_otp") val requiresOtp: Boolean? = null,
    @SerializedName("access_token") val accessToken: String? = null
)

data class RegisterResponse(
    val message: String? = null,
    val error: String? = null,
    val user: UserData? = null,
    @SerializedName("access_token") val accessToken: String? = null
)

data class UserData(
    val id: Int,
    val email: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val role: String,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("secondary_email") val secondaryEmail: String? = null,
    @SerializedName("two_factor_enabled") val twoFactorEnabled: Boolean? = null,
    @SerializedName("is_suspended") val isSuspended: Boolean? = null,
    val status: String? = null,
    @SerializedName("profile_pic") val profilePic: String? = null,
    @SerializedName("has_completed_profile") val hasCompletedProfile: Boolean? = null,
    @SerializedName("profile_skipped") val profileSkipped: Boolean? = null,
    val description: String? = null,
    val profile: ProfileData? = null,
    @SerializedName("experiences_count") val experiencesCount: Int? = 0,
    @SerializedName("total_helpful_votes") val totalHelpfulVotes: Int? = 0,
    @SerializedName("assisted_count") val assistedCount: Int? = 0,
    @SerializedName("saved_count") val savedCount: Int? = 0,
    @SerializedName("questions_count") val questionsCount: Int? = 0
)

data class ProfileRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    val email: String? = null,
    @SerializedName("phone_number") val phoneNumber: String,
    val major: String? = null,
    @SerializedName("expected_grad_year") val expectedGradYear: String,
    @SerializedName("current_year") val currentYear: String,
    val bio: String,
    @SerializedName("profile_pic") val profilePic: String? = null,
    @SerializedName("linkedin_url") val linkedinUrl: String? = null,
    @SerializedName("current_company") val currentCompany: String? = null,
    @SerializedName("designation") val designation: String? = null,
    val specialization: String? = null
)

data class ProfileResponse(
    val message: String? = null,
    val error: String? = null
)

// Forgot Password Models
data class ForgotPasswordRequest(
    val email: String,
    val role: String
)

data class ForgotPasswordResponse(
    val message: String? = null,
    val error: String? = null,
    val otp: String? = null // For Dev purposes
)

data class VerifyOTPRequest(
    val email: String,
    val role: String,
    val otp: String
)

data class VerifyOTPResponse(
    val message: String? = null,
    val error: String? = null
)

data class ResetPasswordRequest(
    val email: String,
    val role: String,
    val otp: String,
    @SerializedName("new_password") val newPassword: String
)

data class ResetPasswordResponse(
    val message: String? = null,
    val error: String? = null
)

data class UserProfileResponse(
    val id: Int,
    val email: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val role: String,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("secondary_email") val secondaryEmail: String? = null,
    @SerializedName("two_factor_enabled") val twoFactorEnabled: Boolean? = null,
    @SerializedName("is_suspended") val isSuspended: Boolean? = null,
    val status: String? = null,
    @SerializedName("profile_pic") val profilePic: String? = null,
    val profile: ProfileData? = null,
    @SerializedName("experiences_count") val experiencesCount: Int? = 0,
    @SerializedName("total_helpful_votes") val totalHelpfulVotes: Int? = 0,
    @SerializedName("assisted_count") val assistedCount: Int? = 0,
    @SerializedName("saved_count") val savedCount: Int? = 0,
    @SerializedName("questions_count") val questionsCount: Int? = 0
)

data class ProfileData(
    @SerializedName("phone_number") val phoneNumber: String? = null,
    val major: String? = null,
    @SerializedName("expected_grad_year") val expectedGradYear: String? = null,
    @SerializedName("current_year") val currentYear: String? = null,
    val bio: String? = null,
    @SerializedName("profile_pic") val profilePic: String? = null,
    @SerializedName("linkedin_url") val linkedinUrl: String? = null,
    @SerializedName("current_company") val currentCompany: String? = null,
    @SerializedName("designation") val designation: String? = null,
    val specialization: String? = null
)

data class ChangePasswordRequest(
    @SerializedName("old_password") val oldPassword: String,
    @SerializedName("new_password") val newPassword: String
)

data class ChangePasswordResponse(
    val message: String? = null,
    val error: String? = null
)

// 2FA Models
data class Toggle2FARequest(
    val role: String,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("secondary_email") val secondaryEmail: String?,
    val enable: Boolean
)

data class Toggle2FAResponse(
    val message: String? = null,
    val error: String? = null
)

data class VerifyLoginOtpRequest(
    val email: String,
    val role: String,
    val otp: String
)

data class DeleteAccountResponse(
    val message: String? = null,
    val error: String? = null
)
