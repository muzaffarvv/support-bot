package uz.vv.repo

import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uz.vv.base.BaseRepo
import uz.vv.entity.Chat
import uz.vv.enum.ChatStatus

@Repository
interface ChatRepo : BaseRepo<Chat> {
    fun findByStatus(status: ChatStatus): List<Chat>
    fun findClientIdsByStatus(status: ChatStatus): List<Long>

    @Query("SELECT c FROM Chat c WHERE c.status = :status AND c.language.id = :languageId AND c.deleted = false ORDER BY c.createdAt ASC")
    fun findPendingChatsByLanguage(status: ChatStatus, languageId: Long): List<Chat>

    @Query("SELECT c FROM Chat c WHERE c.support.id = :supportId AND c.status = :status AND c.deleted = false")
    fun findBySupportIdAndStatus(supportId: Long, status: ChatStatus): List<Chat>

    @Query("SELECT c FROM Chat c WHERE c.client.id = :clientId AND c.deleted = false ORDER BY c.createdAt DESC")
    fun findByClientIdOrderByCreatedAtDesc(clientId: Long): List<Chat>

}