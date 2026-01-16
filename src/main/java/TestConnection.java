import java.sql.Connection;
import java.sql.DriverManager;

public class TestConnection {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/mini_dish_db";
        String user = "mini_dish_db_manager";
        String password = "test";

        System.out.println("Tentative de connexion à : " + url);
        System.out.println("Utilisateur : " + user);

        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("✓ Driver PostgreSQL chargé");

            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("✓ Connexion réussie !");

            conn.close();
        } catch (Exception e) {
            System.err.println("✗ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}