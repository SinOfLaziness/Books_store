package com.github.Books_store.model;

import com.github.Books_store.accessLayer.databaseInitializer;
import com.github.Books_store.accessLayer.databaseTools;
import com.github.Books_store.model.entities.Book;
import com.github.Books_store.model.entities.CartItem;
import com.github.Books_store.model.entities.Purchase;
import com.github.Books_store.service.OneTimeCodeService;
import com.github.Books_store.userInterface.tg.tgBot;
import com.github.Books_store.userInterface.tg.tgTools;
import com.github.Books_store.userInterface.tg.tgKeyboards;
import com.github.Books_store.userInterface.vk.vkBot;
import com.github.Books_store.userInterface.vk.vkKeyboards;
import com.github.Books_store.userInterface.vk.vkTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.github.Books_store.userInterface.templatesMessage.*;

@Component
public class response {

    private final tgTools TGTools;
    private final vkTools VKTools;
    private final tgKeyboards TGKeyboards;
    private final vkKeyboards VKKeyboards;
    private final databaseTools dbTools;
    private final HashMap<Long, String> statusTable ;
    private final HashMap<Long, ArrayList<String>> authTable;
    private final OneTimeCodeService codeService;

    @Autowired
    public response(@Lazy tgBot telegramBot, @Lazy vkBot vkBot, OneTimeCodeService codeService) {
        this.codeService = codeService;
        this.TGTools = new tgTools(telegramBot);
        this.VKTools = new vkTools();
        this.TGKeyboards = new tgKeyboards();
        this.VKKeyboards = new vkKeyboards();
        try {
            this.dbTools = new databaseTools(new databaseInitializer().getDbConnection());
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Ошибка инициализации базы данных", e);
        }
        this.statusTable = new HashMap<>();
        this.authTable = new HashMap<>();
    }
    // Шаг 1: сгенерировать и отправить код
    public void requestOneTimeCode(Long userId, String socNet) throws Exception {
        String code = codeService.generate(userId, socNet);
        sendMsg(userId, "Ваш одноразовый код: " + code +
                "\nДействует 5 минут.", socNet);
    }

    // Шаг 2: предложить ввести код
    public void promptOneTimeCode(Long userId, String socNet) throws Exception {
        sendMsg(userId, "Введите одноразовый код:", socNet);
        setStatus(userId, "awaiting_code");
    }

    // Шаг 3: принять код и связать аккаунты
    public void loginByCode(Long userId, String socNet, String code) throws Exception {
        code = code.trim().toUpperCase();
        var opt = codeService.consume(code);
        if (opt.isPresent()) {
            var rec = opt.get();
            dbTools.linkSocialAccount(
                    rec.userId, rec.socNet,
                    userId, socNet
            );
            sendMsg(userId, "Вход по коду успешен!", socNet);
            sendKeyboard(userId, HELLO, socNet, true);
        } else {
            sendMsg(userId, "Неверный или просроченный код.", socNet);
        }
    }
    // Метод для получения логина по userId
    public String getLogin(Long userId, String socNet) throws SQLException {
        return dbTools.getLoginByUserId(userId, socNet);
    }

    private boolean isTG(String socNet) { return TG.equals(socNet); }
    private boolean isVK(String socNet) { return VK.equals(socNet); }

    public void sendMsg(Long userId, String text, String socNet) throws Exception {
        if (isTG(socNet)) TGTools.sendMessage(userId, text);
        else if (isVK(socNet)) VKTools.sendMessage(userId, text);
    }

    public void requestLogin(Long userId, String socNet) throws Exception {
        sendMsg(userId, "Пожалуйста, введите логин:", socNet);
        setStatus(userId, "login");
    }

    public void option1Callback(Long userId, String socNet) throws Exception {
        sendMsg(userId, "Вы выбрали первый вариант!", socNet);
        boolean signed = dbTools.checkIfSigned(userId, socNet);
        sendKeyboard(userId, signed ? HELLO : SIGN_PLEASE, socNet, signed);
    }

    public void option2Callback(Long userId, String socNet) throws Exception {
        sendMsg(userId, "Вы выбрали второй вариант!", socNet);
        boolean signed = dbTools.checkIfSigned(userId, socNet);
        sendKeyboard(userId, signed ? HELLO : SIGN_PLEASE, socNet, signed);
    }

    private void sendKeyboard(Long userId, String text, String socNet, boolean signed) throws Exception {
        if (isTG(socNet)) {
            InlineKeyboardMarkup kb = signed
                    ? TGKeyboards.createStartKeyboard()
                    : TGKeyboards.createSignKeyboard();
            TGTools.sendMessageWithKeyboard(userId, text, kb);
        } else if (isVK(socNet)) {
            String kbJson = signed
                    ? VKKeyboards.createStartKeyboardJson()
                    : VKKeyboards.createSignKeyboardJson();
            String uniqueText = text + "\u200B";
            VKTools.sendMessageWithKeyboard(userId, uniqueText, kbJson);
        }
    }

