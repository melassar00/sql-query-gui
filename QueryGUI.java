
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class QueryGUI extends JFrame {

    // driver label and dropdown
    private JLabel driverLabel;
    private JComboBox driverDropdown;

    // database label and dropdown
    private JLabel databaseLabel;
    private JComboBox databaseDropdwon;

    // username label and field
    private JLabel usernameLabel;
    private JTextField usernameField;

    // password label and field
    private JLabel passwordLabel;
    private JPasswordField passwordField;

    // sql text area panel and component
    private JPanel sqlTextAreaWrapper;
    private JTextArea sqlTextArea;

    // status label and buttons
    private JLabel statusLabel;
    private JButton connectButton;
    private JButton clearSqlCommandButton;
    private JButton executeButton;

    private JTable outputTable;
    private ResultSetTableModel tableModel = null;

    // clear result button
    private JPanel clearResultWrapper;
    private JButton clearResultButton;

    private Connection databaseConnection;
    private boolean connectionStatus = false;

    public QueryGUI() throws ClassNotFoundException, SQLException, IOException {
        // create the required GUI components
        this.createComponents();

        // add the action for the connect button
        this.connectButton.addActionListener((ActionEvent arg0) -> {
            handleConnectButtonClick();
        });

        // cleat the sql text area on click of the clear sql button
        this.clearSqlCommandButton.addActionListener((ActionEvent arg0) -> {
            sqlTextArea.setText("");
        });

        // add the action for the execute button
        this.executeButton.addActionListener((ActionEvent arg0) -> {
            handleExecuteButtonClick();
        });

        // clear the output table on click of the clear result button
        this.clearResultButton.addActionListener((ActionEvent arg0) -> {
            clearOutput();
        });

        configureComponents();

        // disconnect from database when user exits the application
        addWindowListener(new WindowAdapter() {
            public void closeWindow(WindowEvent event) {
                try {
                    // if the data base isn't already disconnected, then disconnect
                    if (!databaseConnection.isClosed()) {
                        databaseConnection.close();
                    }
                } catch (SQLException e) {
                    System.err.println("Error disconnecting from database");
                }
                System.exit(0);
            }
        });

    }

    public void configureComponents() {
        // group the labels and text fields of the user info section
        JPanel userInfoSection = new JPanel(new GridLayout(4, 2));
        userInfoSection.add(this.driverLabel);
        userInfoSection.add(this.driverDropdown);
        userInfoSection.add(this.databaseLabel);
        userInfoSection.add(this.databaseDropdwon);
        userInfoSection.add(this.usernameLabel);
        userInfoSection.add(this.usernameField);
        userInfoSection.add(this.passwordLabel);
        userInfoSection.add(this.passwordField);

        // group the status label and buttons
        JPanel centerButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerButtons.add(this.statusLabel);
        centerButtons.add(this.connectButton);
        centerButtons.add(this.clearSqlCommandButton);
        centerButtons.add(this.executeButton);

        // group the top of the gui
        JPanel guiTop = new JPanel(new GridLayout(1, 2, 25, 0));
        guiTop.add(userInfoSection);
        guiTop.add(this.sqlTextAreaWrapper);

        // group the bottom of the gui
        JPanel guiBottom = new JPanel();
        guiBottom.setLayout(new BorderLayout(20, 0));
        guiBottom.add(new JScrollPane(this.outputTable), BorderLayout.NORTH);
        guiBottom.add(this.clearResultWrapper, BorderLayout.SOUTH);

        // add all of the group panels to the main gui frame
        add(guiTop, BorderLayout.NORTH);
        add(centerButtons, BorderLayout.CENTER);
        add(guiBottom, BorderLayout.SOUTH);
    }

    public void handleConnectButtonClick() {
        try {
            // get the selected item in the driver dropdown
            Class.forName(String.valueOf(driverDropdown.getSelectedItem()));
        } catch (ClassNotFoundException e) {
            // if an error ocurred, set the status label text
            statusLabel.setText("No Connection Now");
            statusLabel.setForeground(Color.RED);

            // clear the output table
            clearOutput();
        }

        try {
            // establish the database connection with the user info
            databaseConnection = DriverManager.getConnection(String.valueOf(databaseDropdwon.getSelectedItem()),
                    usernameField.getText(), passwordField.getText());

            // change the status label
            statusLabel.setText("Connected to " + String.valueOf(databaseDropdwon.getSelectedItem()));
            statusLabel.setForeground(Color.GREEN);

            // update the connection status
            connectionStatus = true;
        } catch (SQLException e) {
            // if a sql error ocurred, update label and clear output table
            statusLabel.setText("No Connection Now");
            statusLabel.setForeground(Color.RED);
            clearOutput();
        }
    }

    public void handleExecuteButtonClick() {
        // if the database is connected and the tablemodel doesn't exist already
        // execute the sql command
        if (connectionStatus == true && tableModel == null) {
            try {
                // execute the sql command
                tableModel = new ResultSetTableModel(sqlTextArea.getText(), databaseConnection);
                outputTable.setModel(tableModel);
            } catch (ClassNotFoundException | SQLException | IOException e) {
                // clear the output table
                clearOutput();

                // display the error from the exception
                JOptionPane.showMessageDialog(null, e.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
            }
        } // if the database is connected and the tablemodel already exists
        else if (connectionStatus == true && tableModel != null) {
            // if the command a select, execute the command
            String sqlQuery = sqlTextArea.getText();
            if (sqlQuery.contains("select") || sqlQuery.contains("SELECT")) {
                try {
                    tableModel.setQuery(sqlQuery);
                } catch (IllegalStateException | SQLException e) {
                    // clear the output table
                    clearOutput();
                    // display the error from the exception
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
                }
            } // if it's not a select command, then its a command that will change the query
            else {
                try {
                    // execute the command
                    tableModel.setUpdate(sqlQuery);

                    // clear the output table
                    clearOutput();
                } catch (IllegalStateException | SQLException e) {
                    clearOutput();
                    // the exception error
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public void clearOutput() {
        // clear the output table
        this.outputTable.setModel(new DefaultTableModel());
        this.tableModel = null;
    }

    public void createComponents() throws ClassNotFoundException, SQLException, IOException {
        // paths that will be listed in dropdowns
        String[] driverPaths = {"com.mysql.jdbc.Driver", "com.mysql.jdbc.OracleDriver", "com.mysql.jdbc.netDB2Driver",
            "com.mysql.jdbc.OdbcDriver"};
        String[] databasePaths = {"jdbc:mysql://localhost:3312/project3", "dbc:mysql://localhost:3312/bikedb",
            "dbc:mysql://localhost:3310/test"};

        // create the user info section components
        this.driverLabel = new JLabel("JDBC Driver    ", JLabel.RIGHT);
        this.databaseLabel = new JLabel("Database URL    ", JLabel.RIGHT);
        this.usernameLabel = new JLabel("Username    ", JLabel.RIGHT);
        this.passwordLabel = new JLabel("Password    ", JLabel.RIGHT);
        this.driverDropdown = new JComboBox(driverPaths);
        this.driverDropdown.setSelectedIndex(0);
        this.databaseDropdwon = new JComboBox(databasePaths);
        this.usernameField = new JTextField();
        this.passwordField = new JPasswordField();

        // create the sql text area components
        this.sqlTextAreaWrapper = new JPanel(new GridLayout(1, 1));
        this.sqlTextArea = new JTextArea(3, 15);
        sqlTextArea.setBorder(BorderFactory.createLineBorder(Color.black));
        this.sqlTextArea.setWrapStyleWord(true);
        this.sqlTextArea.setLineWrap(true);
        sqlTextAreaWrapper.add(sqlTextArea);
        sqlTextAreaWrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 25));

        // create the center label and buttons
        this.statusLabel = new JLabel("No Connection Now");
        this.statusLabel.setForeground(Color.RED);
        this.statusLabel.setOpaque(true);
        this.statusLabel.setBackground(Color.BLACK);
        this.connectButton = new JButton("Connect to Database");
        connectButton.setBackground(Color.BLUE);
        connectButton.setForeground(Color.YELLOW);
        this.clearSqlCommandButton = new JButton("Clear SQL Command");
        clearSqlCommandButton.setBackground(Color.WHITE);
        clearSqlCommandButton.setForeground(Color.RED);
        this.executeButton = new JButton("Execute SQL Command");
        executeButton.setBackground(Color.GREEN);
        executeButton.setForeground(Color.BLACK);

        // create the output result table section
        this.outputTable = new JTable();

        // create the clear result button section
        this.clearResultWrapper = new JPanel(new GridLayout(1, 3));
        this.clearResultButton = new JButton("Clear Result Window");
        this.clearResultButton.setBackground(Color.YELLOW);
        this.clearResultWrapper.add(this.clearResultButton);
        this.clearResultWrapper.add(new JLabel(""));
        this.clearResultWrapper.add(new JLabel(""));
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
        // create the main GUI frame
        QueryGUI myFrame = new QueryGUI();
        myFrame.pack();
        myFrame.setTitle("SQL Client GUI - (CNT 4714 - Spring 2020)");
        myFrame.setLocationRelativeTo(null);
        myFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        myFrame.setVisible(true);
    }
}
