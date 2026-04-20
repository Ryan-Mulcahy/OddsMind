// CRUD operations for the aml_verifications table
// Tracks customers verified under Anti-Money Laundering rules
// Uses PreparedStatement for safe SQL execution
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AmlCRUD {

    // CREATE - record a new AML verification for a customer
    public static void addVerification( Connection connection, int customerId,
            int triggeringBetId, String verifiedBy ) throws SQLException {

        String sql = "INSERT INTO aml_verifications ( customer_id, triggering_bet_id, "
                   + "verified_by, verified_date ) VALUES ( ?, ?, ?, NOW() )";

        PreparedStatement statement = connection.prepareStatement( sql );
        statement.setInt( 1, customerId );
        statement.setInt( 2, triggeringBetId );
        statement.setString( 3, verifiedBy );
        statement.executeUpdate();
        statement.close();
    }

    // READ - check if a customer has already been AML verified
    public static boolean isVerified( Connection connection, int customerId )
            throws SQLException {

        String sql = "SELECT COUNT(*) AS verified FROM aml_verifications "
                   + "WHERE customer_id = ?";

        PreparedStatement statement = connection.prepareStatement( sql );
        statement.setInt( 1, customerId );
        ResultSet rs = statement.executeQuery();

        boolean verified = false;
        if ( rs.next() ) {
            verified = rs.getInt( "verified" ) > 0;
        }
        rs.close();
        statement.close();
        return verified;
    }

    // UPDATE - update verification details (e.g. re-verified by different staff)
    public static void updateVerification( Connection connection, int verificationId,
            String verifiedBy ) throws SQLException {

        String sql = "UPDATE aml_verifications SET verified_by = ?, "
                   + "verified_date = NOW() WHERE verification_id = ?";

        PreparedStatement statement = connection.prepareStatement( sql );
        statement.setString( 1, verifiedBy );
        statement.setInt( 2, verificationId );
        statement.executeUpdate();
        statement.close();
    }

    // DELETE - remove a verification record
    public static void deleteVerification( Connection connection, int verificationId )
            throws SQLException {

        String sql = "DELETE FROM aml_verifications WHERE verification_id = ?";

        PreparedStatement statement = connection.prepareStatement( sql );
        statement.setInt( 1, verificationId );
        statement.executeUpdate();
        statement.close();
    }
} // end class