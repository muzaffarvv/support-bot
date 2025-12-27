package uz.vv.repo

import uz.vv.base.BaseRepo
import uz.vv.entity.Message

interface MessageRepo : BaseRepo<Message> {

    fun findByChatIdAndDeletedFalse(chatId: Long): List<Message>


}