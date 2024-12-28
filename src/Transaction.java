import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

class Transaction extends JFrame {

    public Transaction() {
        setTitle("Transaction Processing");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        JPanel transactionPanel = new JPanel();
        transactionPanel.setLayout(null);
        transactionPanel.setBackground(Color.LIGHT_GRAY);

        JLabel receiverLabel = new JLabel("Receiver Account Number:");
        receiverLabel.setBounds(50, 50, 200, 30);
        transactionPanel.add(receiverLabel);

        JTextField receiverField = new JTextField();
        receiverField.setBounds(250, 50, 200, 30);
        transactionPanel.add(receiverField);

        JLabel amountLabel = new JLabel("Amount:");
        amountLabel.setBounds(50, 100, 200, 30);
        transactionPanel.add(amountLabel);

        JTextField amountField = new JTextField();
        amountField.setBounds(250, 100, 200, 30);
        transactionPanel.add(amountField);

        JButton sendButton = new JButton("Send");
        sendButton.setBounds(180, 200, 100, 30);
        transactionPanel.add(sendButton);

        sendButton.addActionListener(e -> {
            String receiverAccount = receiverField.getText().trim();
            String amountText = amountField.getText().trim();

            if (receiverAccount.isEmpty() || amountText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.");
                return;
            }

            if (!receiverAccount.matches("\\d{10,20}")) {
                JOptionPane.showMessageDialog(this, "Invalid account number format.");
                return;
            }

            try {
                int amount = Integer.parseInt(amountText);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Amount must be positive.");
                } else {
                    new PinValidation(receiverAccount, amount);
                    this.dispose();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount format.");
            }
        });

        add(transactionPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    static void transactionHandler(String toAccountNo, int amount) {
        if (toAccountNo.equals(Session.getAccount_no())) {
            JOptionPane.showMessageDialog(null, "Cannot send money to your own account.");
            return;
        }

        Connection connection = null; // Declare connection outside the try block

        try {
            connection = DBConnection.getConnection();
            connection.setAutoCommit(false); // Disable auto-commit

            String validateToAccountNo = "SELECT * FROM Account WHERE account_number = ? AND status = ?";
            PreparedStatement stmt = connection.prepareStatement(validateToAccountNo);
            stmt.setString(1, toAccountNo);
            stmt.setString(2, "open");
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(null, "Account number does not exist or has been closed.");
                connection.rollback(); // Rollback transaction
                return;
            }

            int balance = getBalance(Session.getAccount_no());
            if (balance < amount) {
                JOptionPane.showMessageDialog(null, "Insufficient balance.");
                connection.rollback(); // Rollback transaction
                return;
            }

            // Deduct from sender
            String deductBalance = "UPDATE Account SET balance = balance - ? WHERE account_number = ?";
            stmt = connection.prepareStatement(deductBalance);
            stmt.setInt(1, amount);
            stmt.setString(2, Session.getAccount_no());
            stmt.executeUpdate();

            // Add to receiver
            String addBalance = "UPDATE Account SET balance = balance + ? WHERE account_number = ?";
            stmt = connection.prepareStatement(addBalance);
            stmt.setInt(1, amount);
            stmt.setString(2, toAccountNo);
            stmt.executeUpdate();

            // Record transaction
            String insertTransaction = "INSERT INTO Transaction (to_account_number, from_account_number, amount) VALUES (?, ?, ?)";
            stmt = connection.prepareStatement(insertTransaction);
            stmt.setString(1, toAccountNo);
            stmt.setString(2, Session.getAccount_no());
            stmt.setInt(3, amount);
            stmt.executeUpdate();

            connection.commit(); // Commit transaction
            JOptionPane.showMessageDialog(null, "Transaction successful.");
            CustomerDashboard.updateBalance();
        } catch (Exception e) {
            try {
                if (connection != null) {
                    connection.rollback(); // Rollback transaction on error
                }
            } catch (Exception rollbackEx) {
                JOptionPane.showMessageDialog(null, "Rollback failed.");
            }
            JOptionPane.showMessageDialog(null, "Transaction failed. Please try again.");
        } finally {
            if (connection != null) {
                try {
                    connection.close(); // Ensure the connection is closed
                } catch (Exception closeEx) {
                    JOptionPane.showMessageDialog(null, "Failed to close the connection.");
                }
            }
        }
    }


    static int getBalance(String accountNo) {
        try (Connection connection = DBConnection.getConnection()) {
            String getBalance = "SELECT balance FROM Account WHERE account_number = ?";
            PreparedStatement stmt = connection.prepareStatement(getBalance);
            stmt.setString(1, accountNo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("balance");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Unable to retrieve balance.");
        }
        return -1;
    }

    public static void main(String[] args) {
        new Transaction();
    }
}