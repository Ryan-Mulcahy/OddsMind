import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import io.github.cdimascio.dotenv.Dotenv;

public class myJDBC {
    private static String clean(String s) {
        if (s == null) return null;
        return s.trim().replace("\"", "");
    }

    public static Connection getConnection() throws SQLException {
        Dotenv dotenv = Dotenv.load();

        String url = clean(dotenv.get("SQL_URL"));
        String user = clean(dotenv.get("SQL_USERNAME"));
        String pass = clean(dotenv.get("SQL_PASSWORD"));

        // Helpful debug (remove later)
        System.out.println("ENV URL=[" + url + "]");

        return DriverManager.getConnection(url, user, pass);
    }
}
