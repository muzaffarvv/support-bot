package uz.vv.mapper

import uz.vv.base.BaseMapper
import uz.vv.dto.LanguageDTO
import uz.vv.dto.RoleDTO
import uz.vv.dto.UserCreateDTO
import uz.vv.dto.UserResponseDTO
import uz.vv.entity.User

class UserMapper() : BaseMapper<
        User,
        UserCreateDTO,
        UserResponseDTO
        > {

    override fun toEntity(dto: UserCreateDTO): User {
        return User(
            telegramId = dto.telegramId,
            firstName = dto.firstName,
            lastName = dto.lastName,
            phoneNumber = dto.phoneNumber
        )
    }

    override fun toDTO(entity: User): UserResponseDTO {
        return UserResponseDTO(
            telegramId = entity.telegramId,
            firstName = entity.firstName,
            lastName = entity.lastName,
            phoneNumber = entity.phoneNumber,
            roles = entity.roles.map { RoleDTO(it.code, it.name) }.toMutableSet(),
            languages = entity.languages.map { LanguageDTO(it.code, it.name) }.toMutableSet()
        ).apply { mapBaseFields(entity, this) }
    }
}