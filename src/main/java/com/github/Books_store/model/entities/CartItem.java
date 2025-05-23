package com.github.Books_store.model.entities;

public class CartItem {
    private final int bookId;
    private final String bookTitle;
    private final int quantity;

    public CartItem(int bookId, String bookTitle, int quantity) {
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.quantity = quantity;
    }

    public int getBookId() {
        return bookId;
    }
    public String getBookTitle() {
        return bookTitle;
    }
    public int getQuantity() {
        return quantity;
    }
}
