/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package za.ac.cput.studentenrollment;
import javax.swing.*;
import java.awt.*;
import za.ac.cput.studentenrollment.Gui.LoginPanel;
/**
 *
 * @author elzas
 */



public class StudentEnrollment extends JFrame {
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private ClientCommunicator communicator;

    public StudentEnrollment() {
        super("CPUT Student Enrollment System");
        this.communicator = new ClientCommunicator();
        initializeGUI();
    }

    private void initializeGUI() {
        // Setup main window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(800, 600));
        setMinimumSize(new Dimension(700, 500));

        // Create card layout for screen management
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Create and add login panel
        LoginPanel loginPanel = new LoginPanel(cardPanel, cardLayout, communicator);
        cardPanel.add(loginPanel, "login");

        // Add card panel to frame
        add(cardPanel);

        // Center window on screen
        pack();
        setLocationRelativeTo(null);

        // Test server connection on startup
        testServerConnection();
    }

    private void testServerConnection() {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return communicator.connect();
            }

            @Override
            protected void done() {
                try {
                    boolean connected = get();
                    if (!connected) {
                        int choice = JOptionPane.showConfirmDialog(
                            StudentEnrollment.this,
                            "Cannot connect to server. The server might not be running.\n" +
                            "Would you like to continue anyway?",
                            "Connection Warning",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                        );
                        
                        if (choice != JOptionPane.YES_OPTION) {
                            System.exit(0);
                        }
                    } else {
                        JOptionPane.showMessageDialog(
                            StudentEnrollment.this,
                            "Successfully connected to server!",
                            "Connection Established",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                        StudentEnrollment.this,
                        "Error testing connection: " + e.getMessage(),
                        "Connection Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        worker.execute();
    }

    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
        } catch (Exception e) {
            System.err.println("Error setting look and feel: " + e.getMessage());
        }

        // Start the application
        SwingUtilities.invokeLater(() -> {
            StudentEnrollment app = new StudentEnrollment();
            app.setVisible(true);
            
            // Show welcome message
            JOptionPane.showMessageDialog(app,
                "Welcome to CPUT Student Enrollment System!\n\n" +
                "Default Admin Login:\n" +
                "Student Number: admin\n" +
                "Password: admin123\n" +
                "Email: admin@cput.ac.za\n\n" +
                "Make sure the server is running on localhost:6666",
                "Welcome",
                JOptionPane.INFORMATION_MESSAGE);
        });
    }
}
