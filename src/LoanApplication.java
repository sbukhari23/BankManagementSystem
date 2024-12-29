import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class LoanApplication extends JFrame {

    public LoanApplication() {
        // Get screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        // Set JFrame properties
        setTitle("Loan Application Form");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Open in maximized state
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);
        setLocationRelativeTo(null);

        // Background image
        JLabel background = new JLabel(new ImageIcon("images\\login_signup_background.png"));
        background.setLayout(null);
        add(background, BorderLayout.CENTER);

        // Create a semi-transparent panel for loan application form
        JPanel formPanel = new JPanel();
        formPanel.setBackground(new Color(255, 255, 255, 216)); // Semi-transparent white
        formPanel.setLayout(null);

        // Center the form panel
        int panelWidth = 400;
        int panelHeight = 300;
        formPanel.setBounds((screenWidth / 2 - panelWidth / 2), (screenHeight / 2 - panelHeight / 2), panelWidth, panelHeight);
        background.add(formPanel);

        // Custom fonts and placeholders
        Font fieldFont = new Font("Arial", Font.PLAIN, 16);

        // Loan amount field
        JTextField amountField = createPlaceholderField("Enter loan amount", fieldFont);
        amountField.setBounds(100, 80, 200, 30);
        formPanel.add(amountField);

        // Dropdown for loan purpose
        String[] loanPurposes = {"select loan purpose", "home", "car", "personal", "educational", "corporate", "others"};
        JComboBox<String> purposeDropdown = new JComboBox<>(loanPurposes);
        purposeDropdown.setFont(fieldFont);
        purposeDropdown.setBounds(100, 120, 200, 30);
        formPanel.add(purposeDropdown);

        // Dropdown for loan duration
        String[] loanDurations = {"select duration", "6 months", "12 months"};
        JComboBox<String> durationDropdown = new JComboBox<>(loanDurations);
        durationDropdown.setFont(fieldFont);
        durationDropdown.setBounds(100, 160, 200, 30); // Adjust the position and size as needed
        formPanel.add(durationDropdown);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton submitButton = new JButton("Submit");
        JButton cancelButton = new JButton("Cancel");
        submitButton.setBackground(new Color(34, 139, 34)); // Green
        submitButton.setForeground(Color.WHITE);
        cancelButton.setBackground(Color.RED);
        cancelButton.setForeground(Color.WHITE);
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);
        buttonPanel.setOpaque(false);
        buttonPanel.setBounds(100, 190, 200, 30);
        formPanel.add(buttonPanel);

        // Redirect focus away from text fields when clicking elsewhere
        background.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                background.requestFocusInWindow(); // Redirect focus
            }
        });

        // Submit button action
        submitButton.addActionListener(e -> {
            String amountText = amountField.getText();
            String purpose = (String) purposeDropdown.getSelectedItem();
            String duration = (String) durationDropdown.getSelectedItem();

            try {
                // Validate loan duration
                if (duration == null || duration.equals("select duration")) {
                    throw new IllegalArgumentException("You have to select a loan duration.");
                }
                // Validate loan amount
                if (amountText.isEmpty() || amountText.equals("Enter loan amount")) {
                    throw new IllegalArgumentException("Loan amount is required.");
                }
                double amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    throw new IllegalArgumentException("Loan amount must be a positive value.");
                }

                // Validate loan purpose
                if (purpose == null || purpose.equals("select loan purpose")) {
                    throw new IllegalArgumentException("You have to select a loan purpose.");
                }

                // If all validations pass
                JOptionPane.showMessageDialog(this, "Loan application submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid loan amount. Please enter a numeric value.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }

            try(Connection connection = DBConnection.getConnection()) {
                String insertLoan = "INSERT INTO Loan (loan_amount, loan_duration, loan_purpose, remaining_amount, customer_id) " +
                        "VALUES (?, ?, ?, ?, ?)";
                PreparedStatement stmt = connection.prepareStatement(insertLoan);
                stmt.setInt(1, Integer.parseInt(amountText));
                stmt.setString(2, duration);
                stmt.setString(3, purpose);
                stmt.setInt(4, Integer.parseInt(amountText));
                stmt.setInt(5, Session.getUser_id());
                stmt.executeUpdate();
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
        });

        // Cancel button action
        cancelButton.addActionListener(e -> dispose());

        // Set visibility
        setVisible(true);
    }

    // Method to create placeholder text field
    private JTextField createPlaceholderField(String placeholder, Font font) {
        JTextField textField = new JTextField(placeholder);
        textField.setFont(font);
        textField.setForeground(Color.GRAY);
        textField.setPreferredSize(new Dimension(200, 30)); // Dynamic size

        // Add focus listeners for placeholder functionality
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

    public static void main(String[] args) {
        new LoanApplication();
    }
}