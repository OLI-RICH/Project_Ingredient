package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public Connection getDBConnection() {

        String jdbcUrl = System.getenv("JDBC_URL");
        String username = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");


        if (jdbcUrl == null) {
            jdbcUrl = "jdbc:postgresql://localhost:5432/mini_dish_db";
        }
        if (username == null) {
            username = "mini_dish_db_manager";
        }
        if (password == null) {
            password = "test";
        }

        try {
            return DriverManager.getConnection(jdbcUrl, username, password);
        } catch (SQLException e) {
            System.err.println("=== ERREUR DE CONNEXION ===");
            System.err.println("JDBC URL: " + jdbcUrl);
            System.err.println("Username: " + username);
            System.err.println("Message: " + e.getMessage());
            throw new RuntimeException("Erreur de connexion à la base de données: " + e.getMessage(), e);
        }
    }

    public void attemptCloseConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
            }
        }
    }
}