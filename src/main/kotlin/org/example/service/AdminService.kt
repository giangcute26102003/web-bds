package org.example.service

import org.example.model.Admin
import jakarta.enterprise.context.ApplicationScoped


@ApplicationScoped
class AdminService {
    fun findByUsername(username: String): Admin? =
        Admin.find("username", username).firstResult()
    fun createAdmin(username: String, passwordHash: String): Admin {
        val admin = Admin().apply {
            this.username = username
            this.passwordHash = passwordHash
        }
        admin.persist()
        return admin
    }

}