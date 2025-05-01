package com.github.Books_store.userInterface.tg;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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
        return "7910001494:AAEtJ0X5fiJ4lZfqZACWN1slCKa79T60_sA";
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
