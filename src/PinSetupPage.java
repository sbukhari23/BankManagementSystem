import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class PinSetupPage extends JFrame {

    public PinSetupPage() {
        // Set JFrame properties
        setTitle("Setup your PIN (6 Digits)");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setResizable(false);

        // Add background image (top portion)
        JLabel background = new JLabel(new ImageIcon("images\\pin_setup_background_small.jpg"));
        background.setBounds(0, 0, 500, 150); // Image size fixed at the top
        add(background);

        // Add title label below the image
        JLabel titleLabel = new JLabel("Setup your PIN", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setBounds(0, 110, 500, 30); // Moved title below the image
        add(titleLabel);

        // Create panel for PIN setup (Moved higher)
        JPanel pinPanel = new JPanel();
        pinPanel.setBounds(100, 150, 300, 80); // Adjusted position
        pinPanel.setLayout(new GridLayout(1, 6, 10, 10)); // 6 fields with space between
        pinPanel.setOpaque(false); // Transparent background

        // Create text fields for PIN input (6 fields)
        JTextField[] pinFields = new JTextField[6];
        for (int i = 0; i < 6; i++) {
            pinFields[i] = new JTextField();
            pinFields[i].setHorizontalAlignment(JTextField.CENTER);
            pinFields[i].setFont(new Font("Arial", Font.PLAIN, 20));
            pinFields[i].setPreferredSize(new Dimension(40, 40));
            pinFields[i].setDocument(new JTextFieldLimit(1));  // Only 1 character per field
            pinFields[i].setBackground(Color.WHITE);
            pinFields[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));

            // Add key listener to move to next field
            final int index = i;
            pinFields[i].addKeyListener(new KeyAdapter() {
                public void keyReleased(KeyEvent e) {
                    if (Character.isDigit(e.getKeyChar()) && pinFields[index].getText().length() == 1) {
                        if (index < 5) {
                            pinFields[index + 1].requestFocus();
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                        if (pinFields[index].getText().length() == 0 && index > 0) {
                            pinFields[index - 1].requestFocus();
                        }
                    }
                }
            });

            pinPanel.add(pinFields[i]);
        }
        add(pinPanel);

        // Create confirm PIN label (Moved higher)
        JLabel confirmLabel = new JLabel("Confirm your PIN", JLabel.CENTER);
        confirmLabel.setFont(new Font("Arial", Font.BOLD, 18));
        confirmLabel.setForeground(Color.BLACK);
        confirmLabel.setBounds(0, 230, 500, 30); // Moved confirm label higher
        add(confirmLabel);

        // Create panel for confirming PIN setup
        JPanel confirmPinPanel = new JPanel();
        confirmPinPanel.setBounds(100, 260, 300, 80); // Moved confirm PIN fields up
        confirmPinPanel.setLayout(new GridLayout(1, 6, 10, 10));
        confirmPinPanel.setOpaque(false);

        // Create text fields for confirming PIN (6 fields)
        JTextField[] confirmPinFields = new JTextField[6];
        for (int i = 0; i < 6; i++) {
            confirmPinFields[i] = new JTextField();
            confirmPinFields[i].setHorizontalAlignment(JTextField.CENTER);
            confirmPinFields[i].setFont(new Font("Arial", Font.PLAIN, 20));
            confirmPinFields[i].setPreferredSize(new Dimension(40, 40));
            confirmPinFields[i].setDocument(new JTextFieldLimit(1));  // Only 1 character per field
            confirmPinFields[i].setBackground(Color.WHITE);
            confirmPinFields[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));

            // Add key listener to move to next field
            final int index = i;
            confirmPinFields[i].addKeyListener(new KeyAdapter() {
                public void keyReleased(KeyEvent e) {
                    if (Character.isDigit(e.getKeyChar()) && confirmPinFields[index].getText().length() == 1) {
                        if (index < 5) {
                            confirmPinFields[index + 1].requestFocus();
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                        if (confirmPinFields[index].getText().length() == 0 && index > 0) {
                            confirmPinFields[index - 1].requestFocus();
                        }
                    }
                }
            });

            confirmPinPanel.add(confirmPinFields[i]);
        }
        add(confirmPinPanel);

        // Create submit button
        JButton submitButton = new JButton("Submit");
        submitButton.setFont(new Font("Arial", Font.BOLD, 14));
        submitButton.setBounds(200, 350, 100, 40); // Adjusted submit button position
        add(submitButton);

        // Submit button action listener
        submitButton.addActionListener(e -> {
            String pin = getPinFromFields(pinFields);
            String confirmPin = getPinFromFields(confirmPinFields);

            // Validate PIN length and format
            if (pin.length() != 6 || !isValidPin(pin)) {
                JOptionPane.showMessageDialog(this, "Invalid PIN. PIN must be 6 digits and cannot be a sequence.");
                return;
            }

            if (!pin.equals(confirmPin)) {
                JOptionPane.showMessageDialog(this, "PINs do not match. Please try again.");
                return;
            }

            insertPin(pin);
        });

        // Set the window to be visible
        setVisible(true);
    }

    private void insertPin(String pin) {
        try(Connection connection = DBConnection.getConnection()) {
            String insert_pin = "UPDATE customer SET transaction_pin = ? WHERE customer_id = ?";
            PreparedStatement stmt = connection.prepareStatement(insert_pin);
            stmt.setString(1, pin);
            stmt.setInt(2, Session.getUser_id());
            int rows = stmt.executeUpdate();
            if(rows > 0) {
                JOptionPane.showMessageDialog(this, "PIN successfully set up!");
                this.dispose(); // Close the current window
                new CustomerDashboard(); // Open the login page
            }
            else {
                JOptionPane.showMessageDialog(this, "Failed to setup PIN");
            }
        }
        catch(Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to setup PIN");
            e.printStackTrace();
        }
    }

    // Helper method to get the PIN from the fields
    private String getPinFromFields(JTextField[] fields) {
        StringBuilder pin = new StringBuilder();
        for (JTextField field : fields) {
            pin.append(field.getText());
        }
        return pin.toString();
    }

    // Method to check if the PIN is valid (not a sequence)
    private boolean isValidPin(String pin) {
        if (pin.length() != 6) {
            return false;
        }

        // Check if PIN is a sequence of digits
        for (int i = 1; i < pin.length(); i++) {
            if (pin.charAt(i) != pin.charAt(i - 1) + 1) {
                return true;
            }
        }
        return false;
    }

    // JTextFieldLimit class to limit number of characters
    private static class JTextFieldLimit extends javax.swing.text.PlainDocument {
        private final int limit;

        JTextFieldLimit(int limit) {
            this.limit = limit;
        }

        public void insertString(int offset, String str, javax.swing.text.AttributeSet attr) throws javax.swing.text.BadLocationException {
            if (str == null) {
                return;
            }

            if ((getLength() + str.length()) <= limit) {
                super.insertString(offset, str, attr);
            }
        }
    }

    public static void main(String[] args) {
        new PinSetupPage();
    }
}
