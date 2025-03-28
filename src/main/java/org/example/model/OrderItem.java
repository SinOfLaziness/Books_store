package org.example.model;

import javax.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ссылка на заказ
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    // Ссылка на книгу
    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    // Количество купленных книг
    private Integer quantity;

    // Цена книги на момент покупки
    private Double priceAtPurchase;

    public OrderItem() {}

    public OrderItem(Order order, Book book, Integer quantity, Double priceAtPurchase) {
        this.order = order;
        this.book = book;
        this.quantity = quantity;
        this.priceAtPurchase = priceAtPurchase;
    }

    // Getters и Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Order getOrder() {
        return order;
    }
    public void setOrder(Order order) {
        this.order = order;
    }
    public Book getBook() {
        return book;
    }
    public void setBook(Book book) {
        this.book = book;
    }
    public Integer getQuantity() {
        return quantity;
    }
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    public Double getPriceAtPurchase() {
        return priceAtPurchase;
    }
    public void setPriceAtPurchase(Double priceAtPurchase) {
        this.priceAtPurchase = priceAtPurchase;
    }
}
