package com.github.Books_store.accessLayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Функционал базы данных

public class databaseTools {
    private final Connection dbConnection;

    public databaseTools(Connection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public boolean checkIfSigned(Long chatID) throws SQLException {
        int counter = 0;
        try (ResultSet result = getUserCount(chatID)) {
            if (result.next()) {
                counter = result.getInt(1);
            }
        }
        return counter >= 1;
    }

    private ResultSet getUserCount(Long chatID) {
        ResultSet resultSet = null;
        String insert = "SELECT COUNT(*) FROM " + constantDB.USERS_TABLE + " WHERE " + constantDB.TG_ID + "=?";
        try {
            PreparedStatement prSt = dbConnection.prepareStatement(insert);
            prSt.setLong(1, chatID);
            resultSet = prSt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultSet;
    }

    public void signUpUser(Long telegramID) {
        String insert = String.format("INSERT INTO %s(%s) VALUES (?)",
                constantDB.USERS_TABLE, constantDB.TG_ID);
        try (PreparedStatement prSt = dbConnection.prepareStatement(insert)) {
            prSt.setLong(1, telegramID);
            prSt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
