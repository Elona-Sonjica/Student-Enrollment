/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.ac.cput.studentenrollment.Gui;

import com.sun.net.httpserver.Request;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
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
    
    private JLabel lblWelcome;
    private JTable coursesTable;
    private DefaultTableModel coursesModel;
    private JButton btnEnroll, btnViewMyCourses, btnRefresh, btnLogout;
    private JComboBox<String> courseComboBox;

    public StudentPanel(JPanel cardPanel, CardLayout cardLayout, ClientCommunicator communicator, Student student) {
        this.cardPanel = cardPanel;
        this.cardLayout = cardLayout;
        this.communicator = communicator;
        this.currentStudent = student;
        initializeComponents();
        setupGUI();
        loadAvailableCourses();
    }

    private void initializeComponents() {
        lblWelcome = new JLabel("Welcome, " + currentStudent.getName() + " " + currentStudent.getSurname());
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 18));

        // Table setup
        String[] columns = {"Course Code", "Course Name", "Instructor"};
        coursesModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        coursesTable = new JTable(coursesModel);
        coursesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // ComboBox for course selection
        courseComboBox = new JComboBox<>();
        courseComboBox.setPreferredSize(new Dimension(250, 25));

        // Buttons
        btnEnroll = new JButton("Enroll in Selected Course");
        btnViewMyCourses = new JButton("View My Enrollments");
        btnRefresh = new JButton("Refresh Courses");
        btnLogout = new JButton("Logout");

        // Add action listeners
        btnEnroll.addActionListener(e -> enrollInCourse());
        btnViewMyCourses.addActionListener(e -> viewMyEnrollments());
        btnRefresh.addActionListener(e -> loadAvailableCourses());
        btnLogout.addActionListener(e -> logout());
    }

    private void setupGUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // North Panel - Welcome and controls
        JPanel northPanel = new JPanel(new BorderLayout());
        
        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        welcomePanel.add(lblWelcome);
        
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlsPanel.add(btnLogout);
        
        northPanel.add(welcomePanel, BorderLayout.WEST);
        northPanel.add(controlsPanel, BorderLayout.EAST);

        // Center Panel - Course management
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Course Enrollment"));

        // Course selection panel
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectionPanel.add(new JLabel("Select Course:"));
        selectionPanel.add(courseComboBox);
        selectionPanel.add(btnEnroll);
        selectionPanel.add(btnViewMyCourses);
        selectionPanel.add(btnRefresh);

        // Table panel
        JScrollPane tableScrollPane = new JScrollPane(coursesTable);
        tableScrollPane.setPreferredSize(new Dimension(600, 300));

        centerPanel.add(selectionPanel, BorderLayout.NORTH);
        centerPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Add all panels
        add(northPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void loadAvailableCourses() {
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
                        showMessage("Courses loaded successfully", Color.BLUE);
                    } else {
                        showMessage("Failed to load courses", Color.RED);
                    }
                } catch (Exception e) {
                    showMessage("Error loading courses: " + e.getMessage(), Color.RED);
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void updateCourseComboBox(List<Course> courses) {
        courseComboBox.removeAllItems();
        for (Course course : courses) {
            courseComboBox.addItem(course.getCourseCode() + " - " + course.getTitle());
        }
    }

    private void enrollInCourse() {
        if (courseComboBox.getSelectedIndex() == -1) {
            showMessage("Please select a course first", Color.RED);
            return;
        }

        String selectedItem = (String) courseComboBox.getSelectedItem();
        String courseCode = selectedItem.split(" - ")[0];

        SwingWorker<Response, Void> worker = new SwingWorker<Response, Void>() {
            @Override
            protected Response doInBackground() throws Exception {
                Object[] enrollmentData = {currentStudent.getStudentNumber(), courseCode};
                Request request = new Request(RequestType.ENROLL_STUDENT, enrollmentData);
                return communicator.sendRequest(request);
            }

            @Override
            protected void done() {
                try {
                    Response response = get();
                    if (response.isSuccess()) {
                        showMessage("Successfully enrolled in " + courseCode, Color.GREEN);
                    } else {
                        showMessage("Enrollment failed: " + response.getMessage(), Color.RED);
                    }
                } catch (Exception e) {
                    showMessage("Error during enrollment: " + e.getMessage(), Color.RED);
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void viewMyEnrollments() {
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
                                course.getInstructor()
                            });
                        }
                        showMessage("Loaded " + enrollments.size() + " enrolled courses", Color.BLUE);
                    } else {
                        showMessage("You are not enrolled in any courses", Color.BLUE);
                    }
                } catch (Exception e) {
                    showMessage("Error loading enrollments: " + e.getMessage(), Color.RED);
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void logout() {
        communicator.disconnect();
        cardLayout.show(cardPanel, "login");
        LoginPanel loginPanel = (LoginPanel) cardPanel.getComponent(0);
        loginPanel.clearForm();
    }

    private void showMessage(String message, Color color) {
        JOptionPane.showMessageDialog(this, message, "Student Dashboard", 
                                    JOptionPane.INFORMATION_MESSAGE);
    }
}