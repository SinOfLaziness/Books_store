package com.github.Books_store.userInterface.tg;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class tgKeyboards {
    private InlineKeyboardButton createButton(String text, String callbackData){
        final InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    public InlineKeyboardMarkup createStartKeyboard() {
        final InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        final List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        final List<InlineKeyboardButton> row1 = new ArrayList<>();
        final List<InlineKeyboardButton> row2 = new ArrayList<>();

        row1.add(createButton("Опция 1", "option1"));
        row1.add(createButton("Опция 2", "option2"));
        row2.add(createButton("Выйти из аккаунта", "unlogging"));
        rows.add(row1);
        rows.add(row2);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup createSignKeyboard() {
        final InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        final List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        final List<InlineKeyboardButton> row = new ArrayList<>();

        row.add(createButton("Зарегистрироваться","requestLogin"));
        rows.add(row);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }
}
