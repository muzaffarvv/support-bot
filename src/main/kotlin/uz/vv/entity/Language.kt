package uz.vv.entity

import jakarta.persistence.*
import uz.vv.base.BaseEntity

@Entity
@Table(name = "languages")
data class Language(

    @Column(nullable =  false, unique = true, length = 10)
    var code: String,

    @Column(nullable =  false, length = 30)
    var name: String

): BaseEntity() {}