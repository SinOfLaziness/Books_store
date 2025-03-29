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

    private Map<Long, PurchaseState> purchaseStates = new ConcurrentHashMap<>();
    private Map<Long, AddToCartState> addToCartStates = new ConcurrentHashMap<>();

    public String handleCommand(String messageText, String chatIdString) {
        String command = messageText.trim();
        Long chatId;
        try {
            chatId = Long.valueOf(chatIdString);
        } catch (NumberFormatException e) {
            return "Ошибка: неверный chatId.";
        }

        User user = userRepository.findByChatId(chatId);
        boolean isAdmin = user != null && Boolean.TRUE.equals(user.isAdmin());

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

            // Сортируем книги по ID
            books.sort((b1, b2) -> b1.getId().compareTo(b2.getId()));

            StringBuilder sb = new StringBuilder("Список книг:\n\n");
            int count = 1;
            for (Book book : books) {
                sb.append(count++).append(") [ID: ").append(book.getId()).append("]\n")
                        .append("   Название: ").append(book.getTitle()).append("\n")
                        .append("   Автор: ").append(book.getAuthor()).append("\n")
                        .append("   Цена: ").append(book.getPrice()).append("\n")
                        .append("   В наличии: ").append(book.getQuantity() == null ? 0 : book.getQuantity())
                        .append("\n\n");
            }
            return sb.toString();
        } else if (command.equals("Моя корзина")) {
            return cartService.showCart(chatId);
        } else if (command.equals("Купить корзину")) {
            return cartService.buyCart(chatId);
        } else if (command.equals("Очистить корзину")) {
            return cartService.clearCart(chatId);
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
        } else if (command.startsWith("/addbook ") && isAdmin) {
            String[] parts = command.split(" ", 5);
            if (parts.length != 5) {
                return "Неверный формат. Используйте: /addbook Название Автор Цена Количество";
            }
            try {
                String title = parts[1];
                String author = parts[2];
                double price = Double.parseDouble(parts[3]);
                int quantity = Integer.parseInt(parts[4]);
                Book book = bookService.addBook(title, author, price, quantity);
                return book != null ? "Книга добавлена: " + title : "Ошибка.";
            } catch (Exception e) {
                return "Ошибка: проверьте формат данных.";
            }
        } else if (command.equals("/users") && isAdmin) {
            List<User> users = userRepository.findAll();
            StringBuilder sb = new StringBuilder("Список пользователей:\n");
            for (User u : users) {
                sb.append(u.getChatId()).append(": ")
                        .append(u.getUsername())
                        .append(" (Админ: ").append(u.isAdmin()).append(")\n");
            }
            return sb.toString();
        } else if (command.startsWith("/deletebook ") && isAdmin) {
            try {
                Long bookId = Long.parseLong(command.split(" ")[1]);
                bookService.deleteBook(bookId);
                return "Книга удалена!";
            } catch (Exception e) {
                return "Используйте: /deletebook ID_книги";
            }
        } else if (command.startsWith("/makeadmin ") && isAdmin) {
            try {
                Long targetChatId = Long.parseLong(command.split(" ")[1]);
                User targetUser = userRepository.findByChatId(targetChatId);
                if (targetUser == null) return "Пользователь не найден.";
                targetUser.setAdmin(true);
                userRepository.save(targetUser);
                return "Пользователь " + targetChatId + " стал администратором!";
            } catch (Exception e) {
                return "Используйте: /makeadmin CHAT_ID";
            }
        } else {
            return "Неизвестная команда. Используйте кнопки меню.";
        }
    }

    private static class PurchaseState {
        private Long bookId;
        public Long getBookId() { return bookId; }
        public void setBookId(Long bookId) { this.bookId = bookId; }
    }

    private static class AddToCartState {
        private Long bookId;
        public Long getBookId() { return bookId; }
        public void setBookId(Long bookId) { this.bookId = bookId; }
    }
}