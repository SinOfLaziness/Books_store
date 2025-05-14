package com.github.Books_store.model.entities;

public class Book {
    private final int id;
    private final String title;
    private final int stock;

    public Book(int id, String title, int stock) {
        this.id = id;
        this.title = title;
        this.stock = stock;
    }

    public int getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public int getStock() {
        return stock;
    }
}
