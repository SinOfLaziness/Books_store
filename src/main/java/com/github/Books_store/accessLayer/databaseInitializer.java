package com.github.Books_store.accessLayer;

import java.sql.*;

// Инициализация базы данных

public class databaseInitializer extends configs {
    private Connection dbConnection;
    private com.github.Books_store.accessLayer.databaseTools databaseTools;

    public databaseInitializer() {
        try {
            dbConnection = getDbConnection();
            databaseTools = new databaseTools(dbConnection);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getDbConnection() throws ClassNotFoundException, SQLException {
        String connectionString = String.format("jdbc:mysql://%s:%s/%s", dbHost, dbPort, dbName);
        Class.forName("com.mysql.cj.jdbc.Driver");
        dbConnection = DriverManager.getConnection(connectionString, dbUser, dbPass);
        return dbConnection;
    }

    public com.github.Books_store.accessLayer.databaseTools getDatabaseTools() {
        return databaseTools;
    }

}