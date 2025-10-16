package za.ac.cput.studentenrollment.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
    private static final String URL = "jdbc:derby:StudentEnrolmentDB;";
    private static final String USER = "Elona";
    private static final String PASSWORD = "Elona123";
    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                initializeDatabase();
            } catch (ClassNotFoundException e) {
                throw new SQLException("Derby JDBC Driver not found", e);
            }
        }
        return connection;
    }

    private static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            createStudentsTable(stmt);
            createCoursesTable(stmt);
            createEnrollmentsTable(stmt);
            insertSampleData(stmt);

        } catch (SQLException e) {
            System.out.println("Database initialization: " + e.getMessage());
        }
    }

    private static void createStudentsTable(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE students (" +
                "student_number VARCHAR(20) PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "surname VARCHAR(100) NOT NULL, " +
                "email VARCHAR(150) NOT NULL UNIQUE, " +
                "password VARCHAR(100) NOT NULL)";
        try { stmt.executeUpdate(sql); } catch (SQLException e) {}
    }

    private static void createCoursesTable(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE courses (" +
                "course_code VARCHAR(20) PRIMARY KEY, " +
                "title VARCHAR(150) NOT NULL, " +
                "instructor VARCHAR(100) NOT NULL)";
        try { stmt.executeUpdate(sql); } catch (SQLException e) {}
    }

    private static void createEnrollmentsTable(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE enrollments (" +
                "enrollment_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                "student_number VARCHAR(20) NOT NULL, " +
                "course_code VARCHAR(20) NOT NULL, " +
                "enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (student_number) REFERENCES students(student_number), " +
                "FOREIGN KEY (course_code) REFERENCES courses(course_code), " +
                "UNIQUE (student_number, course_code))";
        try { stmt.executeUpdate(sql); } catch (SQLException e) {}
    }

    private static void insertSampleData(Statement stmt) throws SQLException {
        insertSampleStudents(stmt);
        insertSampleCourses(stmt);
        insertSampleEnrollments(stmt);
    }

    private static void insertSampleStudents(Statement stmt) {
        String sql = "INSERT INTO students (student_number, name, surname, email, password) VALUES " +
                "('12345', 'John', 'Doe', 'john@cput.ac.za', 'password123'), " +
                "('23456', 'Jane', 'Smith', 'jane@cput.ac.za', 'password123'), " +
                "('34567', 'Mike', 'Johnson', 'mike@cput.ac.za', 'password123'), " +
                "('admin', 'Admin', 'User', 'admin@cput.ac.za', 'admin123')";
        try { stmt.executeUpdate(sql); } catch (SQLException e) {}
    }

    private static void insertSampleCourses(Statement stmt) {
        String sql = "INSERT INTO courses (course_code, title, instructor) VALUES " +
                "('ADP262S', 'Applications Development Practice', 'Dr. Smith'), " +
                "('ITS362S', 'Information Systems', 'Prof. Johnson'), " +
                "('WTW364S', 'Web Technology', 'Dr. Brown')";
        try { stmt.executeUpdate(sql); } catch (SQLException e) {}
    }

    private static void insertSampleEnrollments(Statement stmt) {
        String sql = "INSERT INTO enrollments (student_number, course_code) VALUES " +
                "('12345', 'ADP262S'), " +
                "('12345', 'ITS362S'), " +
                "('23456', 'WTW364S')";
        try { stmt.executeUpdate(sql); } catch (SQLException e) {}
    }

    public static void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            if (!e.getSQLState().equals("XJ015")) {
                System.err.println("Database shutdown error: " + e.getMessage());
            }
        }
    }
}