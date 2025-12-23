package uz.vv.mapper

import org.springframework.stereotype.Component
import uz.vv.base.BaseMapper
import uz.vv.dto.*
import uz.vv.entity.User

@Component
class UserMapper : BaseMapper<
        User,
        UserResponseDTO> {

    fun toEntity(dto: UserCreateDTO): User =
        User(
            telegramId = dto.telegramId,
            firstName = dto.firstName,
            lastName = dto.lastName,
            phoneNumber = dto.phoneNumber
        )

    override fun toDTO(entity: User): UserResponseDTO =
        UserResponseDTO(
            telegramId = entity.telegramId,
            firstName = entity.firstName,
            lastName = entity.lastName,
            phoneNumber = entity.phoneNumber,
            roles = RoleDTO.toDTO(entity.roles),
            languages = LanguageDTO.toDTO(entity.languages)
        ).applyBase(entity)
}
