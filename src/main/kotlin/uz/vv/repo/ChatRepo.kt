package uz.vv.repo

import org.springframework.stereotype.Repository
import uz.vv.base.BaseRepo
import uz.vv.entity.Chat
import uz.vv.enum.ChatStatus

@Repository
interface ChatRepo : BaseRepo<Chat> {
    fun findByStatus(status: ChatStatus): List<Chat>
    fun findClientIdsByStatus(status: ChatStatus): List<Long>
}