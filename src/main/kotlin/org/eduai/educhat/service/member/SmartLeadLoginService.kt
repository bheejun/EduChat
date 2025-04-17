package org.eduai.educhat.service.member

import okhttp3.OkHttpClient
import org.eduai.educhat.common.util.SmartLeadConnector
import org.springframework.stereotype.Service

@Service
class SmartLeadLoginService {
    private var loginObject: SmartLeadConnector? = null

    fun login(userId: String, userPw: String): Boolean {
        return try {
            if (loginObject == null) {
                loginObject = SmartLeadConnector()
            }
            loginObject!!.userLogin(userId, userPw)
        } catch (e: Exception) {
            println("Login error: ${e.message}")
            false
        }
    }

    fun logout(): Pair<Boolean, String> {
        if (loginObject == null) {
            return Pair(false, "login_object none")
        }
        return loginObject!!.userLogout()
    }

    fun getUserProfile(): Pair<Boolean, Map<String, String>> {
        if (loginObject == null) {
            return Pair(false, mapOf("error_msg" to "login_object none"))
        }
        val (result, dataOrError) = loginObject!!.getUserProfile()
        return if (result && dataOrError is Map<*, *>) {
            @Suppress("UNCHECKED_CAST")
            Pair(true, dataOrError as Map<String, String>)
        } else {
            Pair(false, mapOf("error_msg" to dataOrError.toString()))
        }
    }

    fun getSession(): OkHttpClient? {
        return loginObject?.getSession()
    }

    fun getSesskey(): Pair<Boolean, String> {
        if (loginObject == null) {
            return Pair(false, "login_object none")
        }
        return loginObject!!.getSesskey()
    }

    fun getUserMoodleId(): Pair<Boolean, String> {
        if (loginObject == null) {
            return Pair(false, "login_object none")
        }
        return loginObject!!.getUserMoodleId()
    }
}
