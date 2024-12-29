import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PinValidation extends JFrame {

    private static boolean validatePin(String pin) {
        boolean isValid = false;
        int customerID = Session.getUser_id();
        try (Connection connection = DBConnection.getConnection()) {
            String getPin = "SELECT transaction_pin FROM customer WHERE customer_id = ?";

            PreparedStatement stmt = connection.prepareStatement(getPin);
            stmt.setInt(1, customerID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String actualPin = rs.getString("transaction_pin");
                isValid = pin.equals(actualPin);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to connect to the database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return isValid;

    }

    public PinValidation(String receiverAccount, int amount) {
        setTitle("Verify your PIN (6 Digits)");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setResizable(false);

        JLabel background = new JLabel(new ImageIcon("C:\\Users\\DELL\\OneDrive\\Desktop\\NewBankManagementSystem3\\NewBankManagementSystem\\BankManagementSystem\\images\\pin_setup_background_small.jpg"));
        background.setBounds(0, 0, 500, 150);
        add(background);

        JLabel titleLabel = new JLabel("Verify your PIN", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setBounds(0, 110, 500, 30);
        add(titleLabel);

        JPanel pinPanel = new JPanel();
        pinPanel.setBounds(100, 150, 300, 80);
        pinPanel.setLayout(new GridLayout(1, 6, 10, 10));
        pinPanel.setOpaque(false);

        JTextField[] pinFields = new JTextField[6];
        for (int i = 0; i < 6; i++) {
            pinFields[i] = new JTextField();
            pinFields[i].setHorizontalAlignment(JTextField.CENTER);
            pinFields[i].setFont(new Font("Arial", Font.PLAIN, 20));
            pinFields[i].setPreferredSize(new Dimension(40, 40));
            pinFields[i].setDocument(new JTextFieldLimit(1));
            pinFields[i].setBackground(Color.WHITE);
            pinFields[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));

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

        JButton submitButton = new JButton("Submit");
        submitButton.setFont(new Font("Arial", Font.BOLD, 14));
        submitButton.setBounds(200, 250, 100, 40);
        add(submitButton);

        submitButton.addActionListener(e -> {
            String enteredPin = getPinFromFields(pinFields);

            if (enteredPin.length() != 6) {
                JOptionPane.showMessageDialog(this, "PIN must be 6 digits.");
                return;
            }

            if (validatePin(enteredPin)) {
                Transaction.transactionHandler(receiverAccount, amount);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Incorrect PIN. Returning to Dashboard.");
                this.dispose();
            }
        });

        centerWindow();
        setVisible(true);
    }

    private String getPinFromFields(JTextField[] fields) {
        StringBuilder pin = new StringBuilder();
        for (JTextField field : fields) {
            pin.append(field.getText());
        }
        return pin.toString();
    }

    private void centerWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - getWidth()) / 2;
        int y = (screenSize.height - getHeight()) / 2;
        setLocation(x, y);
    }

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
}


