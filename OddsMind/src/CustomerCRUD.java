// CRUD operations for the customers table
// Uses PreparedStatement for safe SQL execution
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CustomerCRUD {

    // CREATE - add a new customer
    public static void addCustomer( Connection connection, String firstName,
            String lastName, String email, String phone, String bettingType )
            throws SQLException {

        String sql = "INSERT INTO customers ( first_name, last_name, email, phone, betting_type ) "
                   + "VALUES ( ?, ?, ?, ?, ? )";

        PreparedStatement statement = connection.prepareStatement( sql );
        statement.setString( 1, firstName );
        statement.setString( 2, lastName );
        statement.setString( 3, email );
        statement.setString( 4, phone );
        statement.setString( 5, bettingType );
        statement.executeUpdate();
        statement.close();
    }

    // UPDATE - update an existing customer by customer_id
    public static void updateCustomer( Connection connection, int customerId,
            String firstName, String lastName, String email, String phone,
            String bettingType ) throws SQLException {

        String sql = "UPDATE customers SET first_name = ?, last_name = ?, "
                   + "email = ?, phone = ?, betting_type = ? WHERE customer_id = ?";

        PreparedStatement statement = connection.prepareStatement( sql );
        statement.setString( 1, firstName );
        statement.setString( 2, lastName );
        statement.setString( 3, email );
        statement.setString( 4, phone );
        statement.setString( 5, bettingType );
        statement.setInt( 6, customerId );
        statement.executeUpdate();
        statement.close();
    }

    // DELETE - remove a customer by customer_id
    public static void deleteCustomer( Connection connection, int customerId )
            throws SQLException {

        String sql = "DELETE FROM customers WHERE customer_id = ?";

        PreparedStatement statement = connection.prepareStatement( sql );
        statement.setInt( 1, customerId );
        statement.executeUpdate();
        statement.close();
    }
} // end class