package uz.vv.config

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uz.vv.entity.*
import uz.vv.repo.*

@Configuration
class DataLoader {

    @Bean
    fun loadInitialData(
        languageRepo: LanguageRepo,
        roleRepo: RoleRepo,
        permissionRepo: PermissionRepo,
        userRepo: UserRepo
    ): CommandLineRunner = CommandLineRunner {

        listOf(
            "uz" to "O'zbek",
            "ru" to "Русский",
            "en" to "English"
        ).forEach { (code, name) ->
            if (languageRepo.findByCodeAndDeletedFalse(code) == null) {
                languageRepo.saveAndRefresh(Language(code, name))
            }
        }

        listOf(
            "USER_READ" to "User read",
            "SHOW_STATS" to "Show stats",
            "PROMOTE_SUPPORT" to "Promote support",
            "USER_DELETE" to "User delete",
            "CHAT_MANAGE" to "Chat manage",
            "LANGUAGE_MANAGE" to "Language manage"
        ).forEach { (code, name) ->
            if (permissionRepo.findByCodeAndDeletedFalse(code) == null) {
                permissionRepo.saveAndRefresh(Permission(code, name))
            }
        }

        val userRead = permissionRepo.findByCodeAndDeletedFalse("USER_READ")!!
        val showStats = permissionRepo.findByCodeAndDeletedFalse("SHOW_STATS")!!
        val promoteSupport = permissionRepo.findByCodeAndDeletedFalse("PROMOTE_SUPPORT")!!
        val userDelete = permissionRepo.findByCodeAndDeletedFalse("USER_DELETE")!!
        val chatManage = permissionRepo.findByCodeAndDeletedFalse("CHAT_MANAGE")!!
        val languageManage = permissionRepo.findByCodeAndDeletedFalse("LANGUAGE_MANAGE")!!

        fun role(code: String, name: String, permissions: MutableSet<Permission>) {
            if (roleRepo.findByCodeAndDeletedFalse(code) == null) {
                roleRepo.saveAndRefresh(Role(code, name, permissions))
            }
        }

        role(
            "ADMIN",
            "Administrator",
            mutableSetOf(
                userRead,
                showStats,
                promoteSupport,
                userDelete,
                chatManage,
                languageManage
            )
        )

        role(
            "SUPPORT",
            "Support",
            mutableSetOf(
                chatManage,
                languageManage
            )
        )

        role(
            "CLIENT",
            "Client",
            mutableSetOf(
                chatManage,
                languageManage
            )
        )

        // ==================== ADMIN USER ====================
        if (userRepo.findByTelegramIdAndDeletedFalse(0L) == null) {
            userRepo.saveAndRefresh(
                User(
                    telegramId = 0L,
                    firstName = "Admin",
                    lastName = "System",
                    phoneNumber = "+998900000000",
                    roles = mutableSetOf(roleRepo.findByCodeAndDeletedFalse("ADMIN")!!),
                    languages = mutableSetOf(languageRepo.findByCodeAndDeletedFalse("uz")!!)
                )
            )
        }
    }
}

