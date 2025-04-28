package com.github.Books_store.UserInterface.TG;

import static com.github.Books_store.UserInterface.TemplatesMessage.*;

import com.github.Books_store.UserInterface.TG.TGResponse;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.sql.SQLException;

// Здесь происходит обработка взаимодействия пользователя с ботом
// HandleTextMessage обрабатывает текстовые сообщения,
// HandleCallbackQuery обрабатывает нажатия на кнопки
// Функции, вызываемые при обработке, хранятся в TGResponse
// Вызываются они через response.

public class TGUpdateHandler{

    private final TGResponse response;

    public TGUpdateHandler(TGbot telegramBot) {
        response = new TGResponse(telegramBot);
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
            case "/start" ->    response.startCommand(chatId);
            case "/help" ->     response.SendMessage(chatId, HELP);
            default ->          response.SendMessage(chatId, UNKNOWN);
        }
    }

    private void handleCallbackQuery(Update update) throws TelegramApiException {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();

        switch (data) {
            case "option1" ->       response.option1Callback(chatId);
            case "option2" ->       response.option2Callback(chatId);
            case "signUpUser" ->    response.signUpUser(chatId);
            default ->              response.SendMessage(chatId, UNKNOWN);
        }
    }
}