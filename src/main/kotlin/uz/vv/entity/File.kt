package uz.vv.entity

import jakarta.persistence.*
import uz.vv.base.BaseEntity
import uz.vv.enum.MessageType

@Entity
@Table(name = "files")
class File(

    @Column(name = "org_name", nullable = false)
    var orgName: String = "",

    @Column(name = "key_name", nullable = false, length = 50)
    var keyName: String = "",

    @Column(name = "size", nullable = false)
    var size: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    var type: MessageType = MessageType.DOCUMENT,  // Photo / Video / Document / Sticker ...

    @ManyToOne(fetch = FetchType.LAZY)
    var message: Message

) : BaseEntity()
