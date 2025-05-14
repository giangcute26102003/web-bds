package org.example.api

import io.smallrye.jwt.build.Jwt
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped
import org.example.service.AdminService

@Path("/auth")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class AuthResource @Inject constructor(
    val adminService: AdminService
) {
    data class LoginRequest(val username: String, val password: String)
    data class LoginResponse(val token: String)

    @POST
    @Path("/login")
    fun login(req: LoginRequest): Response {
        val admin = adminService.findByUsername(req.username)
            ?: return Response.status(Response.Status.UNAUTHORIZED).build()

        if (!BcryptUtil.matches(req.password, admin.passwordHash))
            return Response.status(Response.Status.UNAUTHORIZED).build()

        val token = Jwt.preferredUserName(admin.username)
            .groups(setOf("ADMIN"))
            .claim("role", "ADMIN")
            .sign()

        return Response.ok(LoginResponse(token)).build()
    }
    @POST
    @Path("/register")
    fun register(req: LoginRequest): Response {
        // Kiểm tra trùng username
        if (adminService.findByUsername(req.username) != null) {
            return Response.status(Response.Status.CONFLICT)
                .entity(mapOf("error" to "Username already exists"))
                .build()
        }

        val hashedPassword = BcryptUtil.bcryptHash(req.password)
        adminService.createAdmin(req.username, hashedPassword)

        return Response.status(Response.Status.CREATED)
            .entity(mapOf("message" to "Admin account created"))
            .build()
    }
}