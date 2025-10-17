package za.ac.cput.studentenrollment.modelclasses;

import java.io.Serializable;

public class Course implements Serializable {
    private static final long serialVersionUID = 1L;

    private String courseCode;
    private String title;
    private String instructor;

    public Course() {}

    public Course(String courseCode, String title, String instructor) {
        this.courseCode = courseCode;
        this.title = title;
        this.instructor = instructor;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    @Override
    public String toString() {
        return courseCode + " - " + title;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Course course = (Course) obj;
        return courseCode != null ? courseCode.equals(course.courseCode) : course.courseCode == null;
    }

    @Override
    public int hashCode() {
        return courseCode != null ? courseCode.hashCode() : 0;
    }
}