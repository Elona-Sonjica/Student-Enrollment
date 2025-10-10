/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.ac.cput.studentenrollment.modelclasses;

import java.io.Serializable;

/**
 *
 * @author elzas
 */
public class Course implements Serializable {
    private String courseCode;
    private String title;
    private String instructor;
    
    public Course() {}
    
    public Course(String courseCode, String title, String instructor) {
        this.courseCode = courseCode;
        this.title = title;
        this.instructor = instructor;
    }
    
    // Getters and Setters
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }
    
    @Override
    public String toString() {
        return courseCode + " - " + title + " (" + instructor + ")";
    }
}
