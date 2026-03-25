package com.simats.interviewassist.data.models

import com.google.gson.annotations.SerializedName

data class NotificationResponse(
    val id: Int,
    val title: String,
    val description: String,
    val type: String, // Registration, Upgrade, Experience
    @SerializedName("target_id") val targetId: Int? = null,
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("created_at") val createdAt: String,
    val date: String
)

data class MarkReadRequest(
    val ids: List<Int>
)
