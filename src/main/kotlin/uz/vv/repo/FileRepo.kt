package uz.vv.repo

import org.springframework.stereotype.Repository
import uz.vv.base.BaseRepo
import uz.vv.entity.File

@Repository
interface FileRepo : BaseRepo<File> {
    fun findByKeyName(keyName: String): File?
    fun findByMessageId(messageId: Long): File?
}