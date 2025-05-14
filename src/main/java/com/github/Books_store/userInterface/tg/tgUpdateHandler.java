package com.github.Books_store.userInterface.tg;

import static com.github.Books_store.userInterface.templatesMessage.*;

import com.github.Books_store.model.response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class tgUpdateHandler {

    private final response response;

    // Внедряем response через конструктор (через Spring)
    @Autowired
    public tgUpdateHandler(response response) {
        this.response = response;
    }

    public void handleUpdate(Update update) throws Exception {
        if (update.hasMessage()) {
            handleTextMessage(update);
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
        }
    }

    private void handleTextMessage(Update update) throws Exception {
        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        switch (text.toLowerCase()) {
            case "/start" -> response.startCommand(chatId, TG);
            case "/help"  -> response.help(chatId, TG);
            default      -> response.unknown(chatId, text, TG);
        }
    }

    private void handleCallbackQuery(Update update) throws Exception {
        CallbackQuery cq = update.getCallbackQuery();
        String data = cq.getData();
        Long chatId = cq.getMessage().getChatId();

        switch (data) {
            case "option1"        -> response.option1Callback(chatId, TG);
            case "option2"        -> response.option2Callback(chatId, TG);
            case "requestLogin"   -> response.requestLogin(chatId, TG);
            case "unlogging"      -> response.unlogging(chatId, TG);
            default               -> response.unknown(chatId, data, TG);
        }
    }
}
