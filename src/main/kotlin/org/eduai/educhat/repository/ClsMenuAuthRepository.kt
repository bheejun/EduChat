package org.eduai.educhat.repository

import org.eduai.educhat.entity.ClsMenuAuth
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ClsMenuAuthRepository:JpaRepository<ClsMenuAuth, String> {
}