package uz.vv.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

@Service
class BotService(
    @Value("\${bot.token}") private val botTokenStr: String,
    @Value("\${bot.username}") private val botUsernameStr: String
) : TelegramLongPollingBot(botTokenStr) {

    override fun getBotUsername(): String = botUsernameStr

    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage() && update.message.hasText()) {
            val chatId = update.message.chatId.toString()

            sendSimpleMessage(chatId, "hi ")
        }
    }

    override fun onRegister() {
        val commands = ArrayList<BotCommand>()
        commands.add(BotCommand("/start", " start or refresh bot"))
        //  other commands can also be added like /start
        try {
            this.execute(SetMyCommands(commands, BotCommandScopeDefault(), null))
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    private fun sendSimpleMessage(chatId: String, text: String) {
        val message = SendMessage()
        message.chatId = chatId
        message.text = text
        try {
            execute(message)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }
}