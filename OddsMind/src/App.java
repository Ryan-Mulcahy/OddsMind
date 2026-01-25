import java.sql.Connection;

public class App {
    public static void main(String[] args) {
        try (Connection c = myJDBC.getConnection()) {
            System.out.println("✅ Database connection successful!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
