package uz.vv.mapper

import org.springframework.stereotype.Component
import uz.vv.base.BaseMapper
import uz.vv.dto.MessageCreateDTO
import uz.vv.dto.MessageResponseDTO
import uz.vv.entity.Chat
import uz.vv.entity.Message
import uz.vv.entity.User

@Component
class MessageMapper() : BaseMapper<
        Message,
        MessageResponseDTO
        > {

    fun toEntity(dto: MessageCreateDTO, sender: User, chat: Chat) =
        Message(
            telegramMsgId = dto.messageTgId,
            content = dto.content,
            type = dto.type,
            sender = sender,
            chat = chat
        )

    override fun toDTO(entity: Message): MessageResponseDTO =
        MessageResponseDTO(
            telegramMsgId = entity.telegramMsgId,
            content = entity.content,
            type = entity.type,
            chatId = entity.chat.id!!,
            senderId = entity.sender.id!!
        ).applyBase(entity = entity)

}