package com.simats.interviewassist.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf

class PreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("interview_assist_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_FIRST_TIME = "is_first_time"
        private const val KEY_IS_DARK_MODE = "is_dark_mode"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_PROFILE_COMPLETED_PREFIX = "profile_completed_"
        private const val KEY_COMPLETION_PROMPT_SHOWN_PREFIX = "prompt_shown_"
        private const val KEY_FIRST_NAME = "first_name"
        private const val KEY_LAST_NAME = "last_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_TOKEN = "access_token"
        private const val KEY_PROFILE_PIC_PATH = "profile_pic_path"
        private const val KEY_MAJOR = "major"
        private const val KEY_GRAD_YEAR = "grad_year"
        private const val KEY_CURRENT_YEAR = "current_year"
        private const val KEY_BIO = "bio"
        private const val KEY_SECONDARY_EMAIL = "secondary_email"
        private const val KEY_PHONE_NUMBER = "phone_number"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_SAVED_EMAIL = "saved_email"
        private const val KEY_SAVED_PASSWORD = "saved_password"
        private const val KEY_CURRENT_COMPANY = "current_company"
        private const val KEY_DESIGNATION = "designation"
        private const val KEY_STATUS = "status"
    }

    val firstNameState = mutableStateOf(sharedPreferences.getString(KEY_FIRST_NAME, "") ?: "")
    val lastNameState = mutableStateOf(sharedPreferences.getString(KEY_LAST_NAME, "") ?: "")
    val emailState = mutableStateOf(sharedPreferences.getString(KEY_EMAIL, "") ?: "")
    val profilePicPathState = mutableStateOf(sharedPreferences.getString(KEY_PROFILE_PIC_PATH, null))
    val majorState = mutableStateOf(sharedPreferences.getString(KEY_MAJOR, "") ?: "")
    val gradYearState = mutableStateOf(sharedPreferences.getString(KEY_GRAD_YEAR, "") ?: "")
    val currentYearState = mutableStateOf(sharedPreferences.getString(KEY_CURRENT_YEAR, "") ?: "")
    val bioState = mutableStateOf(sharedPreferences.getString(KEY_BIO, "") ?: "")
    val secondaryEmailState = mutableStateOf(sharedPreferences.getString(KEY_SECONDARY_EMAIL, "") ?: "")
    val phoneNumberState = mutableStateOf(sharedPreferences.getString(KEY_PHONE_NUMBER, "") ?: "")
    val currentCompanyState = mutableStateOf(sharedPreferences.getString(KEY_CURRENT_COMPANY, "") ?: "")
    val designationState = mutableStateOf(sharedPreferences.getString(KEY_DESIGNATION, "") ?: "")
    val statusState = mutableStateOf(sharedPreferences.getString(KEY_STATUS, "") ?: "")

    fun isFirstTime(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_FIRST_TIME, true)
    }

    fun setOnboardingCompleted() {
        sharedPreferences.edit().putBoolean(KEY_IS_FIRST_TIME, false).apply()
    }

    fun saveUserId(userId: Int) {
        sharedPreferences.edit().putInt(KEY_USER_ID, userId).apply()
    }

    fun getUserId(): Int {
        return sharedPreferences.getInt(KEY_USER_ID, -1)
    }

    fun saveUserDetails(firstName: String, lastName: String, email: String) {
        sharedPreferences.edit()
            .putString(KEY_FIRST_NAME, firstName)
            .putString(KEY_LAST_NAME, lastName)
            .putString(KEY_EMAIL, email)
            .apply()
        firstNameState.value = firstName
        lastNameState.value = lastName
        emailState.value = email
    }

    fun getUserName(): String {
        val first = firstNameState.value
        val last = lastNameState.value
        return if (first.isBlank() && last.isBlank()) "Anonymous User" else "$first $last".trim()
    }

    fun getEmail(): String {
        return emailState.value
    }

    fun saveProfilePicPath(path: String?) {
        sharedPreferences.edit().putString(KEY_PROFILE_PIC_PATH, path).apply()
        profilePicPathState.value = path
    }

    fun getProfilePicPath(): String? {
        return profilePicPathState.value
    }

    fun saveFullProfile(
        major: String?,
        gradYear: String?,
        currentYear: String?,
        bio: String?,
        secondaryEmail: String?,
        phoneNumber: String?,
        currentCompany: String? = null,
        designation: String? = null
    ) {
        sharedPreferences.edit()
            .putString(KEY_MAJOR, major)
            .putString(KEY_GRAD_YEAR, gradYear)
            .putString(KEY_CURRENT_YEAR, currentYear)
            .putString(KEY_BIO, bio)
            .putString(KEY_SECONDARY_EMAIL, secondaryEmail)
            .putString(KEY_PHONE_NUMBER, phoneNumber)
            .putString(KEY_CURRENT_COMPANY, currentCompany)
            .putString(KEY_DESIGNATION, designation)
            .apply()

        majorState.value = major ?: ""
        gradYearState.value = gradYear ?: ""
        currentYearState.value = currentYear ?: ""
        bioState.value = bio ?: ""
        secondaryEmailState.value = secondaryEmail ?: ""
        phoneNumberState.value = phoneNumber ?: ""
        currentCompanyState.value = currentCompany ?: ""
        designationState.value = designation ?: ""
    }

    fun getMajor(): String = majorState.value
    fun getGradYear(): String = gradYearState.value
    fun getCurrentYear(): String = currentYearState.value
    fun getBio(): String = bioState.value
    fun getSecondaryEmail(): String = secondaryEmailState.value
    fun getPhoneNumber(): String = phoneNumberState.value

    fun saveToken(token: String) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }

    fun clearToken() {
        sharedPreferences.edit().remove(KEY_TOKEN).apply()
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
        firstNameState.value = ""
        lastNameState.value = ""
        emailState.value = ""
        profilePicPathState.value = null
        majorState.value = ""
        gradYearState.value = ""
        currentYearState.value = ""
        bioState.value = ""
        secondaryEmailState.value = ""
        phoneNumberState.value = ""
        statusState.value = ""
    }

    fun logout() {
        sharedPreferences.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_FIRST_NAME)
            .remove(KEY_LAST_NAME)
            .remove(KEY_EMAIL)
            .remove(KEY_TOKEN)
            .remove(KEY_PROFILE_PIC_PATH)
            .remove(KEY_MAJOR)
            .remove(KEY_GRAD_YEAR)
            .remove(KEY_CURRENT_YEAR)
            .remove(KEY_BIO)
            .remove(KEY_SECONDARY_EMAIL)
            .remove(KEY_PHONE_NUMBER)
            .remove(KEY_STATUS)
            .apply()
        
        firstNameState.value = ""
        lastNameState.value = ""
        emailState.value = ""
        profilePicPathState.value = null
        majorState.value = ""
        gradYearState.value = ""
        currentYearState.value = ""
        bioState.value = ""
        secondaryEmailState.value = ""
        phoneNumberState.value = ""
        statusState.value = ""
    }

    fun isProfileCompleted(userId: Int): Boolean {
        return sharedPreferences.getBoolean("$KEY_PROFILE_COMPLETED_PREFIX$userId", false)
    }

    fun setProfileCompleted(userId: Int) {
        sharedPreferences.edit().putBoolean("$KEY_PROFILE_COMPLETED_PREFIX$userId", true).apply()
    }

    fun isCompletionPromptShown(userId: Int): Boolean {
        return sharedPreferences.getBoolean("$KEY_COMPLETION_PROMPT_SHOWN_PREFIX$userId", false)
    }

    fun setCompletionPromptShown(userId: Int, shown: Boolean) {
        sharedPreferences.edit().putBoolean("$KEY_COMPLETION_PROMPT_SHOWN_PREFIX$userId", shown).apply()
    }

    fun isProfileSkipped(userId: Int): Boolean {
        return sharedPreferences.getBoolean("profile_skipped_$userId", false)
    }

    fun setProfileSkipped(userId: Int, skipped: Boolean) {
        sharedPreferences.edit().putBoolean("profile_skipped_$userId", skipped).apply()
    }

    private fun isDarkMode(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_DARK_MODE, false)
    }

    val isDarkModeState = mutableStateOf(isDarkMode())

    fun setDarkMode(isDark: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_DARK_MODE, isDark).apply()
        isDarkModeState.value = isDark
    }

    fun saveRememberMeCredentials(rememberMe: Boolean, email: String? = null, password: String? = null) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe)
        if (rememberMe) {
            editor.putString(KEY_SAVED_EMAIL, email)
            editor.putString(KEY_SAVED_PASSWORD, password)
        } else {
            editor.remove(KEY_SAVED_EMAIL)
            editor.remove(KEY_SAVED_PASSWORD)
        }
        editor.apply()
    }

    fun getSavedCredentials(): Triple<Boolean, String, String> {
        val rememberMe = sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)
        val email = sharedPreferences.getString(KEY_SAVED_EMAIL, "") ?: ""
        val password = sharedPreferences.getString(KEY_SAVED_PASSWORD, "") ?: ""
        return Triple(rememberMe, email, password)
    }

    fun saveStatus(status: String?) {
        sharedPreferences.edit().putString(KEY_STATUS, status).apply()
        statusState.value = status ?: ""
    }

    fun getStatus(): String = statusState.value
}
