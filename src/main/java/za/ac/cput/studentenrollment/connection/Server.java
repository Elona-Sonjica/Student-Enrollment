/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.ac.cput.studentenrollment.connection;

import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.util.List;
import za.ac.cput.studentenrollment.DAO.CourseDAO;
import za.ac.cput.studentenrollment.DAO.EnrollmentDAO;
import za.ac.cput.studentenrollment.DAO.StudentDAO;
import za.ac.cput.studentenrollment.modelclasses.Course;
import za.ac.cput.studentenrollment.modelclasses.Student;

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

            // Initialize database
            initializeDatabase();

            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");
                
                // Handle client in new thread
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

private void initializeDatabase() {
    // This will trigger the database initialization through DBConnection.getConnection()
    try {
        Connection conn = za.ac.cput.studentenrollment.Database.DBConnection.getConnection();
        System.out.println("Database checked and ready");
    } catch (Exception e) {
        System.out.println("Database initialization failed: " + e.getMessage());
    }
}

    private void addSampleData() {
        CourseDAO courseDAO = new CourseDAO();
        StudentDAO studentDAO = new StudentDAO();
        
        Course[] courses = {
            new Course("ADF262S", "Application Development Fundamentals", "Mr Burger"),
            new Course("ADP262S", "Application Development Practice", "Mr Naidoo"),
            new Course("ICT262S", "ICT Electives", "Mr Olivier"),
            new Course("PROJ262S", "Project", "Ms Tswane"),
            new Course("INM262S", "Information Management", "Mr Ayodeji")
        };
        
        for (Course course : courses) {
            if (!courseDAO.courseExists(course.getCourseCode())) {
                courseDAO.addCourse(course);
            }
        }

        // Add admin user
        Student admin = new Student("admin", "Admin", "User", "admin@cput.ac.za", "admin123");
        if (studentDAO.getStudentByNumber("admin") == null) {
            studentDAO.addStudent(admin);
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
                    // Use fully qualified name to avoid import conflict
                    za.ac.cput.studentenrollment.connection.Request request = 
                        (za.ac.cput.studentenrollment.connection.Request) input.readObject();
                    Response response = processRequest(request);
                    output.writeObject(response);
                    output.flush();
                    
                    if (request.getType() == RequestType.LOGOUT) {
                        break;
                    }
                }
            } catch (Exception e) {
                System.out.println("Client disconnected: " + e.getMessage());
            } finally {
                closeConnection();
            }
        }

        private Response processRequest(za.ac.cput.studentenrollment.connection.Request request) {
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
                    case LOGOUT:
                        return new Response(ResponseStatus.SUCCESS, "Logged out");
                    default:
                        return new Response(ResponseStatus.ERROR, "Unknown request");
                }
            } catch (Exception e) {
                return new Response(ResponseStatus.ERROR, "Server error: " + e.getMessage());
            }
        }

        private Response handleAuthentication(za.ac.cput.studentenrollment.connection.Request request) {
            Object[] data = (Object[]) request.getData();
            String studentNumber = (String) data[0];
            String password = (String) data[1];
            String email = (String) data[2];
            boolean isAdmin = (boolean) data[3];

            if (isAdmin) {
                if ("admin".equals(studentNumber) && "admin123".equals(password)) {
                    Student admin = new Student("admin", "Admin", "User", "admin@cput.ac.za", "");
                    return new Response(ResponseStatus.SUCCESS, admin);
                }
            } else {
                StudentDAO studentDAO = new StudentDAO();
                Student student = studentDAO.authenticate(studentNumber, password);
                if (student != null && student.getEmail().equals(email)) {
                    student.setPassword(""); // Don't send password back
                    return new Response(ResponseStatus.SUCCESS, student);
                }
            }
            return new Response(ResponseStatus.ERROR, "Login failed");
        }

        private Response handleGetAllCourses() {
            CourseDAO courseDAO = new CourseDAO();
            List<Course> courses = courseDAO.getAllCourses();
            return new Response(ResponseStatus.SUCCESS, courses);
        }

        private Response handleEnrollStudent(za.ac.cput.studentenrollment.connection.Request request) {
            Object[] data = (Object[]) request.getData();
            String studentNumber = (String) data[0];
            String courseCode = (String) data[1];
            
            EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
            boolean success = enrollmentDAO.enrollStudent(studentNumber, courseCode);
            
            if (success) {
                return new Response(ResponseStatus.SUCCESS, "Enrolled successfully");
            } else {
                return new Response(ResponseStatus.ERROR, "Enrollment failed");
            }
        }

        private Response handleGetStudentEnrollments(za.ac.cput.studentenrollment.connection.Request request) {
            String studentNumber = (String) request.getData();
            EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
            List<Course> enrollments = enrollmentDAO.getStudentEnrollments(studentNumber);
            return new Response(ResponseStatus.SUCCESS, enrollments);
        }

        private Response handleAddStudent(za.ac.cput.studentenrollment.connection.Request request) {
            Student student = (Student) request.getData();
            StudentDAO studentDAO = new StudentDAO();
            
            boolean success = studentDAO.addStudent(student);
            if (success) {
                return new Response(ResponseStatus.SUCCESS, "Student added");
            } else {
                return new Response(ResponseStatus.ERROR, "Failed to add student");
            }
        }

        private Response handleAddCourse(za.ac.cput.studentenrollment.connection.Request request) {
            Course course = (Course) request.getData();
            CourseDAO courseDAO = new CourseDAO();
            
            boolean success = courseDAO.addCourse(course);
            if (success) {
                return new Response(ResponseStatus.SUCCESS, "Course added");
            } else {
                return new Response(ResponseStatus.ERROR, "Failed to add course");
            }
        }

        private Response handleGetAllStudents() {
            StudentDAO studentDAO = new StudentDAO();
            List<Student> students = studentDAO.getAllStudents();
            // Remove passwords for security
            for (Student student : students) {
                student.setPassword("");
            }
            return new Response(ResponseStatus.SUCCESS, students);
        }

        private Response handleGetCourseEnrollments(za.ac.cput.studentenrollment.connection.Request request) {
            String courseCode = (String) request.getData();
            EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
            List<Student> students = enrollmentDAO.getCourseEnrollments(courseCode);
            return new Response(ResponseStatus.SUCCESS, students);
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