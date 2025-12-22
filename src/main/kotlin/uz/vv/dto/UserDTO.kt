package uz.vv.dto

import uz.vv.base.BaseDTO
import uz.vv.entity.Language
import uz.vv.entity.Role

data class UserCreateDTO(
    var telegramId: Long,
    var firstName: String,
    var lastName: String?,
    var phoneNumber: String
)

data class UserUpdateDTO(
    var firstName: String?,
    var lastName: String?,
    var languages: MutableSet<LanguageDTO>?
)

data class UserResponseDTO(
    var telegramId: Long,
    var firstName: String,
    var lastName: String?,
    var phoneNumber: String,
    var roles: MutableSet<RoleDTO>,
    var languages: MutableSet<LanguageDTO>
) : BaseDTO()

data class RoleDTO(
    var code: String,
    var name: String
) {
    fun toDTO(role: Role) = RoleDTO(
            role.code,
            role.name
    )
}

data class LanguageDTO(
    var code: String,
    var name: String
) {
    fun toDTO(language: Language) = LanguageDTO(
        language.code,
        language.name
    )
}