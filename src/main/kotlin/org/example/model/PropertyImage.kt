package org.example.model

import com.fasterxml.jackson.annotation.JsonIgnore
import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "property_image")
class PropertyImage : PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    lateinit var property: Property

    @Column(name = "image_url", nullable = false)
    lateinit var imageUrl: String

    @Column(name = "is_main")
    var isMain: Boolean = false

    @Column(name = "sort_order")
    var sortOrder: Int = 0

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now()

    companion object : PanacheCompanion<PropertyImage>
}
