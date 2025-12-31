package uz.vv.service.handler

import org.springframework.stereotype.Component
import uz.vv.dto.MessageCreateDTO
import uz.vv.dto.UserCreateDTO
import uz.vv.enum.ChatStatus
import uz.vv.enum.MessageType
import uz.vv.exception.UserAlreadyExistException
import uz.vv.service.BotExecutor
import uz.vv.service.ChatService
import uz.vv.service.FileService
import uz.vv.service.MessageService
import uz.vv.service.UserService

@Component
class MessageHandler(
    private val userService: UserService,
    private val chatService: ChatService,
    private val messageService: MessageService,
    private val fileService: FileService
) {


    fun handleStart(chatId: Long, telegramUserId: Long, bot: BotExecutor) {
        try {
            val user = userService.findByTelegramId(telegramUserId)

            if (user != null) {
                // User'ning faol yoki to'xtatilgan chatlarini tekshirish
                val userEntity = userService.getEntityById(user.id!!)
                val activeOrPausedChats = chatService.getActiveOrPausedChatsByUser(user.id!!)

                // Agar chat bor bo'lsa, uni yakunlash
                if (activeOrPausedChats.isNotEmpty()) {
                    activeOrPausedChats.forEach { chat ->
                        chatService.closeChat(chat.id!!)

                        // Ikkinchi foydalanuvchiga xabar
                        val otherUserId = if (chat.client.id == user.id!!) {
                            chat.support?.id
                        } else {
                            chat.client.id
                        }

                        otherUserId?.let {
                            val otherUser = userService.getEntityById(it)
                            bot.sendSimpleMessage(
                                otherUser.telegramId,
                                "❌ Suhbat ${user.firstName} tomonidan yakunlandi (chatdan chiqdi)."
                            )

                            val isSupport = otherUser.roles.any { role -> role.code == "SUPPORT" }
                            if (isSupport) {
                                bot.sendSupportMenu(otherUser.telegramId, "Support menyusi:")
                            } else {
                                bot.sendClientMenu(otherUser.telegramId, "Asosiy menyu:")
                            }
                        }
                    }

                    bot.sendSimpleMessage(chatId, "⚠️ Avvalgi suhbat(lar) yakunlandi.")
                }

                // User rolini tekshirish
                val isSupport = user.roles.any { it.code == "SUPPORT" }

                if (isSupport) {
                    bot.sendSupportMenu(chatId, "Xush kelibsiz, ${user.firstName}!")
                } else {
                    bot.sendClientMenu(chatId, "Xush kelibsiz, ${user.firstName}!")
                }
            } else {
                bot.sendContactRequest(chatId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            bot.sendSimpleMessage(chatId, "Xatolik yuz berdi. Iltimos qaytadan /start bosing.")
        }
    }

    fun handleContactShared(
        chatId: Long,
        telegramUserId: Long,
        contact: org.telegram.telegrambots.meta.api.objects.Contact,
        bot: BotExecutor
    ) {
        val phoneNumber = contact.phoneNumber
        val firstName = contact.firstName ?: "User"
        val lastName = contact.lastName

        try {
            val userDto = UserCreateDTO(
                telegramId = telegramUserId,
                firstName = firstName,
                lastName = lastName,
                phoneNumber = if (phoneNumber.startsWith("+")) phoneNumber else "+$phoneNumber"
            )

            userService.create(userDto)
            bot.sendSimpleMessage(chatId, "✅ Ro'yxatdan o'tdingiz!")
            bot.sendLanguageSelection(chatId)
        } catch (e: UserAlreadyExistException) {
            bot.sendSimpleMessage(chatId, "Siz allaqachon ro'yxatdan o'tgansiz!")
            bot.sendClientMenu(chatId, "Asosiy menyu:")
        } catch (e: Exception) {
            e.printStackTrace()
            bot.sendSimpleMessage(chatId, "Xatolik: ${e.message}")
        }
    }


    fun handleRegularMessage(chatId: Long, telegramUserId: Long, text: String, bot: BotExecutor) {
        try {
            val user = userService.findByTelegramId(telegramUserId)
                ?: throw Exception("User not found")

            val activeChats = chatService.getActiveChatsByUser(user.id!!)

            if (activeChats.isEmpty()) {
                bot.sendSimpleMessage(chatId, "❌ Siz hozir suhbatda emassiz.")
                return
            }

            val chat = activeChats.first()

            // ✅ Chat holati tekshiruvi
            if (chat.status != ChatStatus.ACTIVE) {
                bot.sendSimpleMessage(chatId, "⚠️ Suhbat hali boshlanmagan yoki to'xtatilgan.")
                return
            }

            // ✅ Xabarni saqlash
            val messageDto = MessageCreateDTO(
                messageTgId = System.currentTimeMillis(),
                content = text,
                type = MessageType.TEXT,
                senderId = user.id!!,
                chatId = chat.id!!
            )
            messageService.create(messageDto)

            // ✅ Ikkinchi foydalanuvchini topish
            val otherUserId = if (chat.client.id == user.id) {
                chat.support?.id
            } else {
                chat.client.id
            }

            // ✅ Null tekshiruvi bilan xabar yuborish
            if (otherUserId == null) {
                bot.sendSimpleMessage(chatId, "⚠️ Ikkinchi foydalanuvchi topilmadi. Support hali ulanmagan.")
                return
            }

            val otherUser = userService.getEntityById(otherUserId)
            bot.forwardMessage(otherUser.telegramId, text, user.firstName)

        } catch (e: Exception) {
            e.printStackTrace()
            bot.sendSimpleMessage(chatId, "Xatolik: ${e.message}")
        }
    }

    // Yangi: Fayllarni qayta ishlash
    fun handleFileMessage(
        chatId: Long,
        telegramUserId: Long,
        telegramFileId: String,
        fileType: MessageType,
        caption: String?,
        bot: BotExecutor
    ) {
        try {
            val user = userService.findByTelegramId(telegramUserId)
                ?: throw Exception("User not found")

            val activeChats = chatService.getActiveChatsByUser(user.id!!)

            if (activeChats.isNotEmpty()) {
                val chat = activeChats.first()

                val messageDto = MessageCreateDTO(
                    messageTgId = System.currentTimeMillis(),
                    content = caption,
                    type = fileType,
                    senderId = user.id!!,
                    chatId = chat.id!!
                )
                // Faylni saqlash (bu yerda Telegram file download va saqlash logikasi)
                // Haqiqiy implementatsiya Telegram file download API ga bog'liq

                // Xabarni ikkinchi tomonga yuborish
                val otherUserId = if (chat.client.id == user.id) {
                    chat.support?.id
                } else {
                    chat.client.id
                }

                otherUserId?.let {
                    val otherUser = userService.getEntityById(it)

                    when (fileType) {
                        MessageType.PHOTO ->
                            bot.sendPhoto(otherUser.telegramId, telegramFileId, caption ?: "", user.firstName)

                        MessageType.VIDEO ->
                            bot.sendVideo(otherUser.telegramId, telegramFileId, caption ?: "", user.firstName)

                        MessageType.VOICE ->
                            bot.sendVoice(otherUser.telegramId, telegramFileId, caption ?: "", user.firstName)

                        MessageType.DOCUMENT ->
                            bot.sendDocument(otherUser.telegramId, telegramFileId, caption ?: "", user.firstName)

                        else -> {bot.sendSimpleMessage(chatId, "Fayl turi noto'g'ri!")}
                    }

                }
            } else {
                bot.sendSimpleMessage(chatId, "Siz hozir suhbatda emassiz.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            bot.sendSimpleMessage(chatId, "Xatolik: ${e.message}")
        }
    }
}

