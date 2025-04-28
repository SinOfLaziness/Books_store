package com.github.Books_store.AccessLayer;

import java.sql.*;

// Инициализация базы данных

public class DatabaseInitializer extends Configs {
    private Connection dbConnection;
    private DatabaseTools databaseTools;

    public DatabaseInitializer() {
        try {
            dbConnection = getDbConnection();
            databaseTools = new DatabaseTools(dbConnection);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection getDbConnection() throws ClassNotFoundException, SQLException {
        String connectionString = String.format("jdbc:mysql://%s:%s/%s", dbHost, dbPort, dbName);
        Class.forName("com.mysql.cj.jdbc.Driver");
        dbConnection = DriverManager.getConnection(connectionString, dbUser, dbPass);
        return dbConnection;
    }

    public DatabaseTools getDatabaseTools() {
        return databaseTools;
    }

}