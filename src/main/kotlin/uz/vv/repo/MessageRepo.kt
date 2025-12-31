package uz.vv.repo

import org.springframework.data.jpa.repository.Query
import uz.vv.base.BaseRepo
import uz.vv.entity.Message

interface MessageRepo : BaseRepo<Message> {

    fun findByChatIdAndDeletedFalse(chatId: Long): List<Message>
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId AND m.deleted = false ORDER BY m.createdAt ASC")
    fun findByChatIdOrderByCreatedAtAsc(chatId: Long): List<Message>

    @Query("SELECT m FROM Message m WHERE m.sender.id = :senderId AND m.deleted = false")
    fun findBySenderId(senderId: Long): List<Message>

    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat.id = :chatId AND m.deleted = false")
    fun countByChatId(chatId: Long): Long

}