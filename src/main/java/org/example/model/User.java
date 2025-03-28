package org.example.model;

import javax.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Уникальный chatId Telegram.
     * Можно хранить как Long (совпадает с update.getMessage().getChatId())
     */
    @Column(name = "chat_id", unique = true, nullable = false)
    private Long chatId;

    /**
     * Логин/имя пользователя. Для примера — можно не использовать.
     */
    @Column(nullable = false)
    private String username;

    /**
     * Пароль, для учебного примера можно не шифровать.
     */
    @Column(nullable = false)
    private String password;

    public User() {}

    public User(Long chatId, String username, String password) {
        this.chatId = chatId;
        this.username = username;
        this.password = password;
    }

    // ===== GETTERS / SETTERS =====

    public Long getId() {
        return id;
    }

    public Long getChatId() {
        return chatId;
    }
    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
