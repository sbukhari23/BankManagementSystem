import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ActivityLogsDashboard extends JFrame {

    public ActivityLogsDashboard() {
        // Get screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        // Set JFrame properties
        setTitle("Activity Logs Dashboard");
        setSize(screenWidth / 2, screenHeight / 2);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        setResizable(false);
        setLocationRelativeTo(null);

        // Background image
        JLabel background = new JLabel(new ImageIcon("images/login_signup_background.png"));
        background.setLayout(null);
        add(background, BorderLayout.CENTER);

        // Create a semi-transparent panel for logs buttons
        JPanel logsPanel = new JPanel();
        logsPanel.setBackground(new Color(255, 255, 255, 216)); // Semi-transparent white
        logsPanel.setLayout(null);

        // Center the logs panel
        int panelWidth = 400;
        int panelHeight = 500;
        logsPanel.setBounds((screenWidth / 2 - panelWidth / 2), (screenHeight / 2 - panelHeight / 2), panelWidth, panelHeight);
        background.add(logsPanel);

        // Bank logo at the top
        JLabel logoLabel = new JLabel("", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Arial", Font.BOLD, 18));
        logoLabel.setBounds(170, 30, 66, 66);
        logoLabel.setIcon(new ImageIcon("images/bank_logo_small.png"));
        logsPanel.add(logoLabel);

        // Dashboard title
        JLabel titleLabel = new JLabel("Activity Logs Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setBounds(50, 125, 300, 30);
        logsPanel.add(titleLabel);

        // Create and position the four buttons
        JButton customerLogBtn = createStyledButton("Customer Log", 190);
        JButton accountLogBtn = createStyledButton("Account Log", 250);
        JButton employeeLogBtn = createStyledButton("Employee Log", 310);
        JButton loanLogBtn = createStyledButton("Loan Log", 370);

        // Add buttons to the panel
        logsPanel.add(customerLogBtn);
        logsPanel.add(accountLogBtn);
        logsPanel.add(employeeLogBtn);
        logsPanel.add(loanLogBtn);

        // Add action listeners for each button
        customerLogBtn.addActionListener(e -> {
            String query = "SELECT cl.employee_id AS 'Employee ID', cl.action AS Action, DATE_FORMAT(cl.timestamp, '%Y-%m-%d %H:%i:%s') AS Time" +
                    ", cl.customer_id AS 'Customer ID', c.name AS Name, c.cnic AS CNIC FROM customer_logs cl NATURAL JOIN customer c where employee_id in (select employee_id from " +
                    "employee where branch_id = (select branch_id from employee where employee_id = ?))";
            showLog("Employee Log", query);
        });

        accountLogBtn.addActionListener(e -> {
            // Add logic to show account logs
            JOptionPane.showMessageDialog(null, "Opening Account Logs...");
            String query = "SELECT al.employee_id, al.action, DATE_FORMAT(al.timestamp, '%Y-%m-%d %H:%i:%s'), al.account_number, a.account_title FROM Account_logs al NATURAL JOIN Account a WHERE employee_id" +
                    " in (select employee_id from employee where branch_id = (select branch_id from employee where employee_id = ?))";

            showLog("Account Logs", query);
        });

        employeeLogBtn.addActionListener(e -> {
            // Add logic to show employee logs
            JOptionPane.showMessageDialog(null, "Opening Employee Logs...");
            String query = "SELECT action AS Action, DATE_FORMAT(timestamp, '%Y-%m-%d %H:%i:%s') AS Timestamp, employee_id AS 'Employee ID' FROM employee_logs WHERE manager_id = ?";
            showLog("Employee Log", query);
        });

        loanLogBtn.addActionListener(e -> {
            // Add logic to show loan logs
            JOptionPane.showMessageDialog(null, "Opening Loan Logs...");
            String query = "SELECT ll.loan_id, ll.action, DATE_FORMAT(ll.timestamp, '%Y-%m-%d %H:%i:%s') AS Timestamp, l.loan_amount, l.loan_duration, c.name, c.cnic FROM Loan_logs ll" +
                    " NATURAL JOIN Loan l NATURAL JOIN Customer c WHERE manager_id = ?";
            showLog("Loan Log", query);
        });

        // Add a mouse listener to the background to handle focus
        background.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                background.requestFocusInWindow();
            }
        });

        // Set visibility
        setVisible(true);
    }

    // Helper method to create styled buttons
    private JButton createStyledButton(String text, int yPosition) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBounds(100, yPosition, 200, 40);
        button.setBackground(new Color(51, 153, 255));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(0, 102, 204));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(27, 143, 255));
            }
        });

        return button;
    }

    public static void showLog(String title, String query) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        frame.setExtendedState(MAXIMIZED_BOTH);

        try (Connection connection = DBConnection.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, Session.getUser_id());
            ResultSet rs = stmt.executeQuery();
            DefaultTableModel tableModel = EmployeeDashboard.buildTableModel(rs);

            // Create JTable with a non-editable model
            JTable table = new JTable(tableModel) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Disable editing
                }
            };
            JScrollPane scrollPane = new JScrollPane(table);

            // Add table to frame
            frame.add(scrollPane);
            frame.setSize(800, 400);
            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ActivityLogsDashboard());
    }
}