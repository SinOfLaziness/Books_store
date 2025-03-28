package org.example.model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Корзина принадлежит пользователю
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Статус корзины, например, "ACTIVE" или "COMPLETED"
    private String status;

    // Элементы корзины
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items;

    public Cart() {}

    public Cart(User user, String status) {
        this.user = user;
        this.status = status;
    }

    // Getters и Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public List<CartItem> getItems() {
        return items;
    }
    public void setItems(List<CartItem> items) {
        this.items = items;
    }
}
