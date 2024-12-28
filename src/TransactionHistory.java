import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TransactionHistory {
    public static void showHistory() {
        // Create JFrame
        JFrame frame = new JFrame("Transaction History");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(600, 400);

        // JTable setup
        DefaultTableModel model = new DefaultTableModel();
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane);

        model.addColumn("Receiver Account no.");
        model.addColumn("Sender Account no.");
        model.addColumn("Amount");
        model.addColumn("Transaction Type");
        model.addColumn("Time");

        try(Connection conn = DBConnection.getConnection()) {
            String getHistory = "SELECT to_account_number, from_account_number, amount, IF(to_account_number = ?, 'Credit', 'Debit'), DATE_FORMAT(transaction_date_time, '%Y-%m-%d %H:%i:%s') FROM Transaction WHERE to_account_number = ? OR from_account_number = ?";
            PreparedStatement stmt = conn.prepareStatement(getHistory);
            stmt.setString(1, Session.getAccount_no());
            stmt.setString(2, Session.getAccount_no());
            stmt.setString(3, Session.getAccount_no());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[5];
                for (int i = 1; i <= 5; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                model.addRow(row);
            }

        }
        catch(Exception e) {
            e.printStackTrace();
        }

        frame.setVisible(true);
    }
}