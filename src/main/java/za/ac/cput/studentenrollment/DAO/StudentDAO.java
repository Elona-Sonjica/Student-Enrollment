/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.ac.cput.studentenrollment.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import za.ac.cput.studentenrollment.Database.DBConnection;
import za.ac.cput.studentenrollment.modelclasses.Student;
/**
 *
 * @author elzas
 */
public class StudentDAO {
    
    public void createTable() {
        String sql = "CREATE TABLE students (" +
                    "student_number VARCHAR(20) PRIMARY KEY, " +
                    "name VARCHAR(50) NOT NULL, " +
                    "surname VARCHAR(50) NOT NULL, " +
                    "email VARCHAR(100) NOT NULL, " +
                    "password VARCHAR(50) NOT NULL, " +
                    "role VARCHAR(10) DEFAULT 'student')";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Students table created successfully");
        } catch (SQLException e) {
            // Table might already exist - this is normal
            if (!e.getSQLState().equals("X0Y32")) { // Ignore "table already exists" error
                System.out.println("Note creating students table: " + e.getMessage());
            }
        }
    }
    
    public boolean addStudent(Student student) {
        String sql = "INSERT INTO students (student_number, name, surname, email, password, role) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, student.getStudentNumber());
            pstmt.setString(2, student.getName());
            pstmt.setString(3, student.getSurname());
            pstmt.setString(4, student.getEmail());
            pstmt.setString(5, student.getPassword());
            pstmt.setString(6, "student");
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding student: " + e.getMessage());
            return false;
        }
    }
    
    public Student authenticate(String studentNumber, String password) {
        String sql = "SELECT * FROM students WHERE student_number = ? AND password = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentNumber);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Student student = new Student();
                student.setStudentNumber(rs.getString("student_number"));
                student.setName(rs.getString("name"));
                student.setSurname(rs.getString("surname"));
                student.setEmail(rs.getString("email"));
                student.setPassword(rs.getString("password"));
                return student;
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating student: " + e.getMessage());
        }
        return null;
    }
    
    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students WHERE role = 'student'";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Student student = new Student(
                    rs.getString("student_number"),
                    rs.getString("name"),
                    rs.getString("surname"),
                    rs.getString("email"),
                    rs.getString("password")
                );
                students.add(student);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all students: " + e.getMessage());
        }
        return students;
    }
    
    public Student getStudentByNumber(String studentNumber) {
        String sql = "SELECT * FROM students WHERE student_number = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Student(
                    rs.getString("student_number"),
                    rs.getString("name"),
                    rs.getString("surname"),
                    rs.getString("email"),
                    rs.getString("password")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting student by number: " + e.getMessage());
        }
        return null;
    }
}
