package org.example.model

import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "property")
class Property : PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // IDENTITY dành cho MySQL
    var id: Long? = null

    @Column(nullable = false)
    lateinit var title: String

    @Column(columnDefinition = "TEXT")
    var description: String? = null

    @Column(nullable = false)
    var price: Double = 0.0

    @Column
    var location: String? = null

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now()

    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now()

    @OneToMany(mappedBy = "property", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var images: MutableList<PropertyImage> = mutableListOf()

    companion object : PanacheCompanion<Property>
}
