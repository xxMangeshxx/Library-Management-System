package mypackage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;
import java.awt.Font;
import java.sql.*;

public class ReceiptHistoryFrame extends JFrame {
    private JTable receiptsTable;
    private DefaultTableModel tableModel;

    public ReceiptHistoryFrame() {
        // Set up JFrame
        setTitle("Receipt History");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Title
        JLabel titleLabel = new JLabel("Receipt History", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(titleLabel, BorderLayout.NORTH);

        // Table to Display Receipts
        tableModel = new DefaultTableModel(new String[]{"Receipt ID", "Customer ID", "Book ID", "Quantity", "Total Price", "Date of Purchase"}, 0);
        receiptsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(receiptsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Load Receipts
        loadReceipts();

        setVisible(true);
    }

    // Load Receipts from Database
    private void loadReceipts() {
        try (Connection conn = myclass.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM receipts")) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getInt("customer_id"),
                        rs.getInt("book_id"),
                        rs.getInt("quantity"),
                        rs.getDouble("total_price"),
                        rs.getTimestamp("date_of_purchase")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading receipts: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}