package mypackage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class customerframe extends JFrame {
    private JTable booksTable;
    private DefaultTableModel tableModel;
    private JTextField bookIdField, quantityField;
    private JLabel totalAmountLabel;
    private int loggedInCustomerId;  // Add this to store the logged-in customer ID

    public customerframe(int customerId) {
        this.loggedInCustomerId = customerId;  // Set the logged-in customer ID
        // Set up JFrame
        setTitle("Customer Panel");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Title
        JLabel titleLabel = new JLabel("Customer Panel - Buy Books", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(titleLabel, BorderLayout.NORTH);

        // Table to Display Books
        tableModel = new DefaultTableModel(new String[]{"ID", "Title", "Author", "Price", "Stock"}, 0);
        booksTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(booksTable);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Panel for Actions
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new GridLayout(4, 1)); // Changed grid layout to 4 rows for Back button

        // View All Books Panel
        JPanel viewPanel = new JPanel();
        JButton viewBooksButton = new JButton("See All Books");
        viewPanel.add(viewBooksButton);
        actionPanel.add(viewPanel);

        // Buy Book Panel
        JPanel buyPanel = new JPanel(new GridLayout(1, 3));
        bookIdField = new JTextField();
        quantityField = new JTextField();
        JButton buyButton = new JButton("Buy Book");
        buyPanel.add(new JLabel("Book ID:"));
        buyPanel.add(bookIdField);
        buyPanel.add(new JLabel("Quantity:"));
        buyPanel.add(quantityField);
        buyPanel.add(buyButton);
        actionPanel.add(buyPanel);

        // Total Amount Panel
        JPanel totalPanel = new JPanel();
        totalAmountLabel = new JLabel("Total: ₹0.00 (Tax: ₹90.00)");
        totalPanel.add(totalAmountLabel);
        actionPanel.add(totalPanel);

        // Back to Login Panel (new panel added)
        JPanel backPanel = new JPanel();
        JButton backButton = new JButton("Back to Login");
        backPanel.add(backButton);
        actionPanel.add(backPanel);

        add(actionPanel, BorderLayout.SOUTH);

        // Load Books Initially (table will be empty at start)
        loadBooks(false);  // Initially don't load books

        // View Books Action
        viewBooksButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadBooks(true);  // Load books when the button is clicked
            }
        });

        // Buy Book Action
        buyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buyBook();
            }
        });

        // Back Button Action
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                backToLogin();  // Calls method to return to login page
            }
        });

        setVisible(true);
    }

    // Load Books from Database
    private void loadBooks(boolean load) {
        if (!load) {
            tableModel.setRowCount(0); // Clear existing rows
            return;
        }

        try (Connection conn = myclass.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM books")) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getDouble("price"),
                        rs.getInt("stock")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading books: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Buy a Book
    private void buyBook() {
        String bookIdText = bookIdField.getText();
        String quantityText = quantityField.getText();

        if (bookIdText.isEmpty() || quantityText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in both fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int bookId = Integer.parseInt(bookIdText);
            int quantity = Integer.parseInt(quantityText);

            // Fetch book details
            try (Connection conn = myclass.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM books WHERE id = ?")) {
                ps.setInt(1, bookId);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    int stock = rs.getInt("stock");
                    double price = rs.getDouble("price");

                    // Check if enough stock is available
                    if (quantity > stock) {
                        JOptionPane.showMessageDialog(this, "Not enough stock available.", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        // Update stock and calculate total
                        double total = price * quantity;
                        double tax = 90.00; // Fixed tax
                        double finalTotal = total + tax;

                        // Update stock in the database
                        try (PreparedStatement updatePs = conn.prepareStatement("UPDATE books SET stock = stock - ? WHERE id = ?")) {
                            updatePs.setInt(1, quantity);
                            updatePs.setInt(2, bookId);
                            updatePs.executeUpdate();
                        }

                        // Insert receipt into the 'receipts' table
                        try (PreparedStatement insertReceiptPs = conn.prepareStatement(
                                "INSERT INTO receipts (customer_id, book_id, quantity, total_price, date_of_purchase) VALUES (?, ?, ?, ?, ?)")) {
                            insertReceiptPs.setInt(1, loggedInCustomerId);  // Use the logged-in customer ID
                            insertReceiptPs.setInt(2, bookId);
                            insertReceiptPs.setInt(3, quantity);
                            insertReceiptPs.setDouble(4, finalTotal);
                            insertReceiptPs.setTimestamp(5, new Timestamp(System.currentTimeMillis()));  // Current date and time
                            insertReceiptPs.executeUpdate();
                        }

                        // Show the receipt
                        showReceipt(bookId, quantity, price, total, tax, finalTotal);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Book not found with the provided ID.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error buying book: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid book ID or quantity.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Show Receipt
    private void showReceipt(int bookId, int quantity, double price, double total, double tax, double finalTotal) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("Receipt\n");
        receipt.append("Book ID: ").append(bookId).append("\n");
        receipt.append("Quantity: ").append(quantity).append("\n");
        receipt.append("Price per Book: ₹").append(price).append("\n");
        receipt.append("Total: ₹").append(total).append("\n");
        receipt.append("Tax: ₹").append(tax).append("\n");
        receipt.append("Final Total: ₹").append(finalTotal).append("\n");

        JOptionPane.showMessageDialog(this, receipt.toString(), "Receipt", JOptionPane.INFORMATION_MESSAGE);
    }

    // Return to Login Page
    private void backToLogin() {
        // Dispose of the current frame and open the login frame
        this.dispose();
        new identify(); // Assuming 'loginframe' is the class for the login page
    }

    // Main Method for Testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new customerframe(1));  // Pass customer ID for testing
    }
}