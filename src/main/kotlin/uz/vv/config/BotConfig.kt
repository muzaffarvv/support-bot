package uz.vv.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import uz.vv.service.BotService

@Configuration
class BotConfig {

    @Bean
    fun telegramBotsApi(botService: BotService): TelegramBotsApi {
        val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
        botsApi.registerBot(botService)
        return botsApi
    }
}