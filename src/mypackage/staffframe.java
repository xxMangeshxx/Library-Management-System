package mypackage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class staffframe extends JFrame {
    private JTable booksTable;
    private DefaultTableModel tableModel;
    private JTextField titleField, authorField, priceField, stockField, bookIdField;

    public staffframe() {
        // Set up JFrame
        setTitle("Staff Panel");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Title
        JLabel titleLabel = new JLabel("Staff Panel - Manage Books", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(titleLabel, BorderLayout.NORTH);

        // Table to Display Books
        tableModel = new DefaultTableModel(new String[]{"ID", "Title", "Author", "Price", "Stock"}, 0);
        booksTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(booksTable);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Panel for Actions
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new GridLayout(5, 1));  // Adjust grid layout to accommodate the new button

        // Add Book Panel
        JPanel addPanel = new JPanel(new GridLayout(1, 5));
        titleField = new JTextField();
        authorField = new JTextField();
        priceField = new JTextField();
        stockField = new JTextField();
        JButton addButton = new JButton("Add Book");
        addPanel.add(new JLabel("Title:"));
        addPanel.add(titleField);
        addPanel.add(new JLabel("Author:"));
        addPanel.add(authorField);
        addPanel.add(new JLabel("Price:"));
        addPanel.add(priceField);
        addPanel.add(new JLabel("Stock:"));
        addPanel.add(stockField);
        addPanel.add(addButton);
        actionPanel.add(addPanel);

        // Delete Book Panel
        JPanel deletePanel = new JPanel(new GridLayout(1, 2));
        bookIdField = new JTextField();
        JButton deleteButton = new JButton("Delete Book");
        deletePanel.add(new JLabel("Book ID:"));
        deletePanel.add(bookIdField);
        deletePanel.add(deleteButton);
        actionPanel.add(deletePanel);

        // Show Receipt History Button
        JButton showReceiptsButton = new JButton("Show Receipt History");
        actionPanel.add(showReceiptsButton);

        // Logout Button
        JButton logoutButton = new JButton("Logout");
        actionPanel.add(logoutButton);

        add(actionPanel, BorderLayout.SOUTH);

        // Load Books Initially
        loadBooks();

        // Add Book Action
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addBook();
            }
        });

        // Delete Book Action
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteBook();
            }
        });

        // Show Receipt History Action
        showReceiptsButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                new ReceiptHistoryFrame();  // Open ReceiptHistoryFrame when clicked
            }
        });

        // Logout Action
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();  // Close the current staff frame
                new identify();  // Open the login frame again
            }
        });

        setVisible(true);
    }

    // Load Books from Database
    private void loadBooks() {
        tableModel.setRowCount(0); // Clear existing rows
        try (Connection conn = myclass.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM books")) {

            while (rs.next()) {
                tableModel.addRow(new Object[] {
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

    // Add a New Book
    private void addBook() {
        String title = titleField.getText();
        String author = authorField.getText();
        String priceText = priceField.getText();
        String stockText = stockField.getText();

        if (title.isEmpty() || author.isEmpty() || priceText.isEmpty() || stockText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            int stock = Integer.parseInt(stockText);

            try (Connection conn = myclass.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO books (title, author, price, stock) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, title);
                ps.setString(2, author);
                ps.setDouble(3, price);
                ps.setInt(4, stock);

                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Book added successfully!");
                loadBooks();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid price or stock value.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding book: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Delete a Book
    private void deleteBook() {
        String bookIdText = bookIdField.getText();

        if (bookIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Book ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int bookId = Integer.parseInt(bookIdText);

            try (Connection conn = myclass.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM books WHERE id = ?")) {
                ps.setInt(1, bookId);

                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Book deleted successfully!");
                    loadBooks();
                } else {
                    JOptionPane.showMessageDialog(this, "No book found with the given ID.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Book ID.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting book: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Main Method for Testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new staffframe());
    }
}