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

/**
 *
 * @author elzas
 */

public class StudentGUI extends JFrame {
    private ClientCommunicator client;
    private Student student;
    private JTable coursesTable;
    private JTable enrollmentsTable;
    private DefaultTableModel coursesModel;
    private DefaultTableModel enrollmentsModel;

    public StudentGUI(ClientCommunicator client, Student student) {
        this.client = client;
        this.student = student;
        initializeUI();
        loadCourses();
        loadEnrollments();
    }

    private void initializeUI() {
        setTitle("Student Portal - " + student.getStudentNumber());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
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

        // Available Courses Tab
        tabbedPane.addTab("Available Courses", createCoursesPanel());

        // My Enrollments Tab
        tabbedPane.addTab("My Enrollments", createEnrollmentsPanel());

        add(tabbedPane);
        setVisible(true);
    }

    private JPanel createCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Table for available courses
        String[] columns = {"Course Code", "Title", "Instructor", "Action"};
        coursesModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Only action column is editable
            }
        };
        coursesTable = new JTable(coursesModel);
        JScrollPane scrollPane = new JScrollPane(coursesTable);

        // Add enroll button to each row
        coursesTable.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());
        coursesTable.getColumnModel().getColumn(3).setCellEditor(new ButtonEditor(new JCheckBox()));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createEnrollmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Table for enrolled courses
        String[] columns = {"Course Code", "Title", "Instructor"};
        enrollmentsModel = new DefaultTableModel(columns, 0);
        enrollmentsTable = new JTable(enrollmentsModel);
        JScrollPane scrollPane = new JScrollPane(enrollmentsTable);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
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
                
                // Load current enrollments to check which courses are already enrolled
                za.ac.cput.studentenrollment.connection.Request enrollRequest = 
                    new za.ac.cput.studentenrollment.connection.Request(RequestType.GET_STUDENT_ENROLLMENTS, student.getStudentNumber());
                Response enrollResponse = client.sendRequest(enrollRequest);
                List<Course> currentEnrollments = (List<Course>) enrollResponse.getData();
                
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
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Error loading courses: " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadEnrollments() {
        try {
            // Use fully qualified name for Request
            za.ac.cput.studentenrollment.connection.Request request = 
                new za.ac.cput.studentenrollment.connection.Request(RequestType.GET_STUDENT_ENROLLMENTS, student.getStudentNumber());
            Response response = client.sendRequest(request);
            
            if (response.isSuccess()) {
                List<Course> enrollments = (List<Course>) response.getData();
                enrollmentsModel.setRowCount(0);
                
                for (Course course : enrollments) {
                    enrollmentsModel.addRow(new Object[]{
                        course.getCourseCode(),
                        course.getTitle(),
                        course.getInstructor()
                    });
                }
            } else {
                JOptionPane.showMessageDialog(this, "Error loading enrollments: " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void enrollInCourse(String courseCode) {
        try {
            Object[] enrollmentData = new Object[]{student.getStudentNumber(), courseCode};
            // Use fully qualified name for Request
            za.ac.cput.studentenrollment.connection.Request request = 
                new za.ac.cput.studentenrollment.connection.Request(RequestType.ENROLL_STUDENT, enrollmentData);
            Response response = client.sendRequest(request);
            
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Successfully enrolled in " + courseCode, "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshTables();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to enroll: " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTables() {
        loadCourses();
        loadEnrollments();
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

    // Button renderer and editor for enroll button
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
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

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                    enrollInCourse(currentCourseCode);
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentCourseCode = table.getValueAt(row, 0).toString();
            button.setText(value.toString());
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "Enroll";
        }
    }
}