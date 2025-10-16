package za.ac.cput.studentenrollment.Database;

import java.sql.*;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:derby:StudentEnrolmentDB;create=true";
    private static Connection connection = null;
    private static boolean initialized = false;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Load Derby driver
                Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
                // Create connection
                connection = DriverManager.getConnection(URL);
                System.out.println("Database connection established successfully");
                
                // Initialize database only once
                if (!initialized) {
                    initializeDatabase();
                    initialized = true;
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Derby JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
        return connection;
    }

    private static void initializeDatabase() {
        System.out.println("Initializing database tables...");
        createStudentsTable();
        createCoursesTable();
        createEnrollmentsTable();
        insertSampleData();
        System.out.println("Database initialization completed");
    }

    private static void createStudentsTable() {
        String sql = "CREATE TABLE students (" +
                "student_number VARCHAR(20) PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "surname VARCHAR(100) NOT NULL, " +
                "email VARCHAR(150) NOT NULL UNIQUE, " +
                "password VARCHAR(100) NOT NULL)";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Students table created successfully");
        } catch (SQLException e) {
            // Table already exists - this is normal after first run
            if (!e.getSQLState().equals("X0Y32")) {
                System.out.println("Note creating students table: " + e.getMessage());
            }
        }
    }

    private static void createCoursesTable() {
        String sql = "CREATE TABLE courses (" +
                "course_code VARCHAR(20) PRIMARY KEY, " +
                "title VARCHAR(150) NOT NULL, " +
                "instructor VARCHAR(100) NOT NULL)";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Courses table created successfully");
        } catch (SQLException e) {
            // Table already exists - this is normal after first run
            if (!e.getSQLState().equals("X0Y32")) {
                System.out.println("Note creating courses table: " + e.getMessage());
            }
        }
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
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Enrollments table created successfully");
        } catch (SQLException e) {
            // Table already exists - this is normal after first run
            if (!e.getSQLState().equals("X0Y32")) {
                System.out.println("Note creating enrollments table: " + e.getMessage());
            }
        }
    }

    private static void insertSampleData() {
        insertSampleStudents();
        insertSampleCourses();
        insertSampleEnrollments();
    }

    private static void insertSampleStudents() {
        String checkSql = "SELECT COUNT(*) FROM students WHERE student_number = '12345'";
        String insertSql = "INSERT INTO students (student_number, name, surname, email, password) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             ResultSet rs = checkStmt.executeQuery()) {
            
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    String[][] students = {
                        {"12345", "John", "Doe", "john@cput.ac.za", "password123"},
                        {"23456", "Jane", "Smith", "jane@cput.ac.za", "password123"},
                        {"34567", "Mike", "Johnson", "mike@cput.ac.za", "password123"},
                        {"admin", "Admin", "User", "admin@cput.ac.za", "admin123"}
                    };
                    
                    for (String[] student : students) {
                        insertStmt.setString(1, student[0]);
                        insertStmt.setString(2, student[1]);
                        insertStmt.setString(3, student[2]);
                        insertStmt.setString(4, student[3]);
                        insertStmt.setString(5, student[4]);
                        insertStmt.executeUpdate();
                    }
                    System.out.println("Sample students inserted successfully");
                }
            } else {
                System.out.println("Students already exist, skipping insertion");
            }
        } catch (SQLException e) {
            System.out.println("Note inserting sample students: " + e.getMessage());
        }
    }

    private static void insertSampleCourses() {
        String checkSql = "SELECT COUNT(*) FROM courses WHERE course_code = 'ADP262S'";
        String insertSql = "INSERT INTO courses (course_code, title, instructor) VALUES (?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             ResultSet rs = checkStmt.executeQuery()) {
            
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    String[][] courses = {
                        {"ADP262S", "Applications Development Practice", "Dr. Smith"},
                        {"ITS362S", "Information Systems", "Prof. Johnson"},
                        {"WTW364S", "Web Technology", "Dr. Brown"},
                        {"ADF262S", "Application Development Fundamentals", "Mr Burger"},
                        {"ICT262S", "ICT Electives", "Mr Olivier"},
                        {"PROJ262S", "Project", "Ms Tswane"},
                        {"INM262S", "Information Management", "Mr Ayodeji"}
                    };
                    
                    for (String[] course : courses) {
                        insertStmt.setString(1, course[0]);
                        insertStmt.setString(2, course[1]);
                        insertStmt.setString(3, course[2]);
                        insertStmt.executeUpdate();
                    }
                    System.out.println("Sample courses inserted successfully");
                }
            } else {
                System.out.println("Courses already exist, skipping insertion");
            }
        } catch (SQLException e) {
            System.out.println("Note inserting sample courses: " + e.getMessage());
        }
    }

    private static void insertSampleEnrollments() {
        String checkSql = "SELECT COUNT(*) FROM enrollments WHERE student_number = '12345' AND course_code = 'ADP262S'";
        String insertSql = "INSERT INTO enrollments (student_number, course_code) VALUES (?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             ResultSet rs = checkStmt.executeQuery()) {
            
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    String[][] enrollments = {
                        {"12345", "ADP262S"},
                        {"12345", "ITS362S"},
                        {"23456", "WTW364S"}
                    };
                    
                    for (String[] enrollment : enrollments) {
                        insertStmt.setString(1, enrollment[0]);
                        insertStmt.setString(2, enrollment[1]);
                        insertStmt.executeUpdate();
                    }
                    System.out.println("Sample enrollments inserted successfully");
                }
            } else {
                System.out.println("Enrollments already exist, skipping insertion");
            }
        } catch (SQLException e) {
            System.out.println("Note inserting sample enrollments: " + e.getMessage());
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