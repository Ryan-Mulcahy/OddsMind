// Database connection class for OddsMinds
// Based on slides (JDBC slide 50)
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class myJDBC {

    // database connection details
    static final String DATABASE_URL = "jdbc:mysql://localhost:3306/oddsmind";
    static final String USERNAME = "root";
    static final String PASSWORD = "R0kOOSDdad!!";

    // get a connection to the database
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection( DATABASE_URL, USERNAME, PASSWORD );
    }
}