import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoanApplicationNew extends JFrame {

    public LoanApplicationNew() {
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
        JLabel background = new JLabel(new ImageIcon("E:\\DBS Project\\NewBankManagementSystem\\BankManagementSystem\\images\\login_signup_background.png"));
        background.setLayout(null);
        add(background, BorderLayout.CENTER);

        // Create a semi-transparent panel for loan application form
        JPanel formPanel = new JPanel();
        formPanel.setBackground(new Color(255, 255, 255, 216)); // Semi-transparent white
        formPanel.setLayout(null);

        // Center the form panel
        int panelWidth = 400;
        int panelHeight = 600;
        formPanel.setBounds((screenWidth / 2 - panelWidth / 2), (screenHeight / 2 - panelHeight / 2), panelWidth, panelHeight);
        background.add(formPanel);

        // Custom fonts and placeholders
        Font fieldFont = new Font("Arial", Font.PLAIN, 16);

        // Create input fields
        JTextField nameField = createPlaceholderField("Enter your full name", fieldFont);
        nameField.setBounds(100, 80, 200, 30);
        formPanel.add(nameField);

        JTextField cnicField = createPlaceholderField("Enter CNIC (13 digits)", fieldFont);
        cnicField.setBounds(100, 400, 200, 30);
        formPanel.add(cnicField);

        JTextField amountField = createPlaceholderField("Enter loan amount", fieldFont);
        amountField.setBounds(100, 120, 200, 30);
        formPanel.add(amountField);

        // Dropdown for loan purpose
        String[] loanPurposes = {"select loan purpose", "home", "car", "personal", "educational", "corporate", "others"};
        JComboBox<String> purposeDropdown = new JComboBox<>(loanPurposes);
        purposeDropdown.setFont(fieldFont);
        purposeDropdown.setBounds(100, 160, 200, 30);
        formPanel.add(purposeDropdown);

        JTextField phoneField = createPlaceholderField("Enter phone number", fieldFont);
        phoneField.setBounds(100, 200, 200, 30);
        formPanel.add(phoneField);

        JTextField emailField = createPlaceholderField("Enter email address", fieldFont);
        emailField.setBounds(100, 240, 200, 30);
        formPanel.add(emailField);

        JTextField streetField = createPlaceholderField("Enter street", fieldFont);
        streetField.setBounds(100, 280, 200, 30);
        formPanel.add(streetField);

        JTextField cityField = createPlaceholderField("Enter city", fieldFont);
        cityField.setBounds(100, 320, 200, 30);
        formPanel.add(cityField);

        JTextField stateField = createPlaceholderField("Enter state", fieldFont);
        stateField.setBounds(100, 360, 200, 30);
        formPanel.add(stateField);

        JTextField dobField = createPlaceholderField("Enter DOB (YYYY-MM-DD)", fieldFont);
        dobField.setBounds(100, 440, 200, 30);
        formPanel.add(dobField);

        // Dropdown for loan duration
        String[] loanDurations = {"select duration", "6 months", "1 year"};
        JComboBox<String> durationDropdown = new JComboBox<>(loanDurations);
        durationDropdown.setFont(fieldFont);
        durationDropdown.setBounds(100, 485, 200, 30); // Adjust the position and size as needed
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
        buttonPanel.setBounds(100, 520, 200, 30);
        formPanel.add(buttonPanel);

        // Redirect focus away from text fields when clicking elsewhere
        background.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                background.requestFocusInWindow(); // Redirect focus
            }
        });




        // Submit button action
        submitButton.addActionListener(e -> {
            String name = nameField.getText();
            String amountText = amountField.getText();
            String purpose = (String) purposeDropdown.getSelectedItem();
            String phone = phoneField.getText();
            String email = emailField.getText();
            String street = streetField.getText();
            String city = cityField.getText();
            String state = stateField.getText();
            String cnic = cnicField.getText();
            String dob = dobField.getText();
            String duration = (String) durationDropdown.getSelectedItem();


            try {
                // Validate loan duration
                if (duration == null || duration.equals("select duration")) {
                    throw new IllegalArgumentException("You have to select a loan duration.");
                }

                // Validate CNIC
                if (cnic.isEmpty() || cnic.equals("Enter CNIC (13 digits)")) {
                    throw new IllegalArgumentException("CNIC is required.");
                }
                if (!cnicField.getText().matches("\\d{13}")) {
                    throw new IllegalArgumentException("CNIC must be a numeric string with exactly 13 digits.");
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

                // Validate phone number
                if (phone.isEmpty() || phone.equals("Enter phone number")) {
                    throw new IllegalArgumentException("Phone number is required.");
                }
                if (!phone.matches("\\d{1,15}")) {
                    throw new IllegalArgumentException("Phone number must be a numeric string with a maximum of 15 digits.");
                }

                // Validate email
                if (email.isEmpty() || email.equals("Enter email address")) {
                    throw new IllegalArgumentException("Email address is required.");
                }
                if (!email.contains("@")) {
                    throw new IllegalArgumentException("Email address must contain '@'.");
                }

                // Validate DOB
                if (dob.isEmpty() || dob.equals("Enter DOB (YYYY-MM-DD)")) {
                    throw new IllegalArgumentException("Date of Birth is required.");
                }
                if (!dob.matches("\\d{4}-\\d{2}-\\d{2}") || !CreateCustomerAccountForm.isValidDate(dob, "yyyy-MM-dd")) {
                    throw new IllegalArgumentException("DOB must be in the format YYYY-MM-DD.");
                }

                // Validate all other fields
                if (name.isEmpty() || name.equals("Enter your full name") ||
                        street.isEmpty() || street.equals("Enter street") ||
                        city.isEmpty() || city.equals("Enter city") ||
                        state.isEmpty() || state.equals("Enter state")) {
                    throw new IllegalArgumentException("All fields are required.");
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
                String checkCNIC = "SELECT customer_id FROM Customer WHERE cnic = ?";
                PreparedStatement stmt = connection.prepareStatement(checkCNIC);
                stmt.setString(1, cnic);
                ResultSet rs = stmt.executeQuery();

                boolean existingCustomer = false;
                if(rs.next()) {
                    String checkCustomer = "SELECT c.customer_id FROM Customer c NATURAL JOIN AccountCustomer WHERE customer_id = ?";
                    stmt = connection.prepareStatement(checkCustomer);
                    stmt.setInt(1, rs.getInt("customer_id"));
                    rs = stmt.executeQuery();

                    if(rs.next()) {
                        JOptionPane.showMessageDialog(null, "Existing customers are required to login through the app");
                    }
                    else {
                        existingCustomer = true;
                    }
                }

                if(!existingCustomer) {
                    String insertCustomer = "INSERT INTO Customer (name, contact_number, email, dob, street, city, state, cnic) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    stmt = connection.prepareStatement(insertCustomer);
                    stmt.setString(1, name);
                    stmt.setString(2, phone);
                    stmt.setString(3, email);
                    stmt.setString(4, dob);
                    stmt.setString(5, street);
                    stmt.setString(6, city);
                    stmt.setString(7, state);
                    stmt.setString(8, cnic);
                    stmt.executeUpdate();
                }

                String getID = "SELECT customer_id FROM Customer WHERE cnic = ?";
                stmt = connection.prepareStatement(getID);
                stmt.setString(1, cnic);
                rs = stmt.executeQuery();

                String insertLoan = "INSERT INTO Loan (loan_amount, loan_purpose, remaining_amount, customer_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
                stmt = connection.prepareStatement(insertLoan);
                stmt.setInt(1, Integer.parseInt(amountText));
                stmt.setString(2, purpose);
                stmt.setInt(3, Integer.parseInt(amountText));
                stmt.setInt(4, rs.getInt("customer_id"));
                stmt.executeUpdate();
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
        });

        // Cancel button action
        cancelButton.addActionListener(e -> {dispose();
            new SignupPage();
        });

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
        new LoanApplicationNew();
    }
}
