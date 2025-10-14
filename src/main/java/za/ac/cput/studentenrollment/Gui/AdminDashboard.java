package za.ac.cput.studentenrollment.Gui;

import za.ac.cput.studentenrollment.modelclasses.Student;
import za.ac.cput.studentenrollment.modelclasses.Course;
import za.ac.cput.studentenrollment.connection.ClientCommunicator;
import za.ac.cput.studentenrollment.connection.Request;
import za.ac.cput.studentenrollment.connection.RequestType;
import za.ac.cput.studentenrollment.connection.Response;
import za.ac.cput.studentenrollment.connection.ResponseStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class AdminDashboard extends JFrame {
    private Student currentAdmin;
    private ClientCommunicator client;
    private JTabbedPane tabbedPane;
    private JTable studentsTable;
    private JTable coursesTable;
    private DefaultTableModel studentsModel;
    private DefaultTableModel coursesModel;

    public AdminDashboard(JPanel admin, CardLayout cardLayout, ClientCommunicator communicator) {
        this.currentAdmin = admin;
        this.client = new ClientCommunicator();
        
        if (!client.connect()) {
            JOptionPane.showMessageDialog(this, "Cannot connect to server", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        initializeUI();
        loadData();
    }

    public AdminDashboard(Student student) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void initializeUI() {
        setTitle("Admin Dashboard - CPUT");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(240, 240, 240));

        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Content area with tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        // Students tab
        JPanel studentsPanel = createStudentsPanel();
        tabbedPane.addTab("Students", studentsPanel);
        
        // Courses tab
        JPanel coursesPanel = createCoursesPanel();
        tabbedPane.addTab("Courses", coursesPanel);
        
        // Enrollments tab
        JPanel enrollmentsPanel = createEnrollmentsPanel();
        tabbedPane.addTab("Enrollments", enrollmentsPanel);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Administrator Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);

        JLabel userLabel = new JLabel("Logged in as: " + currentAdmin.getName());
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userLabel.setForeground(Color.LIGHT_GRAY);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutButton.setBackground(new Color(231, 76, 60));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> logout());

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        infoPanel.add(titleLabel, BorderLayout.NORTH);
        infoPanel.add(userLabel, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(logoutButton);

        headerPanel.add(infoPanel, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Table
        String[] columns = {"Student Number", "Name", "Surname", "Email"};
        studentsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        studentsTable = new JTable(studentsModel);
        studentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(studentsTable);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addStudentButton = new JButton("Add Student");
        JButton refreshButton = new JButton("Refresh");
        
        styleButton(addStudentButton, new Color(46, 204, 113));
        styleButton(refreshButton, new Color(52, 152, 219));
        
        addStudentButton.addActionListener(e -> addStudent());
        refreshButton.addActionListener(e -> loadStudents());
        
        buttonPanel.add(addStudentButton);
        buttonPanel.add(refreshButton);

        panel.add(new JLabel("Student Management"), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Table
        String[] columns = {"Course Code", "Title", "Instructor"};
        coursesModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        coursesTable = new JTable(coursesModel);
        coursesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(coursesTable);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addCourseButton = new JButton("Add Course");
        JButton refreshButton = new JButton("Refresh");
        
        styleButton(addCourseButton, new Color(46, 204, 113));
        styleButton(refreshButton, new Color(52, 152, 219));
        
        addCourseButton.addActionListener(e -> addCourse());
        refreshButton.addActionListener(e -> loadCourses());
        
        buttonPanel.add(addCourseButton);
        buttonPanel.add(refreshButton);

        panel.add(new JLabel("Course Management"), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createEnrollmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel infoLabel = new JLabel("Select a course from the Courses tab to view enrollments");
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        infoLabel.setForeground(Color.GRAY);

        panel.add(infoLabel, BorderLayout.CENTER);
        return panel;
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
    }

    private void loadData() {
        loadStudents();
        loadCourses();
    }

    private void loadStudents() {
        Request request = new Request(RequestType.GET_ALL_STUDENTS);
        Response response = client.sendRequest((com.sun.net.httpserver.Request) request);
        
        if (response.isSuccess()) {
            List<Student> students = (List<Student>) response.getData();
            studentsModel.setRowCount(0);
            for (Student student : students) {
                studentsModel.addRow(new Object[]{
                    student.getStudentNumber(),
                    student.getName(),
                    student.getSurname(),
                    student.getEmail()
                });
            }
        } else {
            JOptionPane.showMessageDialog(this, "Failed to load students: " + response.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCourses() {
        Request request = new Request(RequestType.GET_ALL_COURSES);
        Response response = client.sendRequest((com.sun.net.httpserver.Request) request);
        
        if (response.isSuccess()) {
            List<Course> courses = (List<Course>) response.getData();
            coursesModel.setRowCount(0);
            for (Course course : courses) {
                coursesModel.addRow(new Object[]{
                    course.getCourseCode(),
                    course.getTitle(),
                    course.getInstructor()
                });
            }
        } else {
            JOptionPane.showMessageDialog(this, "Failed to load courses: " + response.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addStudent() {
        // Create dialog for adding student
        JTextField numberField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField surnameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        Object[] message = {
            "Student Number:", numberField,
            "Name:", nameField,
            "Surname:", surnameField,
            "Email:", emailField,
            "Password:", passwordField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add Student", 
            JOptionPane.OK_CANCEL_OPTION);
        
        if (option == JOptionPane.OK_OPTION) {
            String number = numberField.getText().trim();
            String name = nameField.getText().trim();
            String surname = surnameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (number.isEmpty() || name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Student newStudent = new Student(number, name, surname, email, password);
            Request request = new Request(RequestType.ADD_STUDENT, newStudent);
            Response response = client.sendRequest((com.sun.net.httpserver.Request) request);
            
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Student added successfully");
                loadStudents();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add student: " + response.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addCourse() {
        // Create dialog for adding course
        JTextField codeField = new JTextField();
        JTextField titleField = new JTextField();
        JTextField instructorField = new JTextField();

        Object[] message = {
            "Course Code:", codeField,
            "Title:", titleField,
            "Instructor:", instructorField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add Course", 
            JOptionPane.OK_CANCEL_OPTION);
        
        if (option == JOptionPane.OK_OPTION) {
            String code = codeField.getText().trim();
            String title = titleField.getText().trim();
            String instructor = instructorField.getText().trim();

            if (code.isEmpty() || title.isEmpty() || instructor.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Course newCourse = new Course(code, title, instructor);
            Request request = new Request(RequestType.ADD_COURSE, newCourse);
            Response response = client.sendRequest((com.sun.net.httpserver.Request) request);
            
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Course added successfully");
                loadCourses();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add course: " + response.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void logout() {
        client.disconnect();
        new LoginScreen().setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        // For testing only
        Student testAdmin = new Student("admin", "Admin", "User", "admin@cput.ac.za", "");
        SwingUtilities.invokeLater(() -> new AdminPanel(testAdmin, cardLayout, communicator).setVisible(true));
    }
}