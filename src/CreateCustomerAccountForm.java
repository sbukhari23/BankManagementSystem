import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class CreateCustomerAccountForm {

    Connection connection = DBConnection.getConnection();


    // Constructor
    public CreateCustomerAccountForm() throws SQLException {
        connection.setAutoCommit(false);

        String accountTitle = JOptionPane.showInputDialog(null, "Enter Account Title:", "Account Information", JOptionPane.PLAIN_MESSAGE);
        if (accountTitle == null || accountTitle.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Account Title is required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String balanceInput = JOptionPane.showInputDialog(null, "Enter Initial Balance:", "Account Information", JOptionPane.PLAIN_MESSAGE);
        if (balanceInput == null || balanceInput.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Balance is required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int balance;
        AtomicInteger counter = new AtomicInteger();
        try {
            balance = Integer.parseInt(balanceInput);
            if (balance < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Invalid balance. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Step 2: Ask if it's a joint account
        int isJoint = JOptionPane.showConfirmDialog(null, "Is this a joint account?", "Account Type", JOptionPane.YES_NO_OPTION);
        if (isJoint == JOptionPane.YES_OPTION) {
            String holdersInput = JOptionPane.showInputDialog(null, "Enter the number of account holders (including primary):", "Account Holders", JOptionPane.PLAIN_MESSAGE);
            if (holdersInput == null || holdersInput.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Number of account holders is required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int numHolders;
            try {
                numHolders = Integer.parseInt(holdersInput);
                if (numHolders < 2) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid number. Joint accounts require at least two holders.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            counter.set(numHolders);
        }
        else {
            counter.set(1);
        }

        // Get screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        // Create the frame
        JFrame frame = new JFrame("Create Customer Account");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Open in maximized mode
        frame.setResizable(true);
        frame.setLocationRelativeTo(null); // Center the frame

        // Background image
        JLabel background = new JLabel(new ImageIcon("images\\login_signup_background.png"));
        background.setLayout(null);
        frame.add(background);

        // Create a semi-transparent panel for the form
        JPanel formPanel = new JPanel();
        formPanel.setBackground(new Color(255, 255, 255, 216));
        formPanel.setLayout(new GridLayout(13, 2, 10, 10));  // Increased row count for the title
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Center the form panel dynamically based on screen size
        int panelWidth = 400;
        int panelHeight = 550;  // Increased height to fit the title
        formPanel.setBounds((screenWidth / 2 - panelWidth / 2), (screenHeight / 2 - panelHeight / 2), panelWidth, panelHeight);
        background.add(formPanel);

        // Create title label and add it to the top of the form
        JLabel titleLabel = new JLabel("Create Customer Account", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.BLACK);
        formPanel.add(titleLabel);  // Adding the title to the form panel
        formPanel.add(new JLabel());  // Empty cell for layout adjustment

        // Add form fields with placeholders
        formPanel.add(new JLabel("CNIC (13 digits):"));
        JTextField cnicField = createTextField("Enter CNIC");
        formPanel.add(cnicField);

        formPanel.add(new JLabel("Name:"));
        JTextField nameField = createTextField("Enter your name");
        formPanel.add(nameField);

        formPanel.add(new JLabel("Contact Number:"));
        JTextField contactField = createTextField("Enter contact number");
        formPanel.add(contactField);

        formPanel.add(new JLabel("Email:"));
        JTextField emailField = createTextField("Enter email");
        formPanel.add(emailField);

        formPanel.add(new JLabel("Date of Birth (YYYY-MM-DD):"));
        JTextField dobField = createTextField("Enter date of birth");
        formPanel.add(dobField);

        formPanel.add(new JLabel("Street Address:"));
        JTextField streetField = createTextField("Enter street address");
        formPanel.add(streetField);

        formPanel.add(new JLabel("City:"));
        JTextField cityField = createTextField("Enter city");
        formPanel.add(cityField);

        formPanel.add(new JLabel("State:"));
        JTextField stateField = createTextField("Enter state");
        formPanel.add(stateField);

        // Create Submit and Cancel buttons and add them directly to the panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); // Center buttons

        JButton submitButton = new JButton("Submit");
        JButton cancelButton = new JButton("Cancel");

        // Add button actions
        cancelButton.addActionListener(e -> {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            frame.dispose();
        });


        // Add checkbox to specify if the customer already has an account
        JCheckBox existingCustomerCheckBox = new JCheckBox("Existing Customer");
        existingCustomerCheckBox.setBackground(new Color(255, 255, 255, 216));
        formPanel.add(existingCustomerCheckBox);
        formPanel.add(new JLabel()); // Empty label for layout adjustment

// Add listeners to toggle fields
        existingCustomerCheckBox.addItemListener(e -> {
            boolean isExisting = existingCustomerCheckBox.isSelected();
            nameField.setEnabled(!isExisting);
            contactField.setEnabled(!isExisting);
            emailField.setEnabled(!isExisting);
            dobField.setEnabled(!isExisting);
            streetField.setEnabled(!isExisting);
            cityField.setEnabled(!isExisting);
            stateField.setEnabled(!isExisting);
        });

        final boolean[] isAccountGenerated = {false};
        AtomicReference<String> account_number = new AtomicReference<>("");

        submitButton.addActionListener(e -> {
            // Extract data
            String cnic = cnicField.getText().trim();
            String name = nameField.getText().trim();
            String contact = contactField.getText().trim();
            String email = emailField.getText().trim();
            String dob = dobField.getText().trim();

            // Format validation
            if (!cnic.matches("\\d{13}")) {
                JOptionPane.showMessageDialog(frame, "Invalid CNIC. It must be a 13-digit numeric string.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if(!existingCustomerCheckBox.isSelected()){

                if (!name.matches("[a-zA-Z ]+")) {
                    JOptionPane.showMessageDialog(frame, "Invalid Name. Only alphabets and spaces are allowed.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!contact.matches("\\d{1,15}")) {
                    JOptionPane.showMessageDialog(frame, "Invalid Contact Number. It must be a numeric string with up to 15 digits.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!email.contains("@") || email.startsWith("@") || email.endsWith("@")) {
                    JOptionPane.showMessageDialog(frame, "Invalid Email. It must contain a valid '@' symbol.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!dob.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    JOptionPane.showMessageDialog(frame, "Invalid Date of Birth. Format must be YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }


            try{

                Statement statement = connection.createStatement();
                statement.execute("SET @current_user_id = " + Session.getUser_id());

                if (existingCustomerCheckBox.isSelected()) {
                    // Existing customer logic
                    if (cnic.isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "CNIC required for existing customers.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    System.out.println(isAccountGenerated[0]);
                    if (!isAccountGenerated[0]) {
                        account_number.set(generateAccountNumber(cnic));
                        isAccountGenerated[0] = true;
                        String checkCNIC = "SELECT customer_id FROM customer WHERE cnic = ?";
                        PreparedStatement stmt = connection.prepareStatement(checkCNIC);
                        stmt.setString(1, cnic);
                        ResultSet rs = stmt.executeQuery();

                        if (!rs.next()) {
                            JOptionPane.showMessageDialog(frame, "Not an existing customer");
                            return;
                        }

                        // Get branch ID for the employee
                        String getBranch = "SELECT get_branch_id(?)";
                        stmt = connection.prepareStatement(getBranch);
                        stmt.setInt(1, Session.getUser_id());
                        rs = stmt.executeQuery();
                        int branch_id = rs.next() ? rs.getInt(1) : -1;

                        // Insert into Account table
                        String insertAccount = "INSERT INTO Account (account_number, balance, branch_id, account_title)" +
                                " VALUES (?, ?, ?, ?)";
                        stmt = connection.prepareStatement(insertAccount);
                        stmt.setString(1, account_number.get());
                        stmt.setInt(2, balance);
                        stmt.setInt(3, branch_id);
                        stmt.setString(4, accountTitle);
                        stmt.executeUpdate();
                    }

                    // Get customer ID
                    String getCID = "SELECT get_cid(?)";
                    PreparedStatement stmt = connection.prepareStatement(getCID);
                    stmt.setString(1, cnic);
                    ResultSet rs = stmt.executeQuery();
                    rs.next();
                    int customer_id = rs.getInt(1);

                    // Insert into AccountCustomer table
                    String insertAccCustomer = "INSERT INTO AccountCustomer (customer_id, account_number) VALUES (?, ?)";
                    stmt = connection.prepareStatement(insertAccCustomer);
                    stmt.setInt(1, customer_id);
                    stmt.setString(2, account_number.get());
                    stmt.executeUpdate();

//                    JOptionPane.showMessageDialog(frame, "Account Created for Existing Customer!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // New customer logic (existing code for new customer creation)
                    String street = streetField.getText().trim();
                    String city = cityField.getText().trim();
                    String state = stateField.getText().trim();

                    if (cnic.isEmpty() || name.isEmpty() || contact.isEmpty() || email.isEmpty() || dob.isEmpty() ||
                            street.isEmpty() || city.isEmpty() || state.isEmpty() || accountTitle.isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "All fields are required for new customers.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String checkCNIC = "SELECT customer_id FROM customer WHERE cnic = ?";
                    PreparedStatement stmt = connection.prepareStatement(checkCNIC);
                    stmt.setString(1, cnic);
                    ResultSet rs = stmt.executeQuery();

                    if(rs.next()) {
                        JOptionPane.showMessageDialog(null, "CNIC already exists. Please check the box for existing customers");
                        return;
                    }

                    if (!isAccountGenerated[0]) {
                        account_number.set(generateAccountNumber(cnic));
                        isAccountGenerated[0] = true;

                        // Get branch ID for the employee
                        String getBranch = "SELECT get_branch_id(?)";
                        stmt = connection.prepareStatement(getBranch);
                        stmt.setInt(1, Session.getUser_id());
                        rs = stmt.executeQuery();
                        int branch_id = rs.next() ? rs.getInt(1) : -1;

                        // Insert into Account table
                        String insertAccount = "INSERT INTO Account (account_number, balance, branch_id, account_title)" +
                                " VALUES (?, ?, ?, ?)";
                        stmt = connection.prepareStatement(insertAccount);
                        stmt.setString(1, account_number.get());
                        stmt.setInt(2, balance);
                        stmt.setInt(3, branch_id);
                        stmt.setString(4, accountTitle);
                        stmt.executeUpdate();
                    }

                    String insertCustomer = "INSERT INTO Customer (cnic, name, contact_number, email, dob, street, city, state)" +
                            " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    stmt = connection.prepareStatement(insertCustomer);
                    stmt.setString(1, cnic);
                    stmt.setString(2, name);
                    stmt.setString(3, contact);
                    stmt.setString(4, email);
                    stmt.setString(5, dob);
                    stmt.setString(6, street);
                    stmt.setString(7, city);
                    stmt.setString(8, state);
                    stmt.executeUpdate();

                    String getCID = "SELECT get_cid(?)";
                    stmt = connection.prepareStatement(getCID);
                    stmt.setString(1, cnic);
                    rs = stmt.executeQuery();
                    rs.next();

                    String insertAccCustomer = "INSERT INTO AccountCustomer VALUES (?, ?)";
                    stmt = connection.prepareStatement(insertAccCustomer);
                    stmt.setInt(1, rs.getInt(1));
                    stmt.setString(2, account_number.get());
                    stmt.executeUpdate();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                try {
                    connection.rollback();
                } catch (SQLException exc) {
                    throw new RuntimeException(exc);
                }
            }

            counter.getAndDecrement();

            if (counter.get() == 0) {
                frame.dispose();
                try {
                    connection.commit();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                JOptionPane.showMessageDialog(null, "Account created");
            }

            clearTextFields(formPanel);

        });



        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

        // Add button panel to the form panel
        formPanel.add(new JLabel());  // Empty label to take up space
        formPanel.add(buttonPanel);

        // Remove focus from text fields when clicking elsewhere
        background.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                background.requestFocusInWindow();
            }
        }

        );

        // Set frame visibility
        frame.setVisible(true);
    }

    // Create text fields with placeholder functionality
    private JTextField createTextField(String placeholder) {
        JTextField textField = new JTextField(placeholder);
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

    public static String generateAccountNumber(String cnic) {
        // Extract the last 6 digits of the CNIC
        String cnicPart = cnic.substring(cnic.length() - 7);

        // Generate a 4-digit random number
        Random random = new Random();
        int randomNumber = 10000 + random.nextInt(90000); // Range: 1000-9999

        // Combine components to create account number
        return cnicPart + randomNumber;
    }

    private void clearTextFields(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JTextField) {
                ((JTextField) component).setText("");
            } else if (component instanceof Container) {
                clearTextFields((Container) component); // Recursively check child containers
            }
        }
    }

    // Main method
    public static void main(String[] args) throws SQLException {
        new CreateCustomerAccountForm();
    }
}