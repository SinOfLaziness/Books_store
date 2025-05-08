package com.github.Books_store.userInterface.tg;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import static com.github.Books_store.hiddenConstants.BOT_TOKEN;

import java.sql.SQLException;

// Запуск телеграм бота

public class tgBot extends TelegramLongPollingBot {
    private final tgUpdateHandler TGupdateHandler;

    public tgBot() {
        this.TGupdateHandler = new tgUpdateHandler(this);
    }
    @Override
    public String getBotUsername() {
        return "B00ks_St0re_bot";
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            TGupdateHandler.handleUpdate(update);
        } catch (SQLException | TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
