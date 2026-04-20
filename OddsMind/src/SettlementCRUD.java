// CRUD operations for the settlements table
// Uses PreparedStatement for safe SQL execution
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SettlementCRUD {

    // CREATE - settle a bet
    public static void addSettlement( Connection connection, int betId,
            String result, double payout ) throws SQLException {

        String sql = "INSERT INTO settlements ( bet_id, result, payout, settled_date ) "
                   + "VALUES ( ?, ?, ?, NOW() )";

        PreparedStatement statement = connection.prepareStatement( sql );
        statement.setInt( 1, betId );
        statement.setString( 2, result );
        statement.setDouble( 3, payout );
        statement.executeUpdate();
        statement.close();

        // also update the bet status to match the result
        String updateBetSql = "UPDATE bets SET status = ? WHERE bet_id = ?";
        PreparedStatement updateStatement = connection.prepareStatement( updateBetSql );
        updateStatement.setString( 1, result );
        updateStatement.setInt( 2, betId );
        updateStatement.executeUpdate();
        updateStatement.close();
    }

    // UPDATE - update a settlement by settlement_id
    public static void updateSettlement( Connection connection, int settlementId,
            String result, double payout ) throws SQLException {

        String sql = "UPDATE settlements SET result = ?, payout = ?, "
                   + "settled_date = NOW() WHERE settlement_id = ?";

        PreparedStatement statement = connection.prepareStatement( sql );
        statement.setString( 1, result );
        statement.setDouble( 2, payout );
        statement.setInt( 3, settlementId );
        statement.executeUpdate();
        statement.close();
    }

    // DELETE - remove a settlement by settlement_id
    public static void deleteSettlement( Connection connection, int settlementId )
            throws SQLException {

        String sql = "DELETE FROM settlements WHERE settlement_id = ?";

        PreparedStatement statement = connection.prepareStatement( sql );
        statement.setInt( 1, settlementId );
        statement.executeUpdate();
        statement.close();
    }
} // end class