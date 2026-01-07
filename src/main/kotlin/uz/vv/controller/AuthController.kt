package uz.vv.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import uz.vv.dto.AdminLoginDTO
import uz.vv.dto.LoginResponseDTO
import uz.vv.exception.UnauthorizedException
import uz.vv.config.JwtTokenProvider
import uz.vv.service.UserService
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService,
    private val jwtTokenProvider: JwtTokenProvider
) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody dto: AdminLoginDTO): ResponseEntity<LoginResponseDTO> {
        val user = userService.findByTelegramId(dto.telegramId)
            ?: throw UnauthorizedException("Foydalanuvchi topilmadi")

        // Check if the user is ADMIN
        val isAdmin = user.roles.any { it.code == "ADMIN" }
        if (!isAdmin) {
            throw UnauthorizedException("Faqat adminlar tizimga kirishlari mumkin")
        }

        // Generate JWT token
        val roles = user.roles.map { it.code }
        val token = jwtTokenProvider.generateToken(user.telegramId, roles)

        val response = LoginResponseDTO(
            token = token,
            user = user
        )

        return ResponseEntity.ok(response)
    }

    @GetMapping("/validate")
    fun validateToken(@RequestHeader("Authorization") authorization: String): ResponseEntity<Map<String, Any>> {
        val token = authorization.removePrefix("Bearer ")

        val isValid = jwtTokenProvider.validateToken(token)
        val telegramId = jwtTokenProvider.getTelegramIdFromToken(token)
        val roles = jwtTokenProvider.getRolesFromToken(token)

        return ResponseEntity.ok(
            mapOf(
                "valid" to isValid,
                "telegramId" to telegramId,
                "roles" to roles
            )
        )
    }
}