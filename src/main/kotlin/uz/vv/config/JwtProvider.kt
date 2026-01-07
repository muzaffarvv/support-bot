package uz.vv.config

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.SignatureException
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uz.vv.exception.InvalidTokenException
import uz.vv.exception.TokenExpiredException
import java.security.Key
import java.util.*

@Component
class JwtTokenProvider {

    @Value("\${jwt.secret:MyVerySecureSecretKeyForJWTTokenGenerationAndValidation2024}")
    private lateinit var jwtSecret: String

    @Value("\${jwt.expiration:86400000}") // 24 saat
    private var jwtExpiration: Long = 86400000

    private val key: Key by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    fun generateToken(telegramId: Long, roles: List<String>): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpiration)

        return Jwts.builder()
            .setSubject(telegramId.toString())
            .claim("roles", roles)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }

    fun getTelegramIdFromToken(token: String): Long {
        val claims = getAllClaimsFromToken(token)
        return claims.subject.toLong()
    }

    fun getRolesFromToken(token: String): List<String> {
        val claims = getAllClaimsFromToken(token)
        @Suppress("UNCHECKED_CAST")
        return claims["roles"] as? List<String> ?: emptyList()
    }

    fun validateToken(token: String): Boolean {
        try {
            getAllClaimsFromToken(token)
            return true
        } catch (e: Exception) {
            when (e) {
                is ExpiredJwtException -> throw TokenExpiredException("Token muddati tugagan")
                is MalformedJwtException -> throw InvalidTokenException("Token formati noto'g'ri")
                is SignatureException -> throw InvalidTokenException("Token imzosi noto'g'ri")
                else -> throw InvalidTokenException("Token validatsiyasi muvaffaqiyatsiz")
            }
        }
    }

    private fun getAllClaimsFromToken(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
    }

}