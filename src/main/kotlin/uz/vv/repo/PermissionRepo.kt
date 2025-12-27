package uz.vv.repo

import org.springframework.stereotype.Repository
import uz.vv.base.BaseRepo
import uz.vv.entity.Permission

@Repository
interface PermissionRepo : BaseRepo<Permission> {
    fun findByCodeAndDeletedFalse(code: String): Permission?
}