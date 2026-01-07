package uz.vv.service.handler

import org.springframework.stereotype.Component
import uz.vv.dto.MessageCreateDTO
import uz.vv.dto.UserCreateDTO
import uz.vv.enum.ChatStatus
import uz.vv.enum.MessageType
import uz.vv.exception.ChatNotFoundException
import uz.vv.exception.ChatNotActiveException
import uz.vv.exception.UserAlreadyExistException
import uz.vv.exception.UserNotFoundException
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
                val activeOrPausedChats = chatService.getActiveOrPausedChatsByUser(user.id!!)

                if (activeOrPausedChats.isNotEmpty()) {
                    activeOrPausedChats.forEach { chat ->
                        chatService.closeChat(chat.id!!)

                        val otherUserId = if (chat.client.id == user.id) {
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
                ?: throw UserNotFoundException("Foydalanuvchi topilmadi")

            val activeChats = chatService.getActiveChatsByUser(user.id!!)

            if (activeChats.isEmpty()) {
                bot.sendSimpleMessage(chatId, "❌ Siz hozir suhbatda emassiz.")
                return
            }

            val chat = activeChats.first()

            if (chat.status != ChatStatus.ACTIVE) {
                throw ChatNotActiveException("Suhbat hali boshlanmagan yoki to'xtatilgan")
            }

            val messageDto = MessageCreateDTO(
                messageTgId = System.currentTimeMillis(),
                content = text,
                type = MessageType.TEXT,
                senderId = user.id!!,
                chatId = chat.id!!
            )
            messageService.create(messageDto)

            val otherUserId = if (chat.client.id == user.id) {
                chat.support?.id
            } else {
                chat.client.id
            }

            if (otherUserId == null) {
                bot.sendSimpleMessage(chatId, "⚠️ Ikkinchi foydalanuvchi topilmadi. Support hali ulanmagan.")
                return
            }

            val otherUser = userService.getEntityById(otherUserId)
            bot.forwardMessage(otherUser.telegramId, text, user.firstName)

        } catch (e: UserNotFoundException) {
            bot.sendSimpleMessage(chatId, "❌ ${e.message}")
        } catch (e: ChatNotActiveException) {
            bot.sendSimpleMessage(chatId, "⚠️ ${e.message}")
        } catch (e: Exception) {
            e.printStackTrace()
            bot.sendSimpleMessage(chatId, "Xatolik: ${e.message}")
        }
    }

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
                ?: throw UserNotFoundException("Foydalanuvchi topilmadi")

            val activeChats = chatService.getActiveChatsByUser(user.id!!)

            if (activeChats.isEmpty()) {
                bot.sendSimpleMessage(chatId, "❌ Siz hozir suhbatda emassiz.")
                return
            }

            val chat = activeChats.first()

            if (chat.status != ChatStatus.ACTIVE) {
                throw ChatNotActiveException("Suhbat hali boshlanmagan yoki to'xtatilgan")
            }

            // Xabarni DBga saqlash
            val messageDto = MessageCreateDTO(
                messageTgId = System.currentTimeMillis(),
                content = caption ?: telegramFileId, // Faylning file_id sini saqlaymiz
                type = fileType,
                senderId = user.id!!,
                chatId = chat.id!!
            )
            val savedMessage = messageService.create(messageDto)

            // File metadata ni DBga yozish (optional - agar kerak bo'lsa)
            // fileService ni kengaytirish kerak bo'lsa bu yerda qo'shishingiz mumkin

            val otherUserId = if (chat.client.id == user.id) {
                chat.support?.id
            } else {
                chat.client.id
            }

            if (otherUserId == null) {
                bot.sendSimpleMessage(chatId, "⚠️ Ikkinchi foydalanuvchi topilmadi.")
                return
            }

            val otherUser = userService.getEntityById(otherUserId)

            // Faylni ikkinchi foydalanuvchiga yuborish
            when (fileType) {
                MessageType.PHOTO ->
                    bot.sendPhoto(otherUser.telegramId, telegramFileId, caption ?: "", user.firstName)

                MessageType.VIDEO ->
                    bot.sendVideo(otherUser.telegramId, telegramFileId, caption ?: "", user.firstName)

                MessageType.VOICE ->
                    bot.sendVoice(otherUser.telegramId, telegramFileId, caption ?: "", user.firstName)

                MessageType.DOCUMENT ->
                    bot.sendDocument(otherUser.telegramId, telegramFileId, caption ?: "", user.firstName)

                MessageType.STICKER ->
                    bot.sendSticker(otherUser.telegramId, telegramFileId, user.firstName)

                else -> {
                    bot.sendSimpleMessage(chatId, "❌ Fayl turi qo'llab-quvvatlanmaydi!")
                }
            }

        } catch (e: UserNotFoundException) {
            bot.sendSimpleMessage(chatId, "❌ ${e.message}")
        } catch (e: ChatNotActiveException) {
            bot.sendSimpleMessage(chatId, "⚠️ ${e.message}")
        } catch (e: Exception) {
            e.printStackTrace()
            bot.sendSimpleMessage(chatId, "❌ Xatolik: ${e.message}")
        }
    }

    fun handleLocationMessage(
        chatId: Long,
        telegramUserId: Long,
        latitude: Double,
        longitude: Double,
        bot: BotExecutor
    ) {
        try {
            val user = userService.findByTelegramId(telegramUserId)
                ?: throw UserNotFoundException("Foydalanuvchi topilmadi")

            val activeChats = chatService.getActiveChatsByUser(user.id!!)

            if (activeChats.isEmpty()) {
                bot.sendSimpleMessage(chatId, "❌ Siz hozir suhbatda emassiz.")
                return
            }

            val chat = activeChats.first()

            if (chat.status != ChatStatus.ACTIVE) {
                throw ChatNotActiveException("Suhbat hali boshlanmagan yoki to'xtatilgan")
            }

            // Location xabarini DBga saqlash (agar kerak bo'lsa)
            val messageDto = MessageCreateDTO(
                messageTgId = System.currentTimeMillis(),
                content = "Location: $latitude, $longitude",
                type = MessageType.LOCATION,
                senderId = user.id!!,
                chatId = chat.id!!
            )
            messageService.create(messageDto)

            val otherUserId = if (chat.client.id == user.id) {
                chat.support?.id
            } else {
                chat.client.id
            }

            if (otherUserId == null) {
                bot.sendSimpleMessage(chatId, "⚠️ Ikkinchi foydalanuvchi topilmadi.")
                return
            }

            val otherUser = userService.getEntityById(otherUserId)
            bot.sendLocation(otherUser.telegramId, latitude, longitude, user.firstName)

        } catch (e: UserNotFoundException) {
            bot.sendSimpleMessage(chatId, "❌ ${e.message}")
        } catch (e: ChatNotActiveException) {
            bot.sendSimpleMessage(chatId, "⚠️ ${e.message}")
        } catch (e: Exception) {
            e.printStackTrace()
            bot.sendSimpleMessage(chatId, "❌ Xatolik: ${e.message}")
        }
    }

    fun handleVideoNoteMessage(
        chatId: Long,
        telegramUserId: Long,
        fileId: String,
        bot: BotExecutor
    ) {
        try {
            val user = userService.findByTelegramId(telegramUserId)
                ?: throw UserNotFoundException("Foydalanuvchi topilmadi")

            val activeChats = chatService.getActiveChatsByUser(user.id!!)

            if (activeChats.isEmpty()) {
                bot.sendSimpleMessage(chatId, "❌ Siz hozir suhbatda emassiz.")
                return
            }

            val chat = activeChats.first()

            if (chat.status != ChatStatus.ACTIVE) {
                throw ChatNotActiveException("Suhbat hali boshlanmagan yoki to'xtatilgan")
            }

            // Video note xabarini DBga saqlash
            val messageDto = MessageCreateDTO(
                messageTgId = System.currentTimeMillis(),
                content = fileId,
                type = MessageType.VIDEO_NOTE,
                senderId = user.id!!,
                chatId = chat.id!!
            )
            messageService.create(messageDto)

            val otherUserId = if (chat.client.id == user.id) {
                chat.support?.id
            } else {
                chat.client.id
            }

            if (otherUserId == null) {
                bot.sendSimpleMessage(chatId, "⚠️ Ikkinchi foydalanuvchi topilmadi.")
                return
            }

            val otherUser = userService.getEntityById(otherUserId)
            bot.sendVideoNote(otherUser.telegramId, fileId, user.firstName)

        } catch (e: UserNotFoundException) {
            bot.sendSimpleMessage(chatId, "❌ ${e.message}")
        } catch (e: ChatNotActiveException) {
            bot.sendSimpleMessage(chatId, "⚠️ ${e.message}")
        } catch (e: Exception) {
            e.printStackTrace()
            bot.sendSimpleMessage(chatId, "❌ Xatolik: ${e.message}")
        }
    }
}