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
            val user = userService.findByTelegramId(telegramUserId)
                ?: throw Exception("User not found")

            // Avval active chat bor yoki yo'qligini tekshirish
            val existingActiveChats = chatService.getActiveChatsByUser(user.id!!)
            if (existingActiveChats.isNotEmpty()) {
                bot.sendSimpleMessage(chatId, "ℹ️ Sizda allaqachon faol suhbat mavjud!")
                bot.sendChatMenu(chatId)
                return
            }

            // Support'ning tillarini olish
            val supportLanguages = user.languages.map { it.code }.toSet()

            // User'ning tillari bo'yicha pending chatlarni filtrlash
            val pendingChats = chatService.getPendingChats()
                .filter { chat ->
                    // Chat tili support'ning tillaridan biriga mos kelishi kerak
                    supportLanguages.contains(chat.languageDTO.code)
                }

            if (pendingChats.isEmpty()) {
                bot.sendSimpleMessage(chatId, "Sizning tillaringizga mos mijozlar hozircha yo'q.")
                // Qaysi tillarda mijozlar borligini ko'rsatish
                val availableChats = chatService.getPendingChats()
                if (availableChats.isNotEmpty()) {
                    val availableLangs = availableChats
                        .map { it.languageDTO.name }
                        .distinct()
                        .joinToString(", ")
                    bot.sendSimpleMessage(chatId, "📋 Mavjud mijozlar tillari: $availableLangs")
                }
                return
            }

            // Eng uzoq kutgan chatni tanlash
            val firstChat = pendingChats.minByOrNull { it.createdAt!! }!!

            // Til mosligini qayta tekshirish
            val chat = chatService.getEntityById(firstChat.id!!)
            val chatLanguage = chat!!.language
            val userEntity = userService.getEntityById(user.id!!)

            if (!userEntity.languages.any { it.code == chatLanguage.code }) {
                bot.sendSimpleMessage(chatId, "❌ Siz bu tilni bilmaysiz. Chat tili: ${chatLanguage.name}")
                bot.sendLanguageSelection(chatId)
                return
            }

            // Chatni active qilish
            chatService.assignSupport(firstChat.id!!, user.id!!)

            bot.sendSimpleMessage(chatId, "✅ Mijoz bilan bog'landingiz!")
            bot.sendChatMenu(chatId)

            // Client'ga xabar yuborish
            val client = userService.getEntityById(firstChat.clientId)
            bot.sendSimpleMessage(
                client.telegramId,
                "✅ Support topildi! Suhbat boshlanmoqda..."
            )
            bot.sendChatMenu(client.telegramId)

        } catch (e: IllegalStateException) {
            if (e.message?.contains("Support language must be the same") == true) {
                bot.sendSimpleMessage(chatId, "❌ Til mos kelmadi. Iltimos, boshqa chatni tanlang.")
            } else {
                bot.sendSimpleMessage(chatId, "Xatolik: ${e.message}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            bot.sendSimpleMessage(chatId, "Xatolik: ${e.message}")
        }
    }

    fun handleSettings(chatId: Long, telegramUserId: Long, bot: BotExecutor) {
        bot.sendLanguageSelection(chatId)
    }
}


