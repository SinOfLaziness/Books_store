package org.example.service;

import org.example.model.Book;
import org.example.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(@Autowired(required = false) BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> getAllBooks() {
        if (bookRepository == null) {
            return Collections.emptyList();
        }
        return bookRepository.findAll();
    }

    public Book addBook(String title, String author, Double price, Integer quantity) {
        if (bookRepository == null) {
            return null;
        }
        return bookRepository.save(new Book(title, author, price, quantity));
    }

    // Метод покупки книги: уменьшает количество на указанное число, если достаточно
    public boolean purchaseBook(Long bookId, int quantity) {
        if (bookRepository == null) {
            return false;
        }
        Optional<Book> optionalBook = bookRepository.findById(bookId);
        if (optionalBook.isPresent()) {
            Book book = optionalBook.get();
            if (book.getQuantity() != null && book.getQuantity() >= quantity) {
                book.setQuantity(book.getQuantity() - quantity);
                bookRepository.save(book);
                return true;
            }
        }
        return false;
    }
}
