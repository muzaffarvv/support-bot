package uz.vv.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.methods.send.SendVideo
import org.telegram.telegrambots.meta.api.methods.send.SendVoice
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import uz.vv.enum.MessageType
import uz.vv.service.handler.*

@Service
class BotService(
    @Value("\${bot.token}") private val botTokenStr: String,
    @Value("\${bot.username}") private val botUsernameStr: String,
    private val messageHandler: MessageHandler,
    private val callbackHandler: CallbackHandler,
    private val clientHandler: ClientHandler,
    private val supportHandler: SupportHandler,
    private val chatActionHandler: ChatActionHandler,
    private val userService: UserService,
    private val fileService: FileService
) : TelegramLongPollingBot(botTokenStr), BotExecutor {

    override fun getBotUsername(): String = botUsernameStr

    override fun onUpdateReceived(update: Update) {
        when {
            update.hasMessage() -> handleMessage(update)
            update.hasCallbackQuery() -> handleCallbackQuery(update)
        }
    }

    private fun handleMessage(update: Update) {
        val message = update.message
        val chatId = message.chatId
        val telegramUserId = message.from.id

        when {
            message.hasText() -> {
                val text = message.text
                when (text) {
                    "/start" -> messageHandler.handleStart(chatId, telegramUserId, this)
                    "⚙️ Sozlamalar" -> handleSettings(chatId, telegramUserId)
                    "💬 Support bilan bog'lanish" -> clientHandler.handleContactSupport(chatId, telegramUserId, this)
                    "👥 Mijoz bilan bog'lanish" -> supportHandler.handleContactClients(chatId, telegramUserId, this)
                    "⏸️ Suhbatni to'xtatish" -> chatActionHandler.handlePauseChat(chatId, telegramUserId, this)
                    "▶️ Suhbatni davom ettirish" -> chatActionHandler.handleResumeChat(chatId, telegramUserId, this)
                    "❌ Suhbatni yakunlash" -> chatActionHandler.handleEndChat(chatId, telegramUserId, this)
                    else -> messageHandler.handleRegularMessage(chatId, telegramUserId, text, this)
                }
            }
            message.hasContact() -> messageHandler.handleContactShared(chatId, telegramUserId, message.contact, this)
            message.hasPhoto() -> handleFileMessage(chatId, telegramUserId, message, MessageType.PHOTO)
            message.hasVideo() -> handleFileMessage(chatId, telegramUserId, message, MessageType.VIDEO)
            message.hasVoice() -> handleFileMessage(chatId, telegramUserId, message, MessageType.VOICE)
            message.hasDocument() -> handleFileMessage(chatId, telegramUserId, message, MessageType.DOCUMENT)
        }
    }

    private fun handleFileMessage(chatId: Long, telegramUserId: Long, message: org.telegram.telegrambots.meta.api.objects.Message, fileType: MessageType) {
        try {
            // Telegram fayl ID sini olish
            val fileId = when (fileType) {
                MessageType.PHOTO -> message.photo.last().fileId
                MessageType.VIDEO -> message.video.fileId
                MessageType.VOICE -> message.voice.fileId
                MessageType.DOCUMENT -> message.document.fileId
                else -> null
            }

            if (fileId != null) {

                messageHandler.handleFileMessage(
                    chatId = chatId,
                    telegramUserId = telegramUserId,
                    telegramFileId = fileId,
                    fileType = fileType,
                    caption = message.caption,
                    bot = this
                )

            }
        } catch (e: Exception) {
            e.printStackTrace()
            sendSimpleMessage(chatId, "Fayl yuborishda xatolik: ${e.message}")
        }
    }

    private fun handleCallbackQuery(update: Update) {
        val callbackQuery = update.callbackQuery
        val chatId = callbackQuery.message.chatId
        val telegramUserId = callbackQuery.from.id
        val data = callbackQuery.data

        when {
            data.startsWith("lang_") -> {
                val langCode = data.substringAfter("lang_")
                callbackHandler.handleLanguageSelection(chatId, telegramUserId, langCode, this)
            }
            data == "accept_support" -> callbackHandler.handleAcceptSupport(chatId, telegramUserId, this)
            data == "reject_support" -> callbackHandler.handleRejectSupport(chatId, telegramUserId, this)
        }
    }

    private fun handleSettings(chatId: Long, telegramUserId: Long) {
        try {
            val user = userService.findByTelegramId(telegramUserId)
            val isSupport = user?.roles?.any { it.code == "SUPPORT" } ?: false

            if (isSupport) {
                supportHandler.handleSettings(chatId, telegramUserId, this)
            } else {
                clientHandler.handleSettings(chatId, telegramUserId, this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            sendSimpleMessage(chatId, "Xatolik: ${e.message}")
        }
    }

    override fun sendContactRequest(chatId: Long) {
        val message = SendMessage()
        message.chatId = chatId.toString()
        message.text = "Assalomu alaykum! Botdan foydalanish uchun telefon raqamingizni yuboring:"

        val keyboard = ReplyKeyboardMarkup()
        keyboard.resizeKeyboard = true
        keyboard.oneTimeKeyboard = true

        val row = KeyboardRow()
        val button = KeyboardButton("📱 Telefon raqamni yuborish")
        button.requestContact = true
        row.add(button)

        keyboard.keyboard = listOf(row)
        message.replyMarkup = keyboard

        try {
            execute(message)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    override fun sendLanguageSelection(chatId: Long) {
        val message = SendMessage()
        message.chatId = chatId.toString()
        message.text = "Tilni tanlang / Выберите язык / Choose language:"

        val markup = InlineKeyboardMarkup()
        val rows = listOf(
            listOf(InlineKeyboardButton("🇺🇿 O'zbek").apply { callbackData = "lang_uz" }),
            listOf(InlineKeyboardButton("🇷🇺 Русский").apply { callbackData = "lang_ru" }),
            listOf(InlineKeyboardButton("🇬🇧 English").apply { callbackData = "lang_en" })
        )
        markup.keyboard = rows
        message.replyMarkup = markup

        try {
            execute(message)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    override fun removeReplyKeyboard(chatId: Long) {
        val message = SendMessage()
        message.chatId = chatId.toString()
        message.text = " "

        val removeKeyboard = ReplyKeyboardRemove()
        removeKeyboard.removeKeyboard = true
        message.replyMarkup = removeKeyboard

        try {
            execute(message)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    override fun sendClientMenu(chatId: Long, text: String) {
        val message = SendMessage()
        message.chatId = chatId.toString()
        message.text = text

        val keyboard = ReplyKeyboardMarkup()
        keyboard.resizeKeyboard = true

        val row1 = KeyboardRow()
        row1.add(KeyboardButton("💬 Support bilan bog'lanish"))

        val row2 = KeyboardRow()
        row2.add(KeyboardButton("⚙️ Sozlamalar"))

        keyboard.keyboard = listOf(row1, row2)
        message.replyMarkup = keyboard

        try {
            execute(message)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    override fun sendSupportMenu(chatId: Long, text: String) {
        val message = SendMessage()
        message.chatId = chatId.toString()
        message.text = text

        val keyboard = ReplyKeyboardMarkup()
        keyboard.resizeKeyboard = true

        val row1 = KeyboardRow()
        row1.add(KeyboardButton("👥 Mijoz bilan bog'lanish"))

        val row2 = KeyboardRow()
        row2.add(KeyboardButton("⚙️ Sozlamalar"))

        keyboard.keyboard = listOf(row1, row2)
        message.replyMarkup = keyboard

        try {
            execute(message)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    override fun sendChatMenu(chatId: Long) {
        val message = SendMessage()
        message.chatId = chatId.toString()
        message.text = "Suhbat boshlanadi:"

        val keyboard = ReplyKeyboardMarkup()
        keyboard.resizeKeyboard = true

        val row1 = KeyboardRow()
        row1.add(KeyboardButton("⏸️ Suhbatni to'xtatish"))

        val row2 = KeyboardRow()
        row2.add(KeyboardButton("❌ Suhbatni yakunlash"))

        keyboard.keyboard = listOf(row1, row2)
        message.replyMarkup = keyboard

        try {
            execute(message)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    override fun sendSimpleMessage(chatId: Long, text: String) {
        val message = SendMessage()
        message.chatId = chatId.toString()
        message.text = text

        try {
            execute(message)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    override fun forwardMessage(telegramId: Long, text: String, senderName: String) {
        val message = SendMessage()
        message.chatId = telegramId.toString()
        message.text = "💬 $senderName:\n$text"

        try {
            execute(message)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    override fun onRegister() {
        val commands = ArrayList<BotCommand>()
        commands.add(BotCommand("/start", "start or reload the bot"))

        try {
            this.execute(SetMyCommands(commands, BotCommandScopeDefault(), null))
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    override fun sendChatPausedMenu(chatId: Long, text: String, isSupport: Boolean) {
        val message = SendMessage()
        message.chatId = chatId.toString()
        message.text = text

        val keyboard = ReplyKeyboardMarkup()
        keyboard.resizeKeyboard = true

        val row1 = KeyboardRow()
        if (isSupport) {
            row1.add(KeyboardButton("▶️ Suhbatni davom ettirish"))
        } else {
            row1.add(KeyboardButton("▶️ Suhbatni davom ettirish"))
        }

        val row2 = KeyboardRow()
        row2.add(KeyboardButton("❌ Suhbatni yakunlash"))

        keyboard.keyboard = listOf(row1, row2)
        message.replyMarkup = keyboard

        try {
            execute(message)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    override fun sendPhoto(telegramId: Long, fileId: String, caption: String, senderName: String) {
        val message = SendPhoto()
        message.chatId = telegramId.toString()
        message.photo = InputFile(fileId)
        message.caption = "📸 $senderName:\n$caption"

        try {
            execute(message)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    override fun sendVideo(telegramId: Long, fileId: String, caption: String, senderName: String) {
        val message = SendVideo()
        message.chatId = telegramId.toString()
        message.video = InputFile(fileId)
        message.caption = "🎬 $senderName:\n$caption"

        try {
            execute(message)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    override fun sendVoice(telegramId: Long, fileId: String, caption: String, senderName: String) {
        val message = SendVoice()
        message.chatId = telegramId.toString()
        message.voice = InputFile(fileId)
        message.caption = "🎤 $senderName:\n$caption"

        try {
            execute(message)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    override fun sendDocument(telegramId: Long, fileId: String, caption: String, senderName: String) {
        val message = SendDocument()
        message.chatId = telegramId.toString()
        message.document = InputFile(fileId)
        message.caption = "📄 $senderName:\n$caption"

        try {
            execute(message)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }
}
