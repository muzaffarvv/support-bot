package uz.vv.mapper

import org.springframework.stereotype.Component
import uz.vv.base.BaseMapper
import uz.vv.dto.ChatResponseDTO
import uz.vv.dto.LanguageDTO
import uz.vv.entity.Chat
import uz.vv.entity.Language
import uz.vv.entity.User

@Component
class ChatMapper() : BaseMapper<
        Chat,
        ChatResponseDTO
        > {

    fun toEntity(client: User, support: User, language: Language) =
        Chat(
            client = client,
            support = support,
            language = language
        )


    override fun toDTO(entity: Chat): ChatResponseDTO =
        ChatResponseDTO(
            clientId = entity.client.id!!,
            supportId = entity.support?.id,
            status = entity.status,
            languageDTO = LanguageDTO(entity.id, entity.language.code, entity.language.name),
            closedAt = entity.closedAt
        ).applyBase(entity)
}