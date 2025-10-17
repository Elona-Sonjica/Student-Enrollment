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
import za.ac.cput.studentenrollment.connection.Request;
import za.ac.cput.studentenrollment.connection.RequestType;
import za.ac.cput.studentenrollment.connection.Response;
import za.ac.cput.studentenrollment.modelclasses.Student;
import za.ac.cput.studentenrollment.util.InputValidator;
import za.ac.cput.studentenrollment.util.RoundedImageUtil;

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
    private JLabel logoLabel;

    public LoginGUI() {
        client = new ClientCommunicator();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Student Enrollment System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 550); // Increased height to accommodate register button
        setLocationRelativeTo(null);
        setResizable(false);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Logo panel
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoLabel = new JLabel();

        // Load and set the rounded logo (150px size)
        ImageIcon roundedLogo = RoundedImageUtil.createRoundedImageIcon("/images/my-logo-1.jpg", 150);
        if (roundedLogo != null) {
            logoLabel.setIcon(roundedLogo);
        } else {
            // Fallback text if image not found
            logoLabel.setText("CPUT Logo");
            logoLabel.setFont(new Font("Arial", Font.BOLD, 16));
            logoLabel.setForeground(Color.BLUE);
        }

        logoPanel.add(logoLabel);
        mainPanel.add(logoPanel, BorderLayout.NORTH);

        // Title
        JLabel titleLabel = new JLabel("Student Enrollment System", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        mainPanel.add(titleLabel, BorderLayout.CENTER);

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

        // Button panel - UPDATED WITH REGISTER BUTTON
        JPanel buttonPanel = new JPanel(new FlowLayout());
        loginButton = new JButton("Login");
        clearButton = new JButton("Clear");
        JButton registerButton = new JButton("Register New Student"); // NEW: Registration button

        buttonPanel.add(loginButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(registerButton); // NEW: Add register button

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners
        setupEventListeners();

        // NEW: Add action listener for register button
        registerButton.addActionListener(e -> showRegistrationDialog());

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

        // Input validation
        if (studentNumber.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Student number and password are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isAdmin) {
            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Email is required for student login!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!InputValidator.isValidEmail(email)) {
                JOptionPane.showMessageDialog(this, "Please enter a valid email address!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (!isAdmin && !InputValidator.isValidStudentNumber(studentNumber)) {
            JOptionPane.showMessageDialog(this, "Student number must be 5-10 digits!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Connect to server
        if (!client.connect()) {
            JOptionPane.showMessageDialog(this, "Cannot connect to server! Please make sure the server is running.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Create authentication request
            Object[] authData = new Object[]{studentNumber, password, email, isAdmin};
            Request request = new Request(RequestType.AUTHENTICATE, authData);
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
            ex.printStackTrace();
        }
    }

    // UPDATED: Registration dialog method with scroll bar, logo, and larger size
    private void showRegistrationDialog() {
        JDialog dialog = new JDialog(this, "Student Registration", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(500, 450); // Increased size for better layout
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(new Color(240, 240, 240));

        // ADD LOGO PANEL AT TOP
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoPanel.setBackground(new Color(240, 240, 240));
        JLabel regLogoLabel = new JLabel();
        ImageIcon smallLogo = RoundedImageUtil.createRoundedImageIcon("/images/my-logo-1.jpg", 80);
        if (smallLogo != null) {
            regLogoLabel.setIcon(smallLogo);
        } else {
            regLogoLabel.setText("Student Registration");
            regLogoLabel.setFont(new Font("Arial", Font.BOLD, 14));
            regLogoLabel.setForeground(new Color(0, 51, 102));
        }
        logoPanel.add(regLogoLabel);
        dialog.add(logoPanel, BorderLayout.NORTH);

        // CREATE SCROLLABLE FORM PANEL
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        formPanel.setBackground(new Color(240, 240, 240));

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

        formPanel.add(numberLabel);
        formPanel.add(numberField);
        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(surnameLabel);
        formPanel.add(surnameField);
        formPanel.add(emailLabel);
        formPanel.add(emailField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);

        // Add some instructional text
        formPanel.add(new JLabel(""));
        JLabel instructionLabel = new JLabel("All fields are required");
        instructionLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        instructionLabel.setForeground(Color.GRAY);
        formPanel.add(instructionLabel);

        // CREATE SCROLL PANE AND ADD FORM PANEL
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Registration Form"));
        scrollPane.getViewport().setBackground(new Color(240, 240, 240));
        dialog.add(scrollPane, BorderLayout.CENTER);

        // BUTTON PANEL AT BOTTOM
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(240, 240, 240));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton registerButton = new JButton("Register");
        JButton cancelButton = new JButton("Cancel");

        // Style buttons
        registerButton.setBackground(new Color(0, 102, 204));
        registerButton.setForeground(Color.WHITE);
        registerButton.setPreferredSize(new Dimension(100, 30));
        cancelButton.setBackground(new Color(102, 102, 102));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setPreferredSize(new Dimension(100, 30));

        buttonPanel.add(registerButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(cancelButton);

        dialog.add(buttonPanel, BorderLayout.SOUTH);

        registerButton.addActionListener(e -> {
            String number = numberField.getText().trim();
            String name = nameField.getText().trim();
            String surname = surnameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());

            // Validation
            if (number.isEmpty() || name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!InputValidator.isValidStudentNumber(number)) {
                JOptionPane.showMessageDialog(dialog, "Student number must be 5-10 digits!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!InputValidator.isValidEmail(email)) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid email address!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!InputValidator.isValidPassword(password)) {
                JOptionPane.showMessageDialog(dialog, "Password must be at least 6 characters!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Connect to server and register
            ClientCommunicator regClient = new ClientCommunicator();
            if (!regClient.connect()) {
                JOptionPane.showMessageDialog(dialog, "Cannot connect to server! Please try again later.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Student student = new Student(number, name, surname, email, password);
                Request request = new Request(RequestType.ADD_STUDENT, student);
                Response response = regClient.sendRequest(request);

                if (response.isSuccess()) {
                    JOptionPane.showMessageDialog(dialog, "Registration successful! You can now login.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    // Pre-fill login fields
                    studentNumberField.setText(number);
                    this.emailField.setText(email);
                } else {
                    JOptionPane.showMessageDialog(dialog, "Registration failed: " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error during registration: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } finally {
                regClient.disconnect();
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
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