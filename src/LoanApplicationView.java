import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

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
        ArrayList<Object[]> rows = new ArrayList<>();

        try(Connection connection = DBConnection.getConnection()) {
            String getLoans = "SELECT loan_id, loan_amount, loan_duration, loan_purpose, loan_application_status, customer_id" +
                    " FROM Loan";
            PreparedStatement stmt = connection.prepareStatement(getLoans);
            ResultSet rs = stmt.executeQuery();
            int columns = 7;

            while(rs.next()) {
                Object[] row = new Object[columns];
                for(int i = 0; i < columns; i++) {
                    if(i == 6) {
                        row[i] = "";
                    }
                    else {
                        row[i] = rs.getObject(i+1);
                    }
                }

                rows.add(row);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        Object[][] data = rows.toArray(new Object[0][0]);

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
        table.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox(), table));

        // Add mouse listener for double-click events
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                int row = table.getSelectedRow();
                if (evt.getClickCount() == 2 && row >= 0) {
                    long customerId = (long) table.getValueAt(row, 5);
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
            if ("pending".equals(table.getValueAt(row, 4))) {
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
        private JTable table;
        private JPanel panel;
        private JButton acceptButton;
        private JButton rejectButton;
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox, JTable table) {
            super(checkBox);
            this.table = table;
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

            acceptButton.addActionListener(e -> {
                int row = table.getEditingRow();
                handleAction(row, "accepted");
                acceptButton.setEnabled(false);
                rejectButton.setEnabled(false);
            });
            rejectButton.addActionListener(e -> {
                int row = table.getEditingRow();
                handleAction(row, "rejected");
                acceptButton.setEnabled(false);
                rejectButton.setEnabled(false);
            });

            panel.add(acceptButton);
            panel.add(rejectButton);
        }

        private void handleAction(int row, String action) {
            try(Connection connection = DBConnection.getConnection()) {
                Statement statement = connection.createStatement();
                statement.execute("SET @current_user_id = " + Session.getUser_id());

                String acceptLoan = "UPDATE Loan SET loan_application_status = '" + action + "' WHERE loan_id = ?";
                PreparedStatement stmt = connection.prepareStatement(acceptLoan);
                stmt.setInt(1, (int)tableModel.getValueAt(row, 0));
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(null, "Loan " + action);
                tableModel.setValueAt(action, row, 4);
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentRow = row;
            if ("pending".equals(table.getValueAt(row, 4))) {
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

    private void showCustomerDetails(long customerId) {
        // Placeholder for customer details function
        JDialog dialog = new JDialog(currentFrame, "Customer Details", true);
        dialog.setLayout(new BorderLayout());

        String customerDetails = "";
        try (Connection connection = DBConnection.getConnection()) {
            String getDetails = "SELECT customer_id , name, cnic, contact_number, " +
                    "email, street, city, state FROM Customer WHERE " +
                    "customer_id = ?";
            PreparedStatement stmt = connection.prepareStatement(getDetails);
            stmt.setLong(1, customerId);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            customerDetails = "Customer ID: " + rs.getInt("customer_id") + "\n" +
                    "Name: " + rs.getString("name") + "\n" + "CNIC: " + rs.getString("cnic") + "\n" +
                    "Email: " + rs.getString("email") + "\n" +
                    "Phone: " + rs.getString("contact_number") + "\n" +
                    "Address: " + rs.getString("street") + ", " + rs.getString("city") + ", " +
                    rs.getString("state");
        }
        catch(Exception e) {
            e.printStackTrace();
        }

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