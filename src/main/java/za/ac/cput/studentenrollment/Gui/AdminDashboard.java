package za.ac.cput.studentenrollment.Gui;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import za.ac.cput.studentenrollment.connection.ClientCommunicator;
import za.ac.cput.studentenrollment.connection.RequestType;
import za.ac.cput.studentenrollment.connection.Response;
import za.ac.cput.studentenrollment.modelclasses.Course;
import za.ac.cput.studentenrollment.modelclasses.Student;

public class AdminDashboard extends JFrame {
    private ClientCommunicator client;
    private JTable studentsTable;
    private JTable coursesTable;
    private DefaultTableModel studentsModel;
    private DefaultTableModel coursesModel;

    public AdminDashboard(ClientCommunicator client) {
        this.client = client;
        initializeUI();
        loadStudents();
        loadCourses();
    }

    private void initializeUI() {
        setTitle("Admin Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        // Menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem logoutMenuItem = new JMenuItem("Logout");
        logoutMenuItem.addActionListener(e -> logout());
        fileMenu.add(logoutMenuItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // Main panel with tabs
        JTabbedPane tabbedPane = new JTabbedPane();

        // Students Management Tab
        tabbedPane.addTab("Manage Students", createStudentsPanel());

        // Courses Management Tab
        tabbedPane.addTab("Manage Courses", createCoursesPanel());

        // Enrollment Management Tab
        tabbedPane.addTab("View Enrollments", createEnrollmentsPanel());

        add(tabbedPane);
        setVisible(true);
    }

    private JPanel createStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Toolbar
        JToolBar toolBar = new JToolBar();
        JButton addStudentButton = new JButton("Add Student");
        JButton refreshButton = new JButton("Refresh");
        
        toolBar.add(addStudentButton);
        toolBar.add(refreshButton);
        panel.add(toolBar, BorderLayout.NORTH);

        // Students table
        String[] columns = {"Student Number", "Name", "Surname", "Email"};
        studentsModel = new DefaultTableModel(columns, 0);
        studentsTable = new JTable(studentsModel);
        JScrollPane scrollPane = new JScrollPane(studentsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Add action listeners
        addStudentButton.addActionListener(e -> showAddStudentDialog());
        refreshButton.addActionListener(e -> loadStudents());

        return panel;
    }

    private JPanel createCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Toolbar
        JToolBar toolBar = new JToolBar();
        JButton addCourseButton = new JButton("Add Course");
        JButton refreshButton = new JButton("Refresh");
        
        toolBar.add(addCourseButton);
        toolBar.add(refreshButton);
        panel.add(toolBar, BorderLayout.NORTH);

        // Courses table
        String[] columns = {"Course Code", "Title", "Instructor"};
        coursesModel = new DefaultTableModel(columns, 0);
        coursesTable = new JTable(coursesModel);
        JScrollPane scrollPane = new JScrollPane(coursesTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Add action listeners
        addCourseButton.addActionListener(e -> showAddCourseDialog());
        refreshButton.addActionListener(e -> loadCourses());

        return panel;
    }

    private JPanel createEnrollmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Course selection
        JPanel topPanel = new JPanel(new FlowLayout());
        JLabel courseLabel = new JLabel("Select Course:");
        JComboBox<String> courseComboBox = new JComboBox<>();
        JButton viewEnrollmentsButton = new JButton("View Enrollments");
        
        topPanel.add(courseLabel);
        topPanel.add(courseComboBox);
        topPanel.add(viewEnrollmentsButton);
        panel.add(topPanel, BorderLayout.NORTH);

        // Enrollments table
        String[] columns = {"Student Number", "Name", "Surname", "Email"};
        DefaultTableModel enrollmentsModel = new DefaultTableModel(columns, 0);
        JTable enrollmentsTable = new JTable(enrollmentsModel);
        JScrollPane scrollPane = new JScrollPane(enrollmentsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Load courses into combo box
        loadCoursesIntoComboBox(courseComboBox);

        // Add action listener
        viewEnrollmentsButton.addActionListener(e -> {
            if (courseComboBox.getSelectedIndex() != -1) {
                String selected = courseComboBox.getSelectedItem().toString();
                String courseCode = selected.split(" - ")[0];
                loadCourseEnrollments(courseCode, enrollmentsModel);
            }
        });

        return panel;
    }

    private void loadStudents() {
        try {
            // Use fully qualified name for Request
            za.ac.cput.studentenrollment.connection.Request request = 
                new za.ac.cput.studentenrollment.connection.Request(RequestType.GET_ALL_STUDENTS);
            Response response = client.sendRequest(request);
            
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
                JOptionPane.showMessageDialog(this, "Error loading students: " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCourses() {
        try {
            // Use fully qualified name for Request
            za.ac.cput.studentenrollment.connection.Request request = 
                new za.ac.cput.studentenrollment.connection.Request(RequestType.GET_ALL_COURSES);
            Response response = client.sendRequest(request);
            
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
                JOptionPane.showMessageDialog(this, "Error loading courses: " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCoursesIntoComboBox(JComboBox<String> comboBox) {
        try {
            // Use fully qualified name for Request
            za.ac.cput.studentenrollment.connection.Request request = 
                new za.ac.cput.studentenrollment.connection.Request(RequestType.GET_ALL_COURSES);
            Response response = client.sendRequest(request);
            
            if (response.isSuccess()) {
                List<Course> courses = (List<Course>) response.getData();
                comboBox.removeAllItems();
                
                for (Course course : courses) {
                    comboBox.addItem(course.getCourseCode() + " - " + course.getTitle());
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCourseEnrollments(String courseCode, DefaultTableModel model) {
        try {
            // Use fully qualified name for Request
            za.ac.cput.studentenrollment.connection.Request request = 
                new za.ac.cput.studentenrollment.connection.Request(RequestType.GET_COURSE_ENROLLMENTS, courseCode);
            Response response = client.sendRequest(request);
            
            if (response.isSuccess()) {
                List<Student> students = (List<Student>) response.getData();
                model.setRowCount(0);
                
                for (Student student : students) {
                    model.addRow(new Object[]{
                        student.getStudentNumber(),
                        student.getName(),
                        student.getSurname(),
                        student.getEmail()
                    });
                }
            } else {
                JOptionPane.showMessageDialog(this, "Error loading enrollments: " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddStudentDialog() {
        JDialog dialog = new JDialog(this, "Add New Student", true);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JTextField numberField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField surnameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        dialog.add(new JLabel("Student Number:"));
        dialog.add(numberField);
        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Surname:"));
        dialog.add(surnameField);
        dialog.add(new JLabel("Email:"));
        dialog.add(emailField);
        dialog.add(new JLabel("Password:"));
        dialog.add(passwordField);

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(new JLabel()); // empty cell
        dialog.add(buttonPanel);

        saveButton.addActionListener(e -> {
            String number = numberField.getText().trim();
            String name = nameField.getText().trim();
            String surname = surnameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (number.isEmpty() || name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Student student = new Student(number, name, surname, email, password);
                // Use fully qualified name for Request
                za.ac.cput.studentenrollment.connection.Request request = 
                    new za.ac.cput.studentenrollment.connection.Request(RequestType.ADD_STUDENT, student);
                Response response = client.sendRequest(request);

                if (response.isSuccess()) {
                    JOptionPane.showMessageDialog(dialog, "Student added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadStudents();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add student: " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void showAddCourseDialog() {
        JDialog dialog = new JDialog(this, "Add New Course", true);
        dialog.setLayout(new GridLayout(4, 2, 10, 10));
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);

        JTextField codeField = new JTextField();
        JTextField titleField = new JTextField();
        JTextField instructorField = new JTextField();

        dialog.add(new JLabel("Course Code:"));
        dialog.add(codeField);
        dialog.add(new JLabel("Title:"));
        dialog.add(titleField);
        dialog.add(new JLabel("Instructor:"));
        dialog.add(instructorField);

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(new JLabel()); // empty cell
        dialog.add(buttonPanel);

        saveButton.addActionListener(e -> {
            String code = codeField.getText().trim();
            String title = titleField.getText().trim();
            String instructor = instructorField.getText().trim();

            if (code.isEmpty() || title.isEmpty() || instructor.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Course course = new Course(code, title, instructor);
                // Use fully qualified name for Request
                za.ac.cput.studentenrollment.connection.Request request = 
                    new za.ac.cput.studentenrollment.connection.Request(RequestType.ADD_COURSE, course);
                Response response = client.sendRequest(request);

                if (response.isSuccess()) {
                    JOptionPane.showMessageDialog(dialog, "Course added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadCourses();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add course: " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void logout() {
        try {
            // Use fully qualified name for Request
            za.ac.cput.studentenrollment.connection.Request request = 
                new za.ac.cput.studentenrollment.connection.Request(RequestType.LOGOUT);
            client.sendRequest(request);
        } catch (Exception e) {
            // Ignore errors during logout
        } finally {
            client.disconnect();
            new LoginGUI();
            dispose();
        }
    }
}