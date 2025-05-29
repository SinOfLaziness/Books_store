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
        final List<InlineKeyboardButton> row3 = new ArrayList<>();
        final List<InlineKeyboardButton> row4 = new ArrayList<>();

        row1.add(createButton("Опция 1", "opt1"));
        row1.add(createButton("Опция 2", "opt2"));
        row2.add(createButton("Список книг", "list_books"));
        row2.add(createButton("Корзина", "cart"));
        row3.add(createButton("Покупки", "purchases"));
        row3.add(createButton("Выйти из аккаунта", "logout"));

        row4.add(createButton("Получить код",      "get_code"));
        rows.add(row4);
        rows.add(row1);
        rows.add(row2);
        rows.add(row3);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup createSignKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        // одна строка с двумя кнопками
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createButton("Зарегистрироваться", "sign_up"));
        row.add(createButton("Войти по коду",    "login_by_code"));
        rows.add(row);

        markup.setKeyboard(rows);
        return markup;
    }


    public InlineKeyboardMarkup createBookListKeyboard() {
        final InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        final List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        final List<InlineKeyboardButton> row1 = new ArrayList<>();

        row1.add(createButton("Добавить в корзину", "add_to_cart"));
        row1.add(createButton("Назад", "back"));
        rows.add(row1);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup createCartKeyboard() {
        final InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        final List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        final List<InlineKeyboardButton> row1 = new ArrayList<>();

        row1.add(createButton("Купить всю корзину", "purchase_cart"));
        row1.add(createButton("Очистить корзину", "clear_cart"));
        row1.add(createButton("Назад", "back"));
        rows.add(row1);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }
}
