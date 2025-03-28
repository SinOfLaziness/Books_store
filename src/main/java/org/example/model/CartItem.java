package org.example.model;

import javax.persistence.*;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ссылка на корзину
    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    // Ссылка на книгу
    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    // Количество выбранных экземпляров книги
    private Integer quantity;

    public CartItem() {}

    public CartItem(Cart cart, Book book, Integer quantity) {
        this.cart = cart;
        this.book = book;
        this.quantity = quantity;
    }

    // Getters и Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Cart getCart() {
        return cart;
    }
    public void setCart(Cart cart) {
        this.cart = cart;
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
}
