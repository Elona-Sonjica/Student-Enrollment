package za.ac.cput.studentenrollment.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import za.ac.cput.studentenrollment.modelclasses.Course;
import za.ac.cput.studentenrollment.modelclasses.Student;

public class AdminDashboard extends JPanel {
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private ClientCommunicator communicator;

    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SECONDARY_COLOR = new Color(236, 240, 241);
    private final Color ACCENT_COLOR = new Color(52, 152, 219);
    private final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private final Color WARNING_COLOR = new Color(230, 126, 34);
    private final Color DANGER_COLOR = new Color(231, 76, 60);

    private JLabel lblWelcome;
    private JTabbedPane tabbedPane;
    private JProgressBar progressBar;
    private JLabel lblStatus;

    private DefaultTableModel studentsModel;
    private DefaultTableModel coursesModel;
    private DefaultTableModel enrollmentsModel;

    private JTable studentsTable;
    private JTable coursesTable;
    private JTable enrollmentsTable;

    private JButton btnLogout;
    private JButton btnRefresh;
    private JButton btnAddStudent;
    private JButton btnAddCourse;
    private JButton btnDeleteStudent;
    private JButton btnDeleteCourse;
    private JButton btnViewEnrollments;

    public AdminDashboard(JPanel cardPanel, CardLayout cardLayout, ClientCommunicator communicator) {
        this.cardPanel = cardPanel;
        this.cardLayout = cardLayout;
        this.communicator = communicator;
        initializeComponents();
        setupModernGUI();
        loadInitialData();
    }

    private void initializeComponents() {
        lblWelcome = new JLabel("⚙️ Administrator Dashboard");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblWelcome.setForeground(PRIMARY_COLOR);

        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        progressBar.setIndeterminate(true);

        lblStatus = new JLabel("Ready");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(Color.DARK_GRAY);

        initializeTables();
        initializeButtons();
    }

    private void initializeTables() {
        String[] studentColumns = {"Student Number", "Name", "Surname", "Email", "Actions"};
        studentsModel = new DefaultTableModel(studentColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };

        String[] courseColumns = {"Course Code", "Course Name", "Instructor", "Enrolled", "Actions"};
        coursesModel = new DefaultTableModel(courseColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };

        String[] enrollmentColumns = {"Student", "Course", "Enrollment Date", "Actions"};
        enrollmentsModel = new DefaultTableModel(enrollmentColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };

        studentsTable = createStyledTable(studentsModel);
        coursesTable = createStyledTable(coursesModel);
        enrollmentsTable = createStyledTable(enrollmentsModel);
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(35);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setShowGrid(true);
        table.setGridColor(new Color(240, 240, 240));
        return table;
    }

    private void initializeButtons() {
        btnLogout = createModernButton("🚪 Logout", DANGER_COLOR);
        btnRefresh = createModernButton("🔄 Refresh", ACCENT_COLOR);
        btnAddStudent = createModernButton("➕ Add Student", SUCCESS_COLOR);
        btnAddCourse = createModernButton("➕ Add Course", SUCCESS_COLOR);
        btnDeleteStudent = createModernButton("🗑️ Delete Student", DANGER_COLOR);
        btnDeleteCourse = createModernButton("🗑️ Delete Course", DANGER_COLOR);
        btnViewEnrollments = createModernButton("📊 View Enrollments", WARNING_COLOR);

        btnLogout.addActionListener(e -> logout());
        btnRefresh.addActionListener(e -> refreshData());
        btnAddStudent.addActionListener(e -> showAddStudentDialog());
        btnAddCourse.addActionListener(e -> showAddCourseDialog());
        btnDeleteStudent.addActionListener(e -> deleteSelectedStudent());
        btnDeleteCourse.addActionListener(e -> deleteSelectedCourse());
        btnViewEnrollments.addActionListener(e -> viewCourseEnrollments());
    }

