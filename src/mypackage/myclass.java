package mypackage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class myclass {
    private static final String URL = "jdbc:mysql://localhost:3306/lib"; 
    private static final String USER = "root"; 
    private static final String PASSWORD = "m1a2ngesh*"; 

    public static Connection getConnection() throws SQLException {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
            throw new SQLException("Driver not found");
        }
        // Establish and return the database connection
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
