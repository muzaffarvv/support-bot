package uz.vv.service.handler

import org.springframework.stereotype.Component
import uz.vv.exception.NoAvailableSupportException
import uz.vv.exception.ChatAlreadyActiveException
import uz.vv.exception.LanguageMismatchException
import uz.vv.exception.UserNotFoundException
import uz.vv.repo.LanguageRepo
import uz.vv.service.BotExecutor
import uz.vv.service.ChatService
import uz.vv.service.UserService

@Component
class SupportHandler(
    private val userService: UserService,
    private val chatService: ChatService,
    private val langRepo: LanguageRepo
) {

    fun handleContactClients(chatId: Long, telegramUserId: Long, bot: BotExecutor) {
        try {
            val user = userService.findByTelegramId(telegramUserId)
                ?: throw UserNotFoundException("Foydalanuvchi topilmadi")

            // Avval active chat bor yoki yo'qligini tekshirish
            val existingActiveChats = chatService.getActiveChatsByUser(user.id!!)
            if (existingActiveChats.isNotEmpty()) {
                throw ChatAlreadyActiveException("Sizda allaqachon faol suhbat mavjud!")
            }

            // Barcha pending chatlarni olish
            val allPendingChats = chatService.getPendingChats()

            // Agar umuman mijozlar yo'q bo'lsa
            if (allPendingChats.isEmpty()) {
                bot.sendSimpleMessage(chatId, "📭 Hozircha kutayotgan mijozlar yo'q.")
                return
            }

            // Support'ning tillarini olish
            val supportLanguages = user.languages.map { it.code }.toSet()

            // User'ning tillari bo'yicha pending chatlarni filtrlash
            val matchingChats = allPendingChats.filter { chat ->
                supportLanguages.contains(chat.languageDTO.code)
            }

            // Agar support'ning tiliga mos mijozlar yo'q bo'lsa
            if (matchingChats.isEmpty()) {
                val availableLangs = allPendingChats
                    .map { it.languageDTO.name }
                    .distinct()
                    .joinToString(", ")

                bot.sendSimpleMessage(
                    chatId,
                    """
                    ⚠️ Sizning tillaringizga mos mijozlar hozircha yo'q.
                    
                    📋 Mavjud mijozlar tillari: $availableLangs
                    
                    💡 Sozlamalarda tillarni o'zgartiring.
                    """.trimIndent()
                )
                return
            }

            // Eng uzoq kutgan chatni tanlash
            val firstChat = matchingChats.minByOrNull { it.createdAt!! }!!

            // Til mosligini qayta tekshirish
            val chat = chatService.getEntityById(firstChat.id!!)
            val chatLanguage = chat!!.language
            val userEntity = userService.getEntityById(user.id!!)

            if (!userEntity.languages.any { it.code == chatLanguage.code }) {
                throw LanguageMismatchException("Siz bu tilni bilmaysiz. Chat tili: ${chatLanguage.name}")
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

        } catch (e: UserNotFoundException) {
            bot.sendSimpleMessage(chatId, "❌ ${e.message}")
        } catch (e: ChatAlreadyActiveException) {
            bot.sendSimpleMessage(chatId, "ℹ️ ${e.message}")
            bot.sendChatMenu(chatId)
        } catch (e: LanguageMismatchException) {
            bot.sendSimpleMessage(chatId, "❌ ${e.message}")
            bot.sendLanguageSelection(chatId)
        } catch (e: NoAvailableSupportException) {
            bot.sendSimpleMessage(chatId, "❌ ${e.message}")
        } catch (e: Exception) {
            e.printStackTrace()
            bot.sendSimpleMessage(chatId, "❌ Xatolik: ${e.message}")
        }
    }

    fun handleSettings(chatId: Long, telegramUserId: Long, bot: BotExecutor) {
        try {
            val user = userService.findByTelegramId(telegramUserId)
                ?: throw UserNotFoundException("Foydalanuvchi topilmadi")

            // Support uchun ko'p til tanlash
            bot.sendMultiLanguageSelection(chatId, user.languages.mapNotNull { it.id }.toMutableSet())
        } catch (e: UserNotFoundException) {
            bot.sendSimpleMessage(chatId, "❌ ${e.message}")
        } catch (e: Exception) {
            e.printStackTrace()
            bot.sendSimpleMessage(chatId, "❌ Xatolik: ${e.message}")
        }
    }
}