package uz.vv.base

import jakarta.persistence.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@MappedSuperclass
abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    var createdAt: Long = System.currentTimeMillis()

    var updatedAt: Long? = null

    var deleted: Boolean = false

    fun getCreatedAtUTCString(): String = createdAt.toUTCString()

    fun getUpdatedAtUTCString(): String? = updatedAt?.toUTCString()

    private fun Long.toUTCString(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        return Instant.ofEpochMilli(this).atZone(ZoneId.of("UTC")).format(formatter)
    }
}

/**
 * System.currentTimeMillis() millisekundga
 * Instant.now().toEpochMilli() → millisekunddan UTC qaytaradi.
 */