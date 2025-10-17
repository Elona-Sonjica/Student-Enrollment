package za.ac.cput.studentenrollment.Gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import javax.swing.table.DefaultTableCellRenderer;
import za.ac.cput.studentenrollment.connection.ClientCommunicator;
import za.ac.cput.studentenrollment.connection.Request;
import za.ac.cput.studentenrollment.connection.RequestType;
import za.ac.cput.studentenrollment.connection.Response;
import za.ac.cput.studentenrollment.modelclasses.Course;
import za.ac.cput.studentenrollment.modelclasses.Student;
import za.ac.cput.studentenrollment.util.RoundedImageUtil;
/**
 *
 * @author elzas
 */

public class StudentGUI extends JFrame {
    private ClientCommunicator client;
    private Student student;
    private JTable coursesTable;
    private JTable enrollmentsTable;
    private JTable classmatesTable; // NEW: Added for classmates tab
    private DefaultTableModel coursesModel;
    private DefaultTableModel enrollmentsModel;
    private DefaultTableModel classmatesModel; // NEW: Added for classmates tab
    private JLabel statusLabel;
    private JComboBox<String> courseComboBox; // NEW: Added for course selection
    private Map<String, List<Student>> courseStudentsMap; // NEW: Added for storing course-student data
    private JButton logoutButton; // NEW: Added visible logout button

    public StudentGUI(ClientCommunicator client, Student student) {
        this.client = client;
        this.student = student;
        initializeUI();
        loadCourses();
        loadEnrollments();
        loadClassmates(); // NEW: Load classmates data on startup
    }

    private void initializeUI() {
        setTitle("Student Portal - " + student.getStudentNumber());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        // Menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem logoutMenuItem = new JMenuItem("Logout");
        logoutMenuItem.addActionListener(e -> logout());
        fileMenu.add(logoutMenuItem);

        JMenuItem refreshMenuItem = new JMenuItem("Refresh");
        refreshMenuItem.addActionListener(e -> refreshTables());
        fileMenu.add(refreshMenuItem);

        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // Main panel with header and tabs
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Add header with LOGOUT BUTTON
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // Main panel with tabs
        JTabbedPane tabbedPane = new JTabbedPane();

        // Available Courses Tab
        tabbedPane.addTab("Available Courses", createCoursesPanel());

        // My Enrollments Tab
        tabbedPane.addTab("My Enrollments", createEnrollmentsPanel());

        // NEW: Classmates Tab
        tabbedPane.addTab("Classmates", createClassmatesPanel());

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

        // Welcome message and student info
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setBackground(new Color(240, 240, 240));

        JLabel welcomeLabel = new JLabel("Student Portal - Welcome, " + student.getName() + " " + student.getSurname(), JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        welcomeLabel.setForeground(new Color(0, 51, 102));

        JLabel studentInfoLabel = new JLabel("Student Number: " + student.getStudentNumber() + " | Email: " + student.getEmail(), JLabel.CENTER);
        studentInfoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        studentInfoLabel.setForeground(Color.DARK_GRAY);

        infoPanel.add(welcomeLabel);
        infoPanel.add(studentInfoLabel);

        // NEW: Logout button on the right
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutPanel.setBackground(new Color(240, 240, 240));
        logoutButton = new JButton("Logout");
        logoutButton.setBackground(new Color(220, 53, 69)); // Red color for logout
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFont(new Font("Arial", Font.BOLD, 12));
        logoutButton.setPreferredSize(new Dimension(100, 30));
        logoutButton.addActionListener(e -> logout());
        logoutPanel.add(logoutButton);

        headerPanel.add(logoPanel, BorderLayout.WEST);
        headerPanel.add(infoPanel, BorderLayout.CENTER);
        headerPanel.add(logoutPanel, BorderLayout.EAST); // NEW: Add logout button to header

        return headerPanel;
    }

    private JPanel createCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header panel with instructions and refresh button
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel headerLabel = new JLabel("Available Courses - Click 'Enroll' to register for a course");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        headerLabel.setForeground(new Color(0, 51, 102));
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh Courses");
        refreshButton.setBackground(new Color(0, 102, 204));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.addActionListener(e -> loadCourses());
        headerPanel.add(refreshButton, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Table for available courses
        String[] columns = {"Course Code", "Title", "Instructor", "Action"};
        coursesModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Only action column is editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 3 ? JButton.class : String.class;
            }
        };

