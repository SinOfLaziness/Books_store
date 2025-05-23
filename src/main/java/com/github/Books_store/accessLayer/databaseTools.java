package com.github.Books_store.accessLayer;

import com.github.Books_store.model.entities.Book;
import com.github.Books_store.model.entities.CartItem;
import com.github.Books_store.model.entities.Purchase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class databaseTools {
    private final Connection db;

    public databaseTools(Connection dbConnection) {
        this.db = dbConnection;
    }

    // Проверка, привязан ли пользователь соцсетью socNet ("tg" или "vk")
    public boolean checkIfSigned(Long userId, String socNet) throws SQLException {
        String col = socNet.equals("tg") ? constantDB.TG_ID : constantDB.VK_ID;
        String sql = "SELECT COUNT(*) FROM " + constantDB.USERS_TABLE + " WHERE " + col + " = ?";
        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    // Регистрация: если логин есть — обновляем telegram_id/vk_id, иначе вставляем новую строку
    public void signUpUser(Long userId, String login, String pass, String socNet) throws SQLException {
        String col = socNet.equals("tg") ? constantDB.TG_ID : constantDB.VK_ID;

        // сначала попробуем обновить по логину
        String upd = String.format(
                "UPDATE %s SET %s = ? WHERE %s = ? AND %s = ?",
                constantDB.USERS_TABLE, col, constantDB.LOGIN, constantDB.PASSWORD
        );
        try (PreparedStatement ps = db.prepareStatement(upd)) {
            ps.setLong(1, userId);
            ps.setString(2, login);
            ps.setString(3, pass);
            int affected = ps.executeUpdate();
            if (affected > 0) return;  // обновили существующего

        }

        // если нет строки с таким логином+паролем — вставляем новую
        String ins = String.format(
                "INSERT INTO %s (%s,%s,%s,%s) VALUES (?,?,?,?)",
                constantDB.USERS_TABLE,
                constantDB.LOGIN,
                constantDB.PASSWORD,
                constantDB.TG_ID,
                constantDB.VK_ID
        );
        try (PreparedStatement ps = db.prepareStatement(ins)) {
            ps.setString(1, login);
            ps.setString(2, pass);
            if (socNet.equals("tg")) {
                ps.setLong(3, userId);
                ps.setNull(4, Types.BIGINT);
            } else {
                ps.setNull(3, Types.BIGINT);
                ps.setLong(4, userId);
            }
            ps.executeUpdate();
        }
    }
    public boolean addToCart(String login, int bookId, int quantity) throws SQLException {
        if (quantity <= 0) return false;

        // 1. Получаем остаток на складе
        String stockSql = "SELECT stock FROM books WHERE id = ?";
        int stock = 0;
        try (PreparedStatement psStock = db.prepareStatement(stockSql)) {
            psStock.setInt(1, bookId);
            try (ResultSet rs = psStock.executeQuery()) {
                if (rs.next()) {
                    stock = rs.getInt("stock");
                } else {
                    return false; // Книга не найдена
                }
            }
        }

        String selectSql = "SELECT quantity FROM cart WHERE user_login = ? AND book_id = ?";
        String insertSql = "INSERT INTO cart (user_login, book_id, quantity) VALUES (?, ?, ?)";
        String updateSql = "UPDATE cart SET quantity = quantity + ? WHERE user_login = ? AND book_id = ?";

        try {
            db.setAutoCommit(false);

            int existingQty = 0;
            try (PreparedStatement psSelect = db.prepareStatement(selectSql)) {
                psSelect.setString(1, login);
                psSelect.setInt(2, bookId);
                try (ResultSet rs = psSelect.executeQuery()) {
                    if (rs.next()) {
                        existingQty = rs.getInt("quantity");
                    }
                }
            }

            // 2. Проверяем, хватает ли stock
            int newQty = existingQty + quantity;
            if (newQty > stock) {
                db.rollback();
                return false; // Недостаточно книг на складе
            }

            // 3. Добавляем или обновляем корзину
            if (existingQty == 0) {
                try (PreparedStatement psInsert = db.prepareStatement(insertSql)) {
                    psInsert.setString(1, login);
                    psInsert.setInt(2, bookId);
                    psInsert.setInt(3, quantity);
                    psInsert.executeUpdate();
                }
            } else {
                try (PreparedStatement psUpdate = db.prepareStatement(updateSql)) {
                    psUpdate.setInt(1, quantity);
                    psUpdate.setString(2, login);
                    psUpdate.setInt(3, bookId);
                    psUpdate.executeUpdate();
                }
            }

            db.commit();
            return true;

        } catch (SQLException e) {
            db.rollback();
            throw e;
        } finally {
            db.setAutoCommit(true);
        }
    }


    // Получить список товаров в корзине пользователя
    public List<CartItem> getUserCart(String login) throws SQLException {
        String sql = "SELECT c.book_id, b.title, c.quantity FROM cart c JOIN books b ON c.book_id = b.id WHERE c.user_login = ?";
        List<CartItem> cartItems = new ArrayList<>();
        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cartItems.add(new CartItem(
                            rs.getInt("book_id"),
                            rs.getString("title"),
                            rs.getInt("quantity")
                    ));
                }
            }
        }
        return cartItems;
    }

    // Оформить покупку всей корзины
    public boolean purchaseCart(String login) throws SQLException {
        String selectCartSql = "SELECT book_id, quantity FROM cart WHERE user_login = ?";
        String selectStockSql = "SELECT stock FROM books WHERE id = ? FOR UPDATE";
        String updateStockSql = "UPDATE books SET stock = stock - ? WHERE id = ?";
        String insertPurchaseSql = "INSERT INTO purchases (user_login, book_id, quantity) VALUES (?, ?, ?)";
        String updatePurchaseSql = "UPDATE purchases SET quantity = quantity + ? WHERE user_login = ? AND book_id = ?";
        String deleteCartSql = "DELETE FROM cart WHERE user_login = ?";

        try {
            db.setAutoCommit(false);

            List<CartItem> cartItems = new ArrayList<>();

            try (PreparedStatement psCart = db.prepareStatement(selectCartSql)) {
                psCart.setString(1, login);
                try (ResultSet rs = psCart.executeQuery()) {
                    while (rs.next()) {
                        cartItems.add(new CartItem(rs.getInt("book_id"), "", rs.getInt("quantity")));
                    }
                }
            }

            if (cartItems.isEmpty()) {
                db.rollback();
                return false; // Корзина пуста
            }

            // Проверяем остатки
            for (CartItem item : cartItems) {
                try (PreparedStatement psStock = db.prepareStatement(selectStockSql)) {
                    psStock.setInt(1, item.getBookId());
                    try (ResultSet rs = psStock.executeQuery()) {
                        if (!rs.next() || rs.getInt("stock") < item.getQuantity()) {
                            db.rollback();
                            return false; // Недостаточно товара
                        }
                    }
                }
            }

            // Обновляем stock и добавляем в покупки
            for (CartItem item : cartItems) {
                try (PreparedStatement psUpdateStock = db.prepareStatement(updateStockSql)) {
                    psUpdateStock.setInt(1, item.getQuantity());
                    psUpdateStock.setInt(2, item.getBookId());
                    psUpdateStock.executeUpdate();
                }

                // Проверяем есть ли уже запись о покупке
                int existingQty = 0;
                try (PreparedStatement psCheckPurchase = db.prepareStatement("SELECT quantity FROM purchases WHERE user_login = ? AND book_id = ?")) {
                    psCheckPurchase.setString(1, login);
                    psCheckPurchase.setInt(2, item.getBookId());
                    try (ResultSet rs = psCheckPurchase.executeQuery()) {
                        if (rs.next()) {
                            existingQty = rs.getInt("quantity");
                        }
                    }
                }

                if (existingQty == 0) {
                    try (PreparedStatement psInsertPurchase = db.prepareStatement(insertPurchaseSql)) {
                        psInsertPurchase.setString(1, login);
                        psInsertPurchase.setInt(2, item.getBookId());
                        psInsertPurchase.setInt(3, item.getQuantity());
                        psInsertPurchase.executeUpdate();
                    }
                } else {
                    try (PreparedStatement psUpdatePurchase = db.prepareStatement(updatePurchaseSql)) {
                        psUpdatePurchase.setInt(1, item.getQuantity());
                        psUpdatePurchase.setString(2, login);
                        psUpdatePurchase.setInt(3, item.getBookId());
                        psUpdatePurchase.executeUpdate();
                    }
                }
            }

            // Очищаем корзину
            try (PreparedStatement psDeleteCart = db.prepareStatement(deleteCartSql)) {
                psDeleteCart.setString(1, login);
                psDeleteCart.executeUpdate();
            }

            db.commit();
            return true;
        } catch (SQLException e) {
            db.rollback();
            throw e;
        } finally {
            db.setAutoCommit(true);
        }
    }

    // Очистить корзину
    public boolean clearCart(String login) throws SQLException {
        String sql = "DELETE FROM cart WHERE user_login = ?";
        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setString(1, login);
            int affected = ps.executeUpdate();
            return affected > 0;
        }
    }

    // Получить login по userId и socNet
    public String getLoginByUserId(Long userId, String socNet) throws SQLException {
        String col = socNet.equals("tg") ? "telegram_id" : "vk_id";
        String sql = "SELECT login FROM comp_users WHERE " + col + " = ?";
        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("login");
                } else {
                    return null;
                }
            }
        }
    }

    // Разлогинить: обнуляем соответствующее поле
    public void unlogging(Long userId, String socNet) throws SQLException {
        String col = socNet.equals("tg") ? constantDB.TG_ID : constantDB.VK_ID;
        String sql = String.format("UPDATE %s SET %s = NULL WHERE %s = ?",
                constantDB.USERS_TABLE, col, col);
        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }
    }
    // Класс Book, CartItem, Purchase создайте согласно вашим нуждам

    // Получить login пользователя по userId и соцсети
    public List<Book> getBooksList() throws SQLException {
        String sql = "SELECT id, title, stock FROM books";
        List<Book> books = new ArrayList<>();
        try (Statement st = db.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                books.add(new Book(rs.getInt("id"), rs.getString("title"), rs.getInt("stock")));
            }
        }
        return books;
    }

    public List<Purchase> getUserPurchases(String login) throws SQLException {
        String sql = "SELECT p.book_id, b.title, p.quantity, p.purchase_date FROM purchases p JOIN books b ON p.book_id = b.id WHERE p.user_login = ?";
        List<Purchase> purchases = new ArrayList<>();
        try (PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    purchases.add(new Purchase(rs.getInt("book_id"), rs.getString("title"), rs.getInt("quantity"), rs.getTimestamp("purchase_date")));
                }
            }
        }
        return purchases;
    }

    // Операция покупки
    public boolean buyBook(String login, int bookId) throws SQLException {
        try {
            db.setAutoCommit(false);

            // Проверка остатка на складе
            String sqlCheck = "SELECT stock FROM books WHERE id = ? FOR UPDATE";
            int stock;
            try (PreparedStatement ps = db.prepareStatement(sqlCheck)) {
                ps.setInt(1, bookId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        db.rollback();
                        return false; // Книга не найдена
                    }
                    stock = rs.getInt("stock");
                    if (stock <= 0) {
                        db.rollback();
                        return false; // Нет в наличии
                    }
                }
            }

            // Уменьшить stock
            String sqlUpdateStock = "UPDATE books SET stock = stock - 1 WHERE id = ?";
            try (PreparedStatement ps = db.prepareStatement(sqlUpdateStock)) {
                ps.setInt(1, bookId);
                ps.executeUpdate();
            }

            // Добавить в purchases (если уже есть, увеличить количество)
            String sqlCheckPurchase = "SELECT quantity FROM purchases WHERE user_login = ? AND book_id = ?";
            Integer oldQty = null;
            try (PreparedStatement ps = db.prepareStatement(sqlCheckPurchase)) {
                ps.setString(1, login);
                ps.setInt(2, bookId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) oldQty = rs.getInt("quantity");
                }
            }

            if (oldQty == null) {
                String sqlInsertPurchase = "INSERT INTO purchases (user_login, book_id, quantity) VALUES (?, ?, 1)";
                try (PreparedStatement ps = db.prepareStatement(sqlInsertPurchase)) {
                    ps.setString(1, login);
                    ps.setInt(2, bookId);
                    ps.executeUpdate();
                }
            } else {
                String sqlUpdatePurchase = "UPDATE purchases SET quantity = quantity + 1 WHERE user_login = ? AND book_id = ?";
                try (PreparedStatement ps = db.prepareStatement(sqlUpdatePurchase)) {
                    ps.setString(1, login);
                    ps.setInt(2, bookId);
                    ps.executeUpdate();
                }
            }

            // Удалить из корзины (уменьшить или удалить)
            String sqlCheckCart = "SELECT quantity FROM cart WHERE user_login = ? AND book_id = ?";
            Integer cartQty = null;
            try (PreparedStatement ps = db.prepareStatement(sqlCheckCart)) {
                ps.setString(1, login);
                ps.setInt(2, bookId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) cartQty = rs.getInt("quantity");
                }
            }

            if (cartQty != null) {
                if (cartQty > 1) {
                    String sqlUpdateCart = "UPDATE cart SET quantity = quantity - 1 WHERE user_login = ? AND book_id = ?";
                    try (PreparedStatement ps = db.prepareStatement(sqlUpdateCart)) {
                        ps.setString(1, login);
                        ps.setInt(2, bookId);
                        ps.executeUpdate();
                    }
                } else {
                    String sqlDeleteCart = "DELETE FROM cart WHERE user_login = ? AND book_id = ?";
                    try (PreparedStatement ps = db.prepareStatement(sqlDeleteCart)) {
                        ps.setString(1, login);
                        ps.setInt(2, bookId);
                        ps.executeUpdate();
                    }
                }
            }

            db.commit();
            return true;
        } catch (SQLException e) {
            db.rollback();
            throw e;
        } finally {
            db.setAutoCommit(true);
        }
    }

}
