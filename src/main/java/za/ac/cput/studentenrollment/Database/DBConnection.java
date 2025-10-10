/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.ac.cput.studentenrollment.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author elzas
 */
public class DBConnection {
    private static final String URL = "jdbc:derby:StudentEnrolmentDB;create=true";
    private static final String USER = "Elona";
    private static final String PASSWORD = "Elona123";
    
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Derby JDBC Driver not found", e);
        }
    }
    
    public static void shutdown() {
        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            // Expected behavior on shutdown
            if (!e.getSQLState().equals("XJ015")) {
                System.err.println("Database shutdown error: " + e.getMessage());
            }
        }
    }
}