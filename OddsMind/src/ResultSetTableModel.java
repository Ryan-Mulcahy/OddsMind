// A TableModel that supplies ResultSet data to a JTable.
// Based on lecture code from JDBC slides
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import javax.swing.table.AbstractTableModel;

public class ResultSetTableModel extends AbstractTableModel {

    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    private ResultSetMetaData metaData;
    private int numberOfRows;
    private boolean connectedToDatabase = false;

    // constructor initialises resultSet and obtains its meta data object;
    // determines number of rows
    public ResultSetTableModel( Connection connection, String query ) throws SQLException {
        this.connection = connection;
        statement = connection.createStatement(
            ResultSet.TYPE_SCROLL_INSENSITIVE,
            ResultSet.CONCUR_READ_ONLY );
        connectedToDatabase = true;
        setQuery( query );
    }

    public Class getColumnClass( int column ) throws IllegalStateException {
        if ( !connectedToDatabase )
            throw new IllegalStateException( "Not Connected to Database" );
        try {
            String className = metaData.getColumnClassName( column + 1 );
            // return Class object that represents className
            return Class.forName( className );
        }
        catch ( Exception exception ) {
            exception.printStackTrace();
        }
        return Object.class; // if problems occur above, assume type Object
    }

    public int getColumnCount() throws IllegalStateException {
        if ( !connectedToDatabase )
            throw new IllegalStateException( "Not Connected to Database" );
        try {
            return metaData.getColumnCount();
        }
        catch ( SQLException sqlException ) {
            sqlException.printStackTrace();
        }
        return 0; // if problems occur above, return 0 for number of columns
    }

    public String getColumnName( int column ) throws IllegalStateException {
        if ( !connectedToDatabase )
            throw new IllegalStateException( "Not Connected to Database" );
        try {
            return metaData.getColumnName( column + 1 );
        }
        catch ( SQLException sqlException ) {
            sqlException.printStackTrace();
        }
        return ""; // if problems, return empty string for column name
    }

    public int getRowCount() throws IllegalStateException {
        if ( !connectedToDatabase )
            throw new IllegalStateException( "Not Connected to Database" );
        return numberOfRows;
    }

    public Object getValueAt( int row, int column ) throws IllegalStateException {
        if ( !connectedToDatabase )
            throw new IllegalStateException( "Not Connected to Database" );
        try {
            resultSet.absolute( row + 1 );
            return resultSet.getObject( column + 1 );
        }
        catch ( SQLException sqlException ) {
            sqlException.printStackTrace();
        }
        return ""; // if problems, return empty string object
    }

    public void setQuery( String query ) throws SQLException, IllegalStateException {
        if ( !connectedToDatabase )
            throw new IllegalStateException( "Not Connected to Database" );
        resultSet = statement.executeQuery( query );
        metaData = resultSet.getMetaData();
        resultSet.last();
        numberOfRows = resultSet.getRow();
        fireTableStructureChanged();
    }

    public void disconnectFromDatabase() {
        if ( connectedToDatabase ) {
            try {
                resultSet.close();
                statement.close();
                connection.close();
            }
            catch ( SQLException sqlException ) {
                sqlException.printStackTrace();
            }
            finally {
                connectedToDatabase = false;
            }
        }
    }
} // end class