import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class EmployeeDashboard extends JFrame {
    private static JFrame currentFrame;
    public EmployeeDashboard() {
        // Get screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        // Set JFrame properties
        setTitle("Employee Dashboard");
        setSize(screenWidth / 2, screenHeight / 2);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Open in maximized mode
        setLayout(new BorderLayout());
        setResizable(false);
        setLocationRelativeTo(null);

        // Background image
        JLabel background = new JLabel(new ImageIcon("E:\\DBS Project\\NewBankManagementSystem\\BankManagementSystem\\images\\login_signup_background.png"));
        background.setLayout(null);
        add(background, BorderLayout.CENTER);

        // Create a semi-transparent panel for employee options
        JPanel employeePanel = new JPanel();
        employeePanel.setBackground(new Color(255, 255, 255, 216)); // Semi-transparent white
        employeePanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0); // Add spacing between buttons
        gbc.gridx = 0; // Align all buttons in the same column
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;


// Center the employee panel
        int panelWidth = 400;
        int panelHeight = 400;
        employeePanel.setBounds((screenWidth / 2 - panelWidth / 2), (screenHeight / 2 - panelHeight / 2), panelWidth, panelHeight);
        background.add(employeePanel);

// Placeholder for logo
        JLabel logoPlaceholder = new JLabel("", SwingConstants.CENTER);
        logoPlaceholder.setFont(new Font("Arial", Font.BOLD, 18));
        logoPlaceholder.setIcon(new ImageIcon("E:\\DBS Project\\NewBankManagementSystem\\BankManagementSystem\\images\\bank_logo_small.png"));

// Add logo to the panel at the top
        gbc.insets = new Insets(10, 0, 10, 0); // Spacing between components
        gbc.gridx = 0;
        gbc.gridy = 0;
        employeePanel.add(logoPlaceholder, gbc);

// Buttons for employee functionalities
        JButton viewCustomersButton = createButton("Update Customers", 100);
        JButton createCustomerButton = createButton("Create Account", 160);
        JButton deleteAccountsButton = createButton("Delete Accounts", 220);

// Add buttons to the panel in the center
        gbc.gridy++;
        employeePanel.add(viewCustomersButton, gbc);

        gbc.gridy++;
        employeePanel.add(createCustomerButton, gbc);

        gbc.gridy++;
        employeePanel.add(deleteAccountsButton, gbc);



        // Add action listeners to the buttons
        viewCustomersButton.addActionListener(
                e -> {JOptionPane.showMessageDialog(this, "Showing customers...");
                showCustomers();}
        );
        createCustomerButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Creating Account...");
            try {
                new CreateCustomerAccountForm();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        deleteAccountsButton.addActionListener(
                e -> {JOptionPane.showMessageDialog(this, "Delete accounts...");
                showAccounts();}
        );


        // Set visibility
        setVisible(true);
    }

    public static void showCustomers() {
        if (currentFrame != null) {
            currentFrame.dispose(); // Close the old frame if it exists
        }

        JFrame frame = new JFrame("Customers");
        currentFrame = frame; // Assign the current frame
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        try (Connection connection = DBConnection.getConnection()) {
            String getCustomers = "SELECT * FROM customer_view";
            PreparedStatement stmt = connection.prepareStatement(getCustomers);
            ResultSet rs = stmt.executeQuery();
            DefaultTableModel tableModel = buildTableModel(rs);

            // Create JTable with a non-editable model
            JTable table = new JTable(tableModel) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Disable editing
                }
            };
            JScrollPane scrollPane = new JScrollPane(table);

            // Add mouse listener to table
            table.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    if (evt.getClickCount() == 2) { // Double-click to edit
                        int row = table.getSelectedRow();
                        if (row >= 0) {
                            int customerId = ((Number) table.getValueAt(row, 0)).intValue(); // Safely convert to int
                            showUpdateDialog(customerId);
                        }
                    }
                }
            });

            // Add table to frame
            frame.add(scrollPane);
            frame.setSize(800, 400);
            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void showUpdateDialog(int customerId) {

        JDialog dialog = new JDialog(currentFrame, "Update Customer Details", true);
        dialog.setSize(400, 300);
        dialog.setLayout(new GridLayout(0, 2));

        // Fields for customer details
        JLabel nameLabel = new JLabel("Name:");
        JTextField nameField = new JTextField();

        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField();

        JLabel phoneLabel = new JLabel("Phone:");
        JTextField phoneField = new JTextField();

        JLabel streetLabel = new JLabel("Street:");
        JTextField streetField = new JTextField();

        JLabel cityLabel = new JLabel("City:");
        JTextField cityField = new JTextField();

        JLabel stateLabel = new JLabel("State:");
        JTextField stateField = new JTextField();

        // Add fields to dialog
        dialog.add(nameLabel);
        dialog.add(nameField);
        dialog.add(emailLabel);
        dialog.add(emailField);
        dialog.add(phoneLabel);
        dialog.add(phoneField);
        dialog.add(streetLabel);
        dialog.add(streetField);
        dialog.add(cityLabel);
        dialog.add(cityField);
        dialog.add(stateLabel);
        dialog.add(stateField);

        // Fetch current customer details
        String originalName = null, originalEmail = null, originalPhone = null, originalStreet = null, originalCity = null, originalState = null;
        try (Connection connection = DBConnection.getConnection()) {
            String query = "SELECT name, email, contact_number, street, city, state FROM customer WHERE customer_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                originalName = rs.getString("name");
                originalEmail = rs.getString("email");
                originalPhone = rs.getString("contact_number");
                originalStreet = rs.getString("street");
                originalCity = rs.getString("city");
                originalState = rs.getString("state");

                nameField.setText(originalName);
                emailField.setText(originalEmail);
                phoneField.setText(originalPhone);
                streetField.setText(originalStreet);
                cityField.setText(originalCity);
                stateField.setText(originalState);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Update button
        JButton updateButton = new JButton("Update");
        String finalOriginalName = originalName;
        String finalOriginalEmail = originalEmail;
        String finalOriginalPhone = originalPhone;
        String finalOriginalStreet = originalStreet;
        String finalOriginalCity = originalCity;
        String finalOriginalState = originalState;

        updateButton.addActionListener(e -> {
            try (Connection connection = DBConnection.getConnection()) {
                StringBuilder updateQuery = new StringBuilder("UPDATE customer SET ");
                boolean changesMade = false;

                // Compare new values with original values
                if (!nameField.getText().equals(finalOriginalName)) {
                    updateQuery.append("name = ?, ");
                    changesMade = true;
                }
                if (!emailField.getText().equals(finalOriginalEmail)) {
                    updateQuery.append("email = ?, ");
                    changesMade = true;
                }
                if (!phoneField.getText().equals(finalOriginalPhone)) {
                    updateQuery.append("contact_number = ?, ");
                    changesMade = true;
                }
                if (!streetField.getText().equals(finalOriginalStreet)) {
                    updateQuery.append("street = ?, ");
                    changesMade = true;
                }
                if (!cityField.getText().equals(finalOriginalCity)) {
                    updateQuery.append("city = ?, ");
                    changesMade = true;
                }
                if (!stateField.getText().equals(finalOriginalState)) {
                    updateQuery.append("state = ?, ");
                    changesMade = true;
                }

                if (!changesMade) {
                    JOptionPane.showMessageDialog(dialog, "No changes made.");
                    return;
                }

                // Remove trailing comma and space
                updateQuery = new StringBuilder(updateQuery.substring(0, updateQuery.length() - 2));
                updateQuery.append(" WHERE customer_id = ?");

                PreparedStatement stmt = connection.prepareStatement(updateQuery.toString());

                int paramIndex = 1;
                if (!nameField.getText().equals(finalOriginalName)) {
                    stmt.setString(paramIndex++, nameField.getText());
                }
                if (!emailField.getText().equals(finalOriginalEmail)) {
                    stmt.setString(paramIndex++, emailField.getText());
                }
                if (!phoneField.getText().equals(finalOriginalPhone)) {
                    stmt.setString(paramIndex++, phoneField.getText());
                }
                if (!streetField.getText().equals(finalOriginalStreet)) {
                    stmt.setString(paramIndex++, streetField.getText());
                }
                if (!cityField.getText().equals(finalOriginalCity)) {
                    stmt.setString(paramIndex++, cityField.getText());
                }
                if (!stateField.getText().equals(finalOriginalState)) {
                    stmt.setString(paramIndex++, stateField.getText());
                }
                stmt.setInt(paramIndex, customerId);

                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(dialog, "Customer details updated successfully!");
                    dialog.dispose();
                    showCustomers();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Update failed. No rows affected.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        dialog.add(new JLabel()); // Placeholder for layout
        dialog.add(updateButton);

        dialog.setLocationRelativeTo(currentFrame);
        dialog.setVisible(true);
    }

    // Method to create styled buttons
    private JButton createButton(String text, int yPos) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBounds(100, yPos, 250, 40);
        button.setFocusPainted(false);
        button.setBackground(new Color(0, 102, 204));
        button.setForeground(Color.WHITE);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    // Method to convert ResultSet to TableModel
    public static DefaultTableModel buildTableModel(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();

        // Get column names
        int columnCount = metaData.getColumnCount();
        String[] columnNames = new String[columnCount];
        for (int i = 1; i <= columnCount; i++) {
            columnNames[i - 1] = metaData.getColumnName(i);
        }

        // Get data rows
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        while (resultSet.next()) {
            Object[] rowData = new Object[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                rowData[i - 1] = resultSet.getObject(i);
            }
            tableModel.addRow(rowData);
        }

        return tableModel;
    }


    public static void showAccounts() {
        if (currentFrame != null) {
            currentFrame.dispose(); // Close the old frame if it exists
        }

        JFrame frame = new JFrame("Accounts");
        currentFrame = frame; // Assign the current frame
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        try (Connection connection = DBConnection.getConnection()) {
            String getAccounts = "SELECT * FROM account_view";
            PreparedStatement stmt = connection.prepareStatement(getAccounts);
            ResultSet rs = stmt.executeQuery();
            DefaultTableModel tableModel = buildTableModel(rs);

            // Create JTable with a non-editable model
            JTable table = new JTable(tableModel) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Disable editing
                }
            };
            JScrollPane scrollPane = new JScrollPane(table);
            final String[] account_number = new String[1];
            // Add mouse listener to table
            table.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    if (evt.getClickCount() == 1) { // Double-click to edit
                        int row = table.getSelectedRow();
                        if (row >= 0) {
                            account_number[0] = ((String) table.getValueAt(row, 1));
                        }
                    }
                }
            });

            // Create Delete button
            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(e -> {
                // Leave the ActionListener empty for further logic
                closeAccount(account_number[0]);
                JOptionPane.showMessageDialog(null, "Account " + account_number[0] + " closed");

                showAccounts();
            });

            // Add components to the frame
            frame.add(scrollPane, BorderLayout.CENTER);
            frame.add(deleteButton, BorderLayout.SOUTH);
            frame.setSize(800, 400);
            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void closeAccount(String account_number) {
        try(Connection connection = DBConnection.getConnection()) {
            String callProcedure = "CALL close_account(?)";
            PreparedStatement stmt = connection.prepareStatement(callProcedure);
            stmt.setString(1, account_number);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new EmployeeDashboard();
    }
}