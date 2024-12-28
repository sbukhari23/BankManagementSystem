import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Employee extends JFrame {

    public Employee() {
        // Get screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        // Set JFrame properties
        setTitle("Role-Based Dashboard");
        setSize(screenWidth / 2, screenHeight / 2);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        setResizable(false);
        setLocationRelativeTo(null);

        // Background image placeholder
        JLabel background = new JLabel(new ImageIcon("E:\\DBS Project\\NewBankManagementSystem\\BankManagementSystem\\images\\login_signup_background.png"));
        background.setLayout(null);
        add(background, BorderLayout.CENTER);

        // Semi-transparent panel for login/dashboard form
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(255, 255, 255, 216)); // Semi-transparent white
        mainPanel.setLayout(null);

        // Center the main panel
        int panelWidth = 500;
        int panelHeight = 500;
        mainPanel.setBounds((screenWidth / 2 - panelWidth / 2), (screenHeight / 2 - panelHeight / 2), panelWidth, panelHeight);
        background.add(mainPanel);

        // Placeholder for bank logo
        JLabel logoPlaceholder = new JLabel("", SwingConstants.CENTER);
        logoPlaceholder.setFont(new Font("Arial", Font.BOLD, 18));
        logoPlaceholder.setBounds((panelWidth - 100) / 2, 30, 100, 100);
        logoPlaceholder.setIcon(new ImageIcon("E:\\DBS Project\\NewBankManagementSystem\\BankManagementSystem\\images\\bank_logo_small.png"));
        mainPanel.add(logoPlaceholder);

        // Welcome Label
        JLabel welcomeLabel = new JLabel("Welcome to the Bank Management System", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setBounds(50, 150, 400, 30);
        mainPanel.add(welcomeLabel);

        // Manager Login Button
        JButton managerButton = new JButton("Login as Manager");
        managerButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        managerButton.setBounds(150, 200, 200, 40);
        mainPanel.add(managerButton);

        // Employee Login Button
        JButton employeeButton = new JButton("Login as Employee");
        employeeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        employeeButton.setBounds(150, 260, 200, 40);
        mainPanel.add(employeeButton);

        // Add action listeners for buttons
        managerButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Manager Login clicked!");
            new ManagerDashboard();  // Open Manager Dashboard
        });

        employeeButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Employee Login clicked!");
            new EmployeeDashboard();  // Open Employee Dashboard
        });

        // Set visibility
        setVisible(true);
    }

    public static void main(String[] args) {
        new Employee();
    }
}
