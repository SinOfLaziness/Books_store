package com.github.Books_store.model.entities;

import java.sql.Timestamp;

public class Purchase {
    private final int bookId;
    private final String bookTitle;
    private final int quantity;
    private final Timestamp purchaseDate;

    public Purchase(int bookId, String bookTitle, int quantity, Timestamp purchaseDate) {
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.quantity = quantity;
        this.purchaseDate = purchaseDate;
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
    public Timestamp getPurchaseDate() {
        return purchaseDate;
    }
}
