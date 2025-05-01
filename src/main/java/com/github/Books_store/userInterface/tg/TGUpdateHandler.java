package com.github.Books_store.userInterface.tg;

import static com.github.Books_store.userInterface.templatesMessage.*;

import com.github.Books_store.model.response;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.sql.SQLException;

// Здесь происходит обработка взаимодействия пользователя с ботом
// HandleTextMessage обрабатывает текстовые сообщения,
// HandleCallbackQuery обрабатывает нажатия на кнопки
// Функции, вызываемые при обработке, хранятся в TGResponse
// Вызываются они через response.

public class tgUpdateHandler {

    private final response Response;

    public tgUpdateHandler(tgBot telegramBot) {
        Response = new response(telegramBot);
    }

    public void handleUpdate(Update update) throws TelegramApiException, SQLException {
        if (update.hasMessage()) {
            handleTextMessage(update);
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
        }
    }

    private void handleTextMessage(Update update) throws TelegramApiException, SQLException {
        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        switch (text.toLowerCase()) {
            case "/start" ->    Response.startCommand(chatId, TG);
            case "/help" ->     Response.help(chatId, TG);
            default ->          Response.unknown(chatId, TG);
        }
    }

    private void handleCallbackQuery(Update update) throws TelegramApiException {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();

        switch (data) {
            case "option1" ->       Response.option1Callback(chatId, TG);
            case "option2" ->       Response.option2Callback(chatId, TG);
            case "signUpUser" ->    Response.signUpUser(chatId, TG);
            default ->              Response.unknown(chatId, TG);
        }
    }
}