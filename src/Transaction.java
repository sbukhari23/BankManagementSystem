import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

class Transaction extends JFrame {

    public Transaction() {
        // Get screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        // Set JFrame properties
        setTitle("Transaction Processing");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        // Background image
        JLabel background = new JLabel(new ImageIcon("images/login_signup_background.png"));
        background.setLayout(null);
        add(background, BorderLayout.CENTER);

        // Create a semi-transparent panel
        JPanel transactionPanel = new JPanel();
        transactionPanel.setBackground(new Color(255, 255, 255, 216)); // Semi-transparent white
        transactionPanel.setLayout(null);

        // Center the transaction panel
        int panelWidth = 500;
        int panelHeight = 400;
        transactionPanel.setBounds((screenWidth / 2 - panelWidth / 2), (screenHeight / 2 - panelHeight / 2), panelWidth, panelHeight);
        background.add(transactionPanel);

        // Bank logo at the top
        JLabel logoLabel = new JLabel("", SwingConstants.CENTER);
        logoLabel.setBounds(220, 20, 66, 66);
        logoLabel.setIcon(new ImageIcon("images/bank_logo_small.png"));
        transactionPanel.add(logoLabel);

        // Title
        JLabel titleLabel = new JLabel("Money Transfer", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setBounds(100, 100, 300, 30);
        transactionPanel.add(titleLabel);

        // Receiver Account Number
        JLabel receiverLabel = new JLabel("Receiver Account Number:");
        receiverLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        receiverLabel.setBounds(50, 150, 200, 30);
        transactionPanel.add(receiverLabel);

        JTextField receiverField = new JTextField();
        receiverField.setBounds(50, 180, 400, 35);
        receiverField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        transactionPanel.add(receiverField);

        // Amount
        JLabel amountLabel = new JLabel("Amount:");
        amountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        amountLabel.setBounds(50, 230, 200, 30);
        transactionPanel.add(amountLabel);

        JTextField amountField = new JTextField();
        amountField.setBounds(50, 260, 400, 35);
        amountField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        transactionPanel.add(amountField);

        // Send Button
        JButton sendButton = new JButton("Send Money");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendButton.setBounds(150, 320, 200, 40);
        sendButton.setBackground(new Color(51, 153, 255));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false);
        sendButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Add hover effect
        sendButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                sendButton.setBackground(new Color(0, 102, 204));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                sendButton.setBackground(new Color(51, 153, 255));
            }
        });

        transactionPanel.add(sendButton);

        // Keep the original action listener
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