package org.example.security

import io.quarkus.security.identity.SecurityIdentity
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject

@RequestScoped
class JwtSecurityConfig @Inject constructor(
    val identity: SecurityIdentity
) {
    fun isAdmin(): Boolean = identity.roles.contains("ADMIN")
}