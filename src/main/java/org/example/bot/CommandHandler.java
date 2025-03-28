package org.example.bot;

import org.example.model.Book;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.example.service.BookService;
import org.example.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CommandHandler {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartService cartService;

    // Состояние интерактивного ввода для покупки книги
    private Map<Long, PurchaseState> purchaseStates = new ConcurrentHashMap<>();
    // Состояние интерактивного ввода для добавления книги в корзину
    private Map<Long, AddToCartState> addToCartStates = new ConcurrentHashMap<>();

    public String handleCommand(String messageText, String chatIdString) {
        String command = messageText.trim();
        Long chatId;
        try {
            chatId = Long.valueOf(chatIdString);
        } catch (NumberFormatException e) {
            return "Ошибка: неверный chatId.";
        }

        if (command.equalsIgnoreCase("/start")) {
            User existingUser = userRepository.findByChatId(chatId);
            if (existingUser == null) {
                User newUser = new User(chatId, "User_" + chatId, "dummy");
                userRepository.save(newUser);
                return "Вы впервые используете бота, пользователь создан!\nВыберите действие, нажав на кнопку.";
            } else {
                return "С возвращением!\nВыберите действие, нажав на кнопку.";
            }
        } else if (command.equals("Список книг")) {
            List<Book> books = bookService.getAllBooks();
            if (books.isEmpty()) {
                return "На данный момент книги отсутствуют.";
            }
            StringBuilder sb = new StringBuilder("Список книг:\n");
            for (Book book : books) {
                sb.append(book.getId()).append(": ")
                        .append(book.getTitle()).append(" — ")
                        .append(book.getAuthor()).append(" (Цена: ")
                        .append(book.getPrice()).append("), В наличии: ")
                        .append(book.getQuantity() == null ? 0 : book.getQuantity())
                        .append("\n");
            }
            return sb.toString();
        } else if (command.equals("Моя корзина")) {
            return cartService.showCart(chatId);
        } else if (command.equals("Купить корзину")) {
            return cartService.buyCart(chatId);
        } else if (command.equals("Купить книгу")) {
            purchaseStates.remove(chatId);
            purchaseStates.put(chatId, new PurchaseState());
            List<Book> books = bookService.getAllBooks();
            if (books.isEmpty()) {
                return "На данный момент книги отсутствуют.";
            }
            StringBuilder sb = new StringBuilder("Выберите книгу для покупки, отправьте её id:\n");
            for (Book book : books) {
                sb.append(book.getId()).append(": ")
                        .append(book.getTitle()).append(" - Цена: ")
                        .append(book.getPrice()).append("\n");
            }
            return sb.toString();
        } else if (command.equals("Добавить в корзину")) {
            addToCartStates.remove(chatId);
            addToCartStates.put(chatId, new AddToCartState());
            List<Book> books = bookService.getAllBooks();
            if (books.isEmpty()) {
                return "На данный момент книги отсутствуют.";
            }
            StringBuilder sb = new StringBuilder("Выберите книгу для добавления в корзину, отправьте её id:\n");
            for (Book book : books) {
                sb.append(book.getId()).append(": ")
                        .append(book.getTitle()).append(" - Цена: ")
                        .append(book.getPrice()).append("\n");
            }
            return sb.toString();
        } else if (purchaseStates.containsKey(chatId)) {
            PurchaseState state = purchaseStates.get(chatId);
            if (state.getBookId() == null) {
                try {
                    Long bookId = Long.valueOf(messageText.trim());
                    state.setBookId(bookId);
                    return "Введите количество книг, которое хотите купить:";
                } catch (NumberFormatException e) {
                    return "Неверный формат id. Пожалуйста, отправьте числовое значение.";
                }
            } else {
                try {
                    int qty = Integer.parseInt(messageText.trim());
                    boolean success = bookService.purchaseBook(state.getBookId(), qty);
                    purchaseStates.remove(chatId);
                    return success ? "Спасибо за покупку!" : "Ошибка: либо книга не найдена, либо недостаточно экземпляров.";
                } catch (NumberFormatException e) {
                    return "Неверный формат количества. Пожалуйста, отправьте числовое значение.";
                }
            }
        } else if (addToCartStates.containsKey(chatId)) {
            AddToCartState state = addToCartStates.get(chatId);
            if (state.getBookId() == null) {
                try {
                    Long bookId = Long.valueOf(messageText.trim());
                    state.setBookId(bookId);
                    return "Введите количество книг для добавления в корзину:";
                } catch (NumberFormatException e) {
                    return "Неверный формат id. Пожалуйста, отправьте числовое значение.";
                }
            } else {
                try {
                    int qty = Integer.parseInt(messageText.trim());
                    boolean success = cartService.addBookToCart(chatId, state.getBookId(), qty);
                    addToCartStates.remove(chatId);
                    return success ? "Книга добавлена в корзину!" : "Ошибка при добавлении книги в корзину.";
                } catch (NumberFormatException e) {
                    return "Неверный формат количества. Пожалуйста, отправьте числовое значение.";
                }
            }
        } else {
            return "Неизвестная команда. Используйте кнопки меню.";
        }
    }

    // Вспомогательный класс для хранения состояния покупки книги
    private static class PurchaseState {
        private Long bookId;
        public Long getBookId() { return bookId; }
        public void setBookId(Long bookId) { this.bookId = bookId; }
    }

    // Вспомогательный класс для хранения состояния добавления книги в корзину
    private static class AddToCartState {
        private Long bookId;
        public Long getBookId() { return bookId; }
        public void setBookId(Long bookId) { this.bookId = bookId; }
    }
}
