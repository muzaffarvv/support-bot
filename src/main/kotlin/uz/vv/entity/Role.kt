package uz.vv.entity

import jakarta.persistence.*
import uz.vv.base.BaseEntity

@Entity
@Table(name = "roles")
data class Role(

    @Column(nullable = false, unique =  true, length = 20)
    var code: String,

    @Column(nullable = false, length = 50)
    var name: String,

    @ManyToMany(fetch = FetchType.LAZY)
    var permissions: MutableSet<Permission> = HashSet()

): BaseEntity() {}