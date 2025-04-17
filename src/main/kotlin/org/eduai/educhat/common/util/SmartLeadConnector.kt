package org.eduai.educhat.common.util

import okhttp3.*
import org.jsoup.Jsoup
import java.net.CookieManager
import java.net.CookiePolicy

class SmartLeadConnector {

    private var client: OkHttpClient? = createClient()
    private var userIdNumber: String = ""
    private var sessKey: String = ""

    private fun createClient(): OkHttpClient {
        val cookieManager = CookieManager().apply {
            setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        }
        return OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieManager))
            .build()
    }

    private fun resetLogoutVariables() {
        client = null
        userIdNumber = ""
        sessKey = ""
    }

    fun userLogin(userName: String, userPw: String): Boolean {
        if (client == null) {
            client = createClient()
        }
        val httpClient = client!!

        // 헤더 설정
        val headers = Headers.Builder()
            .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36")
            .add("Referer", "https://smartlead.hallym.ac.kr/login/index.php")
            .build()

        // 로그인에 필요한 폼 데이터 구성
        val formBody = FormBody.Builder()
            .add("username", userName)
            .add("password", userPw)
            .add("logintype", "manual")
            .add("type", "popup_login")
            .build()

        // 로그인 요청
        val loginRequest = Request.Builder()
            .url("https://smartlead.hallym.ac.kr/login/index.php")
            .headers(headers)
            .post(formBody)
            .build()

        try {
            httpClient.newCall(loginRequest).execute().use { response ->
                // 로그인 응답은 별도 처리 없이 진행
            }
        } catch (e: Exception) {
            println("로그인 요청 실패: ${e.message}")
            return false
        }

        // 대시보드 페이지 요청
        val dashboardRequest = Request.Builder()
            .url("https://smartlead.hallym.ac.kr/dashboard.php")
            .headers(headers)
            .get()
            .build()

        val dashboardHtml = try {
            httpClient.newCall(dashboardRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    return false
                }
                response.body?.string() ?: ""
            }
        } catch (e: Exception) {
            println("대시보드 요청 실패: ${e.message}")
            return false
        }

        // HTML 파싱하여 로그인 성공 여부 확인
        val doc = Jsoup.parse(dashboardHtml)
        if (doc.select("body#page-my-courses-dashboard").isEmpty()) {
            return false
        }

        // 무들 사용자 아이디 조회
        val (idSuccess, returnIdNumber) = getUserMoodleId()
        if (!idSuccess) {
            return false
        }

        // sesskey 조회
        val (sessSuccess, returnSessKey) = getSesskey()
        if (!sessSuccess) {
            return false
        }

        userIdNumber = returnIdNumber
        sessKey = returnSessKey
        return true
    }

    fun userLogout(): Pair<Boolean, String> {
        val httpClient = client ?: return Pair(false, "session is null")
        val (sessSuccess, sessKey) = getSesskey()
        if (!sessSuccess) {
            return Pair(false, "get_sesskey error")
        }
        val url = "https://smartlead.hallym.ac.kr/login/logout.php?sesskey=$sessKey"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        return try {
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    resetLogoutVariables()
                    Pair(true, "")
                } else {
                    Pair(false, "요청 실패! 상태 코드: ${response.code}")
                }
            }
        } catch (e: Exception) {
            Pair(false, "error: ${e.message}")
        }
    }

    /**
     * 세션을 종료하고 자원을 해제함 (OkHttpClient는 명시적 close()가 필요 없으므로 client를 null 처리)
     */
    fun closeSession() {
        client = null
    }

    /**
     * 무들 사용자 아이디를 조회 (input[name="id"] 의 value)
     */
    fun getUserMoodleId(): Pair<Boolean, String> {
        val httpClient = client ?: return Pair(false, "session is null")
        if (userIdNumber.isNotEmpty()) {
            return Pair(true, userIdNumber)
        }
        val url = "https://smartlead.hallym.ac.kr/user/user_edit.php"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        return try {
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val html = response.body?.string() ?: ""
                    val doc = Jsoup.parse(html)
                    val inputElement = doc.selectFirst("input[name=id]")
                    val idValue = inputElement?.attr("value") ?: ""
                    if (idValue.isEmpty()) {
                        Pair(false, "id 값이 없음")
                    } else {
                        Pair(true, idValue)
                    }
                } else {
                    Pair(false, "페이지 요청 실패! 상태 코드: ${response.code}")
                }
            }
        } catch (e: Exception) {
            Pair(false, "error: ${e.message}")
        }
    }

    fun getSesskey(): Pair<Boolean, String> {
        val httpClient = client ?: return Pair(false, "session is null")
        if (sessKey.isNotEmpty()) {
            return Pair(true, sessKey)
        }
        val url = "https://smartlead.hallym.ac.kr/user/user_edit.php"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        return try {
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val html = response.body?.string() ?: ""
                    val doc = Jsoup.parse(html)
                    val inputElement = doc.selectFirst("input[name=sesskey]")
                    val keyValue = inputElement?.attr("value") ?: ""
                    if (keyValue.isEmpty()) {
                        Pair(false, "sesskey 값이 없음")
                    } else {
                        Pair(true, keyValue)
                    }
                } else {
                    Pair(false, "페이지 요청 실패! 상태 코드: ${response.code}")
                }
            }
        } catch (e: Exception) {
            Pair(false, "error: ${e.message}")
        }
    }

    fun getSession(): OkHttpClient? = client

    fun getUserProfile(): Pair<Boolean, Any> {
        val httpClient = client ?: return Pair(false, "session is null")
        val userProfile = mutableMapOf<String, String>(
            "student_number" to "",
            "department" to "",
            "name_kor" to "",
            "name_eng" to ""
        )
        val url = "https://smartlead.hallym.ac.kr/user/user_edit.php"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        return try {
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val html = response.body?.string() ?: ""
                    val doc = Jsoup.parse(html)

                    val divUserEdit = doc.selectFirst("div.user_edit")
                        ?: return Pair(false, "div_user_edit not found")

                    // 학번 처리
                    val divFitemIdIdnumber = divUserEdit.selectFirst("div#fitem_id_idnumber")
                    if (divFitemIdIdnumber != null) {

                        val formControlStatic = divFitemIdIdnumber.selectFirst("div.form-control-static")
                            ?: return Pair(false, "div_form_control_static not found")

                        userProfile["student_number"] = formControlStatic.text().trim()
                    }

                    // 학과 처리
                    val divFitemIdDepartment = divUserEdit.selectFirst("div#fitem_id_department")
                    if (divFitemIdDepartment != null) {

                        val formControlStatic = divFitemIdDepartment.selectFirst("div.form-control-static")
                            ?: return Pair(false, "div_form_control_static not found")

                        userProfile["department"] = formControlStatic.text().trim()
                    }

                    // 한국어 이름 처리
                    val divFitemFirstname = divUserEdit.selectFirst("div#fitem_id_firstname")
                        ?: return Pair(false, "div_fitem not found")

                    val tagInputFirst = divFitemFirstname.selectFirst("input")
                        ?: return Pair(false, "tag_input not found")

                    userProfile["name_kor"] = tagInputFirst.attr("value").trim()

                    // 영어 이름 처리
                    val divFitemLastname = divUserEdit.selectFirst("div#fitem_id_lastname")
                        ?: return Pair(false, "div_fitem not found")

                    val tagInputLast = divFitemLastname.selectFirst("input")
                        ?: return Pair(false, "tag_input not found")

                    userProfile["name_eng"] = tagInputLast.attr("value").trim()

                    Pair(true, userProfile)
                } else {
                    Pair(false, "페이지 요청 실패! 상태 코드: ${response.code}")
                }
            }
        } catch (e: Exception) {
            Pair(false, "error: ${e.message}")
        }
    }
}
