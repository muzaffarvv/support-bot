package uz.vv.entity

import jakarta.persistence.*
import uz.vv.base.BaseEntity
import uz.vv.enum.ChatStatus
import java.time.Instant

@Entity
@Table(name = "chats")
data class Chat(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    var client: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "support_id")
    var support: User? = null, // nullable — hali tayinlanmagan bo'lishi mumkin)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ChatStatus = ChatStatus.PENDING,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id", nullable = false)
    var language: Language,

    @Column
    var closedAt: Instant? = null

) : BaseEntity()