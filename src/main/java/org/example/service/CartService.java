package org.example.service;

import org.example.model.*;
import org.example.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Transactional
    public Cart findOrCreateActiveCart(User user) {
        Cart cart = cartRepository.findByUserAndStatus(user, "ACTIVE");
        if (cart == null) {
            cart = new Cart(user, "ACTIVE");
            cartRepository.save(cart);
        }
        return cart;
    }

    @Transactional
    public boolean addBookToCart(Long chatId, Long bookId, int qty) {
        User user = userRepository.findByChatId(chatId);
        if (user == null) {
            return false;
        }
        Cart cart = findOrCreateActiveCart(user);
        return addBookToCartInternal(cart, bookId, qty);
    }

    @Transactional
    public boolean addBookToCartInternal(Cart cart, Long bookId, int qty) {
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) return false;
        CartItem cartItem = cartItemRepository.findByCart_IdAndBook_Id(cart.getId(), book.getId());
        if (cartItem == null) {
            cartItem = new CartItem(cart, book, qty);
        } else {
            cartItem.setQuantity(cartItem.getQuantity() + qty);
        }
        cartItemRepository.save(cartItem);
        return true;
    }

    @Transactional
    public String showCart(Long chatId) {
        User user = userRepository.findByChatId(chatId);
        if (user == null) {
            return "Пользователь не найден (chatId = " + chatId + ")";
        }
        Cart cart = cartRepository.findByUserAndStatus(user, "ACTIVE");
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            return "Ваша корзина пуста.";
        }
        StringBuilder sb = new StringBuilder("Ваша корзина:\n");
        for (CartItem item : cart.getItems()) {
            sb.append("Книга: ").append(item.getBook().getTitle())
                    .append(", Количество: ").append(item.getQuantity())
                    .append("\n");
        }
        return sb.toString();
    }

    @Transactional
    public String buyCart(Long chatId) {
        User user = userRepository.findByChatId(chatId);
        if (user == null) {
            return "Пользователь не найден (chatId = " + chatId + ")";
        }
        Cart cart = cartRepository.findByUserAndStatus(user, "ACTIVE");
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            return "Ваша корзина пуста.";
        }
        Order order = new Order(user, "PAID");
        order = orderRepository.save(order);
        double total = 0.0;
        for (CartItem item : cart.getItems()) {
            Book book = item.getBook();
            if (book.getQuantity() < item.getQuantity()) {
                return "Недостаточно книг '" + book.getTitle() + "' в наличии.";
            }
            book.setQuantity(book.getQuantity() - item.getQuantity());
            bookRepository.save(book);
            OrderItem orderItem = new OrderItem(order, book, item.getQuantity(), book.getPrice());
            orderItemRepository.save(orderItem);
            total += item.getQuantity() * book.getPrice();
        }
        cart.setStatus("COMPLETED");
        cartRepository.save(cart);
        return "Спасибо за покупку! Общая сумма: " + total;
    }

    @Transactional
    public String clearCart(Long chatId) {
        User user = userRepository.findByChatId(chatId);
        if (user == null) return "Пользователь не найден.";
        Cart cart = cartRepository.findByUserAndStatus(user, "ACTIVE");
        if (cart == null || cart.getItems().isEmpty()) return "Корзина уже пуста.";

        // Достаточно очистить коллекцию, orphanRemoval удалит CartItem из базы
        cart.getItems().clear();
        cartRepository.save(cart);
        return "Корзина очищена.";
    }
}