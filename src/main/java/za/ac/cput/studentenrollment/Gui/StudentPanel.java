package za.ac.cput.studentenrollment.Gui;

import com.sun.net.httpserver.Request;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import za.ac.cput.studentenrollment.connection.ClientCommunicator;
import za.ac.cput.studentenrollment.connection.RequestType;
import za.ac.cput.studentenrollment.connection.Response;
import za.ac.cput.studentenrollment.modelclasses.Course;
import za.ac.cput.studentenrollment.modelclasses.Student;

/**
 *
 * @author elzas
 */
public class StudentPanel extends JPanel {
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private ClientCommunicator communicator;
    private Student currentStudent;

    // Modern color scheme
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SECONDARY_COLOR = new Color(236, 240, 241);
    private final Color ACCENT_COLOR = new Color(52, 152, 219);
    private final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private final Color WARNING_COLOR = new Color(230, 126, 34);

    // Components
    private JLabel lblWelcome, lblStatus;
    private JTable coursesTable;
    private DefaultTableModel coursesModel;
    private JButton btnEnroll, btnViewMyCourses, btnRefresh, btnLogout, btnCourseInfo;
    private JComboBox<Course> courseComboBox;
    private JProgressBar progressBar;
    private JTabbedPane tabbedPane;

    public StudentPanel(JPanel cardPanel, CardLayout cardLayout, ClientCommunicator communicator, Student student) {
        this.cardPanel = cardPanel;
        this.cardLayout = cardLayout;
        this.communicator = communicator;
        this.currentStudent = student;
        initializeComponents();
        setupModernGUI();
        loadAvailableCourses();
    }

