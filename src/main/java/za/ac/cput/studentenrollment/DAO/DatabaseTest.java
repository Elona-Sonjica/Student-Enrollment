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
        
        //sample courses
        Course[] sampleCourses = {
            new Course("ADF262S", "Application Development Fundamentals", "Mr Burger"),
            new Course("ADP262S", "Application Development Practice", "Mr Naidoo"),
            new Course("ICT262S", "ICT Electives", "Mr Olivier"),
            new Course("PROJ262S", "Project", "Ms Tswane"),
            new Course("INM262S", "Information Management", "Mr Ayodeji")
        };
        
        for (Course course : sampleCourses) {
            if (!courseDAO.courseExists(course.getCourseCode())) {
                courseDAO.addCourse(course);
                System.out.println("Added course: " + course.getCourseCode());
            }
        }
        
        //sample student
        Student sampleStudent = new Student("123456", "John", "Doe", "john.doe@mycput.ac.za", "password123");
        if (studentDAO.getStudentByNumber("123456") == null) {
            studentDAO.addStudent(sampleStudent);
            System.out.println("Added sample student: " + sampleStudent.getStudentNumber());
        }
        
        System.out.println("Database setup completed successfully!");
        System.out.println("Available courses: " + courseDAO.getAllCourses().size());
        System.out.println("Registered students: " + studentDAO.getAllStudents().size());
    }
}
