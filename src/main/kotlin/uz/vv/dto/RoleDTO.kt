package uz.vv.dto

import uz.vv.entity.Role

data class RoleDTO(
    var code: String,
    var name: String
) {
    companion object {
        fun toDTO(roles: MutableSet<Role>) =
            roles.map {
                RoleDTO(
                    it.code,
                    it.name
                )
            }.toMutableSet()
    }
}