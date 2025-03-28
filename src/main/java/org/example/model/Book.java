package org.example.model;

import javax.persistence.*;

@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String author;
    private Double price;
    private Integer quantity;

    public Book() {
    }

    // Конструктор на 3 аргумента (уже есть)
    public Book(String title, String author, Double price) {
        this.title = title;
        this.author = author;
        this.price = price;
        this.quantity = 0; // или другое значение по умолчанию
    }

    // Добавьте вот такой конструктор, если хотите передавать quantity
    public Book(String title, String author, Double price, Integer quantity) {
        this.title = title;
        this.author = author;
        this.price = price;
        this.quantity = quantity;
    }


    // Getters и Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public Double getPrice() {
        return price;
    }
    public void setPrice(Double price) {
        this.price = price;
    }
    public Integer getQuantity() {
        return quantity;
    }
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
