package com.github.Books_store.UserInterface.TG;

import com.github.Books_store.AccessLayer.DatabaseInitializer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Здесь находятся функции для реагирования на дейсвия пользователя

class TGResponse {
    private final TGbot telegramBot;
    private final DatabaseInitializer dbHandler;

    public TGResponse(TGbot telegramBot) {
        this.telegramBot = telegramBot;
        this.dbHandler = new DatabaseInitializer();
    }

    protected void SendMessage(Long chatId, String message) throws TelegramApiException {
        SendMessage messageObject = new SendMessage();
        messageObject.setChatId(chatId.toString());
        messageObject.setText(message);
        telegramBot.execute(messageObject);
    }

    protected SendMessage createMessage(Long chatId, String message){
        SendMessage messageObject = new SendMessage();
        messageObject.setChatId(chatId.toString());
        messageObject.setText(message);
        return messageObject;
    }

    protected void startCommand(Long chatId) throws TelegramApiException, SQLException {
        if (dbHandler.getDatabaseTools().checkIfSigned(chatId)){
            String message = "Привет! Я книжный магазин. Чем могу помочь?";
            InlineKeyboardMarkup keyboardMarkup = createStartKeyboard();
            SendMessage messageObject = createMessage(chatId, message);
            messageObject.setReplyMarkup(keyboardMarkup);
            telegramBot.execute(messageObject);
        }
        else{
            String message = "Для получения доступа к магазину, пожалуйста зарегистрируйтесь.";
            InlineKeyboardMarkup keyboardMarkup = createSignKeyboard();
            SendMessage messageObject = createMessage(chatId, message);
            messageObject.setReplyMarkup(keyboardMarkup);
            telegramBot.execute(messageObject);
        }
    }

    protected void option1Callback(Long chatId) throws TelegramApiException {
        String message = "Вы выбрали первый вариант!";
        SendMessage(chatId, message);
    }

    protected void option2Callback(Long chatId) throws TelegramApiException {
        String message = "Вы выбрали второй вариант!";
        SendMessage(chatId, message);
    }

    protected void signUpUser(Long chatId) throws TelegramApiException {
        SendMessage(chatId, chatId.toString());
        dbHandler.getDatabaseTools().signUpUser(chatId);
        SendMessage(chatId, "Вы успешно зарегистрированы!");
    }

    private InlineKeyboardMarkup createStartKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Опция 1");
        button1.setCallbackData("option1");
        row1.add(button1);
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Опция 2");
        button2.setCallbackData("option2");
        row1.add(button2);
        rows.add(row1);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup createSignKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Зарегистрироваться");
        button1.setCallbackData("signUpUser");
        row1.add(button1);
        rows.add(row1);
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }
}
