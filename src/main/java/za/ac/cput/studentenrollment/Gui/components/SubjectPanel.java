/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.ac.cput.studentenrollment.Gui.components;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import za.ac.cput.studentenrollment.connection.ClientCommunicator;
import za.ac.cput.studentenrollment.modelclasses.Student;

public class SubjectPanel extends JPanel {
    private JPanel pnlNorth, pnlCenter, pnlSouth, pnlcbx;
    private JLabel lblTitle, lblAddSubject;
    private JButton btnAddSubject, btnSave, btnDelete, btnDropDown;
    private JTable displayTable;
    private DefaultTableModel model;
    private JCheckBox[] cbxAddSubject;
    private ClientCommunicator client;
    private Student student;
    
    int i;
    String[] subjects = {"Application Development Fundamentals",
            "Application Development Practice", "ICT Electives", "Project",
            "Information Management"};
    String[] subject_codes = {"ADF262S", "ADP262S", "ICT262S", "PROJ262S", "INM262S"};
    String[] instructors = {"Mr Burger", "Mr Naidoo", "Mr Olivier", "Ms Tswane", "Mr Ayodeji"};

    public SubjectPanel(ClientCommunicator client, Student student) {
        this.client = client;
        this.student = student;
        initializeComponents();
        setupGUI();
    }

    private void initializeComponents() {
        pnlNorth = new JPanel();
        pnlCenter = new JPanel();
        pnlSouth = new JPanel();
        pnlcbx = new JPanel();

        lblTitle = new JLabel("Course Enrollment Dashboard");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));

        lblAddSubject = new JLabel("Click on the drop down to add a subject");

        String[] column = {"Subject", "Subject_Code", "Instructor"};
        model = new DefaultTableModel(column, 0);

        displayTable = new JTable(model);

        btnAddSubject = new JButton("Add Subject");
        btnSave = new JButton("Save Enrollments");
        btnDelete = new JButton("Remove Selected");
        btnDropDown = new JButton("Select Subject");
        btnDropDown.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add action listeners
        setupEventListeners();
    }

    private void setupEventListeners() {
        btnAddSubject.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.setRowCount(0);
                for (int i = 0; i < cbxAddSubject.length; i++) {
                    if (cbxAddSubject[i].isSelected()) {
                        model.addRow(new Object[]{
                                subjects[i],
                                subject_codes[i],
                                instructors[i]
                        });
                    }
                }
                if (model.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(null, "Please select at least one subject.");
                }
            }
        });

        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveEnrollments();
            }
        });

        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedEnrollment();
            }
        });

        btnDropDown.addActionListener(e -> pnlcbx.setVisible(!pnlcbx.isVisible()));
    }

    private void saveEnrollments() {
        int rowCount = model.getRowCount();
        if (rowCount == 0) {
            JOptionPane.showMessageDialog(this, "No courses selected to enroll in.", "No Courses", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int successCount = 0;
        for (int i = 0; i < rowCount; i++) {
            String courseCode = (String) model.getValueAt(i, 1);
            try {
                // Use the existing enrollment logic from StudentGUI
                Object[] enrollmentData = new Object[]{student.getStudentNumber(), courseCode};
                za.ac.cput.studentenrollment.connection.Request request = 
                    new za.ac.cput.studentenrollment.connection.Request(
                        za.ac.cput.studentenrollment.connection.RequestType.ENROLL_STUDENT, 
                        enrollmentData
                    );
                za.ac.cput.studentenrollment.connection.Response response = client.sendRequest(request);
                
                if (response.isSuccess()) {
                    successCount++;
                }
            } catch (Exception ex) {
                System.err.println("Error enrolling in course: " + courseCode + " - " + ex.getMessage());
            }
        }

        if (successCount > 0) {
            JOptionPane.showMessageDialog(this, 
                "Successfully enrolled in " + successCount + " course(s)!", 
                "Enrollment Successful", 
                JOptionPane.INFORMATION_MESSAGE
            );
            model.setRowCount(0); // Clear the table after successful enrollment
        } else {
            JOptionPane.showMessageDialog(this, 
                "Failed to enroll in any courses. You may already be enrolled in these courses.", 
                "Enrollment Failed", 
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void deleteSelectedEnrollment() {
        int selectedRow = displayTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to remove.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String courseCode = (String) model.getValueAt(selectedRow, 1);
        String courseName = (String) model.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Remove " + courseName + " from your selection?", 
            "Confirm Removal", 
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            model.removeRow(selectedRow);
        }
    }

    public void setupGUI() {
        pnlNorth.setLayout(new FlowLayout());
        pnlSouth.setLayout(new FlowLayout());
        pnlCenter.setLayout(new BoxLayout(pnlCenter, BoxLayout.Y_AXIS));

        lblAddSubject.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlNorth.add(lblTitle);
        pnlCenter.add(Box.createVerticalStrut(10));
        pnlCenter.add(lblAddSubject);

        pnlCenter.add(btnDropDown);
        pnlCenter.add(Box.createVerticalStrut(10));

        pnlcbx.setLayout(new BoxLayout(pnlcbx, BoxLayout.Y_AXIS));
        pnlCenter.add(pnlcbx);
        pnlCenter.add(Box.createVerticalStrut(10));

        cbxAddSubject = new JCheckBox[subjects.length];
        for (i = 0; i < subjects.length; i++) {
            cbxAddSubject[i] = new JCheckBox(subjects[i] + " (" + subject_codes[i] + ") - " + instructors[i]);
            cbxAddSubject[i].setAlignmentX(Component.CENTER_ALIGNMENT);
            pnlcbx.add(cbxAddSubject[i]);
        }
        pnlcbx.setVisible(false);
        
        pnlCenter.add(btnAddSubject);
        btnAddSubject.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlCenter.add(Box.createVerticalStrut(10));

        JScrollPane scrollPane = new JScrollPane(displayTable);
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        scrollPane.setPreferredSize(new Dimension(600, 200));
        pnlCenter.add(scrollPane);

        pnlSouth.add(btnSave);
        pnlSouth.add(btnDelete);

        this.setLayout(new BorderLayout());
        this.add(pnlNorth, BorderLayout.NORTH);
        this.add(pnlCenter, BorderLayout.CENTER);
        this.add(pnlSouth, BorderLayout.SOUTH);
        
        this.setPreferredSize(new Dimension(800, 600));
    }

    // Method to load existing enrollments
    public void loadExistingEnrollments() {
        try {
            za.ac.cput.studentenrollment.connection.Request request = 
                new za.ac.cput.studentenrollment.connection.Request(
                    za.ac.cput.studentenrollment.connection.RequestType.GET_STUDENT_ENROLLMENTS, 
                    student.getStudentNumber()
                );
            za.ac.cput.studentenrollment.connection.Response response = client.sendRequest(request);
            
            if (response.isSuccess()) {
                java.util.List<za.ac.cput.studentenrollment.modelclasses.Course> enrollments = 
                    (java.util.List<za.ac.cput.studentenrollment.modelclasses.Course>) response.getData();
                
                model.setRowCount(0);
                for (za.ac.cput.studentenrollment.modelclasses.Course course : enrollments) {
                    model.addRow(new Object[]{
                        course.getTitle(),
                        course.getCourseCode(),
                        course.getInstructor()
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading existing enrollments: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