        coursesTable = new JTable(coursesModel);
        coursesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        coursesTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        coursesTable.setRowHeight(30);
        coursesTable.setFont(new Font("Arial", Font.PLAIN, 12));

        // Center align all columns except action
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < coursesTable.getColumnCount() - 1; i++) {
            coursesTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Add enroll button to each row
        coursesTable.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());
        coursesTable.getColumnModel().getColumn(3).setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(coursesTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Courses Available for Enrollment"));
        scrollPane.setPreferredSize(new Dimension(800, 300));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Status label
        statusLabel = new JLabel("Loading available courses...");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.add(statusLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createEnrollmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel headerLabel = new JLabel("My Current Enrollments");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        headerLabel.setForeground(new Color(0, 51, 102));
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh Enrollments");
        refreshButton.setBackground(new Color(0, 102, 204));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.addActionListener(e -> loadEnrollments());
        headerPanel.add(refreshButton, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Table for enrolled courses
        String[] columns = {"Course Code", "Title", "Instructor"};
        enrollmentsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

        enrollmentsTable = new JTable(enrollmentsModel);
        enrollmentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        enrollmentsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        enrollmentsTable.setRowHeight(25);
        enrollmentsTable.setFont(new Font("Arial", Font.PLAIN, 12));

        // Center align all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < enrollmentsTable.getColumnCount(); i++) {
            enrollmentsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(enrollmentsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Currently Enrolled Courses"));
        scrollPane.setPreferredSize(new Dimension(800, 300));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // NEW: Create classmates panel
    private JPanel createClassmatesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel headerLabel = new JLabel("Students in My Courses");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        headerLabel.setForeground(new Color(0, 51, 102));
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(0, 102, 204));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.addActionListener(e -> loadClassmates());
        headerPanel.add(refreshButton, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Course selection combo box
        JPanel selectionPanel = new JPanel(new FlowLayout());
        selectionPanel.setBorder(BorderFactory.createTitledBorder("Select Course"));

        JLabel courseLabel = new JLabel("Course:");
        courseComboBox = new JComboBox<>();
        courseComboBox.setPreferredSize(new Dimension(300, 25));
        courseComboBox.addActionListener(e -> {
            if (courseComboBox.getSelectedItem() != null) {
                displayStudentsForSelectedCourse();
            }
        });

        selectionPanel.add(courseLabel);
        selectionPanel.add(courseComboBox);
        panel.add(selectionPanel, BorderLayout.CENTER);

        // Table for displaying students
        String[] columns = {"Student Number", "Name", "Surname", "Email"};
        classmatesModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        classmatesTable = new JTable(classmatesModel);
        classmatesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        classmatesTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        classmatesTable.setRowHeight(25);

        // Center align all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < classmatesTable.getColumnCount(); i++) {
            classmatesTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(classmatesTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Students in Selected Course"));
        scrollPane.setPreferredSize(new Dimension(800, 300));
        panel.add(scrollPane, BorderLayout.SOUTH);

        return panel;
    }

    private void loadCourses() {
        try {
            statusLabel.setText("Loading available courses...");
            Request request = new Request(RequestType.GET_ALL_COURSES);
            Response response = client.sendRequest(request);

            if (response.isSuccess()) {
                List<Course> courses = (List<Course>) response.getData();
                coursesModel.setRowCount(0);

                if (courses.isEmpty()) {
                    statusLabel.setText("No courses available. Please ask administrator to add courses.");
                    return;
                }

                // Load current enrollments to check which courses are already enrolled
                Request enrollRequest = new Request(RequestType.GET_STUDENT_ENROLLMENTS, student.getStudentNumber());
                Response enrollResponse = client.sendRequest(enrollRequest);
                List<Course> currentEnrollments = enrollResponse.isSuccess() ?
                        (List<Course>) enrollResponse.getData() : List.of();

                int availableCount = 0;
                for (Course course : courses) {
                    boolean isEnrolled = currentEnrollments.stream()
                            .anyMatch(enrolled -> enrolled.getCourseCode().equals(course.getCourseCode()));

                    if (!isEnrolled) {
                        coursesModel.addRow(new Object[]{
                                course.getCourseCode(),
                                course.getTitle(),
                                course.getInstructor(),
                                "Enroll"
                        });
                        availableCount++;
                    }
                }

                if (availableCount == 0) {
                    statusLabel.setText("No available courses to enroll in. You are already enrolled in all courses.");
                } else {
                    statusLabel.setText(availableCount + " courses available for enrollment");
                }
            } else {
                statusLabel.setText("Error loading courses");
                JOptionPane.showMessageDialog(this, "Error loading courses: " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            statusLabel.setText("Error loading courses");
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void loadEnrollments() {
        try {
            Request request = new Request(RequestType.GET_STUDENT_ENROLLMENTS, student.getStudentNumber());
            Response response = client.sendRequest(request);

            if (response.isSuccess()) {
                List<Course> enrollments = (List<Course>) response.getData();
                enrollmentsModel.setRowCount(0);

                if (enrollments.isEmpty()) {
                    // Show message in table when no enrollments
                    enrollmentsModel.setRowCount(1);
                    enrollmentsModel.setValueAt("No course enrollments found. Please enroll in courses from the 'Available Courses' tab.", 0, 0);
                    enrollmentsModel.setValueAt("", 0, 1);
                    enrollmentsModel.setValueAt("", 0, 2);
                } else {
                    for (Course course : enrollments) {
                        enrollmentsModel.addRow(new Object[]{
                                course.getCourseCode(),
                                course.getTitle(),
                                course.getInstructor()
                        });
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Error loading enrollments: " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // NEW: Load classmates data
    private void loadClassmates() {
        try {
            Request request = new Request(RequestType.GET_STUDENTS_IN_MY_COURSES, student.getStudentNumber());
            Response response = client.sendRequest(request);

            if (response.isSuccess()) {
                courseStudentsMap = (Map<String, List<Student>>) response.getData();
                updateCourseComboBox();

                if (courseStudentsMap.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "You are not enrolled in any courses yet.",
                            "No Enrollments",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error loading classmates: " + response.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // NEW: Update course combo box with enrolled courses
    private void updateCourseComboBox() {
        courseComboBox.removeAllItems();

        if (courseStudentsMap != null && !courseStudentsMap.isEmpty()) {
            for (String courseName : courseStudentsMap.keySet()) {
                courseComboBox.addItem(courseName);
            }
        } else {
            courseComboBox.addItem("No courses available");
        }
    }

    // NEW: Display students for selected course
    private void displayStudentsForSelectedCourse() {
        if (courseComboBox.getSelectedItem() == null ||
                courseComboBox.getSelectedItem().toString().equals("No courses available")) {
            return;
        }

        String selectedCourse = courseComboBox.getSelectedItem().toString();
        List<Student> students = courseStudentsMap.get(selectedCourse);

        classmatesModel.setRowCount(0);

        if (students != null && !students.isEmpty()) {
            for (Student student : students) {
                classmatesModel.addRow(new Object[]{
                        student.getStudentNumber(),
                        student.getName(),
                        student.getSurname(),
                        student.getEmail()
                });
            }
        } else {
            classmatesModel.addRow(new Object[]{
                    "No other students enrolled", "", "", ""
            });
        }
    }

    private void enrollInCourse(String courseCode, String courseTitle) {
        try {
            Object[] enrollmentData = new Object[]{student.getStudentNumber(), courseCode};
            Request request = new Request(RequestType.ENROLL_STUDENT, enrollmentData);
            Response response = client.sendRequest(request);

            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                        "Successfully enrolled in " + courseCode + " - " + courseTitle,
                        "Enrollment Successful",
                        JOptionPane.INFORMATION_MESSAGE
                );
                refreshTables();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to enroll: " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void refreshTables() {
        loadCourses();
        loadEnrollments();
        loadClassmates(); // NEW: Refresh classmates data
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

    // Button renderer and editor for enroll button
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBackground(new Color(0, 102, 204));
            setForeground(Color.WHITE);
            setFont(new Font("Arial", Font.BOLD, 11));
            setBorder(BorderFactory.createRaisedBevelBorder());
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String currentCourseCode;
        private String currentCourseTitle;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setBackground(new Color(0, 102, 204));
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Arial", Font.BOLD, 11));
            button.setBorder(BorderFactory.createRaisedBevelBorder());

            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                    int choice = JOptionPane.showConfirmDialog(
                            StudentGUI.this,
                            "Are you sure you want to enroll in:\n" +
                                    currentCourseCode + " - " + currentCourseTitle + "?",
                            "Confirm Enrollment",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );
                    if (choice == JOptionPane.YES_OPTION) {
                        enrollInCourse(currentCourseCode, currentCourseTitle);
                    }
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentCourseCode = table.getValueAt(row, 0).toString();
            currentCourseTitle = table.getValueAt(row, 1).toString();
            button.setText(value.toString());
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "Enroll";
        }
    }
}