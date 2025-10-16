/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.ac.cput.studentenrollment.modelclasses;

import java.io.Serializable;

public class Student implements Serializable {
    private String studentNumber;
    private String name;
    private String surname;
    private String email;
    private String password;
    
    public Student() {}
    
    public Student(String studentNumber, String name, String surname, String email, String password) {
        this.studentNumber = studentNumber;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
    }
    
    // Getters and Setters
    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    @Override
    public String toString() {
        return name + " " + surname + " (" + studentNumber + ")";
    }
}