    // Отправка специальной клавиатуры для списка книг
    private void sendBookListKeyboard(Long userId, String text, String socNet) throws Exception {
        if (isVK(socNet)) {
            String kbJson = VKKeyboards.createBookListKeyboardJson();
            VKTools.sendMessageWithKeyboard(userId, text + "\u200B", kbJson);
        } else if (isTG(socNet)) {
            InlineKeyboardMarkup kb = TGKeyboards.createBookListKeyboard();
            TGTools.sendMessageWithKeyboard(userId, text, kb);
        }
    }

    // Отправка клавиатуры корзины
    private void sendCartKeyboard(Long userId, String text, String socNet) throws Exception {
        if (isVK(socNet)) {
            String kbJson = VKKeyboards.createCartKeyboardJson();
            VKTools.sendMessageWithKeyboard(userId, text + "\u200B", kbJson);
        } else if (isTG(socNet)) {
            InlineKeyboardMarkup kb = TGKeyboards.createCartKeyboard();
            TGTools.sendMessageWithKeyboard(userId, text, kb);
        }
    }

    public void startCommand(Long userId, String socNet) throws Exception {
        boolean signed = dbTools.checkIfSigned(userId, socNet);
        sendKeyboard(userId, signed ? HELLO : SIGN_PLEASE, socNet, signed);
    }


    public void listBooks(Long userId, String socNet) throws Exception {
        String login = getLogin(userId, socNet);
        if (login == null) {
            sendMsg(userId, "Сначала зарегистрируйтесь, пожалуйста.", socNet);
            return;
        }

        List<Book> books = dbTools.getBooksList();
        if (books.isEmpty()) {
            sendMsg(userId, "Книги отсутствуют.", socNet);
            return;
        }

        StringBuilder sb = new StringBuilder("Список книг:\n");
        for (Book book : books) {
            sb.append(String.format("%d. %s (В наличии: %d)\n", book.getId(), book.getTitle(), book.getStock()));
        }
        sendBookListKeyboard(userId, sb.toString(), socNet);
        statusTable.put(userId, "main_menu");
    }

    public void showPurchases(Long userId, String socNet) throws Exception {
        String login = getLogin(userId, socNet);
        if (login == null) {
            sendMsg(userId, "Сначала зарегистрируйтесь, пожалуйста.", socNet);
            return;
        }

        List<Purchase> purchases = dbTools.getUserPurchases(login);
        if (purchases.isEmpty()) {
            sendMsg(userId, "Покупок пока нет.", socNet);
            return;
        }

        StringBuilder sb = new StringBuilder("Ваши покупки:\n");
        for (Purchase purchase : purchases) {
            sb.append(String.format("%s — количество: %d (дата: %s)\n",
                    purchase.getBookTitle(), purchase.getQuantity(), purchase.getPurchaseDate().toString()));
        }
        sendMsg(userId, sb.toString(), socNet);
    }

    public void addToCartFlow(Long userId, String socNet, String input) throws Exception {
        String state = getStatus(userId);
        ArrayList<String> temp = authTable.getOrDefault(userId, new ArrayList<>());

        if ("awaiting_book_id".equals(state)) {
            temp.clear();
            temp.add(input);
            authTable.put(userId, temp);
            sendMsg(userId, "Введите количество:", socNet);
            setStatus(userId, "awaiting_quantity");

        } else if ("awaiting_quantity".equals(state)) {
            if (temp.isEmpty()) {
                sendMsg(userId, "Ошибка. Сначала введите ID книги.", socNet);
                setStatus(userId, "awaiting_book_id");
                return;
            }
            String bookIdStr = temp.get(0);
            String quantityStr = input.trim();

            try {
                int bookId = Integer.parseInt(bookIdStr);
                int quantity = Integer.parseInt(quantityStr);
                boolean success = dbTools.addToCart(getLogin(userId, socNet), bookId, quantity);
                if (success) {
                    sendMsg(userId, "Книга добавлена в корзину!", socNet);
                } else {
                    sendMsg(userId, "Ошибка: недостаточно книг на складе или неверные данные.", socNet);
                }
            } catch (NumberFormatException e) {
                sendMsg(userId, "Неверный формат ID или количества.", socNet);
            }

            authTable.remove(userId);
            setStatus(userId, "main_menu");
            sendKeyboard(userId, HELLO, socNet, true);

        } else {
            sendMsg(userId, "Пожалуйста, используйте меню для взаимодействия.", socNet);
        }
    }

