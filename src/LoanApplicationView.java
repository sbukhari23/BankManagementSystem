import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

import static java.awt.Frame.MAXIMIZED_BOTH;

public class LoanApplicationView {
    private JFrame currentFrame;

    public void showLoanApplications() {
        if (currentFrame != null) {
            currentFrame.dispose(); // Close the old frame if it exists
        }

        JFrame frame = new JFrame("Loan Applications");
        currentFrame = frame; // Assign the current frame
        currentFrame.setExtendedState(MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Sample data for the table (replace with database query results)
        String[] columnNames = {"Loan ID", "Amount", "Duration", "Purpose", "Status", "Customer ID"};
        Object[][] data = {
                {1, 50000.0, 12, "Home", "Accepted", 101},
                {2, 20000.0, 6, "Car", "Pending", 102},
                {3, 15000.0, 3, "Personal", "Rejected", 103},
        };

        // Create table model
        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);

        // Create JTable with a non-editable model
        JTable table = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Disable editing
            }
        };

        // Add mouse listener to table
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) { // Double-click to view details
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        int loanID = (int) table.getValueAt(row, 0); // Safely cast to int
                        showLoanDetailsDialog(loanID);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);

        // Add components to frame
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setSize(800, 400);
        frame.setVisible(true);
    }

    private void showLoanDetailsDialog(int loanID) {
        // Create a dialog to display loan details
        JDialog dialog = new JDialog(currentFrame, "Loan Details", true);
        dialog.setLayout(new BorderLayout());

        // Sample loan details (replace with database query results)
        String loanDetails = "Loan ID: " + loanID + "\n" +
                "Amount: 50000\n" +
                "Duration: 12 months\n" +
                "Purpose: Home\n" +
                "Status: Accepted\n" +
                "Customer ID: 101";

        JTextArea detailsArea = new JTextArea(loanDetails);
        detailsArea.setEditable(false);
        dialog.add(new JScrollPane(detailsArea), BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        dialog.add(closeButton, BorderLayout.SOUTH);

        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(currentFrame);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoanApplicationView view = new LoanApplicationView();
            view.showLoanApplications();
        });
    }
}
