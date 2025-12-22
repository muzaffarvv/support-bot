package uz.vv.entity

import jakarta.persistence.*
import uz.vv.base.BaseEntity
import uz.vv.enum.MessageType
import uz.vv.enum.SenderType

@Entity
@Table(name = "messages")
data class Message(

    @Column(nullable = false, unique = true)
    var messageTgId: Int,

    @Column(nullable = false, length = 225)
    var content: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var type: MessageType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var sender: SenderType,

    @Column(nullable = false)
    var chatId: Long

) : BaseEntity() {}
