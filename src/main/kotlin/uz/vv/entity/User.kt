package uz.vv.entity

import jakarta.persistence.*
import uz.vv.base.BaseEntity

@Entity
@Table(name = "users")
data class User(

    @Column(nullable = false, unique = true)
    var telegramId: Long,

    @Column(length = 72, nullable = false)
    var firstName: String,

    @Column(length = 60)
    var lastName: String? = null,

    @Column(nullable = false, unique = true)
    var phoneNumber: String,

    @ManyToMany(fetch = FetchType.EAGER)
    var roles: MutableSet<Role> = HashSet(),

    @ManyToMany(fetch = FetchType.EAGER)
    var languages: MutableSet<Language> = HashSet()

) : BaseEntity()
