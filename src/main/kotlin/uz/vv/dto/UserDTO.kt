package uz.vv.dto

import uz.vv.base.BaseDTO
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class UserCreateDTO(

    @field:NotNull(message = "telegramId required")
    @field:Positive(message = "telegramId must be positive")
    var telegramId: Long,

    @field:NotBlank(message = "firstName required")
    @field:Size(max = 72, message = "firstName must not exceed 72 characters")
    var firstName: String,

    @field:Size(max = 60, message = "lastName must not exceed 60 characters")
    var lastName: String?,

    @field:NotBlank(message = "phoneNumber required")
    @field:Pattern(
        regexp = "^\\+?[0-9]{9,15}$",
        message = "phoneNumber format invalid"
    )
    var phoneNumber: String
)

data class UserUpdateDTO(

    @field:Size(min = 1, max = 72, message = "firstName must not exceed 72 characters")
    var firstName: String?,

    @field:Size(min = 1, max = 60, message = "lastName must not exceed 60 characters")
    @field:NotBlank(message = "lastName required")
    var lastName: String?,

    var languageIds: MutableSet<@Positive Long>?
)

data class UserResponseDTO(
    var telegramId: Long,
    var firstName: String,
    var lastName: String?,
    var phoneNumber: String,
    var roles: MutableSet<RoleDTO>,
    var languages: MutableSet<LanguageDTO>
) : BaseDTO()

data class AdminLoginDTO(
    @field:NotNull(message = "Telegram ID must not be null")
    var telegramId: Long,

    @field:NotBlank(message = "First name must not be blank")
    @field:Size( min = 3, max = 10, message = "First name must be at most 50 characters")
    var firstName: String,

    @field:NotBlank(message = "Phone number must not be blank")
    @field:Pattern(
        regexp = "\\+998\\d{9}",
        message = "Phone number must be valid and start with +998"
    )
    var phoneNumber: String
)