// CRUD operations for the bets table
// Uses PreparedStatement for safe SQL execution
// Odds stored as fractional format (e.g. 5/1, 6/4, 1/1)
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BetCRUD {

    // calculate potential payout from fractional odds
    // fractional odds e.g. 5/1 means profit = (5/1) * stake, payout = profit + stake
    public static double calculatePayout( String odds, double stake ) {
        String[] parts = odds.split( "/" );
        double numerator = Double.parseDouble( parts[0] );
        double denominator = Double.parseDouble( parts[1] );
        double profit = ( numerator / denominator ) * stake;
        return profit + stake; // return stake + winnings
    }

    // validate fractional odds format (e.g. 5/1, 6/4, 1/1)
    public static boolean isValidOdds( String odds ) {
        if ( odds == null || !odds.contains( "/" ) ) return false;
        try {
            String[] parts = odds.split( "/" );
            if ( parts.length != 2 ) return false;
            Double.parseDouble( parts[0] );
            double denominator = Double.parseDouble( parts[1] );
            if ( denominator == 0 ) return false;
            return true;
        }
        catch ( NumberFormatException e ) {
            return false;
        }
    }

    // CREATE - place a new bet
    public static void addBet( Connection connection, int customerId, String sport,
            String eventName, String selection, String odds, double stake,
            double potentialPayout ) throws SQLException {

        String sql = "INSERT INTO bets ( customer_id, sport, event_name, selection, "
                   + "odds, stake, potential_payout, bet_date, status ) "
                   + "VALUES ( ?, ?, ?, ?, ?, ?, ?, NOW(), 'Open' )";

        PreparedStatement statement = connection.prepareStatement( sql );
        statement.setInt( 1, customerId );
        statement.setString( 2, sport );
        statement.setString( 3, eventName );
        statement.setString( 4, selection );
        statement.setString( 5, odds );
        statement.setDouble( 6, stake );
        statement.setDouble( 7, potentialPayout );
        statement.executeUpdate();
        statement.close();
    }

    // UPDATE - update a bet by bet_id
    public static void updateBet( Connection connection, int betId, String sport,
            String eventName, String selection, String odds, double stake,
            double potentialPayout, String status ) throws SQLException {

        String sql = "UPDATE bets SET sport = ?, event_name = ?, selection = ?, "
                   + "odds = ?, stake = ?, potential_payout = ?, status = ? "
                   + "WHERE bet_id = ?";

        PreparedStatement statement = connection.prepareStatement( sql );
        statement.setString( 1, sport );
        statement.setString( 2, eventName );
        statement.setString( 3, selection );
        statement.setString( 4, odds );
        statement.setDouble( 5, stake );
        statement.setDouble( 6, potentialPayout );
        statement.setString( 7, status );
        statement.setInt( 8, betId );
        statement.executeUpdate();
        statement.close();
    }

    // DELETE - remove a bet by bet_id
    public static void deleteBet( Connection connection, int betId )
            throws SQLException {

        String sql = "DELETE FROM bets WHERE bet_id = ?";

        PreparedStatement statement = connection.prepareStatement( sql );
        statement.setInt( 1, betId );
        statement.executeUpdate();
        statement.close();
    }
} // end class