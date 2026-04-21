// OddsMinds - Betting Management Application
// GUI based on DisplayQueryResults from  slides (JDBC slides 49-54)
// Uses JFrame, JTable, JButton, JTextField, JComboBox, JList, BorderLayout, ActionListener
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableModel;
import javax.swing.RowFilter;
import javax.swing.BorderFactory;
import java.util.regex.PatternSyntaxException;
import java.util.ArrayList;

public class OddsMinds extends JFrame {

    // default queries for each table
    static final String CUSTOMER_QUERY = "SELECT * FROM customers";
    static final String BET_QUERY = "SELECT * FROM bets";
    static final String SETTLEMENT_QUERY = "SELECT * FROM settlements";

    // AML inner join query - flags customers with payouts over 2000
    // uses inner join across all 3 tables (customers, bets, settlements)
    // also shows if the customer has been verified in the aml_verifications table
    static final String AML_QUERY =
        "SELECT c.customer_id, c.first_name, c.last_name, c.email, c.phone, "
        + "b.bet_id, b.sport, b.event_name, b.odds, b.stake, "
        + "s.result, s.payout, s.settled_date, "
        + "CASE WHEN a.verification_id IS NOT NULL THEN 'Verified' ELSE 'UNVERIFIED' END AS aml_status, "
        + "a.verified_by, a.verified_date AS verification_date "
        + "FROM customers c "
        + "INNER JOIN bets b ON c.customer_id = b.customer_id "
        + "INNER JOIN settlements s ON b.bet_id = s.bet_id "
        + "LEFT JOIN aml_verifications a ON c.customer_id = a.customer_id "
        + "WHERE s.payout > 2000 "
        + "ORDER BY aml_status DESC, s.payout DESC";

    // AML verifications table query
    static final String AML_VERIFICATIONS_QUERY = "SELECT * FROM aml_verifications";

    // AML threshold - any payout over this amount requires verification
    static final double AML_THRESHOLD = 2000.00;

    // staff login credentials (hardcoded)
    static final String STAFF_USERNAME = "admin";
    static final String STAFF_PASSWORD = "admin123";

    // Ladbrokes colour scheme
    static final Color LADBROKES_RED = new Color( 204, 0, 0 );
    static final Color LADBROKES_DARK_RED = new Color( 153, 0, 0 );
    static final Color LADBROKES_WHITE = Color.WHITE;
    static final Color LADBROKES_LIGHT_GREY = new Color( 240, 240, 240 );

    private ResultSetTableModel tableModel;
    private JTable resultTable;
    private Connection connection;
    private TableRowSorter< TableModel > sorter;

    // current view tracking
    private String currentTable = "customers";
    private String currentQuery = CUSTOMER_QUERY;

    // main content panel
    private JPanel mainPanel;
    private JPanel welcomePanel;

