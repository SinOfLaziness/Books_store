package com.github.Books_store.model;

import com.github.Books_store.accessLayer.databaseInitializer;
import com.github.Books_store.userInterface.tg.tgBot;
import com.github.Books_store.userInterface.tg.tgKeyboards;
import com.github.Books_store.userInterface.tg.tgTools;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.SQLException;

import static com.github.Books_store.userInterface.templatesMessage.*;

public class response {
    private final tgTools TGTools;
    private final tgKeyboards TGKeyboards;
    private final databaseInitializer dbHandler;

    public response(tgBot telegramBot) {
        this.TGTools = new tgTools(telegramBot);
        this.TGKeyboards = new tgKeyboards();
        this.dbHandler = new databaseInitializer();
    }

    public void sendMessageWithKeyboard(Long chatId,
                            String message, String socNet, InlineKeyboardMarkup keyboardMarkup)
                            throws TelegramApiException {
        if (socNet.equals(TG))
            TGTools.sendMessageWithKeyboard(chatId, message, keyboardMarkup);
    }

    public void sendMessage(Long chatId, String message, String socNet)
                            throws TelegramApiException {
        if (socNet.equals(TG))
            TGTools.sendMessage(chatId, message);
    }

    public void startCommand(Long chatId, String socNet) throws SQLException, TelegramApiException {
        if (dbHandler.getDatabaseTools().checkIfSigned(chatId)){
            sendMessageWithKeyboard(chatId, HELLO, socNet, TGKeyboards.createStartKeyboard());
        }
        else{
            sendMessageWithKeyboard(chatId, SIGN_PLEASE, socNet, TGKeyboards.createSignKeyboard());
        }
    }

    public void signUpUser(Long chatId, String socNet) throws TelegramApiException {
        dbHandler.getDatabaseTools().signUpUser(chatId);
        sendMessage(chatId, SUCCESSFUL_SIGN, socNet);
    }

    public void help(Long chatId, String socNet) throws TelegramApiException {
        sendMessage(chatId, HELP, socNet);
    }

    public void unknown(Long chatId, String socNet) throws TelegramApiException {
        sendMessage(chatId, UNKNOWN, socNet);
    }


    public void option1Callback(Long chatId, String socNet) throws TelegramApiException {
        sendMessage(chatId, "Вы выбрали первый вариант!", socNet);
    }

    public void option2Callback(Long chatId, String socNet) throws TelegramApiException {
        sendMessage(chatId, "Вы выбрали второй вариант!", socNet);
    }

}
