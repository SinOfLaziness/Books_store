package com.github.Books_store.userInterface.tg;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

// Здесь находятся функции для реагирования на дейсвия пользователя

public class tgTools {
    private final tgBot telegramBot;

    public tgTools(tgBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void sendMessageWithKeyboard(Long chatId, String message, InlineKeyboardMarkup keyboardMarkup)
            throws TelegramApiException {
        SendMessage messageObject = new SendMessage();
        messageObject.setChatId(chatId.toString());
        messageObject.setText(message);
        messageObject.setReplyMarkup(keyboardMarkup);
        telegramBot.execute(messageObject);
    }

    public void sendMessage(Long chatId, String message)
            throws TelegramApiException {
        SendMessage messageObject = new SendMessage();
        messageObject.setChatId(chatId.toString());
        messageObject.setText(message);
        telegramBot.execute(messageObject);
    }
}
