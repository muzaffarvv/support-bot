package uz.vv.entity

import jakarta.persistence.*
import uz.vv.base.BaseEntity
import uz.vv.enum.MessageType

@Entity
@Table(
    name = "messages",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["chat_id", "telegram_msg_id"])
    ]
)
data class Message(

    @Column(nullable = false)
    var telegramMsgId: Long,

    @Column(columnDefinition = "TEXT")
    var content: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var type: MessageType,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    var sender: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    var chat: Chat

) : BaseEntity()
