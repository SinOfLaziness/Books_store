package com.github.Books_store.UserInterface.TG;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.SQLException;

// Запуск телеграм бота

public class TGbot extends TelegramLongPollingBot {
    private final TGUpdateHandler TGupdateHandler;

    public TGbot() {
        this.TGupdateHandler = new TGUpdateHandler(this);
    }
    @Override
    public String getBotUsername() {
        return "B00ks_St0re_bot";
    }

    @Override
    public String getBotToken() {
        return "7910001494:AAHPYxFKYFU_0_h1shYWJFExliCk0Yuxz00";
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
