import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SignupPage extends JFrame {

    public SignupPage() {
        // Get screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        // Set JFrame properties
        setTitle("Sign Up Page");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        setResizable(false);

        // Create a layered pane for stacking components
        JLayeredPane layeredPane = new JLayeredPane();
        add(layeredPane, BorderLayout.CENTER);

        // Add background image
        JLabel background = new JLabel(new ImageIcon("E:\\DBS Project\\NewBankManagementSystem\\BankManagementSystem\\images\\login_signup_background.png"));
        background.setSize(screenSize);
        layeredPane.add(background, JLayeredPane.DEFAULT_LAYER);

        // Use GridBagLayout for centering signup panel
        JPanel transparentOverlay = new JPanel(new GridBagLayout());
        transparentOverlay.setOpaque(false);
        transparentOverlay.setBounds(0, 0, screenWidth, screenHeight);
        layeredPane.add(transparentOverlay, JLayeredPane.PALETTE_LAYER);

        // Create semi-transparent signup panel
        JPanel signupPanel = new JPanel(new GridBagLayout());
        signupPanel.setBackground(new Color(255, 255, 255, 180));
        signupPanel.setPreferredSize(new Dimension(400, 700));
        transparentOverlay.add(signupPanel);

        // Add form components to the signup panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // Add a logo placeholder at the top
        JLabel logoLabel = new JLabel(new ImageIcon("E:\\DBS Project\\NewBankManagementSystem\\BankManagementSystem\\images\\bank_logo_small.png"));
        gbc.gridx = 0;
        gbc.gridy = 0;
        signupPanel.add(logoLabel, gbc);

        // Create fields with specified formats
        JTextField cnicField = createTextField("CNIC (13 digits without dashes)", 250, 30);
        JTextField accountField = createTextField("Bank Account Number (10-20 digits)", 250, 30);
        JTextField usernameField = createTextField("Username (Alphanumeric, 5-15 characters)", 250, 30);
        JPasswordField passwordField = createPasswordField("Password", 250, 30);
        JPasswordField confirmPasswordField = createPasswordField("Confirm Password", 250, 30);

        // Add fields to the signup panel
        gbc.gridy++;
        signupPanel.add(cnicField, gbc);
        gbc.gridy++;
        signupPanel.add(accountField, gbc);
        gbc.gridy++;
        signupPanel.add(usernameField, gbc);
        gbc.gridy++;
        signupPanel.add(passwordField, gbc);
        gbc.gridy++;
        signupPanel.add(confirmPasswordField, gbc);

        // Add terms and conditions checkbox
        JCheckBox termsCheckBox = new JCheckBox("I agree to the terms and conditions");
        termsCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridy++;
        signupPanel.add(termsCheckBox, gbc);

        // Sign Up button
        JButton signUpButton = new JButton("Sign Up");
        signUpButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        signUpButton.setEnabled(false);
        gbc.gridy++;
        signupPanel.add(signUpButton, gbc);

        // Back to Sign In button
        JButton backToSignInButton = new JButton("Back to Sign In");
        backToSignInButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridy++;
        signupPanel.add(backToSignInButton, gbc);

        // Apply For Loan
        JButton applyForLoanButton = new JButton("Apply For Loan");
        applyForLoanButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridy++;
        signupPanel.add(applyForLoanButton, gbc);

        // Field listeners for enabling Sign Up button
        Runnable enableButtonLogic = () -> {
            boolean fieldsFilled = !cnicField.getText().trim().isEmpty()
                    && !accountField.getText().trim().isEmpty()
                    && !usernameField.getText().trim().isEmpty()
                    && passwordField.getPassword().length > 0
                    && confirmPasswordField.getPassword().length > 0;

            signUpButton.setEnabled(fieldsFilled && termsCheckBox.isSelected());
        };

        addFieldListeners(cnicField, enableButtonLogic);
        addFieldListeners(accountField, enableButtonLogic);
        addFieldListeners(usernameField, enableButtonLogic);
        addFieldListeners(passwordField, enableButtonLogic);
        addFieldListeners(confirmPasswordField, enableButtonLogic);

        termsCheckBox.addActionListener(e -> enableButtonLogic.run());

        // Button actions
        backToSignInButton.addActionListener(e -> {
            setVisible(false);
            new LoginPage();
        });

        applyForLoanButton.addActionListener(e -> {
            dispose();
            new LoanApplicationNew();
        });

        // Action listener for Sign Up button
        signUpButton.addActionListener(e -> {
            String cnic = cnicField.getText().trim();
            String accountNumber = accountField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            // Validate if passwords match
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match!");
                return;
            }

            // Validate formats
            if (!username.matches("[a-zA-Z0-9]{5,15}")) {
                JOptionPane.showMessageDialog(this, "Invalid Username. Follow the format: Alphanumeric, 5-15 characters.");
                return;
            }
            if (!cnic.matches("\\d{13}")) {
                JOptionPane.showMessageDialog(this, "Invalid CNIC. Follow the format: 13 digits without dashes.");
                return;
            }
            if (!accountNumber.matches("\\d{10,20}")) {
                JOptionPane.showMessageDialog(this, "Invalid Bank Account Number. Follow the format: 10-20 digits.");
                return;
            }

            // Call signup validation
            boolean signUpIsvalid = signupValidation(cnic, accountNumber, username, password);
            if (signUpIsvalid) {
                // Close the signup page before opening the PinSetupPage
                dispose(); // Close the current SignupPage window
                new PinSetupPage(); // Open the PinSetupPage
            }
        });



        setVisible(true);
    }

    private void addFieldListeners(JTextField field, Runnable enableButtonLogic) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                enableButtonLogic.run();
            }

            public void removeUpdate(DocumentEvent e) {
                enableButtonLogic.run();
            }

            public void changedUpdate(DocumentEvent e) {
                enableButtonLogic.run();
            }
        });
    }

    private JTextField createTextField(String placeholder, int width, int height) {
        JTextField textField = new JTextField(placeholder);
        textField.setPreferredSize(new Dimension(width, height));
        textField.setForeground(Color.GRAY);
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
                    textField.setText(placeholder);
                    textField.setForeground(Color.GRAY);
                }
            }
        });
        return textField;
    }

    private JPasswordField createPasswordField(String placeholder, int width, int height) {
        JPasswordField passwordField = new JPasswordField(placeholder);
        passwordField.setPreferredSize(new Dimension(width, height));
        passwordField.setForeground(Color.GRAY);
        passwordField.setEchoChar((char) 0);
        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (new String(passwordField.getPassword()).equals(placeholder)) {
                    passwordField.setText("");
                    passwordField.setForeground(Color.BLACK);
                    passwordField.setEchoChar('*');
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (new String(passwordField.getPassword()).isEmpty()) {
                    passwordField.setText(placeholder);
                    passwordField.setForeground(Color.GRAY);
                    passwordField.setEchoChar((char) 0);
                }
            }
        });
        return passwordField;
    }

    public static boolean signupValidation(String cnic, String account_number, String username, String password) {
        try (Connection connection = DBConnection.getConnection()) {
            String getCNIC = "SELECT customer_id, username FROM customer WHERE cnic = ?";
            PreparedStatement stmt = connection.prepareStatement(getCNIC);
            stmt.setString(1, cnic);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(null, "Incorrect CNIC!");
                return false;
            }

            String user = rs.getString("username");
            if(!rs.wasNull()) {
                JOptionPane.showMessageDialog(null, "This user has already signed up");
                return false;
            }

            String getCustomerid = "SELECT customer_id FROM accountcustomer WHERE account_number = ?";
            stmt = connection.prepareStatement(getCustomerid);
            stmt.setString(1, account_number);
            int customer_id = rs.getInt("customer_id");
            rs = stmt.executeQuery();

            if (rs.next()) {
                String checkUsername = "SELECT * FROM customer WHERE username = ?";
                stmt = connection.prepareStatement(checkUsername);
                stmt.setString(1, username);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    JOptionPane.showMessageDialog(null, "Username already exists!");
                    return false; // Prevent moving to Pin Setup Page
                } else {
                    String hash = LoginPage.hashPassword(password);
                    String setUsername = "UPDATE customer SET username = ?, passhash = ? WHERE customer_id = ?";

                    stmt = connection.prepareStatement(setUsername);
                    stmt.setString(1, username);
                    stmt.setString(2, hash);
                    stmt.setInt(3, customer_id);
                    int rowsUpdated = stmt.executeUpdate();

                    if (rowsUpdated > 0) {
                        Session.setUser_id(customer_id);
                        Session.setAccount_no(account_number);
                        JOptionPane.showMessageDialog(null, "Sign up successful!");
                    } else {
                        JOptionPane.showMessageDialog(null, "Failed to update user details. Please try again.");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "Account number is either incorrect or account has been closed. Please contact bank for assistance");
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static void main(String[] args) {
        new SignupPage();
    }
}
