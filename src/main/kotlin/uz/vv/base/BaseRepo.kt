package uz.vv.base

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.NoRepositoryBean

@NoRepositoryBean
interface BaseRepo<T : BaseEntity> : JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
    fun saveAndRefresh(entity: T): T
    fun trash(id: Long): Boolean
    fun findByIdAndDeletedFalse(id: Long): T?
    fun findAllNotDeleted(): List<T>
}