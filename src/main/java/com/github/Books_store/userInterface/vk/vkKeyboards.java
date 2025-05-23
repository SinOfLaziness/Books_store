package com.github.Books_store.userInterface.vk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

@Component
public class vkKeyboards {
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Возвращает JSON-клавиатуру для команды /start — две кнопки «Опция 1», «Опция 2» и «Выйти из аккаунта».
     */
    public String createStartKeyboardJson() {
        ArrayNode buttons = mapper.createArrayNode();

        // первая строка: Опция 1, Опция 2
        ArrayNode row1 = mapper.createArrayNode();
        row1.add(createButtonNode("opt1", "Опция 1", "primary"));
        row1.add(createButtonNode("opt2", "Опция 2", "primary"));
        buttons.add(row1);

        // вторая строка: Список книг, Корзина
        ArrayNode row2 = mapper.createArrayNode();
        row2.add(createButtonNode("list_books", "Список книг", "primary"));
        row2.add(createButtonNode("cart", "Корзина", "primary"));
        buttons.add(row2);

        // третья строка: Покупки, Выйти из аккаунта
        ArrayNode row3 = mapper.createArrayNode();
        row3.add(createButtonNode("purchases", "Покупки", "primary"));
        row3.add(createButtonNode("logout", "Выйти из аккаунта", "negative"));
        buttons.add(row3);

        ObjectNode keyboard = mapper.createObjectNode();
        keyboard.put("one_time", false);
        keyboard.set("buttons", buttons);
        return keyboard.toString();
    }


    /**
     * Возвращает JSON-клавиатуру для регистрации — кнопка «Зарегистрироваться».
     */
    public String createSignKeyboardJson() {
        ArrayNode buttons = mapper.createArrayNode();
        ArrayNode row = mapper.createArrayNode();
        row.add(createButtonNode("sign_up", "Зарегистрироваться", "positive"));
        buttons.add(row);

        ObjectNode keyboard = mapper.createObjectNode();
        keyboard.put("one_time", true);
        keyboard.set("buttons", buttons);
        return keyboard.toString();
    }

    /**
     * Вспомогательный метод для создания JSON-узла кнопки.
     * @param payloadCommand команда в payload
     * @param label текст кнопки
     * @param color цвет ("primary", "positive" или "negative")
     */
    private ObjectNode createButtonNode(String payloadCommand, String label, String color) {
        ObjectNode action = mapper.createObjectNode();
        action.put("type", "text");
        action.put("payload", String.format("{\"command\":\"%s\"}", payloadCommand));
        action.put("label", label);

        ObjectNode button = mapper.createObjectNode();
        button.set("action", action);
        button.put("color", color);
        return button;
    }
    public String createBookListKeyboardJson() {
        ArrayNode buttons = mapper.createArrayNode();

        ArrayNode row1 = mapper.createArrayNode();
        row1.add(createButtonNode("add_to_cart", "Добавить в корзину", "primary"));
        row1.add(createButtonNode("back", "Назад", "negative"));
        buttons.add(row1);

        ObjectNode keyboard = mapper.createObjectNode();
        keyboard.put("one_time", false);
        keyboard.set("buttons", buttons);
        return keyboard.toString();
    }

    public String createCartKeyboardJson() {
        ArrayNode buttons = mapper.createArrayNode();

        ArrayNode row1 = mapper.createArrayNode();
        row1.add(createButtonNode("purchase_cart", "Купить всю корзину", "positive"));
        row1.add(createButtonNode("clear_cart", "Очистить корзину", "negative"));
        row1.add(createButtonNode("back", "Назад", "negative"));
        buttons.add(row1);

        ObjectNode keyboard = mapper.createObjectNode();
        keyboard.put("one_time", false);
        keyboard.set("buttons", buttons);
        return keyboard.toString();
    }
}