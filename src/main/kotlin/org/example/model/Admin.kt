package org.example.model

import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "admin")
class Admin : PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(unique = true, nullable = false)
    lateinit var username: String

    @Column(name = "password_hash", nullable = false)
    lateinit var passwordHash: String

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now()

    companion object : PanacheCompanion<Admin>
}
