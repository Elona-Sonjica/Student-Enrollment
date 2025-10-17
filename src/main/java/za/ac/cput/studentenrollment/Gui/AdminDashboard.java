package za.ac.cput.studentenrollment.Gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import za.ac.cput.studentenrollment.connection.ClientCommunicator;
import za.ac.cput.studentenrollment.connection.Request;
import za.ac.cput.studentenrollment.connection.RequestType;
import za.ac.cput.studentenrollment.connection.Response;
import za.ac.cput.studentenrollment.modelclasses.Course;
import za.ac.cput.studentenrollment.modelclasses.Student;
import za.ac.cput.studentenrollment.util.RoundedImageUtil;

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
        setTitle("Admin Portal - Student Enrollment System");
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

        // Main panel with header and tabs
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Add header
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // Main panel with tabs
        JTabbedPane tabbedPane = new JTabbedPane();

        // Students Management Tab
        tabbedPane.addTab("Manage Students", createStudentsPanel());

        // Courses Management Tab
        tabbedPane.addTab("Manage Courses", createCoursesPanel());

        // Enrollment Management Tab
        tabbedPane.addTab("View Enrollments", createEnrollmentsPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        add(mainPanel);
        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        headerPanel.setBackground(new Color(240, 240, 240));
        
        // Logo on the left
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logoPanel.setBackground(new Color(240, 240, 240));
        JLabel logoLabel = new JLabel();
        ImageIcon smallLogo = RoundedImageUtil.createRoundedImageIcon("/images/cput-logo-1.jpg", 50);
        if (smallLogo != null) {
            logoLabel.setIcon(smallLogo);
        } else {
            // Fallback if image not found
            logoLabel.setText("CPUT");
            logoLabel.setFont(new Font("Arial", Font.BOLD, 16));
            logoLabel.setForeground(Color.BLUE);
        }
        logoPanel.add(logoLabel);
        
        // Title in the center
        JLabel titleLabel = new JLabel("Admin Portal - Student Enrollment System", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 51, 102)); // Dark blue color
        
        headerPanel.add(logoPanel, BorderLayout.WEST);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        return headerPanel;
    }

    private JPanel createStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        JButton addStudentButton = new JButton("Add New Student");
        JButton refreshButton = new JButton("Refresh List");
        
        // Style buttons
        addStudentButton.setBackground(new Color(0, 102, 204));
        addStudentButton.setForeground(Color.WHITE);
        refreshButton.setBackground(new Color(102, 102, 102));
        refreshButton.setForeground(Color.WHITE);
        
        toolBar.add(addStudentButton);
        toolBar.add(Box.createHorizontalStrut(10));
        toolBar.add(refreshButton);
        panel.add(toolBar, BorderLayout.NORTH);

        // Students table
        String[] columns = {"Student Number", "Name", "Surname", "Email"};
        studentsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        studentsTable = new JTable(studentsModel);
        studentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        studentsTable.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(studentsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Registered Students"));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Status label
        JLabel statusLabel = new JLabel("No students registered. Click 'Add New Student' to register students.");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.add(statusLabel, BorderLayout.SOUTH);

        // Add action listeners
        addStudentButton.addActionListener(e -> showAddStudentDialog());
        refreshButton.addActionListener(e -> loadStudents());

        return panel;
    }

    private JPanel createCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        JButton addCourseButton = new JButton("Add New Course");
        JButton refreshButton = new JButton("Refresh List");
        
        // Style buttons
        addCourseButton.setBackground(new Color(0, 102, 204));
        addCourseButton.setForeground(Color.WHITE);
        refreshButton.setBackground(new Color(102, 102, 102));
        refreshButton.setForeground(Color.WHITE);
        
        toolBar.add(addCourseButton);
        toolBar.add(Box.createHorizontalStrut(10));
        toolBar.add(refreshButton);
        panel.add(toolBar, BorderLayout.NORTH);

        // Courses table
        String[] columns = {"Course Code", "Title", "Instructor"};
        coursesModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        coursesTable = new JTable(coursesModel);
        coursesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        coursesTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        coursesTable.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(coursesTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Available Courses"));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Status label
        JLabel statusLabel = new JLabel("No courses available. Click 'Add New Course' to create courses.");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.add(statusLabel, BorderLayout.SOUTH);

        // Add action listeners
        addCourseButton.addActionListener(e -> showAddCourseDialog());
        refreshButton.addActionListener(e -> loadCourses());

        return panel;
    }

    private JPanel createEnrollmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Course selection panel
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Course Selection"));
        JLabel courseLabel = new JLabel("Select Course:");
        courseLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        JComboBox<String> courseComboBox = new JComboBox<>();
        courseComboBox.setPreferredSize(new Dimension(300, 25));
        
        JButton viewEnrollmentsButton = new JButton("View Enrollments");
        JButton refreshButton = new JButton("Refresh Courses");
        
        // Style buttons
        viewEnrollmentsButton.setBackground(new Color(0, 102, 204));
        viewEnrollmentsButton.setForeground(Color.WHITE);
        refreshButton.setBackground(new Color(102, 102, 102));
        refreshButton.setForeground(Color.WHITE);
        
        topPanel.add(courseLabel);
        topPanel.add(courseComboBox);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(viewEnrollmentsButton);
        topPanel.add(Box.createHorizontalStrut(5));
        topPanel.add(refreshButton);
        panel.add(topPanel, BorderLayout.NORTH);

        // Enrollments table
        String[] columns = {"Student Number", "Name", "Surname", "Email"};
        DefaultTableModel enrollmentsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        JTable enrollmentsTable = new JTable(enrollmentsModel);
        enrollmentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        enrollmentsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        enrollmentsTable.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(enrollmentsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Course Enrollments"));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Status label
        JLabel statusLabel = new JLabel("Select a course to view enrollments");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.add(statusLabel, BorderLayout.SOUTH);

        // Load courses into combo box
        loadCoursesIntoComboBox(courseComboBox);

        // Add action listeners
        viewEnrollmentsButton.addActionListener(e -> {
            if (courseComboBox.getSelectedIndex() != -1 && !courseComboBox.getSelectedItem().toString().contains("No courses")) {
                String selected = courseComboBox.getSelectedItem().toString();
                String courseCode = selected.split(" - ")[0];
                loadCourseEnrollments(courseCode, enrollmentsModel, statusLabel);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a course first", "No Course Selected", JOptionPane.WARNING_MESSAGE);
            }
        });

        refreshButton.addActionListener(e -> {
            loadCoursesIntoComboBox(courseComboBox);
            enrollmentsModel.setRowCount(0);
            statusLabel.setText("Select a course to view enrollments");
        });

        return panel;
    }

    private void loadStudents() {
        try {
            Request request = new Request(RequestType.GET_ALL_STUDENTS);
            Response response = client.sendRequest(request);
            
            if (response.isSuccess()) {
                List<Student> students = (List<Student>) response.getData();
                studentsModel.setRowCount(0);
                
                if (students.isEmpty()) {
                    // Show message in table when no students
                    studentsModel.setRowCount(1);
                    studentsModel.setValueAt("No students registered. Add students using the 'Add New Student' button.", 0, 0);
                } else {
                    for (Student student : students) {
                        studentsModel.addRow(new Object[]{
                            student.getStudentNumber(),
                            student.getName(),
                            student.getSurname(),
                            student.getEmail()
                        });
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Error loading students: " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void loadCourses() {
        try {
            Request request = new Request(RequestType.GET_ALL_COURSES);
            Response response = client.sendRequest(request);
            
            if (response.isSuccess()) {
                List<Course> courses = (List<Course>) response.getData();
                coursesModel.setRowCount(0);
                
                if (courses.isEmpty()) {
                    // Show message in table when no courses
                    coursesModel.setRowCount(1);
                    coursesModel.setValueAt("No courses available. Add courses using the 'Add New Course' button.", 0, 0);
                } else {
                    for (Course course : courses) {
                        coursesModel.addRow(new Object[]{
                            course.getCourseCode(),
                            course.getTitle(),
                            course.getInstructor()
                        });
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Error loading courses: " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void loadCoursesIntoComboBox(JComboBox<String> comboBox) {
        try {
            Request request = new Request(RequestType.GET_ALL_COURSES);
            Response response = client.sendRequest(request);
            
            if (response.isSuccess()) {
                List<Course> courses = (List<Course>) response.getData();
                comboBox.removeAllItems();
                
                if (courses.isEmpty()) {
                    comboBox.addItem("No courses available - Add courses first");
                } else {
                    for (Course course : courses) {
                        comboBox.addItem(course.getCourseCode() + " - " + course.getTitle());
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void loadCourseEnrollments(String courseCode, DefaultTableModel model, JLabel statusLabel) {
        try {
            Request request = new Request(RequestType.GET_COURSE_ENROLLMENTS, courseCode);
            Response response = client.sendRequest(request);
            
            if (response.isSuccess()) {
                List<Student> students = (List<Student>) response.getData();
                model.setRowCount(0);
                
                if (students.isEmpty()) {
                    statusLabel.setText("No students enrolled in " + courseCode);
                } else {
                    for (Student student : students) {
                        model.addRow(new Object[]{
                            student.getStudentNumber(),
                            student.getName(),
                            student.getSurname(),
                            student.getEmail()
                        });
                    }
                    statusLabel.setText(students.size() + " students enrolled in " + courseCode);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Error loading enrollments: " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void showAddStudentDialog() {
        JDialog dialog = new JDialog(this, "Register New Student", true);
        dialog.setLayout(new GridLayout(6, 2, 10, 10));
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(new Color(240, 240, 240));

        JTextField numberField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField surnameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        // Create labels with bold font
        JLabel numberLabel = new JLabel("Student Number*:");
        JLabel nameLabel = new JLabel("Name*:");
        JLabel surnameLabel = new JLabel("Surname*:");
        JLabel emailLabel = new JLabel("Email*:");
        JLabel passwordLabel = new JLabel("Password*:");
        
        Font boldFont = new Font("Arial", Font.BOLD, 12);
        numberLabel.setFont(boldFont);
        nameLabel.setFont(boldFont);
        surnameLabel.setFont(boldFont);
        emailLabel.setFont(boldFont);
        passwordLabel.setFont(boldFont);

        dialog.add(numberLabel);
        dialog.add(numberField);
        dialog.add(nameLabel);
        dialog.add(nameField);
        dialog.add(surnameLabel);
        dialog.add(surnameField);
        dialog.add(emailLabel);
        dialog.add(emailField);
        dialog.add(passwordLabel);
        dialog.add(passwordField);

        JButton saveButton = new JButton("Register Student");
        JButton cancelButton = new JButton("Cancel");

        // Style buttons
        saveButton.setBackground(new Color(0, 102, 204));
        saveButton.setForeground(Color.WHITE);
        cancelButton.setBackground(new Color(102, 102, 102));
        cancelButton.setForeground(Color.WHITE);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(new JLabel("* Required fields"));
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
                Request request = new Request(RequestType.ADD_STUDENT, student);
                Response response = client.sendRequest(request);

                if (response.isSuccess()) {
                    JOptionPane.showMessageDialog(dialog, "Student registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadStudents();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to register student: " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void showAddCourseDialog() {
        JDialog dialog = new JDialog(this, "Create New Course", true);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(new Color(240, 240, 240));

        JTextField codeField = new JTextField();
        JTextField titleField = new JTextField();
        JTextField instructorField = new JTextField();

        // Create labels with bold font
        JLabel codeLabel = new JLabel("Course Code*:");
        JLabel titleLabel = new JLabel("Course Title*:");
        JLabel instructorLabel = new JLabel("Instructor*:");
        
        Font boldFont = new Font("Arial", Font.BOLD, 12);
        codeLabel.setFont(boldFont);
        titleLabel.setFont(boldFont);
        instructorLabel.setFont(boldFont);

        dialog.add(codeLabel);
        dialog.add(codeField);
        dialog.add(titleLabel);
        dialog.add(titleField);
        dialog.add(instructorLabel);
        dialog.add(instructorField);

        JButton saveButton = new JButton("Create Course");
        JButton cancelButton = new JButton("Cancel");

        // Style buttons
        saveButton.setBackground(new Color(0, 102, 204));
        saveButton.setForeground(Color.WHITE);
        cancelButton.setBackground(new Color(102, 102, 102));
        cancelButton.setForeground(Color.WHITE);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(new JLabel("* Required fields"));
        dialog.add(buttonPanel);
        dialog.add(new JLabel("Format: XXX262S (e.g., ADP262S)"));

        saveButton.addActionListener(e -> {
            String code = codeField.getText().trim().toUpperCase();
            String title = titleField.getText().trim();
            String instructor = instructorField.getText().trim();

            if (code.isEmpty() || title.isEmpty() || instructor.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Course course = new Course(code, title, instructor);
                Request request = new Request(RequestType.ADD_COURSE, course);
                Response response = client.sendRequest(request);

                if (response.isSuccess()) {
                    JOptionPane.showMessageDialog(dialog, "Course created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadCourses();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to create course: " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void logout() {
        try {
            Request request = new Request(RequestType.LOGOUT);
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