/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.ac.cput.studentenrollment.Gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import za.ac.cput.studentenrollment.connection.ClientCommunicator;
import za.ac.cput.studentenrollment.connection.RequestType;
import za.ac.cput.studentenrollment.connection.Response;
import za.ac.cput.studentenrollment.modelclasses.Student;
/**
 *
 * @author elzas
 */
public class LoginGUI extends JFrame {
    private JTextField studentNumberField;
    private JPasswordField passwordField;
    private JTextField emailField;
    private JCheckBox adminCheckBox;
    private JButton loginButton;
    private JButton clearButton;
    private ClientCommunicator client;

    public LoginGUI() {
        client = new ClientCommunicator();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Student Enrollment System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Student Enrollment System", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        
        formPanel.add(new JLabel("Student Number:"));
        studentNumberField = new JTextField();
        formPanel.add(studentNumberField);

        formPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        formPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        formPanel.add(emailField);

        formPanel.add(new JLabel("Admin Login:"));
        adminCheckBox = new JCheckBox();
        formPanel.add(adminCheckBox);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        loginButton = new JButton("Login");
        clearButton = new JButton("Clear");

        buttonPanel.add(loginButton);
        buttonPanel.add(clearButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners
        setupEventListeners();

        add(mainPanel);
        setVisible(true);
    }

    private void setupEventListeners() {
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });

        adminCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (adminCheckBox.isSelected()) {
                    emailField.setEnabled(false);
                    emailField.setText("");
                } else {
                    emailField.setEnabled(true);
                }
            }
        });
    }

    private void login() {
        String studentNumber = studentNumberField.getText().trim();
        String password = new String(passwordField.getPassword());
        String email = emailField.getText().trim();
        boolean isAdmin = adminCheckBox.isSelected();

        if (studentNumber.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Student number and password are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isAdmin && email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email is required for student login!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Connect to server
        if (!client.connect()) {
            JOptionPane.showMessageDialog(this, "Cannot connect to server!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Create authentication request using fully qualified name
            Object[] authData = new Object[]{studentNumber, password, email, isAdmin};
            za.ac.cput.studentenrollment.connection.Request request = 
                new za.ac.cput.studentenrollment.connection.Request(RequestType.AUTHENTICATE, authData);
            Response response = client.sendRequest(request);

            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose(); // Close login window
                
                if (isAdmin) {
                    new AdminDashboard(client);
                } else {
                    Student student = (Student) response.getData();
                    new StudentGUI(client, student);
                }
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Login error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        studentNumberField.setText("");
        passwordField.setText("");
        emailField.setText("");
        adminCheckBox.setSelected(false);
        emailField.setEnabled(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginGUI();
            }
        });
    }
}