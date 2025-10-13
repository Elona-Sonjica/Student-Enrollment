/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.ac.cput.studentenrollment.Gui;
import com.sun.net.httpserver.Request;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import za.ac.cput.studentenrollment.modelclasses.Student;
/**
 *
 * @author elzas
 */
public class LoginPanel extends JPanel {
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private ClientCommunicator communicator;
    
    // Login components
    private JTextField txtStudentNumber;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JRadioButton rdbtnStudent;
    private JRadioButton rdbtnAdmin;
    private JButton btnLogin;
    private JLabel lblStatus;

    public LoginPanel(JPanel cardPanel, CardLayout cardLayout, ClientCommunicator communicator) {
        this.cardPanel = cardPanel;
        this.cardLayout = cardLayout;
        this.communicator = communicator;
        initializeComponents();
        setupGUI();
    }

    private void initializeComponents() {
        // Create components
        txtStudentNumber = new JTextField(20);
        txtEmail = new JTextField(20);
        txtPassword = new JPasswordField(20);
        rdbtnStudent = new JRadioButton("Student", true);
        rdbtnAdmin = new JRadioButton("Admin");
        btnLogin = new JButton("Login");
        lblStatus = new JLabel("Please enter your credentials");
        lblStatus.setForeground(Color.BLUE);

        // Setup radio buttons group
        ButtonGroup roleGroup = new ButtonGroup();
        roleGroup.add(rdbtnStudent);
        roleGroup.add(rdbtnAdmin);

        // Add login button action
        btnLogin.addActionListener(new LoginAction());
        
        // Add Enter key listener to password field
        txtPassword.addActionListener(new LoginAction());
    }

    private void setupGUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // North Panel - Title
        JPanel northPanel = new JPanel();
        JLabel lblTitle = new JLabel("Student Enrollment System");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(new Color(0, 70, 140)); // CPUT blue
        northPanel.add(lblTitle);

        // Center Panel - Form
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Login"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Student Number
        gbc.gridx = 0; gbc.gridy = 0;
        centerPanel.add(new JLabel("Student Number:"), gbc);
        gbc.gridx = 1;
        centerPanel.add(txtStudentNumber, gbc);

        // Row 1: Email
        gbc.gridx = 0; gbc.gridy = 1;
        centerPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        centerPanel.add(txtEmail, gbc);

        // Row 2: Password
        gbc.gridx = 0; gbc.gridy = 2;
        centerPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        centerPanel.add(txtPassword, gbc);

        // Row 3: Role Selection
        gbc.gridx = 0; gbc.gridy = 3;
        centerPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rolePanel.add(rdbtnStudent);
        rolePanel.add(rdbtnAdmin);
        centerPanel.add(rolePanel, gbc);

        // Row 4: Status
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        centerPanel.add(lblStatus, gbc);

        // South Panel - Login Button
        JPanel southPanel = new JPanel();
        southPanel.add(btnLogin);

        // Add all panels
        add(northPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
    }

    private class LoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String studentNumber = txtStudentNumber.getText().trim();
            String email = txtEmail.getText().trim();
            String password = new String(txtPassword.getPassword());
            boolean isAdmin = rdbtnAdmin.isSelected();

            // Validation
            if (studentNumber.isEmpty() || email.isEmpty() || password.isEmpty()) {
                lblStatus.setText("Please fill in all fields");
                lblStatus.setForeground(Color.RED);
                return;
            }

            if (!rdbtnStudent.isSelected() && !rdbtnAdmin.isSelected()) {
                lblStatus.setText("Please select a role");
                lblStatus.setForeground(Color.RED);
                return;
            }

            // Attempt login
            lblStatus.setText("Connecting to server...");
            lblStatus.setForeground(Color.BLUE);
            btnLogin.setEnabled(false);

            // Perform login in background thread
            SwingWorker<Response, Void> worker = new SwingWorker<Response, Void>() {
                @Override
                protected Response doInBackground() throws Exception {
                    Object[] authData = {studentNumber, password, email, isAdmin};
                    Request request = new Request(RequestType.AUTHENTICATE, authData);
                    return communicator.sendRequest(request);
                }

                @Override
                protected void done() {
                    try {
                        Response response = get();
                        
                        if (response.isSuccess()) {
                            lblStatus.setText("Login successful! Loading dashboard...");
                            lblStatus.setForeground(Color.GREEN);
                            
                            // Show appropriate dashboard based on role
                            if (isAdmin) {
                                showAdminPanel();
                            } else {
                                Student student = (Student) response.getData();
                                showStudentPanel(student);
                            }
                            
                            // Clear password field for security
                            txtPassword.setText("");
                            
                        } else {
                            lblStatus.setText("Login failed: " + response.getMessage());
                            lblStatus.setForeground(Color.RED);
                        }
                    } catch (Exception ex) {
                        lblStatus.setText("Error during login: " + ex.getMessage());
                        lblStatus.setForeground(Color.RED);
                        ex.printStackTrace();
                    } finally {
                        btnLogin.setEnabled(true);
                    }
                }
            };
            
            worker.execute();
        }
    }

    private void showAdminPanel() {
        AdminPanel adminPanel = new AdminPanel(cardPanel, cardLayout, communicator);
        cardPanel.add(adminPanel, "admin");
        cardLayout.show(cardPanel, "admin");
    }

    private void showStudentPanel(Student student) {
        StudentPanel studentPanel = new StudentPanel(cardPanel, cardLayout, communicator, student);
        cardPanel.add(studentPanel, "student");
        cardLayout.show(cardPanel, "student");
    }

    public void clearForm() {
        txtStudentNumber.setText("");
        txtEmail.setText("");
        txtPassword.setText("");
        rdbtnStudent.setSelected(true);
        lblStatus.setText("Please enter your credentials");
        lblStatus.setForeground(Color.BLUE);
    }
}