    private void initializeComponents() {
        // Welcome label with better styling
        lblWelcome = new JLabel("Welcome, " + currentStudent.getName() + " " + currentStudent.getSurname());
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblWelcome.setForeground(PRIMARY_COLOR);

        // Status label for real-time feedback
        lblStatus = new JLabel("Ready");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(Color.DARK_GRAY);

        // Modern table setup
        String[] columns = {"Course Code", "Course Name", "Instructor", "Status"};
        coursesModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return String.class;
            }
        };

        coursesTable = new JTable(coursesModel);
        coursesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        coursesTable.setRowHeight(30);
        coursesTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        coursesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        coursesTable.setShowGrid(true);
        coursesTable.setGridColor(new Color(240, 240, 240));

        // Enhanced ComboBox with Course objects
        courseComboBox = new JComboBox<>();
        courseComboBox.setRenderer(new CourseListRenderer());
        courseComboBox.setPreferredSize(new Dimension(300, 35));

        // Modern buttons with icons
        btnEnroll = createModernButton("Enroll in Course", "🎯", PRIMARY_COLOR);
        btnViewMyCourses = createModernButton("My Enrollments", "📚", ACCENT_COLOR);
        btnRefresh = createModernButton("Refresh", "🔄", WARNING_COLOR);
        btnCourseInfo = createModernButton("Course Info", "ℹ️", new Color(155, 89, 182));
        btnLogout = createModernButton("Logout", "🚪", new Color(231, 76, 60));

        // Progress bar for loading states
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        progressBar.setIndeterminate(true);

        // Add action listeners
        btnEnroll.addActionListener(e -> enrollInCourse());
        btnViewMyCourses.addActionListener(e -> viewMyEnrollments());
        btnRefresh.addActionListener(e -> loadAvailableCourses());
        btnCourseInfo.addActionListener(e -> showCourseInfo());
        btnLogout.addActionListener(e -> logout());

        // Double-click listener for table
        coursesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    showSelectedCourseInfo();
                }
            }
        });
    }

    private JButton createModernButton(String text, String emoji, Color color) {
        JButton button = new JButton(emoji + " " + text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
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
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(SECONDARY_COLOR);

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main Content with Tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Available Courses Tab
        JPanel availableCoursesPanel = createAvailableCoursesPanel();
        tabbedPane.addTab("📖 Available Courses", availableCoursesPanel);

        // My Enrollments Tab
        JPanel myEnrollmentsPanel = createMyEnrollmentsPanel();
        tabbedPane.addTab("🎓 My Enrollments", myEnrollmentsPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Status Bar
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        statusPanel.add(lblStatus, BorderLayout.WEST);
        statusPanel.add(progressBar, BorderLayout.EAST);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        // Welcome section
        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        welcomePanel.setBackground(Color.WHITE);
        welcomePanel.add(lblWelcome);

        // Student info badge
        JLabel studentBadge = new JLabel("🎓 " + currentStudent.getStudentNumber());
        studentBadge.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        studentBadge.setForeground(Color.DARK_GRAY);
        welcomePanel.add(studentBadge);

        // Controls section
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlsPanel.setBackground(Color.WHITE);
        controlsPanel.add(btnRefresh);
        controlsPanel.add(btnLogout);

        headerPanel.add(welcomePanel, BorderLayout.WEST);
        headerPanel.add(controlsPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createAvailableCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);

        // Course selection panel
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        selectionPanel.setBackground(Color.WHITE);
        selectionPanel.setBorder(BorderFactory.createTitledBorder("Course Enrollment"));

        selectionPanel.add(new JLabel("Select Course:"));
        selectionPanel.add(courseComboBox);
        selectionPanel.add(btnEnroll);
        selectionPanel.add(btnCourseInfo);

        // Instructions
        JPanel instructionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        instructionPanel.setBackground(Color.WHITE);
        JLabel instruction = new JLabel("💡 Double-click on any course in the table to view details");
        instruction.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        instruction.setForeground(Color.GRAY);
        instructionPanel.add(instruction);

        // Table in scroll pane
        JScrollPane tableScrollPane = new JScrollPane(coursesTable);
        tableScrollPane.setPreferredSize(new Dimension(700, 350));
        tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        panel.add(selectionPanel, BorderLayout.NORTH);
        panel.add(instructionPanel, BorderLayout.CENTER);
        panel.add(tableScrollPane, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createMyEnrollmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(btnViewMyCourses);

        // Create a new table for enrollments to keep them separate
        JTable enrollmentsTable = new JTable(coursesModel); // Reuse model for simplicity
        enrollmentsTable.setRowHeight(30);
        JScrollPane enrollmentsScroll = new JScrollPane(enrollmentsTable);
        enrollmentsScroll.setPreferredSize(new Dimension(700, 350));

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(enrollmentsScroll, BorderLayout.CENTER);

        return panel;
    }

    private void loadAvailableCourses() {
        setLoadingState(true, "Loading available courses...");

        SwingWorker<List<Course>, Void> worker = new SwingWorker<List<Course>, Void>() {
            @Override
            protected List<Course> doInBackground() throws Exception {
                Request request = new Request(RequestType.GET_ALL_COURSES);
                Response response = communicator.sendRequest(request);
                if (response.isSuccess()) {
                    return (List<Course>) response.getData();
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    List<Course> courses = get();
                    if (courses != null) {
                        updateCourseComboBox(courses);
                        updateCoursesTable(courses);
                        setStatus("Loaded " + courses.size() + " available courses", SUCCESS_COLOR);
                    } else {
                        setStatus("Failed to load courses", Color.RED);
                    }
                } catch (Exception e) {
                    setStatus("Error: " + e.getMessage(), Color.RED);
                } finally {
                    setLoadingState(false, "Ready");
                }
            }
        };
        worker.execute();
    }

    private void updateCourseComboBox(List<Course> courses) {
        courseComboBox.removeAllItems();
        for (Course course : courses) {
            courseComboBox.addItem(course);
        }
        if (!courses.isEmpty()) {
            courseComboBox.setSelectedIndex(0);
        }
    }

    private void updateCoursesTable(List<Course> courses) {
        coursesModel.setRowCount(0);
        for (Course course : courses) {
            // Check if student is already enrolled
            boolean isEnrolled = checkIfEnrolled(course.getCourseCode());
            String status = isEnrolled ? "✅ Enrolled" : "⏳ Available";

            coursesModel.addRow(new Object[]{
                    course.getCourseCode(),
                    course.getTitle(),
                    course.getInstructor(),
                    status
            });
        }
    }

    private boolean checkIfEnrolled(String courseCode) {
        // This would ideally check against the student's current enrollments
        // For now, return false - you can implement this with your EnrollmentDAO
        return false;
    }

    private void enrollInCourse() {
        Course selectedCourse = (Course) courseComboBox.getSelectedItem();
        if (selectedCourse == null) {
            showMessage("Please select a course first", Color.RED);
            return;
        }

        setLoadingState(true, "Enrolling in " + selectedCourse.getCourseCode() + "...");

        SwingWorker<Response, Void> worker = new SwingWorker<Response, Void>() {
            @Override
            protected Response doInBackground() throws Exception {
                Object[] enrollmentData = {currentStudent.getStudentNumber(), selectedCourse.getCourseCode()};
                Request request = new Request(RequestType.ENROLL_STUDENT, enrollmentData);
                return communicator.sendRequest(request);
            }

            @Override
            protected void done() {
                try {
                    Response response = get();
                    if (response.isSuccess()) {
                        setStatus("Successfully enrolled in " + selectedCourse.getCourseCode(), SUCCESS_COLOR);
                        showMessage("🎉 Enrollment Successful!",
                                "You have been successfully enrolled in " + selectedCourse.getTitle(),
                                JOptionPane.INFORMATION_MESSAGE);
                        loadAvailableCourses(); // Refresh the list
                    } else {
                        setStatus("Enrollment failed: " + response.getMessage(), Color.RED);
                        showMessage("Enrollment Failed", response.getMessage(), JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    setStatus("Error during enrollment: " + e.getMessage(), Color.RED);
                } finally {
                    setLoadingState(false, "Ready");
                }
            }
        };
        worker.execute();
    }

    private void viewMyEnrollments() {
        setLoadingState(true, "Loading your enrollments...");
        tabbedPane.setSelectedIndex(1); // Switch to enrollments tab

        SwingWorker<List<Course>, Void> worker = new SwingWorker<List<Course>, Void>() {
            @Override
            protected List<Course> doInBackground() throws Exception {
                Request request = new Request(RequestType.GET_STUDENT_ENROLLMENTS, currentStudent.getStudentNumber());
                Response response = communicator.sendRequest(request);
                if (response.isSuccess()) {
                    return (List<Course>) response.getData();
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    List<Course> enrollments = get();
                    coursesModel.setRowCount(0); // Clear table

                    if (enrollments != null && !enrollments.isEmpty()) {
                        for (Course course : enrollments) {
                            coursesModel.addRow(new Object[]{
                                    course.getCourseCode(),
                                    course.getTitle(),
                                    course.getInstructor(),
                                    "✅ Currently Enrolled"
                            });
                        }
                        setStatus("Loaded " + enrollments.size() + " enrolled courses", SUCCESS_COLOR);
                    } else {
                        setStatus("You are not enrolled in any courses", Color.BLUE);
                    }
                } catch (Exception e) {
                    setStatus("Error loading enrollments: " + e.getMessage(), Color.RED);
                } finally {
                    setLoadingState(false, "Ready");
                }
            }
        };
        worker.execute();
    }

    private void showCourseInfo() {
        Course selectedCourse = (Course) courseComboBox.getSelectedItem();
        if (selectedCourse != null) {
            showCourseDetails(selectedCourse);
        }
    }

    private void showSelectedCourseInfo() {
        int selectedRow = coursesTable.getSelectedRow();
        if (selectedRow >= 0) {
            String courseCode = (String) coursesTable.getValueAt(selectedRow, 0);
            String courseName = (String) coursesTable.getValueAt(selectedRow, 1);
            String instructor = (String) coursesTable.getValueAt(selectedRow, 2);

            JOptionPane.showMessageDialog(this,
                    "<html><body style='width: 300px;'>" +
                            "<h3>Course Information</h3>" +
                            "<b>Course Code:</b> " + courseCode + "<br>" +
                            "<b>Course Name:</b> " + courseName + "<br>" +
                            "<b>Instructor:</b> " + instructor + "<br><br>" +
                            "<i>Would you like to enroll in this course?</i>" +
                            "</body></html>",
                    "Course Details",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showCourseDetails(Course course) {
        JOptionPane.showMessageDialog(this,
                "<html><body style='width: 300px;'>" +
                        "<h3>📘 Course Details</h3>" +
                        "<b>Course Code:</b> " + course.getCourseCode() + "<br>" +
                        "<b>Course Name:</b> " + course.getTitle() + "<br>" +
                        "<b>Instructor:</b> " + course.getInstructor() + "<br><br>" +
                        "<small><i>Click 'Enroll' to join this course</i></small>" +
                        "</body></html>",
                "Course Information",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            communicator.disconnect();
            cardLayout.show(cardPanel, "login");
            // Find and clear the login panel
            for (Component comp : cardPanel.getComponents()) {
                if (comp instanceof LoginPanel) {
                    ((LoginPanel) comp).clearForm();
                    break;
                }
            }
        }
    }

    private void setLoadingState(boolean loading, String message) {
        progressBar.setVisible(loading);
        btnEnroll.setEnabled(!loading);
        btnViewMyCourses.setEnabled(!loading);
        btnRefresh.setEnabled(!loading);
        setStatus(message, loading ? Color.BLUE : Color.DARK_GRAY);
    }

    private void setStatus(String message, Color color) {
        lblStatus.setText(message);
        lblStatus.setForeground(color);
    }

    private void showMessage(String title, String message, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    private void showMessage(String message, Color color) {
        // Overloaded method for simple messages
        JOptionPane.showMessageDialog(this, message);
    }

    // Custom renderer for Course ComboBox
    private class CourseListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Course) {
                Course course = (Course) value;
                setText(course.getCourseCode() + " - " + course.getTitle());
            }
            return this;
        }
    }
}