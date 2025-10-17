/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.ac.cput.studentenrollment.connection;

import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import za.ac.cput.studentenrollment.DAO.CourseDAO;
import za.ac.cput.studentenrollment.DAO.EnrollmentDAO;
import za.ac.cput.studentenrollment.DAO.StudentDAO;
import za.ac.cput.studentenrollment.util.InputValidator;
import za.ac.cput.studentenrollment.util.PasswordUtil;
import za.ac.cput.studentenrollment.modelclasses.Course;
import za.ac.cput.studentenrollment.modelclasses.Student;
import za.ac.cput.studentenrollment.util.InputValidator;
import za.ac.cput.studentenrollment.util.PasswordUtil;

/**
 *
 * @author elzas
 */

public class Server {
    private ServerSocket serverSocket;
    private boolean running;
    private static final int PORT = 6666;

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            System.out.println("Server started on port " + PORT);

            // Initialize database - this will create tables and admin user only
            initializeDatabase();

            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");

                // Handle client in new thread
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeDatabase() {
        // This will trigger the database initialization through DBConnection.getConnection()
        try {
            Connection conn = za.ac.cput.studentenrollment.Database.DBConnection.getConnection();
            System.out.println("Database checked and ready");
        } catch (Exception e) {
            System.out.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Error stopping server: " + e.getMessage());
        }
    }

    // Client handler as inner class
    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private ObjectInputStream input;
        private ObjectOutputStream output;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                output = new ObjectOutputStream(clientSocket.getOutputStream());
                input = new ObjectInputStream(clientSocket.getInputStream());

