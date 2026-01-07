package uz.vv.dto

import uz.vv.base.BaseDTO
import jakarta.validation.constraints.*

// Login response DTO
data class LoginResponseDTO(
    val token: String,
    val user: UserResponseDTO
)

// User DTOs - Updated
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
    var lastName: String?,

    // Support uchun ko'p til, Client uchun bitta til
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
    @field:Size(min = 3, max = 50, message = "First name must be between 3 and 50 characters")
    var firstName: String,

    @field:NotBlank(message = "Phone number must not be blank")
    @field:Pattern(
        regexp = "\\+998\\d{9}",
        message = "Phone number must be valid and start with +998"
    )
    var phoneNumber: String
)