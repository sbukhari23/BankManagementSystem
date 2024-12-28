import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class AddEmployee extends JFrame {
    AddEmployee() {
        // Get screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        // Set JFrame properties
        setTitle("Add Employee");
        setSize(screenWidth / 2, screenHeight / 2);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        setResizable(false);
        setLocationRelativeTo(null);

        // Background image
        JLabel background = new JLabel(new ImageIcon("E:\\DBS Project\\NewBankManagementSystem\\BankManagementSystem\\images\\login_signup_background.png"));
        background.setLayout(null);
        add(background, BorderLayout.CENTER);

        // Create a semi-transparent panel for the form
        JPanel formPanel = new JPanel();
        formPanel.setBackground(new Color(255, 255, 255, 216));
        formPanel.setLayout(null);

        // Center the form panel
        int panelWidth = 500;
        int panelHeight = 600;
        formPanel.setBounds((screenWidth / 2 - panelWidth / 2), (screenHeight / 2 - panelHeight / 2), panelWidth, panelHeight);
        background.add(formPanel);

        // Bank logo at top
        JLabel logoPlaceholder = new JLabel("", SwingConstants.CENTER);
        logoPlaceholder.setFont(new Font("Arial", Font.BOLD, 18));
        logoPlaceholder.setBounds((panelWidth - 66) / 2, 20, 66, 66);
        logoPlaceholder.setIcon(new ImageIcon("E:\\DBS Project\\NewBankManagementSystem\\BankManagementSystem\\images\\bank_logo_small.png"));
        formPanel.add(logoPlaceholder);

        // Form fields
        int startY = 100;
        int fieldHeight = 30;
        int spacing = 45;
        int fieldWidth = 300;
        int startX = (panelWidth - fieldWidth) / 2;

        // Create fields with placeholders
        JTextField nameField = createPlaceholderTextField("Enter Name", startX, startY, fieldWidth, fieldHeight);
        JTextField cnicField = createPlaceholderTextField("Enter CNIC (13 digits)", startX, startY + spacing, fieldWidth, fieldHeight);
        JTextField roleField = createPlaceholderTextField("Enter Role", startX, startY + spacing * 2, fieldWidth, fieldHeight);
        JTextField contactField = createPlaceholderTextField("Enter Contact Number", startX, startY + spacing * 3, fieldWidth, fieldHeight);
        JTextField dobField = createPlaceholderTextField("Enter DOB (YYYY-MM-DD)", startX, startY + spacing * 4, fieldWidth, fieldHeight);
        JTextField usernameField = createPlaceholderTextField("Enter Username", startX, startY + spacing * 5, fieldWidth, fieldHeight);
        JPasswordField passwordField = createPlaceholderPasswordField("Enter Password", startX, startY + spacing * 6, fieldWidth, fieldHeight);
        JTextField streetField = createPlaceholderTextField("Enter Street", startX, startY + spacing * 7, fieldWidth, fieldHeight);
        JTextField cityField = createPlaceholderTextField("Enter City", startX, startY + spacing * 8, fieldWidth, fieldHeight);
        JTextField stateField = createPlaceholderTextField("Enter State", startX, startY + spacing * 9, fieldWidth, fieldHeight);

        // Add all fields to the panel
        formPanel.add(nameField);
        formPanel.add(cnicField);
        formPanel.add(roleField);
        formPanel.add(contactField);
        formPanel.add(dobField);
        formPanel.add(usernameField);
        formPanel.add(passwordField);
        formPanel.add(streetField);
        formPanel.add(cityField);
        formPanel.add(stateField);

        // Add Employee button
        JButton addButton = new JButton("Add Employee");
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addButton.setBounds((panelWidth - 150) / 2, startY + spacing * 10, 150, 35);
        addButton.setFocusPainted(false);
        addButton.setBackground(new Color(0, 102, 204));
        addButton.setForeground(Color.WHITE);
        addButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        formPanel.add(addButton);

        // Message label
        JLabel messageLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel.setForeground(Color.RED);
        messageLabel.setBounds(0, startY + spacing * 11, panelWidth, 30);
        formPanel.add(messageLabel);

        // Add mouse listener to background to remove focus from fields
        background.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                background.requestFocusInWindow();
            }
        });

        // Add mouse listener to form panel to remove focus from fields
        formPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                formPanel.requestFocusInWindow();
            }
        });

        // Action listener for Add Employee button
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = getTextFromField(nameField, "Enter Name");
                String cnic = getTextFromField(cnicField, "Enter CNIC (13 digits)");
                String role = getTextFromField(roleField, "Enter Role");
                String dob = getTextFromField(dobField, "Enter DOB (YYYY-MM-DD)");
                String contact = getTextFromField(contactField, "Enter Contact Number");
                String username = getTextFromField(usernameField, "Enter Username");
                String password = new String(passwordField.getPassword()).trim();
                String street = getTextFromField(streetField, "Enter Street");
                String city = getTextFromField(cityField, "Enter City");
                String state = getTextFromField(stateField, "Enter State");

                // Reset message label
                messageLabel.setText("");

                if (name.isEmpty() || cnic.isEmpty() || role.isEmpty() || dob.isEmpty() || contact.isEmpty() ||
                        username.isEmpty() || password.isEmpty() || street.isEmpty() || city.isEmpty() || state.isEmpty()) {
                    messageLabel.setText("All fields are required.");
                    return;
                }

                if (!name.matches("[a-zA-Z ]+")) {
                    JOptionPane.showMessageDialog(null, "Invalid Name. Only alphabets and spaces are allowed.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!cnic.matches("\\d{13}")) {
                    messageLabel.setText("CNIC must be a 13-digit numeric string.");
                    return;
                }

                if (!dob.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    JOptionPane.showMessageDialog(null, "Invalid Date of Birth. Format must be YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!contact.matches("\\d{1,15}")) {
                    JOptionPane.showMessageDialog(null, "Invalid Contact Number. It must be a numeric string with up to 15 digits.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String hashedPass = LoginPage.hashPassword(password);

                try(Connection connection = DBConnection.getConnection()) {
                    String checkCNIC = "SELECT employee_id FROM Employee WHERE cnic = ?";
                    PreparedStatement stmt = connection.prepareStatement(checkCNIC);
                    stmt.setString(1, cnic);
                    ResultSet rs = stmt.executeQuery();
                    if(rs.next()) {
                        JOptionPane.showMessageDialog(null, "This employee is already added");
                        return;
                    }

                    String getBranch = "SELECT branch_id FROM Employee WHERE employee_id = ?";
                    stmt = connection.prepareStatement(getBranch);
                    stmt.setInt(1, Session.getUser_id());
                    rs = stmt.executeQuery();
                    int branch_id = -1;

                    if(rs.next()) {
                        branch_id = rs.getInt("branch_id");
                    }

                    String checkUsername = "SELECT employee_id FROM Employee WHERE username = ? AND branch_id = ?";
                    stmt = connection.prepareStatement(checkUsername);
                    stmt.setString(1, username);
                    stmt.setInt(2, branch_id);
                    rs = stmt.executeQuery();
                    if(rs.next()) {
                        JOptionPane.showMessageDialog(null, "Cannot set same username for multiple employees");
                        return;
                    }

                    Statement statement = connection.createStatement();
                    statement.execute("SET @current_user_id = " + Session.getUser_id());

                    String addEmployee = "INSERT INTO Employee (name, cnic, role, contact_number, username, passhash, street, city, state, branch_id, dob, joining_date) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, curdate())";
                    stmt = connection.prepareStatement(addEmployee);
                    stmt.setString(1, name);
                    stmt.setString(2, cnic);
                    stmt.setString(3, role);
                    stmt.setString(4, contact);
                    stmt.setString(5, username);
                    stmt.setString(6, hashedPass);
                    stmt.setString(7, street);
                    stmt.setString(8, city);
                    stmt.setString(9, state);
                    stmt.setInt(10, branch_id);
                    stmt.setString(11, dob);

                    stmt.executeUpdate();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }

                JOptionPane.showMessageDialog(null, "Employee added successfully");
                dispose();
            }
        });

        setVisible(true);
    }

    private JTextField createPlaceholderTextField(String placeholder, int x, int y, int width, int height) {
        JTextField textField = new JTextField(placeholder);
        textField.setForeground(Color.GRAY);
        textField.setBounds(x, y, width, height);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setForeground(Color.GRAY);
                    textField.setText(placeholder);
                }
            }
        });

        return textField;
    }

    private JPasswordField createPlaceholderPasswordField(String placeholder, int x, int y, int width, int height) {
        JPasswordField passwordField = new JPasswordField(placeholder);
        passwordField.setEchoChar((char) 0);
        passwordField.setForeground(Color.GRAY);
        passwordField.setBounds(x, y, width, height);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (new String(passwordField.getPassword()).equals(placeholder)) {
                    passwordField.setText("");
                    passwordField.setEchoChar('•');
                    passwordField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (new String(passwordField.getPassword()).isEmpty()) {
                    passwordField.setEchoChar((char) 0);
                    passwordField.setForeground(Color.GRAY);
                    passwordField.setText(placeholder);
                }
            }
        });

        return passwordField;
    }

    private String getTextFromField(JTextField field, String placeholder) {
        String text = field.getText().trim();
        return text.equals(placeholder) ? "" : text;
    }
}