                while (true) {
                    Request request = (Request) input.readObject();
                    Response response = processRequest(request);
                    output.writeObject(response);
                    output.flush();

                    if (request.getType() == RequestType.LOGOUT) {
                        break;
                    }
                }
            } catch (EOFException e) {
                System.out.println("Client disconnected normally");
            } catch (Exception e) {
                System.out.println("Client disconnected: " + e.getMessage());
                e.printStackTrace();
            } finally {
                closeConnection();
            }
        }

        private Response processRequest(Request request) {
            try {
                switch (request.getType()) {
                    case AUTHENTICATE:
                        return handleAuthentication(request);
                    case GET_ALL_COURSES:
                        return handleGetAllCourses();
                    case ENROLL_STUDENT:
                        return handleEnrollStudent(request);
                    case GET_STUDENT_ENROLLMENTS:
                        return handleGetStudentEnrollments(request);
                    case ADD_STUDENT:
                        return handleAddStudent(request);
                    case ADD_COURSE:
                        return handleAddCourse(request);
                    case GET_ALL_STUDENTS:
                        return handleGetAllStudents();
                    case GET_COURSE_ENROLLMENTS:
                        return handleGetCourseEnrollments(request);
                    case GET_STUDENTS_IN_MY_COURSES: // NEW: Added case
                        return handleGetStudentsInMyCourses(request);
                    case LOGOUT:
                        return new Response(ResponseStatus.SUCCESS, "Logged out");
                    default:
                        return new Response(ResponseStatus.ERROR, "Unknown request");
                }
            } catch (Exception e) {
                System.err.println("Error processing request: " + e.getMessage());
                e.printStackTrace();
                return new Response(ResponseStatus.ERROR, "Server error: " + e.getMessage());
            }
        }

        private Response handleAuthentication(Request request) {
            try {
                Object[] data = (Object[]) request.getData();
                String studentNumber = (String) data[0];
                String password = (String) data[1];
                String email = (String) data[2];
                boolean isAdmin = (boolean) data[3];

                // Input validation
                if (!InputValidator.isValidStudentNumber(studentNumber) && !"admin".equals(studentNumber)) {
                    return new Response(ResponseStatus.ERROR, "Invalid student number format");
                }

                if (!InputValidator.isValidPassword(password)) {
                    return new Response(ResponseStatus.ERROR, "Invalid password");
                }

                if (isAdmin) {
                    if ("admin".equals(studentNumber) && "admin123".equals(password)) {
                        Student admin = new Student("admin", "System", "Administrator", "admin@cput.ac.za", "");
                        return new Response(ResponseStatus.SUCCESS, admin);
                    }
                } else {
                    if (!InputValidator.isValidEmail(email)) {
                        return new Response(ResponseStatus.ERROR, "Invalid email format");
                    }

                    StudentDAO studentDAO = new StudentDAO();
                    Student student = studentDAO.authenticate(studentNumber, password);
                    if (student != null) {
                        // Case-insensitive email comparison
                        if (email.equalsIgnoreCase(student.getEmail())) {
                            student.setPassword(""); // Don't send password back
                            return new Response(ResponseStatus.SUCCESS, student);
                        } else {
                            return new Response(ResponseStatus.ERROR, "Email does not match student record");
                        }
                    }
                }
                return new Response(ResponseStatus.ERROR, "Login failed - invalid credentials");
            } catch (Exception e) {
                System.err.println("Authentication error: " + e.getMessage());
                return new Response(ResponseStatus.ERROR, "Authentication error");
            }
        }

        private Response handleGetAllCourses() {
            try {
                CourseDAO courseDAO = new CourseDAO();
                List<Course> courses = courseDAO.getAllCourses();
                if (courses.isEmpty()) {
                    return new Response(ResponseStatus.SUCCESS, courses, "No courses available. Please add courses first.");
                }
                return new Response(ResponseStatus.SUCCESS, courses);
            } catch (Exception e) {
                System.err.println("Error getting courses: " + e.getMessage());
                return new Response(ResponseStatus.ERROR, "Failed to load courses");
            }
        }

        private Response handleEnrollStudent(Request request) {
            try {
                Object[] data = (Object[]) request.getData();
                String studentNumber = (String) data[0];
                String courseCode = (String) data[1];

                // Validation
                if (!InputValidator.isValidStudentNumber(studentNumber)) {
                    return new Response(ResponseStatus.ERROR, "Invalid student number");
                }

                CourseDAO courseDAO = new CourseDAO();
                Course course = courseDAO.getCourseByCode(courseCode);
                if (course == null) {
                    return new Response(ResponseStatus.ERROR, "Course not found: " + courseCode);
                }

                EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
                boolean success = enrollmentDAO.enrollStudent(studentNumber, courseCode);

                if (success) {
                    return new Response(ResponseStatus.SUCCESS, "Enrolled successfully in " + course.getTitle());
                } else {
                    return new Response(ResponseStatus.ERROR, "Enrollment failed - student may already be enrolled");
                }
            } catch (Exception e) {
                System.err.println("Enrollment error: " + e.getMessage());
                return new Response(ResponseStatus.ERROR, "Enrollment error");
            }
        }

        private Response handleGetStudentEnrollments(Request request) {
            try {
                String studentNumber = (String) request.getData();

                if (!InputValidator.isValidStudentNumber(studentNumber) && !"admin".equals(studentNumber)) {
                    return new Response(ResponseStatus.ERROR, "Invalid student number");
                }

                EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
                List<Course> enrollments = enrollmentDAO.getStudentEnrollments(studentNumber);
                if (enrollments.isEmpty()) {
                    return new Response(ResponseStatus.SUCCESS, enrollments, "No enrollments found. Please enroll in courses first.");
                }
                return new Response(ResponseStatus.SUCCESS, enrollments);
            } catch (Exception e) {
                System.err.println("Error getting enrollments: " + e.getMessage());
                return new Response(ResponseStatus.ERROR, "Failed to load enrollments");
            }
        }

        private Response handleAddStudent(Request request) {
            try {
                Student student = (Student) request.getData();

                // Validation
                if (!InputValidator.isValidStudentNumber(student.getStudentNumber())) {
                    return new Response(ResponseStatus.ERROR, "Invalid student number format (5-10 digits required)");
                }

                if (!InputValidator.isValidName(student.getName()) || !InputValidator.isValidName(student.getSurname())) {
                    return new Response(ResponseStatus.ERROR, "Invalid name or surname");
                }

                if (!InputValidator.isValidEmail(student.getEmail())) {
                    return new Response(ResponseStatus.ERROR, "Invalid email format");
                }

                if (!InputValidator.isValidPassword(student.getPassword())) {
                    return new Response(ResponseStatus.ERROR, "Password must be at least 6 characters");
                }

                StudentDAO studentDAO = new StudentDAO();

                // Check if student already exists
                if (studentDAO.getStudentByNumber(student.getStudentNumber()) != null) {
                    return new Response(ResponseStatus.ERROR, "Student number already exists");
                }

                // Hash password before storing
                String hashedPassword = PasswordUtil.hashPassword(student.getPassword());
                student.setPassword(hashedPassword);

                boolean success = studentDAO.addStudent(student);
                if (success) {
                    return new Response(ResponseStatus.SUCCESS, "Student added successfully");
                } else {
                    return new Response(ResponseStatus.ERROR, "Failed to add student");
                }
            } catch (Exception e) {
                System.err.println("Error adding student: " + e.getMessage());
                return new Response(ResponseStatus.ERROR, "Failed to add student");
            }
        }

        private Response handleAddCourse(Request request) {
            try {
                Course course = (Course) request.getData();

                // Validation
                if (!InputValidator.isValidCourseCode(course.getCourseCode())) {
                    return new Response(ResponseStatus.ERROR, "Invalid course code format (e.g., ADP262S)");
                }

                if (course.getTitle() == null || course.getTitle().trim().isEmpty()) {
                    return new Response(ResponseStatus.ERROR, "Course title is required");
                }

                if (course.getInstructor() == null || course.getInstructor().trim().isEmpty()) {
                    return new Response(ResponseStatus.ERROR, "Instructor name is required");
                }

                CourseDAO courseDAO = new CourseDAO();

                // Check if course already exists
                if (courseDAO.courseExists(course.getCourseCode())) {
                    return new Response(ResponseStatus.ERROR, "Course code already exists");
                }

                boolean success = courseDAO.addCourse(course);
                if (success) {
                    return new Response(ResponseStatus.SUCCESS, "Course added successfully");
                } else {
                    return new Response(ResponseStatus.ERROR, "Failed to add course");
                }
            } catch (Exception e) {
                System.err.println("Error adding course: " + e.getMessage());
                return new Response(ResponseStatus.ERROR, "Failed to add course");
            }
        }

        private Response handleGetAllStudents() {
            try {
                StudentDAO studentDAO = new StudentDAO();
                List<Student> students = studentDAO.getAllStudents();
                // Remove passwords for security
                for (Student student : students) {
                    student.setPassword("");
                }
                if (students.isEmpty()) {
                    return new Response(ResponseStatus.SUCCESS, students, "No students registered. Please add students first.");
                }
                return new Response(ResponseStatus.SUCCESS, students);
            } catch (Exception e) {
                System.err.println("Error getting students: " + e.getMessage());
                return new Response(ResponseStatus.ERROR, "Failed to load students");
            }
        }

        private Response handleGetCourseEnrollments(Request request) {
            try {
                String courseCode = (String) request.getData();

                CourseDAO courseDAO = new CourseDAO();
                Course course = courseDAO.getCourseByCode(courseCode);
                if (course == null) {
                    return new Response(ResponseStatus.ERROR, "Course not found: " + courseCode);
                }

                EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
                List<Student> students = enrollmentDAO.getCourseEnrollments(courseCode);
                if (students.isEmpty()) {
                    return new Response(ResponseStatus.SUCCESS, students, "No students enrolled in " + courseCode);
                }
                return new Response(ResponseStatus.SUCCESS, students);
            } catch (Exception e) {
                System.err.println("Error getting course enrollments: " + e.getMessage());
                return new Response(ResponseStatus.ERROR, "Failed to load course enrollments");
            }
        }

        // NEW: Handler method for getting students in the same courses
        private Response handleGetStudentsInMyCourses(Request request) {
            try {
                String studentNumber = (String) request.getData();

                if (!InputValidator.isValidStudentNumber(studentNumber)) {
                    return new Response(ResponseStatus.ERROR, "Invalid student number");
                }

                EnrollmentDAO enrollmentDAO = new EnrollmentDAO();

                // First get all courses the student is enrolled in
                List<Course> studentCourses = enrollmentDAO.getStudentEnrollments(studentNumber);

                if (studentCourses.isEmpty()) {
                    return new Response(ResponseStatus.SUCCESS, new HashMap<>(),
                            "You are not enrolled in any courses");
                }

                // For each course, get the enrolled students
                Map<String, List<Student>> courseEnrollments = new HashMap<>();
                for (Course course : studentCourses) {
                    List<Student> students = enrollmentDAO.getCourseEnrollments(course.getCourseCode());
                    // Remove the current student from the list
                    students.removeIf(student -> student.getStudentNumber().equals(studentNumber));
                    courseEnrollments.put(course.getCourseCode() + " - " + course.getTitle(), students);
                }

                return new Response(ResponseStatus.SUCCESS, courseEnrollments);

            } catch (Exception e) {
                System.err.println("Error getting students in courses: " + e.getMessage());
                return new Response(ResponseStatus.ERROR, "Failed to load course enrollments");
            }
        }

        private void closeConnection() {
            try {
                if (input != null) input.close();
                if (output != null) output.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}