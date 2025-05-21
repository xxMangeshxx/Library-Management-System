package mypackage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class identify extends JFrame {
    // Constructor
    public identify() {
        // Set up JFrame
        setTitle("Login Page");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        // Title Label
        JLabel titleLabel = new JLabel("Welcome! Please Log In", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        getContentPane().add(titleLabel, BorderLayout.NORTH);

        // Center Panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(4, 2, 10, 10));

        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField();
        centerPanel.add(usernameLabel);
        centerPanel.add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();
        centerPanel.add(passwordLabel);
        centerPanel.add(passwordField);

        JLabel roleLabel = new JLabel("Role:");
        JComboBox<String> roleComboBox = new JComboBox<>(new String[]{"Customer", "Staff"});
        centerPanel.add(roleLabel);
        centerPanel.add(roleComboBox);

        getContentPane().add(centerPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton loginButton = new JButton("Log In");
        buttonPanel.add(loginButton);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        // Action Listener for Login Button
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String role = (String) roleComboBox.getSelectedItem();

                // Retrieve customerId if role is Customer
                int customerId = -1;

                if (role.equals("Customer")) {
                    customerId = authenticateCustomer(username, password); // Get customer ID
                } else if (role.equals("Staff")) {
                    if (authenticateStaff(username, password)) {
                        JOptionPane.showMessageDialog(null, "Login Successful as Staff!");
                        openStaffPage();  // Opens Staff Frame
                        dispose();  // Close the login window
                    } else {
                        JOptionPane.showMessageDialog(null, "Invalid credentials. Please try again.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                    }
                    return; // Skip further steps for staff login
                }

                if (customerId != -1) {
                    JOptionPane.showMessageDialog(null, "Login Successful as Customer!");
                    openCustomerPage(customerId); // Opens Customer Frame with customerId
                    dispose();  // Close the login window after successful login
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid credentials. Please try again.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        setVisible(true);
    }

    // Authenticate Staff
    private boolean authenticateStaff(String username, String password) {
        String query = "SELECT * FROM staff WHERE username = ? AND password = ?";

        try (Connection conn = myclass.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            return rs.next(); // If a record is found, authentication is successful
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Authenticate Customer and retrieve their ID
    private int authenticateCustomer(String username, String password) {
        String query = "SELECT * FROM customers WHERE username = ? AND password = ?";

        try (Connection conn = myclass.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("id"); // Return customer ID
            } else {
                return -1; // Invalid credentials
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1; // Return -1 in case of an error
        }
    }

    // Open Customer Page and pass the customer ID
    private void openCustomerPage(int customerId) {
        new customerframe(customerId);  // Create an instance of CustomerFrame with customerId
    }

    // Open Staff Page
    private void openStaffPage() {
        new staffframe();  // Create an instance of StaffFrame
    }

    // Main Method
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new identify());
    }
}