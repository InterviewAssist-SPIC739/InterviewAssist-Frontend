package com.simats.interviewassist.data.models

import com.google.gson.annotations.SerializedName

data class RecentActivityResponse(
    val id: Int,
    @SerializedName("user_name") val userName: String,
    val action: String,
    val target: String,
    val time: String
)

data class DashboardStatsResponse(
    @SerializedName("total_users") val totalUsers: Int,
    @SerializedName("pending_reviews") val pendingReviews: Int,
    @SerializedName("reports_count") val reportsCount: Int,
    @SerializedName("new_alumni") val newAlumni: Int,
    @SerializedName("unread_notifications_count") val unreadNotificationsCount: Int,
    @SerializedName("recent_activities") val recentActivities: List<RecentActivityResponse> = emptyList()
)

data class CreateAdminRequest(
    val email: String,
    val password: String
)

data class UpdateAdminPasswordRequest(
    val email: String,
    @SerializedName("new_password") val newPassword: String
)
