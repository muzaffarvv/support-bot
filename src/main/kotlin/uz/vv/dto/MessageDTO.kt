package uz.vv.dto

import uz.vv.base.BaseDTO
import uz.vv.enum.MessageType
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class MessageCreateDTO(

    @field:NotNull(message = "messageTgId required")
    @field:Positive(message = "messageTgId must be positive")
    var messageTgId: Long,

    var content: String?,

    @field:NotNull(message = "type required")
    var type: MessageType,

    @field:NotNull(message = "senderId required")
    @field:Positive(message = "senderId must be positive")
    var senderId: Long,

    @field:NotNull(message = "chatId required")
    @field:Positive(message = "chatId must be positive")
    var chatId: Long
)

data class MessageUpdateDTO(
    var content: String?
)


data class MessageResponseDTO(

    val telegramMsgId: Long,
    val content: String?,
    val type: MessageType,
    val chatId: Long,
    val senderId: Long,

) : BaseDTO()