    public OddsMinds() {
        super( "OddsMinds - Betting Management" );

        try {
            // get connection using myJDBC
            connection = myJDBC.getConnection();

            // create ResultSetTableModel with default query
            tableModel = new ResultSetTableModel( connection, CUSTOMER_QUERY );

            // set up the main content panel
            mainPanel = new JPanel( new BorderLayout() );

            // build the welcome screen
            buildWelcomePanel();

            // start with the welcome screen
            add( mainPanel, BorderLayout.CENTER );
            showWelcomeScreen();

            // dispose of window when user quits (from lecture slide 54)
            setDefaultCloseOperation( DISPOSE_ON_CLOSE );
            addWindowListener(
                new WindowAdapter() {
                    public void windowClosed( WindowEvent event ) {
                        tableModel.disconnectFromDatabase();
                        System.exit( 0 );
                    }
                }
            );

            setSize( 900, 600 );
            setLocationRelativeTo( null );
            setVisible( true );

        }
        catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog( null, sqlException.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE );
            System.exit( 1 );
        }
    }


    // STYLE HELPER METHODS

    private void styleButton( JButton button ) {
        button.setBackground( LADBROKES_RED );
        button.setForeground( LADBROKES_WHITE );
        button.setFocusPainted( false );
        button.setFont( new Font( "SansSerif", Font.BOLD, 14 ) );
        button.setBorder( BorderFactory.createEmptyBorder( 10, 20, 10, 20 ) );
        button.setOpaque( true );
    }

    private void styleSmallButton( JButton button ) {
        button.setBackground( LADBROKES_RED );
        button.setForeground( LADBROKES_WHITE );
        button.setFocusPainted( false );
        button.setFont( new Font( "SansSerif", Font.BOLD, 12 ) );
        button.setBorder( BorderFactory.createEmptyBorder( 5, 15, 5, 15 ) );
        button.setOpaque( true );
    }

    private void styleWelcomeButton( JButton button ) {
        button.setAlignmentX( JButton.CENTER_ALIGNMENT );
        button.setBackground( LADBROKES_WHITE );
        button.setForeground( LADBROKES_RED );
        button.setFont( new Font( "SansSerif", Font.BOLD, 18 ) );
        button.setFocusPainted( false );
        button.setBorder( BorderFactory.createEmptyBorder( 12, 40, 12, 40 ) );
        button.setOpaque( true );
        button.setMaximumSize( new Dimension( 300, 50 ) );
    }


    // WELCOME SCREEN

    private void buildWelcomePanel() {
        welcomePanel = new JPanel();
        welcomePanel.setLayout( new BoxLayout( welcomePanel, BoxLayout.Y_AXIS ) );
        welcomePanel.setBackground( LADBROKES_RED );

        // title label
        JLabel titleLabel = new JLabel( "OddsMinds" );
        titleLabel.setFont( new Font( "SansSerif", Font.BOLD, 48 ) );
        titleLabel.setForeground( LADBROKES_WHITE );
        titleLabel.setAlignmentX( JLabel.CENTER_ALIGNMENT );

        JLabel subtitleLabel = new JLabel( "Betting Management System" );
        subtitleLabel.setFont( new Font( "SansSerif", Font.PLAIN, 20 ) );
        subtitleLabel.setForeground( LADBROKES_WHITE );
        subtitleLabel.setAlignmentX( JLabel.CENTER_ALIGNMENT );

        // navigation buttons
        JButton placeBetButton = new JButton( "Place a Bet" );
        styleWelcomeButton( placeBetButton );

        JButton staffLoginButton = new JButton( "Staff Login" );
        styleWelcomeButton( staffLoginButton );

        // add spacing and components
        welcomePanel.add( Box.createVerticalGlue() );
        welcomePanel.add( titleLabel );
        welcomePanel.add( Box.createVerticalStrut( 10 ) );
        welcomePanel.add( subtitleLabel );
        welcomePanel.add( Box.createVerticalStrut( 50 ) );
        welcomePanel.add( placeBetButton );
        welcomePanel.add( Box.createVerticalStrut( 20 ) );
        welcomePanel.add( staffLoginButton );
        welcomePanel.add( Box.createVerticalStrut( 15 ) );

        JLabel infoLabel = new JLabel( "Staff login required for Customer Management, Bets & Settlements" );
        infoLabel.setFont( new Font( "SansSerif", Font.ITALIC, 12 ) );
        infoLabel.setForeground( new Color( 255, 200, 200 ) );
        infoLabel.setAlignmentX( JLabel.CENTER_ALIGNMENT );
        welcomePanel.add( infoLabel );

        welcomePanel.add( Box.createVerticalGlue() );

        // ActionListeners (anonymous inner class from lecture slides)
        placeBetButton.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent event ) {
                    showPlaceBetScreen();
                }
            }
        );

        staffLoginButton.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent event ) {
                    handleStaffLogin();
                }
            }
        );
    }


    // SHOW SCREENS

    private void showWelcomeScreen() {
        mainPanel.removeAll();
        mainPanel.add( welcomePanel, BorderLayout.CENTER );
        mainPanel.revalidate();
        mainPanel.repaint();
    }


    // PLACE BET SCREEN - Customer facing, guided form
    // No table shown - just like walking up to the counter

    private void showPlaceBetScreen() {
        mainPanel.removeAll();

        JPanel betPanel = new JPanel();
        betPanel.setLayout( new BoxLayout( betPanel, BoxLayout.Y_AXIS ) );
        betPanel.setBackground( LADBROKES_RED );

        // header
        JPanel headerPanel = new JPanel( new BorderLayout() );
        headerPanel.setBackground( LADBROKES_RED );
        headerPanel.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

        JButton backButton = new JButton( "Back to Menu" );
        styleSmallButton( backButton );
        backButton.setBackground( LADBROKES_DARK_RED );

        JLabel headerLabel = new JLabel( "   Place a Bet" );
        headerLabel.setFont( new Font( "SansSerif", Font.BOLD, 24 ) );
        headerLabel.setForeground( LADBROKES_WHITE );

        headerPanel.add( backButton, BorderLayout.WEST );
        headerPanel.add( headerLabel, BorderLayout.CENTER );

        backButton.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent event ) {
                    showWelcomeScreen();
                }
            }
        );

        // form panel - white card in the centre
        JPanel formCard = new JPanel();
        formCard.setLayout( new GridLayout( 8, 2, 10, 10 ) );
        formCard.setBackground( LADBROKES_WHITE );
        formCard.setBorder( BorderFactory.createEmptyBorder( 20, 40, 20, 40 ) );
        formCard.setMaximumSize( new Dimension( 600, 400 ) );

        Font labelFont = new Font( "SansSerif", Font.BOLD, 14 );
        Font fieldFont = new Font( "SansSerif", Font.PLAIN, 14 );

        // Customer name dropdown - loads customers from database
        // customer doesn't need to know their ID, they just pick their name
        JLabel customerLabel = new JLabel( "Customer:" );
        customerLabel.setFont( labelFont );
        final JComboBox< String > customerCombo = new JComboBox< String >();
        customerCombo.setFont( fieldFont );
        final ArrayList< Integer > customerIds = new ArrayList< Integer >();

        // populate the customer dropdown from the database
        try {
            PreparedStatement custStmt = connection.prepareStatement(
                "SELECT customer_id, first_name, last_name FROM customers ORDER BY last_name" );
            ResultSet custRs = custStmt.executeQuery();
            while ( custRs.next() ) {
                int id = custRs.getInt( "customer_id" );
                String name = custRs.getString( "first_name" ) + " " + custRs.getString( "last_name" );
                customerCombo.addItem( name );
                customerIds.add( id );
            }
            custRs.close();
            custStmt.close();
        }
        catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog( this, sqlException.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE );
        }

        // Sport selection using JComboBox (from lecture slides 75-80)
        JLabel sportLabel = new JLabel( "Sport:" );
        sportLabel.setFont( labelFont );
        String[] sports = { "Football", "Horse Racing", "GAA", "Golf", "Tennis", "Boxing", "Other" };
        final JComboBox< String > sportCombo = new JComboBox< String >( sports );
        sportCombo.setFont( fieldFont );

        // Event name
        JLabel eventLabel = new JLabel( "Event:" );
        eventLabel.setFont( labelFont );
        final JTextField eventField = new JTextField();
        eventField.setFont( fieldFont );

        // Selection
        JLabel selectionLabel = new JLabel( "Selection:" );
        selectionLabel.setFont( labelFont );
        final JTextField selectionField = new JTextField();
        selectionField.setFont( fieldFont );

        // Odds (fractional)
        JLabel oddsLabel = new JLabel( "Odds (e.g. 5/1, 6/4, 1/1):" );
        oddsLabel.setFont( labelFont );
        final JTextField oddsField = new JTextField();
        oddsField.setFont( fieldFont );

        // Stake
        JLabel stakeLabel = new JLabel( "Stake:" );
        stakeLabel.setFont( labelFont );
        final JTextField stakeField = new JTextField();
        stakeField.setFont( fieldFont );

        // add fields to form
        formCard.add( customerLabel );
        formCard.add( customerCombo );
        formCard.add( sportLabel );
        formCard.add( sportCombo );
        formCard.add( eventLabel );
        formCard.add( eventField );
        formCard.add( selectionLabel );
        formCard.add( selectionField );
        formCard.add( oddsLabel );
        formCard.add( oddsField );
        formCard.add( stakeLabel );
        formCard.add( stakeField );

        // empty row for spacing
        formCard.add( new JLabel( "" ) );
        formCard.add( new JLabel( "" ) );

        // place bet button
        JLabel emptyLabel = new JLabel( "" );
        JButton placeBetButton = new JButton( "Place Bet" );
        styleButton( placeBetButton );
        formCard.add( emptyLabel );
        formCard.add( placeBetButton );

        // wrap the form card in a container to centre it
        JPanel formWrapper = new JPanel();
        formWrapper.setLayout( new BoxLayout( formWrapper, BoxLayout.Y_AXIS ) );
        formWrapper.setBackground( LADBROKES_RED );
        formWrapper.add( Box.createVerticalGlue() );
        formWrapper.add( formCard );
        formWrapper.add( Box.createVerticalGlue() );

        mainPanel.add( headerPanel, BorderLayout.NORTH );
        mainPanel.add( formWrapper, BorderLayout.CENTER );

        // Place Bet ActionListener
        placeBetButton.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent event ) {
                    try {
                        // get customer ID from the dropdown selection
                        int selectedCustomer = customerCombo.getSelectedIndex();
                        if ( selectedCustomer < 0 || customerIds.isEmpty() ) {
                            JOptionPane.showMessageDialog( OddsMinds.this,
                                "Please select a customer.",
                                "Input error", JOptionPane.ERROR_MESSAGE );
                            return;
                        }
                        int customerId = customerIds.get( selectedCustomer );

                        // get sport from JComboBox
                        String sport = (String) sportCombo.getSelectedItem();

                        // validate event
                        String eventName = eventField.getText().trim();
                        if ( eventName.length() == 0 ) {
                            JOptionPane.showMessageDialog( OddsMinds.this,
                                "Please enter an Event.",
                                "Input error", JOptionPane.ERROR_MESSAGE );
                            return;
                        }

                        // validate selection
                        String selection = selectionField.getText().trim();
                        if ( selection.length() == 0 ) {
                            JOptionPane.showMessageDialog( OddsMinds.this,
                                "Please enter a Selection.",
                                "Input error", JOptionPane.ERROR_MESSAGE );
                            return;
                        }

                        // validate odds
                        String odds = oddsField.getText().trim();
                        if ( !BetCRUD.isValidOdds( odds ) ) {
                            JOptionPane.showMessageDialog( OddsMinds.this,
                                "Invalid odds format. Use fractional odds like 5/1, 6/4, 1/1",
                                "Input error", JOptionPane.ERROR_MESSAGE );
                            return;
                        }

                        // validate stake
                        String stakeStr = stakeField.getText().trim();
                        if ( stakeStr.length() == 0 ) {
                            JOptionPane.showMessageDialog( OddsMinds.this,
                                "Please enter a Stake.",
                                "Input error", JOptionPane.ERROR_MESSAGE );
                            return;
                        }
                        double stake = Double.parseDouble( stakeStr );

                        if ( stake <= 0 ) {
                            JOptionPane.showMessageDialog( OddsMinds.this,
                                "Stake must be greater than zero.",
                                "Input error", JOptionPane.ERROR_MESSAGE );
                            return;
                        }

                        // calculate payout: profit + stake back
                        double potentialPayout = BetCRUD.calculatePayout( odds, stake );

                        // confirm the bet with customer
                        int confirm = JOptionPane.showConfirmDialog( OddsMinds.this,
                            "Confirm Bet:\n\n"
                            + "Sport: " + sport + "\n"
                            + "Event: " + eventName + "\n"
                            + "Selection: " + selection + "\n"
                            + "Odds: " + odds + "\n"
                            + "Stake: " + String.format( "%.2f", stake ) + "\n"
                            + "Potential Payout: " + String.format( "%.2f", potentialPayout ) + "\n"
                            + "(Profit: " + String.format( "%.2f", potentialPayout - stake )
                            + " + Stake: " + String.format( "%.2f", stake ) + ")\n\n"
                            + "Place this bet?",
                            "Confirm Bet", JOptionPane.YES_NO_OPTION );

                        if ( confirm != JOptionPane.YES_OPTION ) return;

                        // AML check - see if this customer has any previous payouts over 2000
                        // uses inner join across customers, bets and settlements
                        PreparedStatement amlCheck = connection.prepareStatement(
                            "SELECT COUNT(*) AS flag_count FROM customers c "
                            + "INNER JOIN bets b ON c.customer_id = b.customer_id "
                            + "INNER JOIN settlements s ON b.bet_id = s.bet_id "
                            + "WHERE c.customer_id = ? AND s.payout > " + AML_THRESHOLD );
                        amlCheck.setInt( 1, customerId );
                        ResultSet amlRs = amlCheck.executeQuery();
                        boolean isAmlFlagged = false;
                        if ( amlRs.next() ) {
                            isAmlFlagged = amlRs.getInt( "flag_count" ) > 0;
                        }
                        amlRs.close();
                        amlCheck.close();

                        // if customer is AML flagged, check if they have been verified
                        if ( isAmlFlagged ) {
                            boolean alreadyVerified = AmlCRUD.isVerified( connection, customerId );

                            if ( alreadyVerified ) {
                                // customer was verified before, just show info
                                JOptionPane.showMessageDialog( OddsMinds.this,
                                    "Note: This customer has been AML verified.\n"
                                    + "Bet can proceed.",
                                    "AML - Customer Verified", JOptionPane.INFORMATION_MESSAGE );
                            }
                            else {
                                // customer flagged but NOT verified - block until verified
                                JOptionPane.showMessageDialog( OddsMinds.this,
                                    "AML BLOCK: This customer has a payout over 2,000 "
                                    + "but has NOT been verified.\n\n"
                                    + "Cannot place bet until AML verification is completed.\n"
                                    + "Please go to Staff > AML Flags to verify this customer.",
                                    "AML Verification Required", JOptionPane.ERROR_MESSAGE );
                                return;
                            }
                        }

                        // place the bet
                        BetCRUD.addBet( connection, customerId, sport, eventName,
                            selection, odds, stake, potentialPayout );

                        JOptionPane.showMessageDialog( OddsMinds.this,
                            "Bet placed successfully!\n\n"
                            + "Potential Payout: " + String.format( "%.2f", potentialPayout ),
                            "Bet Confirmed", JOptionPane.INFORMATION_MESSAGE );

                        // clear the form for next bet
                        customerCombo.setSelectedIndex( 0 );
                        eventField.setText( "" );
                        selectionField.setText( "" );
                        oddsField.setText( "" );
                        stakeField.setText( "" );
                        sportCombo.setSelectedIndex( 0 );
                    }
                    catch ( NumberFormatException numberException ) {
                        JOptionPane.showMessageDialog( OddsMinds.this,
                            "Please enter a valid number for Stake.",
                            "Input error", JOptionPane.ERROR_MESSAGE );
                    }
                    catch ( SQLException sqlException ) {
                        JOptionPane.showMessageDialog( OddsMinds.this,
                            sqlException.getMessage(),
                            "Database error", JOptionPane.ERROR_MESSAGE );
                    }
                }
            }
        );

        mainPanel.revalidate();
        mainPanel.repaint();
    }


    // STAFF LOGIN - gates Customers, Bets view, Settlements

    private void handleStaffLogin() {
        // JTextField and JPasswordField for login (from lecture slides 42-43)
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        Object[] message = {
            "Username:", usernameField,
            "Password:", passwordField
        };

        int option = JOptionPane.showConfirmDialog( this, message,
            "Staff Login", JOptionPane.OK_CANCEL_OPTION );

        if ( option == JOptionPane.OK_OPTION ) {
            String username = usernameField.getText();
            String password = new String( passwordField.getPassword() );

            if ( username.equals( STAFF_USERNAME ) && password.equals( STAFF_PASSWORD ) ) {
                JOptionPane.showMessageDialog( this, "Login successful." );
                showStaffDashboard();
            }
            else {
                JOptionPane.showMessageDialog( this,
                    "Invalid username or password.",
                    "Login failed", JOptionPane.ERROR_MESSAGE );
            }
        }
    }


    // STAFF DASHBOARD - shows after login

    private void showStaffDashboard() {
        try {
            currentTable = "bets";
            currentQuery = BET_QUERY;
            tableModel.setQuery( BET_QUERY );
        }
        catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog( this, sqlException.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE );
            return;
        }
        showStaffTableScreen( "All Bets" );
    }


    // STAFF TABLE SCREEN - Customers / Bets / Settlements

    private void showStaffTableScreen( String title ) {
        mainPanel.removeAll();

        //  TOP PANEL - navigation bar
        JPanel topPanel = new JPanel( new BorderLayout() );
        topPanel.setBackground( LADBROKES_RED );
        topPanel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );

        JButton backButton = new JButton( "Logout" );
        styleSmallButton( backButton );
        backButton.setBackground( LADBROKES_DARK_RED );

        JLabel screenTitle = new JLabel( "   Staff - " + title );
        screenTitle.setFont( new Font( "SansSerif", Font.BOLD, 20 ) );
        screenTitle.setForeground( LADBROKES_WHITE );

        // staff navigation buttons
        JPanel navPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        navPanel.setBackground( LADBROKES_RED );

        JButton customersNav = new JButton( "Customers" );
        JButton betsNav = new JButton( "All Bets" );
        JButton settlementsNav = new JButton( "Settlements" );
        JButton amlNav = new JButton( "AML Flags" );
        styleSmallButton( customersNav );
        styleSmallButton( betsNav );
        styleSmallButton( settlementsNav );
        styleSmallButton( amlNav );
        customersNav.setBackground( LADBROKES_DARK_RED );
        betsNav.setBackground( LADBROKES_DARK_RED );
        settlementsNav.setBackground( LADBROKES_DARK_RED );
        amlNav.setBackground( new Color( 180, 0, 0 ) ); // slightly different to stand out

        navPanel.add( customersNav );
        navPanel.add( betsNav );
        navPanel.add( settlementsNav );
        navPanel.add( amlNav );

        topPanel.add( backButton, BorderLayout.WEST );
        topPanel.add( screenTitle, BorderLayout.CENTER );
        topPanel.add( navPanel, BorderLayout.EAST );

        // nav button listeners
        backButton.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent event ) {
                    showWelcomeScreen();
                }
            }
        );

        customersNav.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent event ) {
                    try {
                        currentTable = "customers";
                        currentQuery = CUSTOMER_QUERY;
                        tableModel.setQuery( CUSTOMER_QUERY );
                    }
                    catch ( SQLException sqlException ) {
                        JOptionPane.showMessageDialog( OddsMinds.this,
                            sqlException.getMessage(),
                            "Database error", JOptionPane.ERROR_MESSAGE );
                        return;
                    }
                    showStaffTableScreen( "Customer Management" );
                }
            }
        );

        betsNav.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent event ) {
                    try {
                        currentTable = "bets";
                        currentQuery = BET_QUERY;
                        tableModel.setQuery( BET_QUERY );
                    }
                    catch ( SQLException sqlException ) {
                        JOptionPane.showMessageDialog( OddsMinds.this,
                            sqlException.getMessage(),
                            "Database error", JOptionPane.ERROR_MESSAGE );
                        return;
                    }
                    showStaffTableScreen( "All Bets" );
                }
            }
        );

        settlementsNav.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent event ) {
                    try {
                        currentTable = "settlements";
                        currentQuery = SETTLEMENT_QUERY;
                        tableModel.setQuery( SETTLEMENT_QUERY );
                    }
                    catch ( SQLException sqlException ) {
                        JOptionPane.showMessageDialog( OddsMinds.this,
                            sqlException.getMessage(),
                            "Database error", JOptionPane.ERROR_MESSAGE );
                        return;
                    }
                    showStaffTableScreen( "Settlements" );
                }
            }
        );

        // AML Flags button - shows inner join of customers/bets/settlements
        // for any payout over the AML threshold (anti money laundering)
        amlNav.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent event ) {
                    try {
                        currentTable = "aml";
                        currentQuery = AML_QUERY;
                        tableModel.setQuery( AML_QUERY );
                    }
                    catch ( SQLException sqlException ) {
                        JOptionPane.showMessageDialog( OddsMinds.this,
                            sqlException.getMessage(),
                            "Database error", JOptionPane.ERROR_MESSAGE );
                        return;
                    }
                    showStaffTableScreen( "AML Flagged Customers (Payout > 2000)" );
                }
            }
        );

        //  CENTRE - JTable with JScrollPane
        resultTable = new JTable( tableModel );
        resultTable.setBackground( LADBROKES_WHITE );
        resultTable.setGridColor( new Color( 200, 200, 200 ) );
        resultTable.setRowHeight( 25 );
        resultTable.getTableHeader().setBackground( LADBROKES_RED );
        resultTable.getTableHeader().setForeground( LADBROKES_WHITE );
        resultTable.getTableHeader().setFont( new Font( "SansSerif", Font.BOLD, 12 ) );

        sorter = new TableRowSorter< TableModel >( tableModel );
        resultTable.setRowSorter( sorter );

        //  BOTTOM PANEL
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout( new GridLayout( 2, 1 ) );
        bottomPanel.setBackground( LADBROKES_LIGHT_GREY );

        // filter row (from lecture slide 51/53)
        Box filterBox = Box.createHorizontalBox();
        filterBox.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
        JLabel filterLabel = new JLabel( " Filter: " );
        filterLabel.setFont( new Font( "SansSerif", Font.BOLD, 12 ) );
        final JTextField filterText = new JTextField();
        JButton filterButton = new JButton( "Apply Filter" );
        styleSmallButton( filterButton );
        filterBox.add( filterLabel );
        filterBox.add( filterText );
        filterBox.add( Box.createHorizontalStrut( 5 ) );
        filterBox.add( filterButton );

        // CRUD buttons row
        Box crudBox = Box.createHorizontalBox();
        crudBox.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );

        // only show Add for customers (bets are placed from customer screen)
        if ( currentTable.equals( "customers" ) ) {
            JButton addButton = new JButton( "Add Customer" );
            JButton updateButton = new JButton( "Update" );
            JButton deleteButton = new JButton( "Delete" );
            styleSmallButton( addButton );
            styleSmallButton( updateButton );
            styleSmallButton( deleteButton );
            crudBox.add( addButton );
            crudBox.add( Box.createHorizontalStrut( 5 ) );
            crudBox.add( updateButton );
            crudBox.add( Box.createHorizontalStrut( 5 ) );
            crudBox.add( deleteButton );

            addButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed( ActionEvent event ) {
                        handleAddCustomer();
                    }
                }
            );
            updateButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed( ActionEvent event ) {
                        handleUpdateCustomer();
                    }
                }
            );
            deleteButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed( ActionEvent event ) {
                        handleDeleteCustomer();
                    }
                }
            );
        }
        else if ( currentTable.equals( "bets" ) ) {
            // staff can update and delete bets
            JButton updateButton = new JButton( "Update Bet" );
            JButton deleteButton = new JButton( "Delete Bet" );
            styleSmallButton( updateButton );
            styleSmallButton( deleteButton );
            crudBox.add( updateButton );
            crudBox.add( Box.createHorizontalStrut( 5 ) );
            crudBox.add( deleteButton );

            updateButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed( ActionEvent event ) {
                        handleUpdateBet();
                    }
                }
            );
            deleteButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed( ActionEvent event ) {
                        handleDeleteBet();
                    }
                }
            );
        }
        else if ( currentTable.equals( "settlements" ) ) {
            JButton settleButton = new JButton( "Settle Open Bet" );
            JButton deleteButton = new JButton( "Delete Settlement" );
            styleSmallButton( settleButton );
            styleSmallButton( deleteButton );
            crudBox.add( settleButton );
            crudBox.add( Box.createHorizontalStrut( 5 ) );
            crudBox.add( deleteButton );

            settleButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed( ActionEvent event ) {
                        handleSettleOpenBet();
                    }
                }
            );
            deleteButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed( ActionEvent event ) {
                        handleDeleteSettlement();
                    }
                }
            );
        }
        else if ( currentTable.equals( "aml" ) ) {
            // AML Flags view - staff can verify or remove verification
            JButton verifyButton = new JButton( "Verify Customer" );
            JButton removeVerButton = new JButton( "Remove Verification" );
            styleSmallButton( verifyButton );
            styleSmallButton( removeVerButton );
            crudBox.add( verifyButton );
            crudBox.add( Box.createHorizontalStrut( 5 ) );
            crudBox.add( removeVerButton );

            verifyButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed( ActionEvent event ) {
                        handleVerifyCustomer();
                    }
                }
            );
            removeVerButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed( ActionEvent event ) {
                        handleRemoveVerification();
                    }
                }
            );
        }

        bottomPanel.add( filterBox );
        bottomPanel.add( crudBox );

        // add to main panel using BorderLayout (from  slide 51)
        mainPanel.add( topPanel, BorderLayout.NORTH );
        mainPanel.add( new JScrollPane( resultTable,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED ),
            BorderLayout.CENTER );
        mainPanel.add( bottomPanel, BorderLayout.SOUTH );

        //  FILTER - ActionListener (from  slide 53)
        filterButton.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    String text = filterText.getText();
                    if ( text.length() == 0 ) {
                        sorter.setRowFilter( null );
                    }
                    else {
                        try {
                            sorter.setRowFilter(
                                RowFilter.regexFilter( text ) );
                        }
                        catch ( PatternSyntaxException pse ) {
                            JOptionPane.showMessageDialog( null,
                                "Bad regex pattern", "Bad regex pattern",
                                JOptionPane.ERROR_MESSAGE );
                        }
                    }
                }
            }
        );

        mainPanel.revalidate();
        mainPanel.repaint();
    }


    // REFRESH TABLE

    private void refreshTable() {
        try {
            tableModel.setQuery( currentQuery );
        }
        catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog( null, sqlException.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE );
        }
    }

    // ==========================================
    // CUSTOMER CRUD (staff only)
    // ==========================================
    private void handleAddCustomer() {
        try {
            String firstName = JOptionPane.showInputDialog( this, "First Name:" );
            if ( firstName == null ) return;
            String lastName = JOptionPane.showInputDialog( this, "Last Name:" );
            if ( lastName == null ) return;
            String email = JOptionPane.showInputDialog( this, "Email:" );
            if ( email == null ) return;
            String phone = JOptionPane.showInputDialog( this, "Phone:" );
            if ( phone == null ) return;

            String[] bettingTypes = { "OTC", "Machines", "Both" };
            String bettingType = (String) JOptionPane.showInputDialog( this,
                "Betting Type:", "Select Betting Type",
                JOptionPane.QUESTION_MESSAGE, null, bettingTypes, bettingTypes[0] );
            if ( bettingType == null ) return;

            CustomerCRUD.addCustomer( connection, firstName, lastName,
                email, phone, bettingType );
            JOptionPane.showMessageDialog( this, "Customer added successfully." );
            refreshTable();
        }
        catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog( this, sqlException.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE );
        }
    }

    private void handleUpdateCustomer() {
        int selectedRow = resultTable.getSelectedRow();
        if ( selectedRow < 0 ) {
            JOptionPane.showMessageDialog( this,
                "Please select a customer to update.",
                "No row selected", JOptionPane.WARNING_MESSAGE );
            return;
        }

        try {
            int customerId = (int) resultTable.getValueAt( selectedRow, 0 );
            String firstName = JOptionPane.showInputDialog( this, "First Name:",
                resultTable.getValueAt( selectedRow, 1 ) );
            if ( firstName == null ) return;
            String lastName = JOptionPane.showInputDialog( this, "Last Name:",
                resultTable.getValueAt( selectedRow, 2 ) );
            if ( lastName == null ) return;
            String email = JOptionPane.showInputDialog( this, "Email:",
                resultTable.getValueAt( selectedRow, 3 ) );
            if ( email == null ) return;
            String phone = JOptionPane.showInputDialog( this, "Phone:",
                resultTable.getValueAt( selectedRow, 4 ) );
            if ( phone == null ) return;

            String[] bettingTypes = { "OTC", "Machines", "Both" };
            String currentType = resultTable.getValueAt( selectedRow, 5 ).toString();
            String bettingType = (String) JOptionPane.showInputDialog( this,
                "Betting Type:", "Select Betting Type",
                JOptionPane.QUESTION_MESSAGE, null, bettingTypes, currentType );
            if ( bettingType == null ) return;

            CustomerCRUD.updateCustomer( connection, customerId, firstName,
                lastName, email, phone, bettingType );
            JOptionPane.showMessageDialog( this, "Customer updated successfully." );
            refreshTable();
        }
        catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog( this, sqlException.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE );
        }
    }

    // ==========================================
    // CUSTOMER DELETE - cascades to child tables
    // ==========================================
    private void handleDeleteCustomer() {
        int selectedRow = resultTable.getSelectedRow();
        if ( selectedRow < 0 ) {
            JOptionPane.showMessageDialog( this,
                "Please select a customer to delete.",
                "No row selected", JOptionPane.WARNING_MESSAGE );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog( this,
            "Are you sure you want to delete this customer and all their related records?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION );
        if ( confirm != JOptionPane.YES_OPTION ) return;

        try {
            int customerId = (int) resultTable.getValueAt( selectedRow, 0 );

            // delete in order: aml_verifications -> settlements -> bets -> customer
            // must remove child records before parent due to foreign key constraints
            PreparedStatement deleteAml = connection.prepareStatement(
                "DELETE FROM aml_verifications WHERE customer_id = ?" );
            deleteAml.setInt( 1, customerId );
            deleteAml.executeUpdate();

            PreparedStatement deleteSettlements = connection.prepareStatement(
                "DELETE FROM settlements WHERE bet_id IN (SELECT bet_id FROM bets WHERE customer_id = ?)" );
            deleteSettlements.setInt( 1, customerId );
            deleteSettlements.executeUpdate();

            PreparedStatement deleteBets = connection.prepareStatement(
                "DELETE FROM bets WHERE customer_id = ?" );
            deleteBets.setInt( 1, customerId );
            deleteBets.executeUpdate();

            CustomerCRUD.deleteCustomer( connection, customerId );
            JOptionPane.showMessageDialog( this, "Customer and all related records deleted." );
            refreshTable();
        }
        catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog( this, sqlException.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE );
        }
    }

    // ==========================================
    // BET UPDATE (staff only)
    // ==========================================
    private void handleUpdateBet() {
        int selectedRow = resultTable.getSelectedRow();
        if ( selectedRow < 0 ) {
            JOptionPane.showMessageDialog( this,
                "Please select a bet to update.",
                "No row selected", JOptionPane.WARNING_MESSAGE );
            return;
        }

        try {
            int betId = (int) resultTable.getValueAt( selectedRow, 0 );

            // sport dropdown
            String[] sports = { "Football", "Horse Racing", "GAA", "Golf", "Tennis", "Boxing", "Other" };
            String currentSport = resultTable.getValueAt( selectedRow, 2 ).toString();
            String sport = (String) JOptionPane.showInputDialog( this,
                "Sport:", "Update Bet",
                JOptionPane.QUESTION_MESSAGE, null, sports, currentSport );
            if ( sport == null ) return;

            String eventName = JOptionPane.showInputDialog( this, "Event:",
                resultTable.getValueAt( selectedRow, 3 ) );
            if ( eventName == null ) return;

            String selection = JOptionPane.showInputDialog( this, "Selection:",
                resultTable.getValueAt( selectedRow, 4 ) );
            if ( selection == null ) return;

            String odds = JOptionPane.showInputDialog( this, "Odds (e.g. 5/1):",
                resultTable.getValueAt( selectedRow, 5 ) );
            if ( odds == null ) return;
            if ( !BetCRUD.isValidOdds( odds ) ) {
                JOptionPane.showMessageDialog( this,
                    "Invalid odds format. Use fractional odds like 5/1, 6/4, 1/1",
                    "Input error", JOptionPane.ERROR_MESSAGE );
                return;
            }

            String stakeStr = JOptionPane.showInputDialog( this, "Stake:",
                resultTable.getValueAt( selectedRow, 6 ) );
            if ( stakeStr == null ) return;
            double stake = Double.parseDouble( stakeStr.toString() );

            double potentialPayout = BetCRUD.calculatePayout( odds, stake );

            // status dropdown - let staff update the bet status
            String[] statuses = { "Open", "Won", "Lost", "Void" };
            String currentStatus = resultTable.getValueAt( selectedRow, 8 ).toString();
            String status = (String) JOptionPane.showInputDialog( this,
                "Status:", "Update Bet Status",
                JOptionPane.QUESTION_MESSAGE, null, statuses, currentStatus );
            if ( status == null ) return;

            BetCRUD.updateBet( connection, betId, sport, eventName,
                selection, odds, stake, potentialPayout, status );
            JOptionPane.showMessageDialog( this, "Bet updated successfully." );
            refreshTable();
        }
        catch ( NumberFormatException numberException ) {
            JOptionPane.showMessageDialog( this,
                "Please enter a valid number for Stake.",
                "Input error", JOptionPane.ERROR_MESSAGE );
        }
        catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog( this, sqlException.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE );
        }
    }

    // ==========================================
    // BET DELETE - cascades to child tables
    // ==========================================
    private void handleDeleteBet() {
        int selectedRow = resultTable.getSelectedRow();
        if ( selectedRow < 0 ) {
            JOptionPane.showMessageDialog( this,
                "Please select a bet to delete.",
                "No row selected", JOptionPane.WARNING_MESSAGE );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog( this,
            "Are you sure you want to delete this bet and its related records?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION );
        if ( confirm != JOptionPane.YES_OPTION ) return;

        try {
            int betId = (int) resultTable.getValueAt( selectedRow, 0 );

            // delete child records first: aml_verifications and settlements
            PreparedStatement deleteAml = connection.prepareStatement(
                "DELETE FROM aml_verifications WHERE triggering_bet_id = ?" );
            deleteAml.setInt( 1, betId );
            deleteAml.executeUpdate();

            PreparedStatement deleteSettlement = connection.prepareStatement(
                "DELETE FROM settlements WHERE bet_id = ?" );
            deleteSettlement.setInt( 1, betId );
            deleteSettlement.executeUpdate();

            BetCRUD.deleteBet( connection, betId );
            JOptionPane.showMessageDialog( this, "Bet and related records deleted." );
            refreshTable();
        }
        catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog( this, sqlException.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE );
        }
    }

    // ==========================================
    // SETTLEMENT DELETE (staff only)
    // ==========================================
    private void handleDeleteSettlement() {
        int selectedRow = resultTable.getSelectedRow();
        if ( selectedRow < 0 ) {
            JOptionPane.showMessageDialog( this,
                "Please select a settlement to delete.",
                "No row selected", JOptionPane.WARNING_MESSAGE );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog( this,
            "Are you sure you want to delete this settlement?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION );
        if ( confirm != JOptionPane.YES_OPTION ) return;

        try {
            int settlementId = (int) resultTable.getValueAt( selectedRow, 0 );
            SettlementCRUD.deleteSettlement( connection, settlementId );
            JOptionPane.showMessageDialog( this, "Settlement deleted." );
            refreshTable();
        }
        catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog( this, sqlException.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE );
        }
    }

    // ==========================================
    // AML - VERIFY CUSTOMER (from AML Flags screen)
    // staff can verify a customer who was previously unverified
    // ==========================================
    private void handleVerifyCustomer() {
        int selectedRow = resultTable.getSelectedRow();
        if ( selectedRow < 0 ) {
            JOptionPane.showMessageDialog( this,
                "Please select a row from the AML table.",
                "No row selected", JOptionPane.WARNING_MESSAGE );
            return;
        }

        try {
            // get customer_id and bet_id from the selected row in the AML view
            int customerId = (int) resultTable.getValueAt( selectedRow, 0 );
            String customerName = resultTable.getValueAt( selectedRow, 1 ).toString()
                + " " + resultTable.getValueAt( selectedRow, 2 ).toString();
            int betId = (int) resultTable.getValueAt( selectedRow, 5 );

            // check if already verified
            boolean alreadyVerified = AmlCRUD.isVerified( connection, customerId );
            if ( alreadyVerified ) {
                JOptionPane.showMessageDialog( this,
                    customerName + " has already been AML verified.",
                    "Already Verified", JOptionPane.INFORMATION_MESSAGE );
                return;
            }

            // ask staff to confirm and enter their name
            int confirm = JOptionPane.showConfirmDialog( this,
                "Verify customer: " + customerName + " (ID: " + customerId + ")?\n\n"
                + "This will allow them to place future bets.",
                "AML Verification", JOptionPane.YES_NO_OPTION );
            if ( confirm != JOptionPane.YES_OPTION ) return;

            String verifiedBy = JOptionPane.showInputDialog( this,
                "Staff name (who is verifying):" );
            if ( verifiedBy == null || verifiedBy.trim().length() == 0 ) {
                JOptionPane.showMessageDialog( this,
                    "Verification cancelled - staff name is required.",
                    "Cancelled", JOptionPane.WARNING_MESSAGE );
                return;
            }

            AmlCRUD.addVerification( connection, customerId, betId, verifiedBy.trim() );
            JOptionPane.showMessageDialog( this,
                customerName + " has been AML verified by " + verifiedBy.trim() + ".\n"
                + "They can now place bets again.",
                "Verification Complete", JOptionPane.INFORMATION_MESSAGE );
            refreshTable();
        }
        catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog( this, sqlException.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE );
        }
    }

    // ==========================================
    // AML - REMOVE VERIFICATION (if entered in error)
    // ==========================================
    private void handleRemoveVerification() {
        int selectedRow = resultTable.getSelectedRow();
        if ( selectedRow < 0 ) {
            JOptionPane.showMessageDialog( this,
                "Please select a row from the AML table.",
                "No row selected", JOptionPane.WARNING_MESSAGE );
            return;
        }

        try {
            int customerId = (int) resultTable.getValueAt( selectedRow, 0 );
            String customerName = resultTable.getValueAt( selectedRow, 1 ).toString()
                + " " + resultTable.getValueAt( selectedRow, 2 ).toString();

            boolean isVerified = AmlCRUD.isVerified( connection, customerId );
            if ( !isVerified ) {
                JOptionPane.showMessageDialog( this,
                    customerName + " is not currently verified.",
                    "Not Verified", JOptionPane.INFORMATION_MESSAGE );
                return;
            }

            int confirm = JOptionPane.showConfirmDialog( this,
                "Remove AML verification for " + customerName + "?\n\n"
                + "This will BLOCK them from placing future bets until re-verified.",
                "Remove Verification", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE );
            if ( confirm != JOptionPane.YES_OPTION ) return;

            // get the verification_id to delete
            PreparedStatement stmt = connection.prepareStatement(
                "SELECT verification_id FROM aml_verifications WHERE customer_id = ?" );
            stmt.setInt( 1, customerId );
            ResultSet rs = stmt.executeQuery();
            if ( rs.next() ) {
                int verificationId = rs.getInt( "verification_id" );
                AmlCRUD.deleteVerification( connection, verificationId );
                JOptionPane.showMessageDialog( this,
                    "Verification removed for " + customerName + ".\n"
                    + "They are now blocked from placing bets.",
                    "Verification Removed", JOptionPane.INFORMATION_MESSAGE );
            }
            rs.close();
            stmt.close();
            refreshTable();
        }
        catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog( this, sqlException.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE );
        }
    }

    // ==========================================
    // SETTLE OPEN BET - shows JList of open bets
    // ==========================================
    private void handleSettleOpenBet() {
        try {
            // query all open bets
            PreparedStatement stmt = connection.prepareStatement(
                "SELECT bet_id, customer_id, sport, event_name, selection, odds, stake "
                + "FROM bets WHERE status = 'Open'" );
            ResultSet rs = stmt.executeQuery();

            // build list of open bets for JList (from lecture slides 83-89)
            ArrayList< String > betList = new ArrayList< String >();
            ArrayList< Integer > betIds = new ArrayList< Integer >();

            while ( rs.next() ) {
                int betId = rs.getInt( "bet_id" );
                String display = "Bet #" + betId
                    + " | Customer: " + rs.getInt( "customer_id" )
                    + " | " + rs.getString( "sport" )
                    + " | " + rs.getString( "event_name" )
                    + " | " + rs.getString( "selection" )
                    + " | Odds: " + rs.getString( "odds" )
                    + " | Stake: " + rs.getDouble( "stake" );
                betList.add( display );
                betIds.add( betId );
            }
            rs.close();
            stmt.close();

            if ( betList.isEmpty() ) {
                JOptionPane.showMessageDialog( this, "No open bets to settle." );
                return;
            }

            // create JList to display open bets (from lecture slides 83-89)
            String[] betArray = betList.toArray( new String[0] );
            JList< String > openBetsList = new JList< String >( betArray );
            openBetsList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
            openBetsList.setVisibleRowCount( 5 );

            JScrollPane listScrollPane = new JScrollPane( openBetsList );

            int choice = JOptionPane.showConfirmDialog( this, listScrollPane,
                "Select an Open Bet to Settle", JOptionPane.OK_CANCEL_OPTION );

            if ( choice != JOptionPane.OK_OPTION ) return;

            int selectedIndex = openBetsList.getSelectedIndex();
            if ( selectedIndex < 0 ) {
                JOptionPane.showMessageDialog( this, "No bet selected." );
                return;
            }

            int betId = betIds.get( selectedIndex );

            // get the bet details to calculate payout
            PreparedStatement betStmt = connection.prepareStatement(
                "SELECT odds, stake FROM bets WHERE bet_id = ?" );
            betStmt.setInt( 1, betId );
            ResultSet betRs = betStmt.executeQuery();

            String odds = "";
            double stake = 0;
            if ( betRs.next() ) {
                odds = betRs.getString( "odds" );
                stake = betRs.getDouble( "stake" );
            }
            betRs.close();
            betStmt.close();

            // select result
            String[] results = { "Won", "Lost", "Void" };
            String result = (String) JOptionPane.showInputDialog( this,
                "Result:", "Select Result",
                JOptionPane.QUESTION_MESSAGE, null, results, results[0] );
            if ( result == null ) return;

            // calculate payout based on result
            double payout = 0;
            if ( result.equals( "Won" ) ) {
                payout = BetCRUD.calculatePayout( odds, stake );
                JOptionPane.showMessageDialog( this,
                    "Payout: " + String.format( "%.2f", payout )
                    + " (Profit: " + String.format( "%.2f", payout - stake )
                    + " + Stake: " + String.format( "%.2f", stake ) + ")" );
            }
            else if ( result.equals( "Void" ) ) {
                payout = stake; // return stake on void
            }
            // Lost = 0 payout

            // get the customer_id for this bet (needed for AML check)
            PreparedStatement custStmt = connection.prepareStatement(
                "SELECT customer_id FROM bets WHERE bet_id = ?" );
            custStmt.setInt( 1, betId );
            ResultSet custRs = custStmt.executeQuery();
            int customerId = 0;
            if ( custRs.next() ) {
                customerId = custRs.getInt( "customer_id" );
            }
            custRs.close();
            custStmt.close();

            SettlementCRUD.addSettlement( connection, betId, result, payout );

            // AML check - if payout exceeds threshold, prompt staff to verify customer
            if ( payout > AML_THRESHOLD ) {
                // check if customer is already verified
                boolean alreadyVerified = AmlCRUD.isVerified( connection, customerId );

                if ( alreadyVerified ) {
                    JOptionPane.showMessageDialog( this,
                        "AML Note: Payout of " + String.format( "%.2f", payout )
                        + " exceeds 2,000 threshold.\n\n"
                        + "This customer has already been AML verified.",
                        "AML - Already Verified", JOptionPane.INFORMATION_MESSAGE );
                }
                else {
                    // customer not yet verified - ask staff to verify now
                    int verifyNow = JOptionPane.showConfirmDialog( this,
                        "AML FLAG: Payout of " + String.format( "%.2f", payout )
                        + " exceeds the 2,000 threshold.\n\n"
                        + "This customer must be verified under Anti-Money Laundering rules.\n"
                        + "All future bets will be BLOCKED until verification is completed.\n\n"
                        + "Verify this customer now?",
                        "AML Verification Required", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE );

                    if ( verifyNow == JOptionPane.YES_OPTION ) {
                        String verifiedBy = JOptionPane.showInputDialog( this,
                            "Staff name (who verified the customer):" );
                        if ( verifiedBy != null && verifiedBy.trim().length() > 0 ) {
                            AmlCRUD.addVerification( connection, customerId,
                                betId, verifiedBy.trim() );
                            JOptionPane.showMessageDialog( this,
                                "Customer verified. Future bets can proceed.",
                                "AML Verification Recorded", JOptionPane.INFORMATION_MESSAGE );
                        }
                        else {
                            JOptionPane.showMessageDialog( this,
                                "Verification not recorded - staff name was blank.\n"
                                + "Customer remains unverified. Go to AML Flags to verify later.",
                                "Verification Skipped", JOptionPane.WARNING_MESSAGE );
                        }
                    }
                    else {
                        JOptionPane.showMessageDialog( this,
                            "Customer remains UNVERIFIED.\n"
                            + "Future bets from this customer will be blocked.\n"
                            + "Go to AML Flags tab to verify later.",
                            "Verification Deferred", JOptionPane.WARNING_MESSAGE );
                    }
                }
            }

            JOptionPane.showMessageDialog( this, "Bet #" + betId + " settled." );
            refreshTable();
        }
        catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog( this, sqlException.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE );
        }
    }

    // main method (from slide 54)
    public static void main( String args[] ) {
        new OddsMinds();
    } // end main
} // end class