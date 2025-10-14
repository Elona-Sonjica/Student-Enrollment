package za.ac.cput.studentenrollment.Gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.border.Border;
import javax.swing.event.DocumentListener;
import org.w3c.dom.events.DocumentEvent;

public class LoginScreen extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel messageLabel;
    private JComboBox<String> roleComboBox;
    private JCheckBox showPasswordCheck;
    private JProgressBar progressBar;
    private JButton closeButton;

    // Modern color scheme
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SECONDARY_COLOR = new Color(236, 240, 241);
    private final Color ACCENT_COLOR = new Color(52, 152, 219);
    private final Color TEXT_COLOR = new Color(44, 62, 80);
    private final Color ERROR_COLOR = new Color(231, 76, 60);
    private final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private final Color WARNING_COLOR = new Color(230, 126, 34);

    public LoginScreen() {
        initializeUI();
        setupAnimations();
    }

    private void initializeUI() {
        setTitle("Student Enrollment System - CPUT");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        // Remove window decorations for custom look
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));

        // Main container with gradient background
        JPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        // Header section
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Form section
        JPanel formPanel = createFormPanel();
        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Footer section
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Add keyboard shortcuts
        setupKeyboardShortcuts();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(0, 0, 30, 0));

        // University logo/name with icon
        JLabel titleLabel = new JLabel("🎓 CPUT ENROLLMENT", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Student Management System", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(255, 255, 255, 200));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.CENTER);

        return headerPanel;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createCompoundBorder((Border) new RoundedBorder(15, new Color(255, 255, 255, 180)),
                new EmptyBorder(30, 25, 30, 25)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Role selection
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel roleLabel = new JLabel("👤 Login As:");
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        roleLabel.setForeground(TEXT_COLOR);
        formPanel.add(roleLabel, gbc);

        gbc.gridy = 1;
        String[] roles = {"🎓 Student", "⚙️ Administrator"};
        roleComboBox = new JComboBox<>(roles);
        styleComboBox(roleComboBox);
        formPanel.add(roleComboBox, gbc);

        // Username
        gbc.gridy = 2;
        JLabel userLabel = new JLabel("📧 Username:");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLabel.setForeground(TEXT_COLOR);
        formPanel.add(userLabel, gbc);

        gbc.gridy = 3;
        usernameField = new JTextField();
        styleTextField(usernameField);
        usernameField.setToolTipText("Enter your student number or admin username");
        formPanel.add(usernameField, gbc);

        // Password
        gbc.gridy = 4;
        JLabel passLabel = new JLabel("🔒 Password:");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passLabel.setForeground(TEXT_COLOR);
        formPanel.add(passLabel, gbc);

        gbc.gridy = 5;
        passwordField = new JPasswordField();
        stylePasswordField(passwordField);
        passwordField.setToolTipText("Enter your password");
        formPanel.add(passwordField, gbc);

        // Show password checkbox
        gbc.gridy = 6;
        showPasswordCheck = new JCheckBox("👁️ Show Password");
        styleCheckbox(showPasswordCheck);
        showPasswordCheck.addActionListener(e -> togglePasswordVisibility());
        formPanel.add(showPasswordCheck, gbc);

        // Progress bar
        gbc.gridy = 7;
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(200, 3));
        formPanel.add(progressBar, gbc);

        // Message label
        gbc.gridy = 8;
        messageLabel = new JLabel("💡 Please enter your credentials", JLabel.CENTER);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageLabel.setForeground(TEXT_COLOR);
        formPanel.add(messageLabel, gbc);

        // Login button
        gbc.gridy = 9;
        gbc.insets = new Insets(20, 8, 8, 8);
        loginButton = new JButton("🚀 SIGN IN");
        styleButton(loginButton);
        loginButton.addActionListener(e -> performLogin());
        formPanel.add(loginButton, gbc);

        return formPanel;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        // Demo credentials panel
        JPanel demoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        demoPanel.setOpaque(false);

        JButton demoButton = new JButton("ℹ️ Demo Credentials");
        demoButton.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        demoButton.setForeground(Color.WHITE);
        demoButton.setBackground(new Color(255, 255, 255, 30));
        demoButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        demoButton.setFocusPainted(false);
        demoButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        demoButton.addActionListener(e -> showDemoCredentials());
        demoPanel.add(demoButton);

        // Close button
        closeButton = new JButton("❌ Exit System");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(new Color(255, 255, 255, 50));
        closeButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> confirmExit());

        footerPanel.add(demoPanel, BorderLayout.CENTER);
        footerPanel.add(closeButton, BorderLayout.EAST);

        return footerPanel;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        field.setBackground(Color.WHITE);
        field.setForeground(TEXT_COLOR);
    }

    private void stylePasswordField(JPasswordField field) {
        styleTextField(field);
        field.setEchoChar('•');
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(Color.WHITE);
        combo.setForeground(TEXT_COLOR);
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
    }

    private void styleCheckbox(JCheckBox check) {
        check.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        check.setBackground(new Color(255, 255, 255, 0));
        check.setForeground(TEXT_COLOR);
        check.setFocusPainted(false);
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effects
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(ACCENT_COLOR);
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2),
                        BorderFactory.createEmptyBorder(13, 28, 13, 28)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(PRIMARY_COLOR);
                button.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
            }
        });
    }

    private void togglePasswordVisibility() {
        if (showPasswordCheck.isSelected()) {
            passwordField.setEchoChar((char) 0);
        } else {
            passwordField.setEchoChar('•');
        }
    }

    private void setupKeyboardShortcuts() {
        // Enter key to login
        getRootPane().setDefaultButton(loginButton);

        // Escape key to close
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        getRootPane().getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmExit();
            }
        });

        // Ctrl+D for demo credentials
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK), "demo");
        getRootPane().getActionMap().put("demo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDemoCredentials();
            }
        });
    }

    private void setupAnimations() {
        // Add focus listeners for better UX
        addFocusListenerToFields();

        // Add input validation
        addInputValidation();
    }

    private void addFocusListenerToFields() {
        for (Component comp : new Component[]{usernameField, passwordField}) {
            comp.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    comp.setBackground(new Color(240, 248, 255));
                    if (comp instanceof JTextField) {
                        ((JTextField) comp).setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(ACCENT_COLOR, 2),
                                BorderFactory.createEmptyBorder(11, 14, 11, 14)
                        ));
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    comp.setBackground(Color.WHITE);
                    if (comp instanceof JTextField) {
                        ((JTextField) comp).setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                                BorderFactory.createEmptyBorder(12, 15, 12, 15)
                        ));
                    }
                }
            });
        }
    }

    private void addInputValidation() {

        DocumentListener documentListener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { validateInput(); }
            public void removeUpdate(DocumentEvent e) { validateInput(); }
            public void insertUpdate(DocumentEvent e) { validateInput(); }

            private void validateInput() {
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword());

                if (!username.isEmpty() && !password.isEmpty()) {
                    loginButton.setEnabled(true);
                    loginButton.setBackground(PRIMARY_COLOR);
                } else {
                    loginButton.setEnabled(false);
                    loginButton.setBackground(Color.GRAY);
                }
            }
        };

        usernameField.getDocument().addDocumentListener(documentListener);
        passwordField.getDocument().addDocumentListener(documentListener);
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = getSelectedRole();

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("❌ Please fill in all fields", ERROR_COLOR);
            return;
        }

        // Show loading
        setLoadingState(true, "🔐 Authenticating...");

        // Perform authentication
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                Thread.sleep(1500); // Simulate network delay
                return authenticateUser(username, password, role);
            }

            @Override
            protected void done() {
                setLoadingState(false, "Ready");
                try {
                    boolean authenticated = get();
                    if (authenticated) {
                        showMessage("✅ Login successful! Redirecting...", SUCCESS_COLOR);
                        openDashboard(role, username);
                    } else {
                        showMessage("❌ Invalid credentials. Please try again.", ERROR_COLOR);
                        shakeLoginForm();
                        passwordField.setText("");
                        passwordField.requestFocus();
                    }
                } catch (Exception ex) {
                    showMessage("⚠️ Login error: " + ex.getMessage(), WARNING_COLOR);
                }
            }
        };
        worker.execute();
    }

    private String getSelectedRole() {
        String selected = (String) roleComboBox.getSelectedItem();
        if (selected.contains("Administrator")) {
            return "administrator";
        } else {
            return "student";
        }
    }

    private boolean authenticateUser(String username, String password, String role) {
        // Enhanced authentication logic
        if ("administrator".equals(role)) {
            return "admin".equals(username) && "admin123".equals(password);
        } else {
            // For students, check common student credentials
            return ("student".equals(username) && "student123".equals(password)) ||
                    ("12345".equals(username) && "password123".equals(password));
        }
    }

    private void setLoadingState(boolean loading, String message) {
        progressBar.setVisible(loading);
        loginButton.setEnabled(!loading);
        loginButton.setText(loading ? "🔄 AUTHENTICATING..." : "🚀 SIGN IN");
        usernameField.setEnabled(!loading);
        passwordField.setEnabled(!loading);
        roleComboBox.setEnabled(!loading);
        showPasswordCheck.setEnabled(!loading);
        closeButton.setEnabled(!loading);

        if (!loading) {
            showMessage(message, TEXT_COLOR);
        }
    }

    private void showMessage(String message, Color color) {
        messageLabel.setText(message);
        messageLabel.setForeground(color);
    }

    private void shakeLoginForm() {
        Timer timer = new Timer(50, null);
        final int[] shakeCount = {0};
        final Point originalLocation = getLocation();

        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (shakeCount[0] < 8) {
                    int offset = (shakeCount[0] % 2 == 0) ? 8 : -8;
                    setLocation(originalLocation.x + offset, originalLocation.y);
                    shakeCount[0]++;
                } else {
                    setLocation(originalLocation);
                    timer.stop();
                }
            }
        });
        timer.start();
    }

    private void openDashboard(String role, String username) {
        Timer timer = new Timer(1000, e -> {
            dispose();

            // Open appropriate dashboard based on role
            if ("administrator".equals(role)) {
                openAdminDashboard(username);
            } else {
                openStudentDashboard(username);
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void openAdminDashboard(String username) {
        // This will be integrated with your actual AdminPanel
        JOptionPane.showMessageDialog(null,
                "<html><body style='width: 300px;'>" +
                        "<h3>⚙️ Admin Access Granted</h3>" +
                        "<b>Welcome:</b> " + username + "<br><br>" +
                        "<b>Available Features:</b><br>" +
                        "• Add/Manage Courses<br>" +
                        "• Manage Students<br>" +
                        "• View Enrollment Reports<br>" +
                        "• System Configuration<br><br>" +
                        "<small><i>Admin dashboard will open shortly...</i></small>" +
                        "</body></html>",
                "Administrator Dashboard",
                JOptionPane.INFORMATION_MESSAGE);

        // TODO: Replace with actual AdminPanel integration
        System.out.println("Opening Admin Dashboard for: " + username);
    }

    private void openStudentDashboard(String username) {
        // This will be integrated with your actual StudentPanel
        JOptionPane.showMessageDialog(null,
                "<html><body style='width: 300px;'>" +
                        "<h3>🎓 Student Access Granted</h3>" +
                        "<b>Welcome:</b> " + username + "<br><br>" +
                        "<b>Available Features:</b><br>" +
                        "• Browse Available Courses<br>" +
                        "• Enroll in Courses<br>" +
                        "• View Your Enrollments<br>" +
                        "• Track Academic Progress<br><br>" +
                        "<small><i>Student dashboard will open shortly...</i></small>" +
                        "</body></html>",
                "Student Dashboard",
                JOptionPane.INFORMATION_MESSAGE);

        // TODO: Replace with actual StudentPanel integration
        System.out.println("Opening Student Dashboard for: " + username);
    }

    private void showDemoCredentials() {
        JOptionPane.showMessageDialog(this,
                "<html><body style='width: 350px;'>" +
                        "<h3>🔑 Demo Credentials</h3>" +
                        "<b>Administrator Access:</b><br>" +
                        "• Username: <code>admin</code><br>" +
                        "• Password: <code>admin123</code><br><br>" +
                        "<b>Student Access:</b><br>" +
                        "• Username: <code>student</code><br>" +
                        "• Password: <code>student123</code><br>" +
                        "• Username: <code>12345</code><br>" +
                        "• Password: <code>password123</code><br><br>" +
                        "<small><i>Use Ctrl+D to quickly access this info</i></small>" +
                        "</body></html>",
                "Demo Credentials",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void confirmExit() {
        int result = JOptionPane.showConfirmDialog(this,
                "<html><body style='width: 250px;'>" +
                        "<h3>⚠️ Confirm Exit</h3>" +
                        "Are you sure you want to exit the<br>Student Enrollment System?" +
                        "</body></html>",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new LoginScreen().setVisible(true);
        });
    }
}

// Custom gradient panel
class GradientPanel extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        Color color1 = new Color(41, 128, 185);
        Color color2 = new Color(52, 152, 219);
        GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}

// Rounded border class
class RoundedBorder implements Border {
    private int radius;
    private Color color;

    RoundedBorder(int radius, Color color) {
        this.radius = radius;
        this.color = color;
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(this.radius+1, this.radius+1, this.radius+1, this.radius+1);
    }

    @Override
    public boolean isBorderOpaque() {
        return true;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(color);
        g2d.drawRoundRect(x, y, width-1, height-1, radius, radius);
        g2d.dispose();
    }
}