package uz.vv.base

import java.time.Instant

open class BaseDTO {

    var id: Long? = null
    var createdAt: Instant? = null
    var updatedAt: Instant? = null

    @Suppress("UNCHECKED_CAST")
    fun <T : BaseDTO> applyBase(entity: BaseEntity): T {
        this.id = entity.id
        this.createdAt = entity.createdAt
        this.updatedAt = entity.updatedAt
        return this as T
    }
}
