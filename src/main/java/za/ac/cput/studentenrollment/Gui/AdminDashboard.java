package za.ac.cput.studentenrollment.Gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private JComboBox<String> studentComboBox;
    private JComboBox<String> courseComboBox;

    public AdminDashboard(ClientCommunicator client) {
        this.client = client;
        initializeUI();
        loadStudents();
        loadCourses();
    }

    private void initializeUI() {
        setTitle("Admin Portal - Student Enrollment System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem logoutMenuItem = new JMenuItem("Logout");
        logoutMenuItem.addActionListener(e -> logout());
        fileMenu.add(logoutMenuItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Manage Students", createStudentsPanel());
        tabbedPane.addTab("Manage Courses", createCoursesPanel());
        tabbedPane.addTab("Enrollments by Course", createCourseEnrollmentsPanel());
        tabbedPane.addTab("Enrollments by Student", createStudentEnrollmentsPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        headerPanel.setBackground(new Color(240, 240, 240));

        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logoPanel.setBackground(new Color(240, 240, 240));
        JLabel logoLabel = new JLabel();
        ImageIcon smallLogo = RoundedImageUtil.createRoundedImageIcon("/images/cput-logo-1.jpg", 50);
        if (smallLogo != null) {
            logoLabel.setIcon(smallLogo);
        } else {
            logoLabel.setText("CPUT");
            logoLabel.setFont(new Font("Arial", Font.BOLD, 16));
            logoLabel.setForeground(Color.BLUE);
        }
        logoPanel.add(logoLabel);

        JLabel titleLabel = new JLabel("Admin Portal - Student Enrollment System", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 51, 102));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(new Color(240, 240, 240));

        JButton refreshButton = createRoundedButton("🔄 Refresh", new Color(0, 102, 204), Color.WHITE);
        refreshButton.addActionListener(e -> refreshAllData());

        JButton logoutButton = createRoundedButton("🚪 Logout", new Color(220, 53, 69), Color.WHITE);
        logoutButton.addActionListener(e -> logout());

        buttonPanel.add(refreshButton);
        buttonPanel.add(logoutButton);

        headerPanel.add(logoPanel, BorderLayout.WEST);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JButton createRoundedButton(String text, Color backgroundColor, Color textColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(backgroundColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(backgroundColor.brighter());
                } else {
                    g2.setColor(backgroundColor);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                g2.setColor(backgroundColor.darker());
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);

                super.paintComponent(g);
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(textColor);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 35));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(textColor.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(textColor);
            }
        });

        return button;
    }

    private void refreshAllData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                loadStudents();
                loadCourses();
                loadCoursesIntoComboBox(courseComboBox);
                loadStudentsIntoComboBox(studentComboBox);
                return null;
            }

            @Override
            protected void done() {
                JOptionPane.showMessageDialog(AdminDashboard.this,
                        "All data refreshed successfully!",
                        "Refresh Complete",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        };
        worker.execute();
    }

    private JPanel createStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        JButton addStudentButton = createRoundedButton("➕ Add Student", new Color(40, 167, 69), Color.WHITE);
        JButton editStudentButton = createRoundedButton("✏️ Edit Student", new Color(255, 193, 7), Color.BLACK);
        JButton deleteStudentButton = createRoundedButton("🗑️ Delete Student", new Color(220, 53, 69), Color.WHITE);
        JButton refreshStudentsButton = createRoundedButton("🔄 Refresh", new Color(108, 117, 125), Color.WHITE);

        addStudentButton.addActionListener(e -> showAddStudentDialog());
        editStudentButton.addActionListener(e -> editSelectedStudent());
        deleteStudentButton.addActionListener(e -> deleteSelectedStudent());
        refreshStudentsButton.addActionListener(e -> loadStudents());

        toolBar.add(addStudentButton);
        toolBar.add(Box.createHorizontalStrut(5));
        toolBar.add(editStudentButton);
        toolBar.add(Box.createHorizontalStrut(5));
        toolBar.add(deleteStudentButton);
        toolBar.add(Box.createHorizontalStrut(5));
        toolBar.add(refreshStudentsButton);
        panel.add(toolBar, BorderLayout.NORTH);

        String[] columns = {"Student Number", "Name", "Surname", "Email"};
        studentsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        studentsTable = new JTable(studentsModel);
        studentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        studentsTable.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(studentsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Registered Students"));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        JButton addCourseButton = createRoundedButton("➕ Add Course", new Color(40, 167, 69), Color.WHITE);
        JButton editCourseButton = createRoundedButton("✏️ Edit Course", new Color(255, 193, 7), Color.BLACK);
        JButton deleteCourseButton = createRoundedButton("🗑️ Delete Course", new Color(220, 53, 69), Color.WHITE);
        JButton refreshCoursesButton = createRoundedButton("🔄 Refresh", new Color(108, 117, 125), Color.WHITE);

        addCourseButton.addActionListener(e -> showAddCourseDialog());
        editCourseButton.addActionListener(e -> editSelectedCourse());
        deleteCourseButton.addActionListener(e -> deleteSelectedCourse());
        refreshCoursesButton.addActionListener(e -> loadCourses());

        toolBar.add(addCourseButton);
        toolBar.add(Box.createHorizontalStrut(5));
        toolBar.add(editCourseButton);
        toolBar.add(Box.createHorizontalStrut(5));
        toolBar.add(deleteCourseButton);
        toolBar.add(Box.createHorizontalStrut(5));
        toolBar.add(refreshCoursesButton);
        panel.add(toolBar, BorderLayout.NORTH);

        String[] columns = {"Course Code", "Title", "Instructor"};
        coursesModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        coursesTable = new JTable(coursesModel);
        coursesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        coursesTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        coursesTable.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(coursesTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Available Courses"));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCourseEnrollmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Course Selection"));
        JLabel courseLabel = new JLabel("Select Course:");
        courseLabel.setFont(new Font("Arial", Font.BOLD, 12));

        courseComboBox = new JComboBox<>();
        courseComboBox.setPreferredSize(new Dimension(300, 25));

        JButton viewEnrollmentsButton = createRoundedButton("👥 View Enrollments", new Color(0, 102, 204), Color.WHITE);
        JButton refreshCoursesComboButton = createRoundedButton("🔄 Refresh", new Color(108, 117, 125), Color.WHITE);

        topPanel.add(courseLabel);
        topPanel.add(courseComboBox);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(viewEnrollmentsButton);
        topPanel.add(Box.createHorizontalStrut(5));
        topPanel.add(refreshCoursesComboButton);
        panel.add(topPanel, BorderLayout.NORTH);

        String[] columns = {"Student Number", "Name", "Surname", "Email"};
        DefaultTableModel enrollmentsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable enrollmentsTable = new JTable(enrollmentsModel);
        enrollmentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        enrollmentsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        enrollmentsTable.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(enrollmentsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Course Enrollments"));
        panel.add(scrollPane, BorderLayout.CENTER);

        JLabel statusLabel = new JLabel("Select a course to view enrollments");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.add(statusLabel, BorderLayout.SOUTH);

        loadCoursesIntoComboBox(courseComboBox);

        viewEnrollmentsButton.addActionListener(e -> {
            if (courseComboBox.getSelectedIndex() != -1 && !courseComboBox.getSelectedItem().toString().contains("No courses")) {
                String selected = courseComboBox.getSelectedItem().toString();
                String courseCode = selected.split(" - ")[0];
                loadCourseEnrollments(courseCode, enrollmentsModel, statusLabel);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a course first", "No Course Selected", JOptionPane.WARNING_MESSAGE);
            }
        });

        refreshCoursesComboButton.addActionListener(e -> {
            loadCoursesIntoComboBox(courseComboBox);
            enrollmentsModel.setRowCount(0);
            statusLabel.setText("Select a course to view enrollments");
        });

        return panel;
    }

    private JPanel createStudentEnrollmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Student Selection"));
        JLabel studentLabel = new JLabel("Select Student:");
        studentLabel.setFont(new Font("Arial", Font.BOLD, 12));

        studentComboBox = new JComboBox<>();
        studentComboBox.setPreferredSize(new Dimension(300, 25));

        JButton viewEnrollmentsButton = createRoundedButton("📚 View Enrollments", new Color(0, 102, 204), Color.WHITE);
        JButton refreshStudentsComboButton = createRoundedButton("🔄 Refresh", new Color(108, 117, 125), Color.WHITE);

        topPanel.add(studentLabel);
        topPanel.add(studentComboBox);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(viewEnrollmentsButton);
        topPanel.add(Box.createHorizontalStrut(5));
        topPanel.add(refreshStudentsComboButton);
        panel.add(topPanel, BorderLayout.NORTH);

        String[] columns = {"Course Code", "Title", "Instructor"};
        DefaultTableModel enrollmentsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable enrollmentsTable = new JTable(enrollmentsModel);
        enrollmentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        enrollmentsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        enrollmentsTable.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(enrollmentsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Student Enrollments"));
        panel.add(scrollPane, BorderLayout.CENTER);

        JLabel statusLabel = new JLabel("Select a student to view their enrollments");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.add(statusLabel, BorderLayout.SOUTH);

        loadStudentsIntoComboBox(studentComboBox);

        viewEnrollmentsButton.addActionListener(e -> {
            if (studentComboBox.getSelectedIndex() != -1 && !studentComboBox.getSelectedItem().toString().contains("No students")) {
                String selected = studentComboBox.getSelectedItem().toString();
                String studentNumber = selected.split(" - ")[0];
                loadStudentEnrollments(studentNumber, enrollmentsModel, statusLabel);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a student first", "No Student Selected", JOptionPane.WARNING_MESSAGE);
            }
        });

        refreshStudentsComboButton.addActionListener(e -> {
            loadStudentsIntoComboBox(studentComboBox);
            enrollmentsModel.setRowCount(0);
            statusLabel.setText("Select a student to view their enrollments");
        });

        return panel;
    }

    private void showAddStudentDialog() {
        JDialog dialog = new JDialog(this, "Register New Student", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField numberField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField surnameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        formPanel.add(new JLabel("Student Number*:"));
        formPanel.add(numberField);
        formPanel.add(new JLabel("Name*:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Surname*:"));
        formPanel.add(surnameField);
        formPanel.add(new JLabel("Email*:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Password*:"));
        formPanel.add(passwordField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = createRoundedButton("Register Student", new Color(40, 167, 69), Color.WHITE);
        JButton cancelButton = createRoundedButton("Cancel", new Color(108, 117, 125), Color.WHITE);

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
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showAddCourseDialog() {
        JDialog dialog = new JDialog(this, "Create New Course", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField codeField = new JTextField();
        JTextField titleField = new JTextField();
        JTextField instructorField = new JTextField();

        formPanel.add(new JLabel("Course Code*:"));
        formPanel.add(codeField);
        formPanel.add(new JLabel("Course Title*:"));
        formPanel.add(titleField);
        formPanel.add(new JLabel("Instructor*:"));
        formPanel.add(instructorField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = createRoundedButton("Create Course", new Color(40, 167, 69), Color.WHITE);
        JButton cancelButton = createRoundedButton("Cancel", new Color(108, 117, 125), Color.WHITE);

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
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void editSelectedStudent() {
        int selectedRow = studentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to edit", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String studentNumber = studentsModel.getValueAt(selectedRow, 0).toString();
        String name = studentsModel.getValueAt(selectedRow, 1).toString();
        String surname = studentsModel.getValueAt(selectedRow, 2).toString();
        String email = studentsModel.getValueAt(selectedRow, 3).toString();

        JDialog dialog = new JDialog(this, "Edit Student", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField nameField = new JTextField(name);
        JTextField surnameField = new JTextField(surname);
        JTextField emailField = new JTextField(email);

        formPanel.add(new JLabel("Student Number:"));
        formPanel.add(new JLabel(studentNumber));
        formPanel.add(new JLabel("Name*:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Surname*:"));
        formPanel.add(surnameField);
        formPanel.add(new JLabel("Email*:"));
        formPanel.add(emailField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = createRoundedButton("Update Student", new Color(255, 193, 7), Color.BLACK);
        JButton cancelButton = createRoundedButton("Cancel", new Color(108, 117, 125), Color.WHITE);

        saveButton.addActionListener(e -> {
            String newName = nameField.getText().trim();
            String newSurname = surnameField.getText().trim();
            String newEmail = emailField.getText().trim();

            if (newName.isEmpty() || newSurname.isEmpty() || newEmail.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Student updatedStudent = new Student(studentNumber, newName, newSurname, newEmail, "");
                Request request = new Request(RequestType.UPDATE_STUDENT, updatedStudent);
                Response response = client.sendRequest(request);

                if (response.isSuccess()) {
                    JOptionPane.showMessageDialog(dialog, "Student updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadStudents();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update student: " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void editSelectedCourse() {
        int selectedRow = coursesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to edit", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String courseCode = coursesModel.getValueAt(selectedRow, 0).toString();
        String title = coursesModel.getValueAt(selectedRow, 1).toString();
        String instructor = coursesModel.getValueAt(selectedRow, 2).toString();

        JDialog dialog = new JDialog(this, "Edit Course", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField titleField = new JTextField(title);
        JTextField instructorField = new JTextField(instructor);

        formPanel.add(new JLabel("Course Code:"));
        formPanel.add(new JLabel(courseCode));
        formPanel.add(new JLabel("Title*:"));
        formPanel.add(titleField);
        formPanel.add(new JLabel("Instructor*:"));
        formPanel.add(instructorField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = createRoundedButton("Update Course", new Color(255, 193, 7), Color.BLACK);
        JButton cancelButton = createRoundedButton("Cancel", new Color(108, 117, 125), Color.WHITE);

        saveButton.addActionListener(e -> {
            String newTitle = titleField.getText().trim();
            String newInstructor = instructorField.getText().trim();

            if (newTitle.isEmpty() || newInstructor.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Course updatedCourse = new Course(courseCode, newTitle, newInstructor);
                Request request = new Request(RequestType.UPDATE_COURSE, updatedCourse);
                Response response = client.sendRequest(request);

                if (response.isSuccess()) {
                    JOptionPane.showMessageDialog(dialog, "Course updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadCourses();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update course: " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteSelectedStudent() {
        int selectedRow = studentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String studentNumber = studentsModel.getValueAt(selectedRow, 0).toString();
        String studentName = studentsModel.getValueAt(selectedRow, 1).toString() + " " + studentsModel.getValueAt(selectedRow, 2).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete student:\n" + studentNumber + " - " + studentName + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Request request = new Request(RequestType.DELETE_STUDENT, studentNumber);
                Response response = client.sendRequest(request);

                if (response.isSuccess()) {
                    JOptionPane.showMessageDialog(this, "Student deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadStudents();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete student: " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedCourse() {
        int selectedRow = coursesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String courseCode = coursesModel.getValueAt(selectedRow, 0).toString();
        String courseTitle = coursesModel.getValueAt(selectedRow, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete course:\n" + courseCode + " - " + courseTitle + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Request request = new Request(RequestType.DELETE_COURSE, courseCode);
                Response response = client.sendRequest(request);

                if (response.isSuccess()) {
                    JOptionPane.showMessageDialog(this, "Course deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadCourses();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete course: " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadStudents() {
        try {
            Request request = new Request(RequestType.GET_ALL_STUDENTS);
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
            Request request = new Request(RequestType.GET_ALL_COURSES);
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
        }
    }

    private void loadStudentsIntoComboBox(JComboBox<String> comboBox) {
        try {
            Request request = new Request(RequestType.GET_ALL_STUDENTS);
            Response response = client.sendRequest(request);

            if (response.isSuccess()) {
                List<Student> students = (List<Student>) response.getData();
                comboBox.removeAllItems();

                if (students.isEmpty()) {
                    comboBox.addItem("No students registered - Add students first");
                } else {
                    for (Student student : students) {
                        comboBox.addItem(student.getStudentNumber() + " - " + student.getName() + " " + student.getSurname());
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        }
    }

    private void loadStudentEnrollments(String studentNumber, DefaultTableModel model, JLabel statusLabel) {
        try {
            Request request = new Request(RequestType.GET_STUDENT_ENROLLMENTS, studentNumber);
            Response response = client.sendRequest(request);

            if (response.isSuccess()) {
                List<Course> courses = (List<Course>) response.getData();
                model.setRowCount(0);

                if (courses.isEmpty()) {
                    statusLabel.setText("No enrollments found for student " + studentNumber);
                } else {
                    for (Course course : courses) {
                        model.addRow(new Object[]{
                                course.getCourseCode(),
                                course.getTitle(),
                                course.getInstructor()
                        });
                    }
                    statusLabel.setText(courses.size() + " courses enrolled for student " + studentNumber);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Error loading student enrollments: " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logout() {
        try {
            Request request = new Request(RequestType.LOGOUT);
            client.sendRequest(request);
        } catch (Exception e) {
        } finally {
            client.disconnect();
            new LoginGUI();
            dispose();
        }
    }
}