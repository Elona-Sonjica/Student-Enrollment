/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.ac.cput.studentenrollment.DAO;

import za.ac.cput.studentenrollment.modelclasses.Course;
import za.ac.cput.studentenrollment.modelclasses.Student;

/**
 *
 * @author elzas
 */

public class DatabaseTest {
    public static void main(String[] args) {
        System.out.println("Testing database setup...");
        
        // Initialize DAOs
        StudentDAO studentDAO = new StudentDAO();
        CourseDAO courseDAO = new CourseDAO();
        EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
        
        // Create tables
        studentDAO.createTable();
        courseDAO.createTable();
        enrollmentDAO.createTable();
        
        System.out.println("Database setup completed successfully!");
        System.out.println("Available courses: " + courseDAO.getAllCourses().size());
        System.out.println("Registered students: " + studentDAO.getAllStudents().size());
        System.out.println("Note: No sample data added. Users must register students and create courses through the application.");
    }
}