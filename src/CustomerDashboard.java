import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

public class CustomerDashboard extends JFrame {
    private static JLabel balanceLabel;
    public CustomerDashboard() {
        // Get screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        // Set JFrame properties
        setTitle("Customer Dashboard");
        setSize(screenWidth, screenHeight);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Open in maximized mode
        setLayout(null);
        setResizable(false);
        setLocationRelativeTo(null);

        // Background image path
        String backgroundImagePath = "images\\login_signup_background.png";
        ImageIcon backgroundImage = new ImageIcon(backgroundImagePath);
        JLabel backgroundLabel = new JLabel(backgroundImage);
        backgroundLabel.setBounds(0, 0, screenWidth, screenHeight);
        add(backgroundLabel);

        // Bank logo path at the top center
        String logoImagePath = "images\\bank_logo_small.png"; // Update path
        JLabel logoPlaceholder = new JLabel("", SwingConstants.CENTER);
        logoPlaceholder.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logoPlaceholder.setBounds(screenWidth / 2 - 100, 20, 64, 64); // Adjust logo size and position
        logoPlaceholder.setIcon(new ImageIcon(logoImagePath));
        backgroundLabel.add(logoPlaceholder);

        // Switch Account button path at the top left with image icon
        String switchAccountIconPath = "images\\switch_account_icon.png"; // Update path
        JButton switchAccountButton = new JButton("Switch Account");
        switchAccountButton.setBounds(20, 20, 170, 30);
        switchAccountButton.setIcon(new ImageIcon(switchAccountIconPath));
        switchAccountButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        switchAccountButton.setBackground(Color.WHITE);
        switchAccountButton.setFocusPainted(false);
        switchAccountButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backgroundLabel.add(switchAccountButton);

        // Panel for buttons and descriptions
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(1, 3, 20, 20)); // 1 row, 3 columns with 20px spacing
        buttonsPanel.setBounds(screenWidth / 2 - 450, screenHeight / 2 - 200, 900, 400);
        buttonsPanel.setOpaque(false); // Make the panel transparent
        backgroundLabel.add(buttonsPanel);

        // Button 1: Transfer Money
        String transferIconPath = "E:\\DBS Project\\NewBankManagementSystem\\BankManagementSystem\\images\\transfer_icon.png"; // Update path
        JPanel transferMoneyPanel = createButtonPanel("Transfer Money", transferIconPath);
        buttonsPanel.add(transferMoneyPanel);

        // Button 2: Transaction History
        String historyIconPath = "E:\\DBS Project\\NewBankManagementSystem\\BankManagementSystem\\images\\history_icon.png"; // Update path
        JPanel transactionHistoryPanel = createButtonPanel("Transaction History", historyIconPath);
        buttonsPanel.add(transactionHistoryPanel);

        // Button 3: Apply for Loan
        String loanIconPath = "E:\\DBS Project\\NewBankManagementSystem\\BankManagementSystem\\images\\loan_icon.png"; // Update path
        JPanel applyForLoanPanel = createButtonPanel("Apply for Loan", loanIconPath);

        // Add a label to display the account balance
        balanceLabel = new JLabel("Balance: $0.00", SwingConstants.RIGHT);
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        balanceLabel.setForeground(Color.BLACK);
        balanceLabel.setBounds(screenWidth - 220, 20, 200, 30); // Position at top-right
        backgroundLabel.add(balanceLabel);

        // ActionListeners
        // Switch Account Button
        switchAccountButton.addActionListener(e -> {
            switchAccount();
            updateBalance();
        });

        // Transfer Money Button
        JButton transferMoneyButton = (JButton) transferMoneyPanel.getComponent(0);
        transferMoneyButton.addActionListener(e -> {
            new Transaction(); // Open Transaction panel
        });

        // Transaction History Button
        JButton transactionHistoryButton = (JButton) transactionHistoryPanel.getComponent(0);
        transactionHistoryButton.addActionListener(e -> {
            TransactionHistory.showHistory();
        });

        // Apply for Loan Button
        JButton applyForLoanButton = (JButton) applyForLoanPanel.getComponent(0);
        applyForLoanButton.addActionListener(e -> {
            new LoanApplication();
        });

        buttonsPanel.add(applyForLoanPanel);

        // Set visibility
        setVisible(true);

        updateBalance();
    }

    static void updateBalance() {
        double balance = Transaction.getBalance(Session.getAccount_no()); // Fetch balance
        balanceLabel.setText(String.format("Balance: PKR %.2f", balance));
    }

    private JPanel createButtonPanel(String buttonText, String iconPath) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton button = new JButton();
        button.setIcon(new ImageIcon(iconPath));
        button.setPreferredSize(new Dimension(100, 100));
        button.setFont(new Font("Segoe UI", Font.BOLD, 20));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0), 5, true)); // Circular border
        button.setIconTextGap(10);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);

        panel.add(button, BorderLayout.CENTER);

        JLabel descriptionLabel = new JLabel(buttonText, SwingConstants.CENTER);
        descriptionLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        descriptionLabel.setForeground(Color.BLACK);
        panel.add(descriptionLabel, BorderLayout.SOUTH);

        return panel;
    }

    private void switchAccount() {
        try (Connection connection = DBConnection.getConnection()) {
            String getAccounts = "SELECT account_number FROM AccountCustomer WHERE customer_id = ?";
            PreparedStatement stmt = connection.prepareStatement(getAccounts);
            stmt.setInt(1, Session.getUser_id());
            ResultSet rs = stmt.executeQuery();

            Vector<String> accounts = new Vector<>();
            while (rs.next()) {
                accounts.add(rs.getString("account_number"));
            }

            String selectedAccount = (String) JOptionPane.showInputDialog(
                    null,
                    "Select an account:",
                    "Switch Account",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    accounts.toArray(),
                    accounts.get(0));

            if (selectedAccount != null) {
                // Update the session or perform further actions with the selected account
                Session.setAccount_no(selectedAccount);
                JOptionPane.showMessageDialog(null, "You have switched to account: " + selectedAccount, "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "No account selected.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred while switching accounts.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    public static void main(String[] args) {
        new CustomerDashboard();
    }
}
