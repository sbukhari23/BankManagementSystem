import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class ManagerDashboard extends JFrame {
    private JFrame currentFrame;
    public ManagerDashboard() {
        // Get screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        // Set JFrame properties
        setTitle("Manager Dashboard");
        setSize(screenWidth / 2, screenHeight / 2);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Open in maximized mode
        setLayout(new BorderLayout());
        setResizable(false);
        setLocationRelativeTo(null);

        // Background image
        JLabel background = new JLabel(new ImageIcon("E:\\DBS Project\\NewBankManagementSystem\\BankManagementSystem\\images\\login_signup_background.png"));
        background.setLayout(null);
        add(background, BorderLayout.CENTER);

        // Create a semi-transparent panel for manager options
        JPanel managerPanel = new JPanel();
        managerPanel.setBackground(new Color(255, 255, 255, 216)); // Semi-transparent white
        managerPanel.setLayout(null);

        // Center the manager panel
        int panelWidth = 500;
        int panelHeight = 700;
        managerPanel.setBounds((screenWidth / 2 - panelWidth / 2), (screenHeight / 2 - panelHeight / 2), panelWidth, panelHeight);
        background.add(managerPanel);

        // Placeholder for bank logo
        JLabel logoPlaceholder = new JLabel("", SwingConstants.CENTER);
        logoPlaceholder.setFont(new Font("Arial", Font.BOLD, 18));
        logoPlaceholder.setBounds(217, 20, 66, 66);
        logoPlaceholder.setIcon(new ImageIcon("E:\\DBS Project\\NewBankManagementSystem\\BankManagementSystem\\images\\bank_logo_small.png"));
        managerPanel.add(logoPlaceholder);

        // Buttons for manager functionalities
        JButton viewCustomerAccountsButton = createButton("View Customers", 100);
        JButton createAccountButton = createButton("Create Account", 160);
        JButton deleteAccountButton = createButton("Close Account", 220);
        JButton viewEmployeesButton = createButton("View Employees", 310);
        JButton addEmployeeButton = createButton("Add Employee", 370);
        JButton deleteEmployeeButton = createButton("Delete Employee", 430);
        JButton viewLoansButton = createButton("View Loan Applications", 520);
        JButton activityLogButton = createButton("Activity Log", 580);  // New Activity Log button

        // Adding buttons to the manager panel
        managerPanel.add(viewCustomerAccountsButton);
        managerPanel.add(createAccountButton);
        managerPanel.add(deleteAccountButton);
        managerPanel.add(viewEmployeesButton);
        managerPanel.add(addEmployeeButton);
        managerPanel.add(deleteEmployeeButton);
        managerPanel.add(viewLoansButton);
        managerPanel.add(activityLogButton); // Add Activity Log button

        // Add action listeners to the buttons
        viewCustomerAccountsButton.addActionListener(e -> EmployeeDashboard.showCustomers());
        createAccountButton.addActionListener(e -> {
            try {
                new CreateCustomerAccountForm();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        deleteAccountButton.addActionListener(e -> EmployeeDashboard.showAccounts());
        viewEmployeesButton.addActionListener(e -> showEmployees());
        addEmployeeButton.addActionListener(e -> new AddEmployee());
        deleteEmployeeButton.addActionListener(e -> viewEmployees());
        viewLoansButton.addActionListener(e -> viewLoanApplications());

        // Action listener for the new Activity Log button
        activityLogButton.addActionListener(e -> showActivityLog());

        // Add a mouse listener to the background to remove focus from buttons
        background.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                background.requestFocusInWindow();
            }
        });

        // Set visibility
        setVisible(true);
    }

    // Method to create styled buttons
    private JButton createButton(String text, int yPos) {

        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBounds(100, yPos, 300, 40);
        button.setBackground(new Color(51, 153, 255));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(0, 102, 204));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(27, 143, 255));
            }
        });

        return button;
    }


    // Method to delete an employee
    private void viewEmployees() {
        if (currentFrame != null) {
            currentFrame.dispose(); // Close the old frame if it exists
        }

        JFrame frame = new JFrame("Employees");
        currentFrame = frame; // Assign the current frame
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);

        try (Connection connection = DBConnection.getConnection()) {
            String getEmployees = "SELECT employee_id AS 'Employee ID', name AS Name, cnic AS CNIC, role AS Role, dob AS DOB, contact_number AS Contact, street AS Street, city AS City, state AS State, joining_date AS Joining FROM Employee WHERE branch_id = " +
                    "(SELECT branch_id FROM Employee WHERE employee_id = ?) AND employee_id <> ?;";
            PreparedStatement stmt = connection.prepareStatement(getEmployees);
            stmt.setInt(1, Session.getUser_id());
            stmt.setInt(2, Session.getUser_id());
            ResultSet rs = stmt.executeQuery();
            DefaultTableModel tableModel = EmployeeDashboard.buildTableModel(rs);

            // Create JTable with a non-editable model
            JTable table = new JTable(tableModel) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Disable editing
                }
            };

            // Enable row selection
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane scrollPane = new JScrollPane(table);

            // Create Delete button
            JButton deleteButton = new JButton("Delete");
            deleteButton.setFocusPainted(false);
            deleteButton.setBackground(new Color(232, 3, 3));
            deleteButton.setForeground(Color.WHITE);
            deleteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            deleteButton.addActionListener(e -> {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(frame, "Please select an employee to delete.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Get the employee_id of the selected row
                int employeeId = (int) table.getValueAt(selectedRow, 0);

                int confirm = JOptionPane.showConfirmDialog(
                        frame,
                        "Are you sure you want to delete Employee ID: " + employeeId + "?",
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    deleteEmployee(employeeId);
                    JOptionPane.showMessageDialog(null, "Employee deleted");
                    viewEmployees();
                }
            });

            // Create a JPanel for the button
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(deleteButton);

            // Add components to frame
            frame.setLayout(new BorderLayout());
            frame.add(scrollPane, BorderLayout.CENTER);
            frame.add(buttonPanel, BorderLayout.SOUTH);


            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteEmployee(int employee_id) {
        try(Connection connection = DBConnection.getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("SET @current_user_id = " + Session.getUser_id());

            String deleteEmployee = "DELETE FROM Employee WHERE employee_id = ?";
            PreparedStatement stmt = connection.prepareStatement(deleteEmployee);
            stmt.setInt(1, employee_id);
            stmt.executeUpdate();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }


    // Method to view loan applications
    private void viewLoanApplications() {
        JOptionPane.showMessageDialog(this, "Viewing Loan Applications...");
        // Add logic to fetch and display loan applications
    }

    // Method to show activity log
    private void showActivityLog() {
        // This is where you would fetch and display the activity log
        JOptionPane.showMessageDialog(this, "Displaying Activity Log...");
        // Add logic to fetch and display the activity log
    }


    private void showEmployees() {
        if (currentFrame != null) {
            currentFrame.dispose(); // Close the old frame if it exists
        }

        JFrame frame = new JFrame("Employees");
        currentFrame = frame; // Assign the current frame
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        currentFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        currentFrame.setExtendedState(MAXIMIZED_BOTH);

        try (Connection connection = DBConnection.getConnection()) {
            String getEmployees = "SELECT employee_id AS 'Employee ID', name AS Name, cnic AS CNIC, role AS Role, dob AS DOB, contact_number AS Contact, street AS Street, city AS City, state AS State, joining_date AS Joining FROM Employee WHERE branch_id = " +
                    "(SELECT branch_id FROM Employee WHERE employee_id = ?);";
            PreparedStatement stmt = connection.prepareStatement(getEmployees);
            stmt.setInt(1, Session.getUser_id());
            ResultSet rs = stmt.executeQuery();
            DefaultTableModel tableModel = EmployeeDashboard.buildTableModel(rs);

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
                            int empID = ((Number) table.getValueAt(row, 0)).intValue(); // Safely convert to int
                            showUpdateDialog(empID);
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


    private void showUpdateDialog(int employeeId) {

        JDialog dialog = new JDialog(this, "Update Employee Details", true);
        dialog.setSize(400, 300);
        dialog.setLayout(new GridLayout(0, 2));

        // Fields for customer details
        JLabel nameLabel = new JLabel("Name:");
        JTextField nameField = new JTextField();

        JLabel roleLabel = new JLabel("Role:");
        JTextField roleField = new JTextField();

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
        dialog.add(roleLabel);
        dialog.add(roleField);
        dialog.add(phoneLabel);
        dialog.add(phoneField);
        dialog.add(streetLabel);
        dialog.add(streetField);
        dialog.add(cityLabel);
        dialog.add(cityField);
        dialog.add(stateLabel);
        dialog.add(stateField);

        // Fetch current employee details
        String originalName = null, originalRole = null, originalPhone = null, originalStreet = null, originalCity = null, originalState = null;
        try (Connection connection = DBConnection.getConnection()) {
            String query = "SELECT name, role, contact_number, street, city, state FROM Employee WHERE employee_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                originalName = rs.getString("name");
                originalRole = rs.getString("role");
                originalPhone = rs.getString("contact_number");
                originalStreet = rs.getString("street");
                originalCity = rs.getString("city");
                originalState = rs.getString("state");

                nameField.setText(originalName);
                roleField.setText(originalRole);
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
        String finalOriginalRole = originalRole;
        String finalOriginalPhone = originalPhone;
        String finalOriginalStreet = originalStreet;
        String finalOriginalCity = originalCity;
        String finalOriginalState = originalState;

        updateButton.addActionListener(e -> {
            try (Connection connection = DBConnection.getConnection()) {
                StringBuilder updateQuery = new StringBuilder("UPDATE Employee SET ");
                boolean changesMade = false;

                // Compare new values with original values
                if (!nameField.getText().equals(finalOriginalName)) {
                    updateQuery.append("name = ?, ");
                    changesMade = true;
                }
                if (!roleField.getText().equals(finalOriginalRole)) {
                    updateQuery.append("role = ?, ");
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
                updateQuery.append(" WHERE employee_id = ?");

                PreparedStatement stmt = connection.prepareStatement(updateQuery.toString());

                int paramIndex = 1;
                if (!nameField.getText().equals(finalOriginalName)) {
                    stmt.setString(paramIndex++, nameField.getText());
                }
                if (!roleField.getText().equals(finalOriginalRole)) {
                    stmt.setString(paramIndex++, roleField.getText());
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
                stmt.setInt(paramIndex, employeeId);

                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(dialog, "Employee details updated successfully!");
                    dialog.dispose();
                    showEmployees();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Update failed. No rows affected.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        dialog.add(new JLabel()); // Placeholder for layout
        dialog.add(updateButton);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        new ManagerDashboard();
    }
}
