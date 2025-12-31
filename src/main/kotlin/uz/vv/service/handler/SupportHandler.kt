package uz.vv.service.handler

import org.springframework.stereotype.Component
import uz.vv.repo.LanguageRepo
import uz.vv.service.BotExecutor
import uz.vv.service.ChatService
import uz.vv.service.UserService
@Component
class SupportHandler(
    private val userService: UserService,
    private val chatService: ChatService,
    private val langRepo: LanguageRepo // Yangi servis
) {
    fun handleContactClients(chatId: Long, telegramUserId: Long, bot: BotExecutor) {
        try {
            // 1. Support userni topish
            val support = userService.findByTelegramId(telegramUserId)
                ?: run {
                    bot.sendSimpleMessage(chatId, "User topilmadi")
                    return
                }

            // 2. Active chat borligini tekshirish
            if (chatService.getActiveChatsByUser(support.id!!).isNotEmpty()) {
                bot.sendSimpleMessage(chatId, "ℹ️ Sizda allaqachon faol chat mavjud")
                bot.sendChatMenu(chatId)
                return
            }

            // 3. Umuman pending chat bormi?
            val pendingChats = chatService.getPendingChats()
            if (pendingChats.isEmpty()) {
                bot.sendSimpleMessage(chatId, "📭 Hozircha mijozlar yo‘q")
                return
            }

            // 4. Eng eski pending chatni olish
            val pendingChat = pendingChats.minByOrNull { it.createdAt!! }
                ?: run {
                    bot.sendSimpleMessage(chatId, "Xatolik: pending chat topilmadi")
                    return
                }

            // 5. Til mosligini tekshirish
            val supportLangs = support.languages.map { it.code }.toSet()
            if (pendingChat.languageDTO.code !in supportLangs) {
                bot.sendSimpleMessage(
                    chatId,
                    "❌ Bu chat tili sizga mos emas: ${pendingChat.languageDTO.name}"
                )
                bot.sendLanguageSelection(chatId)
                return
            }

            // 6. Chatni active qilish
            chatService.assignSupport(pendingChat.id!!, support.id!!)

            bot.sendSimpleMessage(chatId, "✅ Mijoz bilan bog‘landingiz")
            bot.sendChatMenu(chatId)

            // 7. Clientga xabar berish
            val client = userService.getEntityById(pendingChat.clientId)
            bot.sendSimpleMessage(
                client.telegramId,
                "✅ Support topildi. Suhbat boshlandi"
            )
            bot.sendChatMenu(client.telegramId)

        } catch (e: Exception) {
            e.printStackTrace()
            bot.sendSimpleMessage(chatId, "❌ Xatolik: ${e.message}")
        }
    }


    fun handleSettings(chatId: Long, telegramUserId: Long, bot: BotExecutor) {
        bot.sendLanguageSelection(chatId)
    }
}


