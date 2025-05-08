package com.github.Books_store.model;

import com.github.Books_store.accessLayer.databaseInitializer;
import com.github.Books_store.userInterface.tg.tgBot;
import com.github.Books_store.userInterface.tg.tgKeyboards;
import com.github.Books_store.userInterface.tg.tgTools;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.github.Books_store.userInterface.templatesMessage.*;

public class response {
    private final tgTools TGTools;
    private final tgKeyboards TGKeyboards;
    private final databaseInitializer dbHandler;
    private final HashMap<Long, String> statusTable;
    private final HashMap<Long, ArrayList<String>> authorisationTable;

    public response(tgBot telegramBot) {
        this.TGTools = new tgTools(telegramBot);
        this.TGKeyboards = new tgKeyboards();
        this.dbHandler = new databaseInitializer();
        this.statusTable = new HashMap<Long, String>();
        this.authorisationTable = new HashMap<Long, ArrayList<String>>();
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

//    public void signUpUser(Long chatId, String socNet) throws TelegramApiException {
//        dbHandler.getDatabaseTools().signUpUser(chatId);
//        sendMessage(chatId, SUCCESSFUL_SIGN, socNet);
//    }

    public void requestLogin(Long chatId, String socNet) throws TelegramApiException {
        sendMessage(chatId, NOTICE_ON_REGISTRATION, socNet);
        sendMessage(chatId, REQUEST_LOGIN, socNet);
        this.statusTable.put(chatId, "inputLogin");
    }

    public void requestPassword(Long chatId, String socNet) throws TelegramApiException {
        sendMessage(chatId, REQUEST_PASSWORD, socNet);
        this.statusTable.put(chatId, "inputPassword");
    }

    public void signUpUser(Long chatId, String socNet) throws SQLException, TelegramApiException {
        this.dbHandler.getDatabaseTools().signUpUser(chatId, this.authorisationTable.get(chatId), socNet);
        if (this.dbHandler.getDatabaseTools().checkIfSigned(chatId))
            sendMessage(chatId, SUCCESSFUL_SIGN, socNet);
        this.statusTable.remove(chatId);
        this.authorisationTable.remove(chatId);
    }

    public void unlogging(Long chatId, String socNet) throws SQLException, TelegramApiException {
        this.dbHandler.getDatabaseTools().unlogging(chatId, socNet);
        if (!this.dbHandler.getDatabaseTools().checkIfSigned(chatId))
            sendMessage(chatId, SUCCESSFUL_EXIT, socNet);
    }

    public void help(Long chatId, String socNet) throws TelegramApiException {
        sendMessage(chatId, HELP, socNet);
    }

    public void unknown(Long chatId, String text, String socNet) throws TelegramApiException, SQLException {
        if (this.statusTable.containsKey(chatId)){
            String status = this.statusTable.get(chatId);
            if (status.equals("inputLogin")){
                ArrayList<String> authorisationData = new ArrayList<String>();
                authorisationData.add(text);
                this.authorisationTable.put(chatId, authorisationData);
                requestPassword(chatId, socNet);
            }
            else if (status.equals("inputPassword")){
                this.authorisationTable.get(chatId).add(text);
                System.out.println(this.authorisationTable.get(chatId));
                signUpUser(chatId, socNet);
            }
        }
        else
            sendMessage(chatId, UNKNOWN, socNet);
    }


    public void option1Callback(Long chatId, String socNet) throws TelegramApiException {
        sendMessage(chatId, "Вы выбрали первый вариант!", socNet);
    }

    public void option2Callback(Long chatId, String socNet) throws TelegramApiException {
        sendMessage(chatId, "Вы выбрали второй вариант!", socNet);
    }

}