    public void showCart(Long userId, String socNet) throws Exception {
        String login = getLogin(userId, socNet);
        if (login == null) {
            sendMsg(userId, "Сначала зарегистрируйтесь, пожалуйста.", socNet);
            return;
        }

        List<CartItem> cartItems = dbTools.getUserCart(login);
        if (cartItems.isEmpty()) {
            sendMsg(userId, "Ваша корзина пуста.", socNet);
            return;
        }

        StringBuilder sb = new StringBuilder("Ваша корзина:\n");
        for (CartItem item : cartItems) {
            sb.append(String.format("%s — количество: %d\n", item.getBookTitle(), item.getQuantity()));
        }
        sendCartKeyboard(userId, sb.toString(), socNet);
        statusTable.put(userId, "cart_menu");
    }

    public void purchaseCart(Long userId, String socNet) throws Exception {
        String login = getLogin(userId, socNet);
        if (login == null) {
            sendMsg(userId, "Сначала зарегистрируйтесь, пожалуйста.", socNet);
            return;
        }

        boolean success = dbTools.purchaseCart(login);
        if (success) {
            sendMsg(userId, "Покупка корзины успешно оформлена!", socNet);
        } else {
            sendMsg(userId, "Не удалось оформить покупку. Корзина может быть пуста или возникла ошибка.", socNet);
        }
        sendKeyboard(userId, HELLO, socNet, true);
        statusTable.put(userId, "main_menu");
    }

    public void clearCart(Long userId, String socNet) throws Exception {
        String login = getLogin(userId, socNet);
        if (login == null) {
            sendMsg(userId, "Сначала зарегистрируйтесь, пожалуйста.", socNet);
            return;
        }

        boolean success = dbTools.clearCart(login);
        if (success) {
            sendMsg(userId, "Корзина очищена.", socNet);
        } else {
            sendMsg(userId, "Не удалось очистить корзину.", socNet);
        }
        sendKeyboard(userId, HELLO, socNet, true);
        statusTable.put(userId, "main_menu");
    }

    public void handleUnknown(Long userId, String text, String socNet) throws Exception {
        String state = statusTable.get(userId);
        if ("awaiting_book_id".equals(state) || "awaiting_quantity".equals(state)) {
            addToCartFlow(userId, socNet, text);
        } else {
            sendMsg(userId, UNKNOWN, socNet);
        }
    }

    public void unknown(Long userId, String text, String socNet) throws Exception {
        sendMsg(userId, "Не понял вас. Попробуйте ещё раз.", socNet);
    }

    // Метод buyBook (если вы раньше называли по-другому, приведите к одному имени)
    public void buyBook(Long userId, String socNet, int bookId) throws Exception {
        String login = getLogin(userId, socNet);
        if (login == null) {
            sendMsg(userId, "Сначала зарегистрируйтесь, пожалуйста.", socNet);
            return;
        }
        boolean success = dbTools.buyBook(login, bookId);
        if (success) {
            sendMsg(userId, "Покупка успешно оформлена!", socNet);
        } else {
            sendMsg(userId, "Покупка не удалась: возможно, книга закончилась или неверный ID.", socNet);
        }
    }

    // Метод unlogging
    public void unlogging(Long userId, String socNet) throws Exception {
        dbTools.unlogging(userId, socNet);
        sendMsg(userId, "Вы успешно вышли из аккаунта.", socNet);
        sendKeyboard(userId, SIGN_PLEASE, socNet, false);
    }

    // Метод help
    public void help(Long userId, String socNet) throws Exception {
        sendMsg(userId, "Помощь: используйте команды или кнопки.", socNet);
    }

    public void setStatus(Long userId, String status) {
        statusTable.put(userId, status);
    }
    public String getStatus(Long userId) {
        return statusTable.get(userId);
    }

    public void processLoginInput(Long userId, String socNet, String input) throws Exception {
        // Сохраняете временно логин
        ArrayList<String> temp = authTable.getOrDefault(userId, new ArrayList<>());
        temp.clear();
        temp.add(input); // логин
        authTable.put(userId, temp);

        sendMsg(userId, "Пожалуйста, введите пароль:", socNet);
        setStatus(userId, "password");
    }

    public void processPasswordInput(Long userId, String socNet, String input) throws Exception {
        ArrayList<String> temp = authTable.get(userId);
        if (temp == null || temp.isEmpty()) {
            sendMsg(userId, "Ошибка. Сначала введите логин.", socNet);
            setStatus(userId, "login");
            return;
        }
        String login = temp.get(0);
        String password = input;

        try {
            dbTools.signUpUser(userId, login, password, socNet);
            sendMsg(userId, "Вы успешно зарегистрированы!", socNet);
            setStatus(userId, "main_menu");
            authTable.remove(userId);
            sendKeyboard(userId, HELLO, socNet, true);
        } catch (Exception e) {
            sendMsg(userId, "Ошибка при регистрации. Попробуйте ещё раз.", socNet);
            setStatus(userId, "login");
            authTable.remove(userId);
        }
    }

}
