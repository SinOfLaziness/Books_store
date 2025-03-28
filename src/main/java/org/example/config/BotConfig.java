package org.example.config;

import org.example.bot.TelegramBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Configuration
public class BotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBot telegramBot) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            telegramBotsApi.registerBot(telegramBot);
        } catch (TelegramApiException e) {
            String message = e.getMessage();
            // Если сообщение содержит "404" (например, "Error deleting webhook: [404] Not Found"), игнорируем ошибку
            if (message != null && message.contains("404")) {
                System.out.println("Webhook не найден, продолжаем регистрацию бота: " + message);
            } else {
                throw e;
            }
        }
        return telegramBotsApi;
    }
}
