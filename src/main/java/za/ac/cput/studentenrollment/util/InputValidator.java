/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.ac.cput.studentenrollment.util;

import java.util.regex.Pattern;
/**
 *
 * @author elzas
 */



/**
 * Utility class for input validation
 */
public class InputValidator {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern STUDENT_NUMBER_PATTERN = 
        Pattern.compile("^\\d{5,10}$");
    private static final Pattern COURSE_CODE_PATTERN = 
        Pattern.compile("^[A-Z]{3}\\d{3}[A-Z]?$");
    
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    public static boolean isValidStudentNumber(String studentNumber) {
        return studentNumber != null && STUDENT_NUMBER_PATTERN.matcher(studentNumber).matches();
    }
    
    public static boolean isValidCourseCode(String courseCode) {
        return courseCode != null && COURSE_CODE_PATTERN.matcher(courseCode).matches();
    }
    
    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty() && name.length() <= 50;
    }
    
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
}
