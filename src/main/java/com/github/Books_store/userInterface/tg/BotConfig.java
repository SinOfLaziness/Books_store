package com.github.Books_store.userInterface.tg;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import com.github.Books_store.userInterface.tg.tgBot;

@Configuration
public class BotConfig {

    /**
     * Регистрирует TelegramLongPollingBot в сессии, чтобы он начал получать апдейты
     */
    @Bean
    public TelegramBotsApi telegramBotsApi(tgBot tgBot) throws Exception {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(tgBot);
        return api;
    }
}