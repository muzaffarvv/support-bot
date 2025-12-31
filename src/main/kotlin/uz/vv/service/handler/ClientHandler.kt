package uz.vv.service.handler

import org.springframework.stereotype.Component
import uz.vv.service.BotExecutor
import uz.vv.service.ChatService
import uz.vv.service.UserService

@Component
class ClientHandler(
    private val userService: UserService,
    private val chatService: ChatService
) {

    fun handleContactSupport(chatId: Long, telegramUserId: Long, bot: BotExecutor) {
        try {
            val user = userService.findByTelegramId(telegramUserId)
                ?: throw Exception("User not found. Please use /start")

            // Avval active chat bor yoki yo'qligini tekshirish
            val activeChats = chatService.getActiveChatsByUser(user.id!!)

            if (activeChats.isNotEmpty()) {
                bot.sendSimpleMessage(chatId, "ℹ️ Sizda allaqachon faol suhbat mavjud!")
                bot.sendChatMenu(chatId)
                return
            }

            val chat = chatService.create(user.id!!)
            bot.sendSimpleMessage(chatId, "🔄 Support kutilmoqda...")

            // TODO: Matching support topish va ulash logikasi
            bot.sendSimpleMessage(chatId, "⏳ Hozircha support band. Iltimos kuting...")
        } catch (e: Exception) {
            e.printStackTrace()
            bot.sendSimpleMessage(chatId, "Xatolik: ${e.message}")
        }
    }

    fun handleSettings(chatId: Long, telegramUserId: Long, bot: BotExecutor) {
        bot.sendLanguageSelection(chatId)
    }
}
