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
/**
 *
 * @author elzas
 */
public class CourseDAO {
    
    public void createTable() {
        String sql = "CREATE TABLE courses (" +
                    "course_code VARCHAR(20) PRIMARY KEY, " +
                    "title VARCHAR(100) NOT NULL, " +
                    "instructor VARCHAR(50) NOT NULL)";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Courses table created successfully");
        } catch (SQLException e) {
            // Table might already exist - this is normal
            if (!e.getSQLState().equals("X0Y32")) { // Ignore "table already exists" error
                System.out.println("Note creating courses table: " + e.getMessage());
            }
        }
    }
    
    public boolean addCourse(Course course) {
        String sql = "INSERT INTO courses (course_code, title, instructor) VALUES (?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, course.getCourseCode());
            pstmt.setString(2, course.getTitle());
            pstmt.setString(3, course.getInstructor());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding course: " + e.getMessage());
            return false;
        }
    }
    
    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses ORDER BY course_code";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Course course = new Course(
                    rs.getString("course_code"),
                    rs.getString("title"),
                    rs.getString("instructor")
                );
                courses.add(course);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all courses: " + e.getMessage());
        }
        return courses;
    }
    
    public Course getCourseByCode(String courseCode) {
        String sql = "SELECT * FROM courses WHERE course_code = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, courseCode);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Course(
                    rs.getString("course_code"),
                    rs.getString("title"),
                    rs.getString("instructor")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting course by code: " + e.getMessage());
        }
        return null;
    }
    
    public boolean courseExists(String courseCode) {
        String sql = "SELECT 1 FROM courses WHERE course_code = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, courseCode);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
            
        } catch (SQLException e) {
            System.err.println("Error checking if course exists: " + e.getMessage());
            return false;
        }
    }
}