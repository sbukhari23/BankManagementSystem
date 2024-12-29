import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class LoginPage extends JFrame {

    public LoginPage() {
        // Get screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        // Set JFrame properties
        setTitle("Login Page");
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

        // Create a semi-transparent panel for login form
        JPanel loginPanel = new JPanel();
        loginPanel.setBackground(new Color(255, 255, 255, 216)); // Semi-transparent white
        loginPanel.setLayout(null);

        // Center the login panel
        int panelWidth = 400;
        int panelHeight = 450;
        loginPanel.setBounds((screenWidth / 2 - panelWidth / 2), (screenHeight / 2 - panelHeight / 2), panelWidth, panelHeight);
        background.add(loginPanel);

        // Placeholder for bank logo
        JLabel logoPlaceholder = new JLabel("", SwingConstants.CENTER);
        logoPlaceholder.setFont(new Font("Arial", Font.BOLD, 18));
        logoPlaceholder.setBounds(170, 30, 66, 66);
        logoPlaceholder.setIcon(new ImageIcon("images\\bank_logo_small.png"));
        loginPanel.add(logoPlaceholder);

        // Username text field with placeholder
        JTextField usernameField = createTextField("Enter your username", 200, 30);
        usernameField.setBounds(100, 120, 200, 30);
        loginPanel.add(usernameField);

        // Password text field with placeholder
        JPasswordField passwordField = createPasswordField("Enter your password", 200, 30);
        passwordField.setBounds(100, 190, 200, 30);
        loginPanel.add(passwordField);

        // Login button
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setBounds(150, 250, 100, 30);
        loginPanel.add(loginButton);

        // Forgot password label
        JLabel forgotPasswordLabel = new JLabel("Forgot Password?");
        forgotPasswordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgotPasswordLabel.setForeground(Color.BLUE.darker());
        forgotPasswordLabel.setBounds(150, 290, 200, 30);
        forgotPasswordLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotPasswordLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(null, "Forgot Password clicked!");
            }
        });
        loginPanel.add(forgotPasswordLabel);

        // Sign up button
        JButton signUpButton = new JButton("Sign Up");
        signUpButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        signUpButton.setBounds(150, 330, 100, 30);
        loginPanel.add(signUpButton);

        // Action listener for Sign Up button
        signUpButton.addActionListener(e -> {
            // Open the Sign Up page and hide the login page
            setVisible(false);
            new SignupPage();
        });

        // Add a mouse listener to the background to remove focus from text fields
        background.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Remove focus from all fields by requesting focus for the background
                background.requestFocusInWindow();
            }
        });

        // Action listener for Login button
        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Both fields are required.");
            } else {
                boolean isValid = validateLogin(username, password);

                if (!isValid) {
                    setVisible(true);  // Show the login page again if login fails
                }
                else {
                    setVisible(false);
                }
            }
        });


        // Set visibility
        setVisible(true);
    }

    // Method to create JTextField with placeholder text
    private JTextField createTextField(String placeholderText, int width, int height) {
        JTextField textField = new JTextField(placeholderText);
        textField.setPreferredSize(new Dimension(width, height));
        textField.setForeground(Color.GRAY);
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholderText)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setText(placeholderText);
                    textField.setForeground(Color.GRAY);
                }
            }
        });
        return textField;
    }

    // Method to create JPasswordField with placeholder text
    private JPasswordField createPasswordField(String placeholderText, int width, int height) {
        JPasswordField passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(width, height));
        passwordField.setForeground(Color.GRAY);
        passwordField.setText(placeholderText);
        passwordField.setEchoChar((char) 0); // Show text (unmasked)

        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (new String(passwordField.getPassword()).equals(placeholderText)) {
                    passwordField.setText("");
                    passwordField.setForeground(Color.BLACK);
                    passwordField.setEchoChar('*'); // Mask text
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (new String(passwordField.getPassword()).isEmpty()) {
                    passwordField.setText(placeholderText);
                    passwordField.setForeground(Color.GRAY);
                    passwordField.setEchoChar((char) 0); // Show text (unmasked)
                }
            }
        });
        return passwordField;
    }

    // Modified validateLogin method to return a boolean indicating success or failure
    public static boolean validateLogin(String username, String password) {
        try (Connection connection = DBConnection.getConnection()) {
            String getPasswordQuery = "SELECT passhash, customer_id FROM Customer NATURAL JOIN AccountCustomer a WHERE username = ? AND a.customer_id IS NOT NULL";
            PreparedStatement stmt = connection.prepareStatement(getPasswordQuery);
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String hashedPassword = rs.getString("passhash");
                if (!verifyPassword(password, hashedPassword)) {
                    JOptionPane.showMessageDialog(null, "Incorrect password");
                    return false;  // Login failed
                }

                Session.setUser_id(rs.getInt("customer_id"));

                String getAccountQuery = "SELECT account_number FROM AccountCustomer WHERE customer_id = ?";
                stmt = connection.prepareStatement(getAccountQuery);
                stmt.setInt(1, Session.getUser_id());

                rs = stmt.executeQuery();

                if (rs.next()) {
                    Session.setAccount_no(rs.getString("account_number"));
                }

                new CustomerDashboard();
                return true;  // Login successful
            } else {
                // Login Failed
                JOptionPane.showMessageDialog(null, "Incorrect username or you no longer have an open account");
                return false;  // Login failed
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;  // In case of an exception, login fails
        }
    }

    public static boolean verifyPassword(String plainTextPassword, String hashedPassword) {
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }

    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    public static void main(String[] args) {
        new LoginPage();
    }
}
