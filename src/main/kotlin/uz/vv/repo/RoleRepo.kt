package uz.vv.repo

import org.springframework.stereotype.Repository
import uz.vv.base.BaseRepo
import uz.vv.entity.Role

@Repository
interface RoleRepo : BaseRepo<Role> {
    fun findByCodeAndDeletedFalse(code: String): Role?
}