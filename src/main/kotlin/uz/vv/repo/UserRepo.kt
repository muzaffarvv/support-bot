package uz.vv.repo

import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uz.vv.base.BaseRepo
import uz.vv.base.BaseRepoImpl
import uz.vv.entity.User

@Repository
interface UserRepo : BaseRepo<User> {
    fun findByTelegramIdAndDeletedFalse(telegramId: Long): User?
    fun findByPhoneNumberAndDeletedFalse(phoneNumber: String): User?

    @Query("SELECT u FROM User u WHERE u.deleted = false")
    override fun findAllNotDeleted(): List<User>

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.code = :roleCode AND u.deleted = false")
    fun findByRoleCode(roleCode: String): List<User>

    @Query("SELECT u FROM User u JOIN u.languages l WHERE l.id = :languageId AND u.deleted = false")
    fun findByLanguageId(languageId: Long): List<User>

    @Query("""
        SELECT u FROM User u 
        JOIN u.roles r 
        JOIN u.languages l 
        WHERE r.code = :roleCode 
        AND l.id = :languageId 
        AND u.deleted = false
    """)
    fun findSupportsByLanguage(roleCode: String, languageId: Long): List<User>
}
