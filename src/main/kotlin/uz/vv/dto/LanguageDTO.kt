package uz.vv.dto

import uz.vv.entity.Language

data class LanguageDTO(
    var id: Long? = null,
    var code: String,
    var name: String
) {
    companion object {
        fun toDTO(languages: MutableSet<Language>) =
            languages.map {
                LanguageDTO(
                    it.id,
                    it.code,
                    it.name
                )
            }.toMutableSet()
    }
}