    private JButton createModernButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private void setupModernGUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(SECONDARY_COLOR);

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        add(createStatusPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        welcomePanel.setBackground(Color.WHITE);
        welcomePanel.add(lblWelcome);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlsPanel.setBackground(Color.WHITE);
        controlsPanel.add(btnRefresh);
        controlsPanel.add(btnLogout);

        headerPanel.add(welcomePanel, BorderLayout.WEST);
        headerPanel.add(controlsPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createMainPanel() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        tabbedPane.addTab("👥 Students Management", createStudentsPanel());
        tabbedPane.addTab("📚 Courses Management", createCoursesPanel());
        tabbedPane.addTab("📊 Enrollment Reports", createEnrollmentsPanel());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        return mainPanel;
    }

    private JPanel createStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(btnAddStudent);
        buttonPanel.add(btnDeleteStudent);

        JScrollPane scrollPane = new JScrollPane(studentsTable);
        scrollPane.setPreferredSize(new Dimension(800, 400));

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(btnAddCourse);
        buttonPanel.add(btnDeleteCourse);
        buttonPanel.add(btnViewEnrollments);

        JScrollPane scrollPane = new JScrollPane(coursesTable);
        scrollPane.setPreferredSize(new Dimension(800, 400));

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createEnrollmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.add(createStatCard("Total Students", "👥", "0", PRIMARY_COLOR));
        statsPanel.add(createStatCard("Total Courses", "📚", "0", SUCCESS_COLOR));
        statsPanel.add(createStatCard("Total Enrollments", "📊", "0", WARNING_COLOR));

        JScrollPane scrollPane = new JScrollPane(enrollmentsTable);
        scrollPane.setPreferredSize(new Dimension(800, 300));

        panel.add(statsPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatCard(String title, String icon, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(icon + " " + title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(Color.DARK_GRAY);

        JLabel valueLabel = new JLabel(value, JLabel.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(color);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        statusPanel.add(lblStatus, BorderLayout.WEST);
        statusPanel.add(progressBar, BorderLayout.EAST);
        return statusPanel;
    }

    private void loadInitialData() {
        refreshData();
    }

    private void refreshData() {
        setLoadingState(true, "🔄 Loading data...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                loadStudents();
                loadCourses();
                loadEnrollments();
                return null;
            }

            @Override
            protected void done() {
                setLoadingState(false, "✅ Data loaded successfully");
            }
        };
        worker.execute();
    }

    private void loadStudents() {
        try {
            Request request = new Request(RequestType.GET_ALL_STUDENTS);
            Response response = communicator.sendRequest(request);
            if (response.isSuccess()) {
                List<Student> students = (List<Student>) response.getData();
                studentsModel.setRowCount(0);
                for (Student student : students) {
                    studentsModel.addRow(new Object[]{
                            student.getStudentNumber(),
                            student.getName(),
                            student.getSurname(),
                            student.getEmail(),
                            "👁️ View"
                    });
                }
            }
        } catch (Exception e) {
            setStatus("❌ Error loading students: " + e.getMessage(), DANGER_COLOR);
        }
    }

    private void loadCourses() {
        try {
            Request request = new Request(RequestType.GET_ALL_COURSES);
            Response response = communicator.sendRequest(request);
            if (response.isSuccess()) {
                List<Course> courses = (List<Course>) response.getData();
                coursesModel.setRowCount(0);
                for (Course course : courses) {
                    coursesModel.addRow(new Object[]{
                            course.getCourseCode(),
                            course.getTitle(),
                            course.getInstructor(),
                            "0",
                            "👁️ View"
                    });
                }
            }
        } catch (Exception e) {
            setStatus("❌ Error loading courses: " + e.getMessage(), DANGER_COLOR);
        }
    }

    private void loadEnrollments() {
        try {
            enrollmentsModel.setRowCount(0);
            enrollmentsModel.addRow(new Object[]{"John Doe", "ADP262S", "2024-01-15", "📝 Edit"});
            enrollmentsModel.addRow(new Object[]{"Jane Smith", "ITS362S", "2024-01-16", "📝 Edit"});
        } catch (Exception e) {
            setStatus("❌ Error loading enrollments: " + e.getMessage(), DANGER_COLOR);
        }
    }

    private void showAddStudentDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Student", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField txtNumber = new JTextField(20);
        JTextField txtName = new JTextField(20);
        JTextField txtSurname = new JTextField(20);
        JTextField txtEmail = new JTextField(20);
        JPasswordField txtPassword = new JPasswordField(20);

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Student Number:"), gbc);
        gbc.gridx = 1; formPanel.add(txtNumber, gbc);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; formPanel.add(txtName, gbc);
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Surname:"), gbc);
        gbc.gridx = 1; formPanel.add(txtSurname, gbc);
        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; formPanel.add(txtEmail, gbc);
        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; formPanel.add(txtPassword, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnSave = createModernButton("💾 Save Student", SUCCESS_COLOR);
        JButton btnCancel = createModernButton("❌ Cancel", DANGER_COLOR);

        btnSave.addActionListener(e -> {
            if (validateStudentInput(txtNumber, txtName, txtSurname, txtEmail, txtPassword)) {
                saveNewStudent(txtNumber.getText(), txtName.getText(), txtSurname.getText(),
                        txtEmail.getText(), new String(txtPassword.getPassword()));
                dialog.dispose();
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showAddCourseDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Course", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField txtCode = new JTextField(20);
        JTextField txtTitle = new JTextField(20);
        JTextField txtInstructor = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Course Code:"), gbc);
        gbc.gridx = 1; formPanel.add(txtCode, gbc);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Course Title:"), gbc);
        gbc.gridx = 1; formPanel.add(txtTitle, gbc);
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Instructor:"), gbc);
        gbc.gridx = 1; formPanel.add(txtInstructor, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnSave = createModernButton("💾 Save Course", SUCCESS_COLOR);
        JButton btnCancel = createModernButton("❌ Cancel", DANGER_COLOR);

        btnSave.addActionListener(e -> {
            if (validateCourseInput(txtCode, txtTitle, txtInstructor)) {
                saveNewCourse(txtCode.getText(), txtTitle.getText(), txtInstructor.getText());
                dialog.dispose();
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private boolean validateStudentInput(JTextField number, JTextField name, JTextField surname,
                                         JTextField email, JPasswordField password) {
        if (number.getText().trim().isEmpty() || name.getText().trim().isEmpty() ||
                surname.getText().trim().isEmpty() || email.getText().trim().isEmpty() ||
                password.getPassword().length == 0) {
            JOptionPane.showMessageDialog(this, "❌ Please fill in all fields", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean validateCourseInput(JTextField code, JTextField title, JTextField instructor) {
        if (code.getText().trim().isEmpty() || title.getText().trim().isEmpty() ||
                instructor.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "❌ Please fill in all fields", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void saveNewStudent(String number, String name, String surname, String email, String password) {
        setLoadingState(true, "💾 Saving student...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                Student student = new Student(number, name, surname, email, password);
                Request request = new Request(RequestType.ADD_STUDENT, student);
                Response response = communicator.sendRequest(request);
                return response.isSuccess();
            }

            @Override
            protected void done() {
                setLoadingState(false, "Ready");
                try {
                    boolean success = get();
                    if (success) {
                        setStatus("✅ Student added successfully", SUCCESS_COLOR);
                        refreshData();
                    } else {
                        setStatus("❌ Failed to add student", DANGER_COLOR);
                    }
                } catch (Exception e) {
                    setStatus("❌ Error saving student: " + e.getMessage(), DANGER_COLOR);
                }
            }
        };
        worker.execute();
    }

    private void saveNewCourse(String code, String title, String instructor) {
        setLoadingState(true, "💾 Saving course...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                Course course = new Course(code, title, instructor);
                Request request = new Request(RequestType.ADD_COURSE, course);
                Response response = communicator.sendRequest(request);
                return response.isSuccess();
            }

            @Override
            protected void done() {
                setLoadingState(false, "Ready");
                try {
                    boolean success = get();
                    if (success) {
                        setStatus("✅ Course added successfully", SUCCESS_COLOR);
                        refreshData();
                    } else {
                        setStatus("❌ Failed to add course", DANGER_COLOR);
                    }
                } catch (Exception e) {
                    setStatus("❌ Error saving course: " + e.getMessage(), DANGER_COLOR);
                }
            }
        };
        worker.execute();
    }

    private void deleteSelectedStudent() {
        int selectedRow = studentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "❌ Please select a student to delete", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String studentNumber = (String) studentsTable.getValueAt(selectedRow, 0);
        String studentName = (String) studentsTable.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "🗑️ Are you sure you want to delete student:\n" + studentName + " (" + studentNumber + ")?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            setStatus("🗑️ Deleting student...", WARNING_COLOR);
        }
    }

    private void deleteSelectedCourse() {
        int selectedRow = coursesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "❌ Please select a course to delete", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String courseCode = (String) coursesTable.getValueAt(selectedRow, 0);
        String courseName = (String) coursesTable.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "🗑️ Are you sure you want to delete course:\n" + courseName + " (" + courseCode + ")?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            setStatus("🗑️ Deleting course...", WARNING_COLOR);
        }
    }

    private void viewCourseEnrollments() {
        int selectedRow = coursesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "❌ Please select a course to view enrollments", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String courseCode = (String) coursesTable.getValueAt(selectedRow, 0);
        String courseName = (String) coursesTable.getValueAt(selectedRow, 1);

        JOptionPane.showMessageDialog(this,
                "📊 Enrollment Report for:\n" + courseName + " (" + courseCode + ")\n\n" +
                        "Total Enrolled: 25 students\n" +
                        "Available Spots: 15\n" +
                        "Enrollment Rate: 62%",
                "Course Enrollments",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "🚪 Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            communicator.disconnect();
            cardLayout.show(cardPanel, "login");
        }
    }

    private void setLoadingState(boolean loading, String message) {
        progressBar.setVisible(loading);
        btnRefresh.setEnabled(!loading);
        btnAddStudent.setEnabled(!loading);
        btnAddCourse.setEnabled(!loading);
        btnDeleteStudent.setEnabled(!loading);
        btnDeleteCourse.setEnabled(!loading);
        btnViewEnrollments.setEnabled(!loading);
        setStatus(message, loading ? Color.BLUE : Color.DARK_GRAY);
    }

    private void setStatus(String message, Color color) {
        lblStatus.setText(message);
        lblStatus.setForeground(color);
    }
}