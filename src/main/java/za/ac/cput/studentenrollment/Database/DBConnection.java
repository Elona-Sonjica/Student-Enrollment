package za.ac.cput.studentenrollment.Database;

import java.sql.*;
import za.ac.cput.studentenrollment.util.PasswordUtil;

public class DBConnection {
    private static final String URL = "jdbc:derby:StudentEnrolmentDB;create=true";
    private static Connection connection = null;
    private static boolean initialized = false;
    private static boolean initializing = false; // Add this flag to prevent recursion

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Load Derby driver
                Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
                // Create connection
                connection = DriverManager.getConnection(URL);
                System.out.println("Database connection established successfully");
                
                // Initialize database only once and prevent recursion
                if (!initialized && !initializing) {
                    initializing = true; // Set flag to prevent recursion
                    initializeDatabase();
                    initialized = true;
                    initializing = false; // Reset flag after initialization
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Derby JDBC Driver not found: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }

    private static void initializeDatabase() {
        System.out.println("Initializing database tables...");
        createStudentsTable();
        createCoursesTable();
        createEnrollmentsTable();
        createAdminUser();
        System.out.println("Database initialization completed");
    }

    private static void createStudentsTable() {
        String sql = "CREATE TABLE students (" +
                "student_number VARCHAR(20) PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "surname VARCHAR(100) NOT NULL, " +
                "email VARCHAR(150) NOT NULL UNIQUE, " +
                "password VARCHAR(100) NOT NULL)";
        
        executeSQL(sql, "Students table created successfully", "Students table");
    }

    private static void createCoursesTable() {
        String sql = "CREATE TABLE courses (" +
                "course_code VARCHAR(20) PRIMARY KEY, " +
                "title VARCHAR(150) NOT NULL, " +
                "instructor VARCHAR(100) NOT NULL)";
        
        executeSQL(sql, "Courses table created successfully", "Courses table");
    }

    private static void createEnrollmentsTable() {
        String sql = "CREATE TABLE enrollments (" +
                "enrollment_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                "student_number VARCHAR(20) NOT NULL, " +
                "course_code VARCHAR(20) NOT NULL, " +
                "enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (student_number) REFERENCES students(student_number), " +
                "FOREIGN KEY (course_code) REFERENCES courses(course_code), " +
                "UNIQUE (student_number, course_code))";
        
        executeSQL(sql, "Enrollments table created successfully", "Enrollments table");
    }

    private static void executeSQL(String sql, String successMessage, String tableName) {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println(successMessage);
        } catch (SQLException e) {
            // Table already exists - this is normal after first run
            if (!e.getSQLState().equals("X0Y32")) {
                System.out.println("Note creating " + tableName + ": " + e.getMessage());
            }
        }
    }

    private static void createAdminUser() {
        String checkSql = "SELECT COUNT(*) FROM students WHERE student_number = 'admin'";
        String insertSql = "INSERT INTO students (student_number, name, surname, email, password) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             ResultSet rs = checkStmt.executeQuery()) {
            
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    // Create only the admin user - no sample students
                    insertStmt.setString(1, "admin");
                    insertStmt.setString(2, "System");
                    insertStmt.setString(3, "Administrator");
                    insertStmt.setString(4, "admin@cput.ac.za");
                    insertStmt.setString(5, PasswordUtil.hashPassword("admin123"));
                    insertStmt.executeUpdate();
                    System.out.println("Admin user created successfully");
                }
            } else {
                System.out.println("Admin user already exists");
            }
        } catch (SQLException e) {
            System.out.println("Note creating admin user: " + e.getMessage());
        }
    }

    public static void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed");
            }
            try {
                DriverManager.getConnection("jdbc:derby:;shutdown=true");
            } catch (SQLException e) {
                if (e.getSQLState().equals("XJ015")) {
                    System.out.println("Derby shutdown successfully");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error shutting down database: " + e.getMessage());
        }
    }
}