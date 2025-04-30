package org.eduai.educhat.service.member

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
interface CustomUserDetailService: UserDetailsService {

    override fun loadUserByUsername(username: String?): UserDetails {
        TODO("Not yet implemented")
    }

}