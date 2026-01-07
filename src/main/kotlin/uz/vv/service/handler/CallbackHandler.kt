package uz.vv.service.handler

import org.springframework.stereotype.Component
import uz.vv.dto.UserUpdateDTO
import uz.vv.exception.LanguageNotFoundException
import uz.vv.exception.UserNotFoundException
import uz.vv.service.BotExecutor
import uz.vv.service.ChatService
import uz.vv.service.UserService

@Component
class CallbackHandler(
    private val userService: UserService,
    private val chatService: ChatService
) {
    private val userLanguageSelections = mutableMapOf<Long, MutableSet<Long>>()

    fun handleLanguageSelection(chatId: Long, telegramUserId: Long, langCode: String, messageId: Int, bot: BotExecutor) {
        try {
            val user = userService.findByTelegramId(telegramUserId)
                ?: throw UserNotFoundException("Foydalanuvchi topilmadi")

            val language = userService.getAllLangs().find { it.code == langCode }
                ?: throw LanguageNotFoundException("Til topilmadi")

            val isSupport = user.roles.any { it.code == "SUPPORT" }

            if (isSupport) {
                val selectedLanguages = userLanguageSelections.getOrPut(telegramUserId) { mutableSetOf() }

                // Tilni qo'shish yoki olib tashlash
                if (selectedLanguages.contains(language.id!!)) {
                    selectedLanguages.remove(language.id!!)
                } else {
                    selectedLanguages.add(language.id!!)
                }

                // YANGI XABAR YUBORMASDAN, ESKISINI TAHRIRLAYMIZ
                bot.editMultiLanguageSelection(
                    chatId = chatId,
                    messageId = messageId,
                    selectedLanguages = selectedLanguages,
                    withSave = selectedLanguages.isNotEmpty()
                )

            } else {
                // Client uchun oddiy mantiq (tahrirlash shart emas, chunki menyu o'zgaradi)
                val updateDto = UserUpdateDTO(
                    firstName = null,
                    lastName = null,
                    languageIds = mutableSetOf(language.id!!)
                )
                userService.update(user.id!!, updateDto)
                bot.sendSimpleMessage(chatId, "✅ ${language.name} tili saqlandi!")
                bot.sendClientMenu(chatId, "Asosiy menyu:")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun handleSaveLanguages(chatId: Long, telegramUserId: Long, bot: BotExecutor) {
        try {
            val user = userService.findByTelegramId(telegramUserId)
                ?: throw UserNotFoundException("Foydalanuvchi topilmadi")

            val selectedLanguages = userLanguageSelections[telegramUserId]
                ?: throw IllegalArgumentException("Tillar tanlanmagan")

            if (selectedLanguages.isEmpty()) {
                bot.sendSimpleMessage(chatId, "⚠️ Kamida bitta til tanlang")
                bot.sendMultiLanguageSelection(chatId, selectedLanguages)
                return
            }

            val updateDto = UserUpdateDTO(
                firstName = null,
                lastName = null,
                languageIds = selectedLanguages
            )

            userService.update(user.id!!, updateDto)

            // Clear selection state
            userLanguageSelections.remove(telegramUserId)

            val selectedLangNames = userService.getAllLangs()
                .filter { selectedLanguages.contains(it.id) }
                .joinToString(", ") { it.name }

            bot.sendSimpleMessage(chatId, "✅ Tillar saqlandi: $selectedLangNames")

            val isSupport = user.roles.any { it.code == "SUPPORT" }
            if (isSupport) {
                bot.sendSupportMenu(chatId, "Support menyusi:")
            } else {
                bot.sendClientMenu(chatId, "Asosiy menyu:")
            }
        } catch (e: UserNotFoundException) {
            bot.sendSimpleMessage(chatId, "❌ ${e.message}")
        } catch (e: Exception) {
            e.printStackTrace()
            bot.sendSimpleMessage(chatId, "❌ Xatolik: ${e.message}")
        }
    }

    fun handleAcceptSupport(chatId: Long, telegramUserId: Long, bot: BotExecutor) {
        try {
            val user = userService.findByTelegramId(telegramUserId)
                ?: throw UserNotFoundException("Foydalanuvchi topilmadi")

            userService.assignToSupport(user.id!!)
            bot.sendSimpleMessage(chatId, "✅ Siz support bo'ldingiz!")
            bot.removeReplyKeyboard(chatId)

            // Support uchun ko'p til tanlash
            bot.sendMultiLanguageSelection(chatId, mutableSetOf())
        } catch (e: uz.vv.exception.UserAlreadySupportException) {
            bot.sendSimpleMessage(chatId, "ℹ️ Siz allaqachon support hisoblanasiz")
            bot.sendSupportMenu(chatId, "Support menyusi:")
        } catch (e: UserNotFoundException) {
            bot.sendSimpleMessage(chatId, "❌ ${e.message}")
        } catch (e: Exception) {
            e.printStackTrace()
            bot.sendSimpleMessage(chatId, "❌ Xatolik: ${e.message}")
        }
    }

    fun handleRejectSupport(chatId: Long, telegramUserId: Long, bot: BotExecutor) {
        bot.sendSimpleMessage(chatId, "❌ Support bo'lish rad etildi.")
        bot.sendClientMenu(chatId, "Asosiy menyu:")
    }
}