package com.simats.interviewassist.data.models

import com.google.gson.annotations.SerializedName
import com.simats.interviewassist.ui.screens.student.ProcessStep
import com.simats.interviewassist.ui.screens.student.ExamSection

data class CompanyResponse(
    val id: Int,
    val name: String,
    val location: String?,
    val sector: String?,
    val logo: String?,
    @SerializedName("logo_url") val logoUrl: String?,
    val difficulty: String,
    val description: String?,
    @SerializedName("website_url") val websiteUrl: String?,
    @SerializedName("exam_pattern") val examPattern: List<ExamSection>?,
    @SerializedName("hiring_process") val hiringProcess: List<ProcessStep>?,
    @SerializedName("experiences_count") val experiencesCount: Int,
    @SerializedName("selected_count") val selectedCount: Int,
    @SerializedName("is_following") val isFollowing: Boolean = false,
    val experiences: List<InterviewExperienceResponse>?,
    val questions: List<QuestionResponse>?
)


data class InterviewExperienceResponse(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("user_name") val userName: String,
    @SerializedName("user_role") val userRole: String,
    @SerializedName("is_user_verified") val isUserVerified: Boolean,
    val difficulty: String,
    val date: String,
    @SerializedName("is_selected") val isSelected: Boolean,
    @SerializedName("work_mode") val workMode: String?,
    @SerializedName("candidate_type") val candidateType: String?,
    @SerializedName("my_experience") val myExperience: String?,
    val brief: String?,
    @SerializedName("application_process") val applicationProcess: String?,
    @SerializedName("interview_rounds") val interviewRounds: List<ProcessStep>?,
    @SerializedName("technical_questions") val technicalQuestions: List<String>?,
    @SerializedName("behavioral_questions") val behavioralQuestions: List<String>?,
    val mistakes: List<String>?,
    @SerializedName("preparation_strategy") val preparationStrategy: Map<String, List<String>>?,
    @SerializedName("final_advice") val finalAdvice: List<String>?,
    val status: String,
    @SerializedName("helpful_count") val helpfulCount: Int,
    @SerializedName("is_helpful") val isHelpful: Boolean,
    @SerializedName("is_saved") val isSaved: Boolean,
    @SerializedName("company_name") val companyName: String?,
    @SerializedName("user_profile_company") val userProfileCompany: String?,
    @SerializedName("user_profile_role") val userProfileRole: String?,
    @SerializedName("user_profile_pic") val userProfilePic: String?,
    @SerializedName("is_user_suspended") val isUserSuspended: Boolean = false
)

data class QuestionResponse(
    val id: Int,
    @SerializedName("user_id") val userId: Int?,
    @SerializedName("company_id") val companyId: Int,
    @SerializedName("company_name") val companyName: String?,
    @SerializedName("question_text") val questionText: String,
    @SerializedName("asked_by") val askedBy: String,
    val date: String,
    val answers: List<AnswerResponse>
)

data class AnswerResponse(
    val id: Int,
    @SerializedName("answerer_name") val answererName: String,
    @SerializedName("answerer_role") val answererRole: String,
    @SerializedName("is_verified_alumni") val isVerifiedAlumni: Boolean,
    @SerializedName("answer_text") val answerText: String,
    @SerializedName("profile_pic") val profilePic: String? = null,
    val date: String,
    @SerializedName("current_company") val currentCompany: String? = null
)

data class ExperienceRequest(
    @SerializedName("company_id") val companyId: Int,
    @SerializedName("user_role") val userRole: String,
    val difficulty: String,
    @SerializedName("is_selected") val isSelected: Boolean,
    @SerializedName("work_mode") val workMode: String?,
    @SerializedName("candidate_type") val candidateType: String?,
    @SerializedName("my_experience") val myExperience: String,
    val brief: String,
    @SerializedName("application_process") val applicationProcess: String,
    @SerializedName("interview_rounds") val interviewRounds: List<ProcessStep>?,
    @SerializedName("technical_questions") val technicalQuestions: List<String>?,
    @SerializedName("behavioral_questions") val behavioralQuestions: List<String>?,
    val mistakes: List<String>?,
    @SerializedName("preparation_strategy") val preparationStrategy: Map<String, List<String>>?,
    @SerializedName("final_advice") val finalAdvice: List<String>?
)

data class ReviewExperienceRequest(
    val status: String
)

data class AddCompanyRequest(
    val name: String,
    val location: String?,
    val sector: String?,
    val logo: String?,
    val difficulty: String = "Medium",
    val description: String?,
    @SerializedName("website_url") val websiteUrl: String?,
    @SerializedName("exam_pattern") val examPattern: List<ExamSection>?,
    @SerializedName("hiring_process") val hiringProcess: List<ProcessStep>?
)

data class AddCompanyResponse(
    val message: String?,
    val error: String?,
    val company: CompanyResponse?
)

data class ToggleSaveResponse(
    val message: String,
    @SerializedName("is_saved") val isSaved: Boolean
)

data class ToggleFollowResponse(
    val message: String,
    @SerializedName("is_following") val isFollowing: Boolean
)