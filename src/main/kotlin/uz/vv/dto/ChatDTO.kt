package uz.vv.dto

import uz.vv.base.BaseDTO
import uz.vv.enum.ChatStatus
import java.time.Instant
import jakarta.validation.constraints.Positive

data class ChatUpdateDTO(
    var status: ChatStatus? = null,
    @field:Positive(message = "supportId must be positive")
    val supportId: Long? = null
)


data class ChatResponseDTO(
    var clientId: Long,
    var supportId: Long?,
    var status: ChatStatus,
    var languageDTO: LanguageDTO,
    var closedAt: Instant?
): BaseDTO()
