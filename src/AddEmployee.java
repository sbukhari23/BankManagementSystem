import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class AddEmployee {
    AddEmployee() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Add Employee");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(600, 700);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout(10, 10));
            mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

            JPanel formPanel = new JPanel();
            formPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Form fields
            JLabel nameLabel = new JLabel("Name:");
            JTextField nameField = new JTextField();
            addField(formPanel, nameLabel, nameField, gbc, 0);

            JLabel cnicLabel = new JLabel("CNIC (13 digits):");
            JTextField cnicField = new JTextField();
            addField(formPanel, cnicLabel, cnicField, gbc, 1);

            JLabel roleLabel = new JLabel("Role:");
            JTextField roleField = new JTextField();
            addField(formPanel, roleLabel, roleField, gbc, 2);

            JLabel contactLabel = new JLabel("Contact Number:");
            JTextField contactField = new JTextField();
            addField(formPanel, contactLabel, contactField, gbc, 3);

            JLabel dobLabel = new JLabel("Date of Birth(YYYY-MM-DD):");
            JTextField dobField = new JTextField();
            addField(formPanel, dobLabel, dobField, gbc, 4);

            JLabel usernameLabel = new JLabel("Username:");
            JTextField usernameField = new JTextField();
            addField(formPanel, usernameLabel, usernameField, gbc, 5);

            JLabel passwordLabel = new JLabel("Password:");
            JPasswordField passwordField = new JPasswordField();
            addField(formPanel, passwordLabel, passwordField, gbc, 6);

            JLabel streetLabel = new JLabel("Street:");
            JTextField streetField = new JTextField();
            addField(formPanel, streetLabel, streetField, gbc, 7);

            JLabel cityLabel = new JLabel("City:");
            JTextField cityField = new JTextField();
            addField(formPanel, cityLabel, cityField, gbc, 8);

            JLabel stateLabel = new JLabel("State:");
            JTextField stateField = new JTextField();
            addField(formPanel, stateLabel, stateField, gbc, 9);

            // Add button and message
            JButton addButton = new JButton("Add Employee");
            JLabel messageLabel = new JLabel("", SwingConstants.CENTER);
            messageLabel.setForeground(Color.RED);

            addButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String name = nameField.getText().trim();
                    String cnic = cnicField.getText().trim();
                    String role = roleField.getText().trim();
                    String dob = dobField.getText().trim();
                    String contact = contactField.getText().trim();
                    String username = usernameField.getText().trim();
                    String password = new String(passwordField.getPassword()).trim();
                    String street = streetField.getText().trim();
                    String city = cityField.getText().trim();
                    String state = stateField.getText().trim();

                    if (name.isEmpty() || cnic.isEmpty() || role.isEmpty() || dob.isEmpty() || contact.isEmpty() || username.isEmpty() || password.isEmpty() || street.isEmpty() || city.isEmpty() || state.isEmpty()) {
                        messageLabel.setText("All fields are required.");
                        return;
                    }

                    if (!name.matches("[a-zA-Z ]+")) {
                        JOptionPane.showMessageDialog(frame, "Invalid Name. Only alphabets and spaces are allowed.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (!cnic.matches("\\d{13}")) {
                        messageLabel.setText("CNIC must be a 13-digit numeric string.");
                        return;
                    }

                    if (!dob.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        JOptionPane.showMessageDialog(frame, "Invalid Date of Birth. Format must be YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (!contact.matches("\\d{1,15}")) {
                        JOptionPane.showMessageDialog(frame, "Invalid Contact Number. It must be a numeric string with up to 15 digits.", "Error", JOptionPane.ERROR_MESSAGE);
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
                    frame.dispose();
                }
            });

            JPanel buttonPanel = new JPanel(new BorderLayout());
            buttonPanel.add(addButton, BorderLayout.CENTER);
            buttonPanel.add(messageLabel, BorderLayout.SOUTH);

            mainPanel.add(formPanel, BorderLayout.CENTER);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            frame.add(mainPanel);
            frame.setVisible(true);
        });
    }

    private static void addField(JPanel panel, JComponent label, JComponent field, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
        field.setPreferredSize(new Dimension(300, field.getPreferredSize().height));
    }
}
