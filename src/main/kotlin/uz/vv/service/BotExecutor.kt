package uz.vv.service

// BotExecutor interface - Bot bilan ishlash uchun
interface BotExecutor {
    fun sendSimpleMessage(chatId: Long, text: String)
    fun sendContactRequest(chatId: Long)
    fun sendLanguageSelection(chatId: Long)
    fun sendMultiLanguageSelection(chatId: Long, selectedLanguages: MutableSet<Long>)
    fun sendMultiLanguageSelectionWithSave(chatId: Long, selectedLanguages: MutableSet<Long>)
    fun sendClientMenu(chatId: Long, text: String)
    fun sendSupportMenu(chatId: Long, text: String)
    fun sendChatMenu(chatId: Long)
    fun removeReplyKeyboard(chatId: Long)
    fun forwardMessage(telegramId: Long, text: String, senderName: String)
    fun sendChatPausedMenu(chatId: Long, text: String, isSupport: Boolean)
    fun sendPhoto(telegramId: Long, fileId: String, caption: String, senderName: String)
    fun sendVideo(telegramId: Long, fileId: String, caption: String, senderName: String)
    fun sendVoice(telegramId: Long, fileId: String, caption: String, senderName: String)
    fun sendDocument(telegramId: Long, fileId: String, caption: String, senderName: String)
    fun sendSticker(telegramId: Long, fileId: String, senderName: String)
    fun sendLocation(telegramId: Long, latitude: Double, longitude: Double, senderName: String)
    fun sendVideoNote(telegramId: Long, fileId: String, senderName: String)
    fun editMultiLanguageSelection(chatId: Long, messageId: Int, selectedLanguages: Set<Long>, withSave: Boolean)
}