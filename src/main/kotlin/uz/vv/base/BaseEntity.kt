package uz.vv.base

import jakarta.persistence.*
import java.time.Instant

@MappedSuperclass
abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false, updatable = false)
    lateinit var createdAt: Instant

    @Column
    var updatedAt: Instant? = null

    @Column(nullable = false)
    var deleted: Boolean = false

    @PrePersist
    fun onCreate() {
        createdAt = Instant.now()
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = Instant.now()
    }
}

