package uz.vv.entity

import jakarta.persistence.*
import uz.vv.base.BaseEntity

@Entity
@Table(name = "permissions")
data class Permission(

    @Column(nullable = false, unique = true, length = 75)
    var code: String,

    @Column(nullable = false, length = 75)
    var name: String

): BaseEntity() {}