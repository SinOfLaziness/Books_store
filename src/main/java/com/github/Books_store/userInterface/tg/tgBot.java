package com.github.Books_store.userInterface.tg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import static com.github.Books_store.hiddenConstants.BOT_TOKEN;

@Component
public class tgBot extends TelegramLongPollingBot {

    private final tgUpdateHandler tgUpdateHandler;

    // Внедряем tgUpdateHandler через конструктор
    @Autowired
    public tgBot(tgUpdateHandler tgUpdateHandler) {
        this.tgUpdateHandler = tgUpdateHandler;
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
            tgUpdateHandler.handleUpdate(update);
        } catch (Exception e) {
            e.printStackTrace();  // Логируем ошибку, а не кидаем RuntimeException
        }
    }
}
