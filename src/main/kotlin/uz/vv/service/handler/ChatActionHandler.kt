package uz.vv.service.handler

import org.springframework.stereotype.Component
import uz.vv.service.BotExecutor
import uz.vv.service.ChatService
import uz.vv.service.UserService

@Component
class ChatActionHandler(
    private val userService: UserService,
    private val chatService: ChatService
) {

    fun handlePauseChat(chatId: Long, telegramUserId: Long, bot: BotExecutor) {
        try {
            val user = userService.findByTelegramId(telegramUserId)
                ?: throw Exception("User not found")

            val activeChats = chatService.getActiveChatsByUser(user.id!!)

            if (activeChats.isNotEmpty()) {
                val chat = activeChats.first()
                chatService.pauseChat(chat.id!!)

                bot.sendSimpleMessage(chatId, "⏸️ Suhbat to'xtatildi.")

                // Ikkinchi tomonga xabar yuborish
                val otherUserId = if (chat.client.id == user.id) {
                    chat.support?.id
                } else {
                    chat.client.id
                }

                otherUserId?.let {
                    val otherUser = userService.getEntityById(it)
                    bot.sendSimpleMessage(
                        otherUser.telegramId,
                        "⏸️ Suhbat to'xtatildi."
                    )
                }

                // O'ziga chat menyusini RESUME ga o'zgartirish
                val isSupport = user.roles.any { it.code == "SUPPORT" }
                if (isSupport) {
                    bot.sendChatPausedMenu(chatId, "Suhbat to'xtatildi:", true)
                } else {
                    bot.sendChatPausedMenu(chatId, "Suhbat to'xtatildi:", false)
                }
            } else {
                bot.sendSimpleMessage(chatId, "Faol suhbat topilmadi.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            bot.sendSimpleMessage(chatId, "Xatolik: ${e.message}")
        }
    }

    fun handleResumeChat(chatId: Long, telegramUserId: Long, bot: BotExecutor) {
        try {
            val user = userService.findByTelegramId(telegramUserId)
                ?: throw Exception("User not found")

            val pausedChats = chatService.getPausedChatsByUser(user.id!!)

            if (pausedChats.isNotEmpty()) {
                val chat = pausedChats.first()
                chatService.resumeChat(chat.id!!)

                bot.sendSimpleMessage(chatId, "▶️ Suhbat davom ettirildi.")

                // Ikkinchi tomonga xabar yuborish
                val otherUserId = if (chat.client.id == user.id) {
                    chat.support?.id
                } else {
                    chat.client.id
                }

                otherUserId?.let {
                    val otherUser = userService.getEntityById(it)
                    bot.sendSimpleMessage(
                        otherUser.telegramId,
                        "▶️ Suhbat davom ettirildi."
                    )

                    // Asl chat menyusiga qaytarish
                    bot.sendChatMenu(otherUser.telegramId)
                }

                // Asl chat menyusiga qaytarish
                bot.sendChatMenu(chatId)
            } else {
                bot.sendSimpleMessage(chatId, "To'xtatilgan suhbat topilmadi.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            bot.sendSimpleMessage(chatId, "Xatolik: ${e.message}")
        }
    }

    fun handleEndChat(chatId: Long, telegramUserId: Long, bot: BotExecutor) {
        try {
            val user = userService.findByTelegramId(telegramUserId)
                ?: throw Exception("User not found")

            val activeChats = chatService.getActiveChatsByUser(user.id!!)
            val pausedChats = chatService.getPausedChatsByUser(user.id!!)
            val allChats = activeChats + pausedChats

            if (allChats.isNotEmpty()) {
                val chat = allChats.first()
                chatService.closeChat(chat.id!!)

                bot.sendSimpleMessage(chatId, "✅ Suhbat yakunlandi.")

                // Ikkinchi tomonga xabar yuborish
                val otherUserId = if (chat.client.id == user.id) {
                    chat.support?.id
                } else {
                    chat.client.id
                }

                otherUserId?.let {
                    val otherUser = userService.getEntityById(it)
                    bot.sendSimpleMessage(
                        otherUser.telegramId,
                        "✅ Suhbat yakunlandi."
                    )

                    // Rolga qarab menyu yuborish
                    val isSupport = otherUser.roles.any { role -> role.code == "SUPPORT" }
                    if (isSupport) {
                        bot.sendSupportMenu(otherUser.telegramId, "Support menyusi:")
                    } else {
                        bot.sendClientMenu(otherUser.telegramId, "Asosiy menyu:")
                    }
                }

                // Rolni tekshirish
                val isSupport = user.roles.any { it.code == "SUPPORT" }
                if (isSupport) {
                    bot.sendSupportMenu(chatId, "Support menyusi:")
                } else {
                    bot.sendClientMenu(chatId, "Asosiy menyu:")
                }
            } else {
                bot.sendSimpleMessage(chatId, "Suhbat topilmadi.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            bot.sendSimpleMessage(chatId, "Xatolik: ${e.message}")
        }
    }
}