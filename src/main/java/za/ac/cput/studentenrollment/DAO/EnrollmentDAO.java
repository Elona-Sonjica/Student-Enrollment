/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.ac.cput.studentenrollment.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import za.ac.cput.studentenrollment.Database.DBConnection;
import za.ac.cput.studentenrollment.modelclasses.Course;
import za.ac.cput.studentenrollment.modelclasses.Student;

/**
 *
 * @author elzas
 */

public class EnrollmentDAO {
    
    public void createTable() {
        String sql = "CREATE TABLE enrollments (" +
                    "enrollment_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                    "student_number VARCHAR(20), " +
                    "course_code VARCHAR(20), " +
                    "enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (student_number) REFERENCES students(student_number), " +
                    "FOREIGN KEY (course_code) REFERENCES courses(course_code), " +
                    "UNIQUE (student_number, course_code))";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Enrollments table created successfully");
        } catch (SQLException e) {
            if (!e.getSQLState().equals("X0Y32")) { // Ignore "table already exists" error
                System.out.println("Note creating enrollments table: " + e.getMessage());
            }
        }
    }
    
    public boolean enrollStudent(String studentNumber, String courseCode) {
        // First check if enrollment already exists
        if (isEnrolled(studentNumber, courseCode)) {
            System.out.println("Student " + studentNumber + " is already enrolled in " + courseCode);
            return false;
        }
        
        String sql = "INSERT INTO enrollments (student_number, course_code) VALUES (?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentNumber);
            pstmt.setString(2, courseCode);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error enrolling student: " + e.getMessage());
            return false;
        }
    }
    
    public boolean isEnrolled(String studentNumber, String courseCode) {
        String sql = "SELECT 1 FROM enrollments WHERE student_number = ? AND course_code = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentNumber);
            pstmt.setString(2, courseCode);
            
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
            
        } catch (SQLException e) {
            System.err.println("Error checking enrollment: " + e.getMessage());
            return false;
        }
    }
    
    public List<Course> getStudentEnrollments(String studentNumber) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.course_code, c.title, c.instructor " +
                    "FROM courses c " +
                    "JOIN enrollments e ON c.course_code = e.course_code " +
                    "WHERE e.student_number = ? " +
                    "ORDER BY c.course_code";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentNumber);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Course course = new Course(
                    rs.getString("course_code"),
                    rs.getString("title"),
                    rs.getString("instructor")
                );
                courses.add(course);
            }
        } catch (SQLException e) {
            System.err.println("Error getting student enrollments: " + e.getMessage());
        }
        return courses;
    }
    
    public List<Student> getCourseEnrollments(String courseCode) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT s.student_number, s.name, s.surname, s.email " +
                    "FROM students s " +
                    "JOIN enrollments e ON s.student_number = e.student_number " +
                    "WHERE e.course_code = ? " +
                    "ORDER BY s.student_number";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, courseCode);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Student student = new Student(
                    rs.getString("student_number"),
                    rs.getString("name"),
                    rs.getString("surname"),
                    rs.getString("email"),
                    "" // Don't return password for security
                );
                students.add(student);
            }
        } catch (SQLException e) {
            System.err.println("Error getting course enrollments: " + e.getMessage());
        }
        return students;
    }
    
    public boolean dropEnrollment(String studentNumber, String courseCode) {
        String sql = "DELETE FROM enrollments WHERE student_number = ? AND course_code = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentNumber);
            pstmt.setString(2, courseCode);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error dropping enrollment: " + e.getMessage());
            return false;
        }
    }
    
    public int getEnrollmentCountForCourse(String courseCode) {
        String sql = "SELECT COUNT(*) as count FROM enrollments WHERE course_code = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, courseCode);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error getting enrollment count: " + e.getMessage());
        }
        return 0;
    }
}