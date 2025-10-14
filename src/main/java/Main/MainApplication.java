package Main;

import za.ac.cput.studentenrollment.ui.LoginScreen;
import za.ac.cput.studentenrollment.Database.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import za.ac.cput.studentenrollment.Gui.LoginScreen;

public class MainApplication {

    public static void main(String[] args) {
        System.out.println("🚀 Starting CPUT Student Enrollment System...");

        // Handle command line arguments
        if (args.length > 0 && args[0].equals("--help")) {
            showHelp();
            return;
        }

        // Initialize in Event Dispatch Thread for Swing
        SwingUtilities.invokeLater(() -> {
            initializeApplication();
        });
    }

    private static void initializeApplication() {
        try {
            // 1. Set modern look and feel
            setModernLookAndFeel();

            // 2. Show startup message
            showStartupInfo();

            // 3. Initialize database (creates tables and sample data)
            initializeDatabase();

            // 4. Launch the login screen
            launchLoginScreen();

        } catch (Exception e) {
            handleStartupError(e);
        }
    }

    private static void setModernLookAndFeel() {
        try {
            // Use system look and feel for native appearance
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Enhanced UI settings for modern look
            UIManager.put("Button.arc", 15);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 8);

            // Better font settings
            UIManager.put("Button.font", new Font("Segoe UI", Font.PLAIN, 12));
            UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 12));
            UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 12));

            System.out.println("✅ UI configured with modern theme");

        } catch (Exception e) {
            System.out.println("⚠️ Using default look and feel");
        }
    }

    private static void showStartupInfo() {
        System.out.println("=========================================");
        System.out.println("📚 CPUT Student Enrollment System");
        System.out.println("🎓 Version 1.0.0");
        System.out.println("👨‍💻 ADP262S Project 2024");
        System.out.println("=========================================");
    }

    private static void initializeDatabase() {
        try {
            System.out.println("🔄 Initializing database...");

            // This will automatically:
            // - Create database if not exists
            // - Create all tables (students, courses, enrollments, users)
            // - Insert sample data for testing
            DBConnection.getConnection();

            System.out.println("✅ Database initialized successfully");
            System.out.println("✅ Sample data loaded");

        } catch (SQLException e) {
            System.err.println("❌ Database error: " + e.getMessage());

            // Show user-friendly error message
            JOptionPane.showMessageDialog(null,
                    "⚠️ Database Connection Issue\n\n" +
                            "The system will run in demonstration mode.\n" +
                            "Some features may be limited.\n\n" +
                            "Technical details: " + e.getMessage(),
                    "Database Warning",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private static void launchLoginScreen() {
        try {
            System.out.println("🖥️ Launching login screen...");

            // Create and display the enhanced login screen
            LoginScreen loginScreen = new LoginScreen();
            loginScreen.setVisible(true);

            System.out.println("✅ Application started successfully");
            System.out.println("🔑 Use these demo credentials to login:");
            System.out.println("   Administrator: admin / admin123");
            System.out.println("   Student: student / student123");
            System.out.println("   Student: 12345 / password123");

        } catch (Exception e) {
            System.err.println("💥 Failed to launch UI: " + e.getMessage());
            e.printStackTrace();

            JOptionPane.showMessageDialog(null,
                    "❌ Failed to start application interface!\n\n" +
                            "Error: " + e.getMessage() + "\n\n" +
                            "Please ensure:\n" +
                            "• Java 8 or higher is installed\n" +
                            "• System has sufficient memory\n" +
                            "• Display is available",
                    "UI Startup Error",
                    JOptionPane.ERROR_MESSAGE);

            System.exit(1);
        }
    }

    private static void handleStartupError(Exception e) {
        System.err.println("💥 Critical startup error: " + e.getMessage());
        e.printStackTrace();

        JOptionPane.showMessageDialog(null,
                "❌ Application cannot start!\n\n" +
                        "A critical error occurred during startup.\n\n" +
                        "Error: " + e.getMessage() + "\n\n" +
                        "Please contact technical support.",
                "Fatal Error",
                JOptionPane.ERROR_MESSAGE);

        System.exit(1);
    }

    private static void showHelp() {
        System.out.println("CPUT Student Enrollment System - Help");
        System.out.println("Usage: java MainApplication [options]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --help    Show this help message");
        System.out.println("  (no args) Start the application normally");
        System.out.println();
        System.out.println("Default credentials:");
        System.out.println("  Admin:     username=admin, password=admin123");
        System.out.println("  Student:   username=student, password=student123");
        System.out.println("  Student:   username=12345, password=password123");
    }

    // Public utility methods for other classes to use
    public static void shutdownApplication() {
        System.out.println("🔄 Shutting down application gracefully...");
        try {
            DBConnection.shutdown();
            System.out.println("✅ Database connections closed");
        } catch (Exception e) {
            System.err.println("⚠️ Error during shutdown: " + e.getMessage());
        }
        System.out.println("🎯 Application shutdown complete");
        System.exit(0);
    }

    public static void showAbout() {
        JOptionPane.showMessageDialog(null,
                "📚 CPUT Student Enrollment System\n\n" +
                        "Version 1.0.0\n" +
                        "Developed for ADP262S Project\n\n" +
                        "Features:\n" +
                        "• Student Management\n" +
                        "• Course Enrollment\n" +
                        "• Administrator Dashboard\n" +
                        "• Modern User Interface\n\n" +
                        "© 2024 CPUT",
                "About",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean isDevelopmentMode() {
        return System.getProperty("development.mode", "false").equals("true");
    }
}
