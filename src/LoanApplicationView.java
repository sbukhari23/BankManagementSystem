import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;

import static java.awt.Frame.MAXIMIZED_BOTH;

public class LoanApplicationView {
    private JFrame currentFrame;
    private DefaultTableModel tableModel;

    public void showLoanApplications() {
        if (currentFrame != null) {
            currentFrame.dispose();
        }

        JFrame frame = new JFrame("Loan Applications");
        currentFrame = frame;
        currentFrame.setExtendedState(MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Sample data for the table
        String[] columnNames = {"Loan ID", "Amount", "Duration", "Purpose", "Status", "Customer ID", "Actions"};
        Object[][] data = {
                {1, 50000.0, 12, "Home", "Pending", 101, ""},
                {2, 20000.0, 6, "Car", "Pending", 102, ""},
                {3, 15000.0, 3, "Personal", "Pending", 103, ""},
        };

        tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };

        JTable table = new JTable(tableModel);

        // Set row height
        table.setRowHeight(35);

        // Set preferred column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(70);  // Loan ID
        table.getColumnModel().getColumn(1).setPreferredWidth(100); // Amount
        table.getColumnModel().getColumn(2).setPreferredWidth(70);  // Duration
        table.getColumnModel().getColumn(3).setPreferredWidth(100); // Purpose
        table.getColumnModel().getColumn(4).setPreferredWidth(80);  // Status
        table.getColumnModel().getColumn(5).setPreferredWidth(80);  // Customer ID
        table.getColumnModel().getColumn(6).setPreferredWidth(200); // Actions

        // Set minimum column widths
        for (int i = 0; i < table.getColumnCount(); i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setMinWidth(70);
        }

        table.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox()));

        // Add mouse listener for double-click events
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                int row = table.getSelectedRow();
                if (evt.getClickCount() == 2 && row >= 0) {
                    int customerId = (int) table.getValueAt(row, 5);
                    showCustomerDetails(customerId);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setSize(800, 400);
        frame.setVisible(true);
    }

    // Custom ButtonRenderer for the Actions column
    class ButtonRenderer extends JPanel implements TableCellRenderer {
        private JButton acceptButton;
        private JButton rejectButton;

        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));

            // Create buttons with smaller preferred size
            acceptButton = new JButton("Accept");
            rejectButton = new JButton("Reject");

            // Set preferred size for buttons
            Dimension buttonSize = new Dimension(80, 30);
            acceptButton.setPreferredSize(buttonSize);
            rejectButton.setPreferredSize(buttonSize);

            // Add buttons to panel
            add(acceptButton);
            add(rejectButton);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            if ("Pending".equals(table.getValueAt(row, 4))) {
                acceptButton.setEnabled(true);
                rejectButton.setEnabled(true);
            } else {
                acceptButton.setEnabled(false);
                rejectButton.setEnabled(false);
            }
            setBackground(table.getBackground());
            return this;
        }
    }

    // Custom ButtonEditor for the Actions column
    class ButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private JButton acceptButton;
        private JButton rejectButton;
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));

            // Create buttons with smaller preferred size
            acceptButton = new JButton("Accept");
            acceptButton.setBackground(new Color(0, 102, 204));
            acceptButton.setForeground(Color.WHITE);
            acceptButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            rejectButton = new JButton("Reject");
            rejectButton.setBackground(new Color(232, 3, 3));
            rejectButton.setForeground(Color.WHITE);
            rejectButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // Set preferred size for buttons
            Dimension buttonSize = new Dimension(80, 30);
            acceptButton.setPreferredSize(buttonSize);
            rejectButton.setPreferredSize(buttonSize);

            acceptButton.addActionListener(e -> handleAction("Accepted"));
            rejectButton.addActionListener(e -> handleAction("Rejected"));

            panel.add(acceptButton);
            panel.add(rejectButton);
        }

        private void handleAction(String action) {
            tableModel.setValueAt(action, currentRow, 4);
            fireEditingStopped();
            JOptionPane.showMessageDialog(currentFrame,
                    "Loan application " + tableModel.getValueAt(currentRow, 0) +
                            " has been " + action.toLowerCase());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentRow = row;
            if ("Pending".equals(table.getValueAt(row, 4))) {
                acceptButton.setEnabled(true);
                rejectButton.setEnabled(true);
            } else {
                acceptButton.setEnabled(false);
                rejectButton.setEnabled(false);
            }
            panel.setBackground(table.getBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }

    private void showCustomerDetails(int customerId) {
        // Placeholder for customer details function
        JDialog dialog = new JDialog(currentFrame, "Customer Details", true);
        dialog.setLayout(new BorderLayout());

        //Replace with database queries
        String customerDetails = "Customer ID: " + customerId + "\n" +
                "Name: John Doe\n" +
                "Email: john.doe@email.com\n" +
                "Phone: (555) 123-4567\n" +
                "Address: 123 Main St\n";

        JTextArea detailsArea = new JTextArea(customerDetails);
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