package uz.vv.repo

import org.springframework.stereotype.Repository
import uz.vv.base.BaseRepo
import uz.vv.entity.Language
import uz.vv.exception.LanguageNotFoundException

@Repository
interface LanguageRepo : BaseRepo<Language> {
    fun findByCodeAndDeletedFalse(code: String): Language?
    fun findAllByDeletedFalse(): List<Language>
}
