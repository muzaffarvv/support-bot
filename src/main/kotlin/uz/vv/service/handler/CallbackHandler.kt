package uz.vv.service.handler

import org.springframework.stereotype.Component
import uz.vv.dto.UserUpdateDTO
import uz.vv.service.BotExecutor
import uz.vv.service.ChatService
import uz.vv.service.UserService

@Component
class CallbackHandler(
    private val userService: UserService,
    private val chatService: ChatService
) {

    fun handleLanguageSelection(chatId: Long, telegramUserId: Long, langCode: String, bot: BotExecutor) {
        try {
            val user = userService.findByTelegramId(telegramUserId)
                ?: throw Exception("User not found. Please use /start")

            val language = userService.getAllLangs().find { it.code == langCode }
                ?: throw Exception("Language not found")

            val updateDto = UserUpdateDTO(
                firstName = null,
                lastName = null,
                languageIds = mutableSetOf(language.id!!)
            )

            userService.update(user.id!!, updateDto)

            bot.sendSimpleMessage(chatId, "✅ Til saqlandi!")

            // Rolni tekshirib to'g'ri menyuni ko'rsatish
            val isSupport = user.roles.any { it.code == "SUPPORT" }
            if (isSupport) {
                bot.sendSupportMenu(chatId, "Support menyusi:")
            } else {
                bot.sendClientMenu(chatId, "Asosiy menyu:")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            bot.sendSimpleMessage(chatId, "Xatolik: ${e.message}")
        }
    }

    fun handleAcceptSupport(chatId: Long, telegramUserId: Long, bot: BotExecutor) {
        try {
            val user = userService.findByTelegramId(telegramUserId)
                ?: throw Exception("User not found")

            userService.assignToSupport(user.id!!)
            bot.sendSimpleMessage(chatId, "✅ Siz support bo'ldingiz!")
            bot.removeReplyKeyboard(chatId)
            bot.sendLanguageSelection(chatId)
        } catch (e: IllegalStateException) { // todo IllegalStateException
            if (e.message == "USER_ALREADY_SUPPORT") {
                bot.sendSimpleMessage(chatId, "ℹ️ Siz allaqachon support hisoblanasiz")
                bot.sendSupportMenu(chatId, "Support menyusi:")
            } else {
                bot.sendSimpleMessage(chatId, "Xatolik: ${e.message}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            bot.sendSimpleMessage(chatId, "Xatolik: ${e.message}")
        }
    }

    fun handleRejectSupport(chatId: Long, telegramUserId: Long, bot: BotExecutor) {
        bot.sendSimpleMessage(chatId, "❌ Support bo'lish rad etildi.")
        bot.sendClientMenu(chatId, "Asosiy menyu:")
    }
}

