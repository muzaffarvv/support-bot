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
}
