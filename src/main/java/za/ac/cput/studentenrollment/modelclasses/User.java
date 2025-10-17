package za.ac.cput.studentenrollment.modelclasses;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private String role;
    private String studentNumber;
    private String email;
    private String fullName;

    private transient String temporaryPassword;
    private boolean accountActive;
    private boolean passwordExpired;
    private int loginAttempts;
    private LocalDateTime lastLogin;
    private LocalDateTime accountCreated;
    private LocalDateTime lastPasswordChange;

    private String themePreference;
    private boolean emailNotifications;
    private boolean smsNotifications;
    private String language;

    private String securityQuestion1;
    private String securityAnswer1;
    private String securityQuestion2;
    private String securityAnswer2;

    public User() {
        this.accountActive = true;
        this.loginAttempts = 0;
        this.accountCreated = LocalDateTime.now();
        this.themePreference = "SYSTEM";
        this.emailNotifications = true;
        this.language = "en";
    }

    public User(String username, String password, String role, String studentNumber) {
        this();
        this.username = username;
        setPassword(password);
        this.role = role != null ? role.toUpperCase() : "STUDENT";
        this.studentNumber = studentNumber;
    }

    public User(String username, String password, String role, String studentNumber,
                String email, String fullName) {
        this(username, password, role, studentNumber);
        this.email = email;
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (username.length() < 3 || username.length() > 50) {
            throw new IllegalArgumentException("Username must be between 3 and 50 characters");
        }
        this.username = username.trim().toLowerCase();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
        this.password = password.trim();
        this.lastPasswordChange = LocalDateTime.now();
        this.passwordExpired = false;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        String upperRole = role.toUpperCase();
        if (!upperRole.matches("ADMIN|STUDENT|LECTURER|SUPERVISOR")) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
        this.role = upperRole;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        if (studentNumber != null && !studentNumber.matches("\\d{5,10}")) {
            throw new IllegalArgumentException("Student number must be 5-10 digits");
        }
        this.studentNumber = studentNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email != null && !email.isEmpty()) {
            if (!isValidEmail(email)) {
                throw new IllegalArgumentException("Invalid email format");
            }
            this.email = email.trim().toLowerCase();
        } else {
            this.email = null;
        }
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName != null ? fullName.trim() : null;
    }

    public void incrementLoginAttempts() {
        this.loginAttempts++;
        if (this.loginAttempts >= 5) {
            this.accountActive = false;
        }
    }

    public void resetLoginAttempts() {
        this.loginAttempts = 0;
        this.accountActive = true;
    }

    public void recordSuccessfulLogin() {
        this.lastLogin = LocalDateTime.now();
        this.loginAttempts = 0;
    }

    public boolean isAccountLocked() {
        return !accountActive || loginAttempts >= 5;
    }

    public void generateTemporaryPassword() {
        this.temporaryPassword = generateRandomPassword(8);
        this.passwordExpired = true;
    }

    public void changePassword(String oldPassword, String newPassword) {
        if (!this.password.equals(oldPassword)) {
            throw new SecurityException("Current password is incorrect");
        }
        setPassword(newPassword);
    }

    public boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    public boolean isPasswordStrong() {
        if (password == null) return false;
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*") &&
                password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
    }

    public boolean canPerformAction(String requiredRole) {
        if (isAccountLocked()) return false;

        switch (this.role) {
            case "ADMIN":
                return true;
            case "LECTURER":
                return !requiredRole.equals("ADMIN");
            case "STUDENT":
                return requiredRole.equals("STUDENT");
            default:
                return false;
        }
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    public String getMaskedPassword() {
        if (password == null) return "null";
        return "•".repeat(Math.min(password.length(), 12));
    }

    public String getUserStatus() {
        if (!accountActive) return "LOCKED";
        if (passwordExpired) return "PASSWORD_EXPIRED";
        if (loginAttempts > 0) return "SUSPICIOUS_ACTIVITY";
        return "ACTIVE";
    }

    public boolean requiresPasswordChange() {
        if (lastPasswordChange == null) return true;
        return lastPasswordChange.isBefore(LocalDateTime.now().minusDays(90)) ||
                passwordExpired;
    }

    public boolean canResetPassword() {
        return securityQuestion1 != null && securityAnswer1 != null &&
                securityQuestion2 != null && securityAnswer2 != null;
    }

    public boolean verifySecurityAnswers(String answer1, String answer2) {
        return securityAnswer1 != null && securityAnswer2 != null &&
                securityAnswer1.equalsIgnoreCase(answer1.trim()) &&
                securityAnswer2.equalsIgnoreCase(answer2.trim());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return String.format(
                "User{username='%s', role='%s', fullName='%s', status='%s', lastLogin=%s}",
                username, role, fullName, getUserStatus(), lastLogin
        );
    }

    public static class Builder {
        private User user;

        public Builder() {
            user = new User();
        }

        public Builder username(String username) {
            user.setUsername(username);
            return this;
        }

        public Builder password(String password) {
            user.setPassword(password);
            return this;
        }

        public Builder role(String role) {
            user.setRole(role);
            return this;
        }

        public Builder studentNumber(String studentNumber) {
            user.setStudentNumber(studentNumber);
            return this;
        }

        public Builder email(String email) {
            user.setEmail(email);
            return this;
        }

        public Builder fullName(String fullName) {
            user.setFullName(fullName);
            return this;
        }

        public Builder securityQuestions(String q1, String a1, String q2, String a2) {
            user.securityQuestion1 = q1;
            user.securityAnswer1 = a1;
            user.securityQuestion2 = q2;
            user.securityAnswer2 = a2;
            return this;
        }

        public User build() {
            if (user.username == null) {
                throw new IllegalStateException("Username is required");
            }
            if (user.password == null) {
                throw new IllegalStateException("Password is required");
            }
            if (user.role == null) {
                user.role = "STUDENT";
            }
            return user;
        }
    }

    public static User createAdmin(String username, String password, String fullName, String email) {
        return new Builder()
                .username(username)
                .password(password)
                .role("ADMIN")
                .fullName(fullName)
                .email(email)
                .build();
    }

    public static User createStudent(String username, String password, String studentNumber,
                                     String fullName, String email) {
        return new Builder()
                .username(username)
                .password(password)
                .role("STUDENT")
                .studentNumber(studentNumber)
                .fullName(fullName)
                .email(email)
                .build();
    }

    String getTemporaryPassword() { return temporaryPassword; }
    boolean isAccountActive() { return accountActive; }
    boolean isPasswordExpired() { return passwordExpired; }
    int getLoginAttempts() { return loginAttempts; }
    LocalDateTime getLastLogin() { return lastLogin; }
    LocalDateTime getAccountCreated() { return accountCreated; }
    LocalDateTime getLastPasswordChange() { return lastPasswordChange; }
    String getThemePreference() { return themePreference; }
    boolean isEmailNotifications() { return emailNotifications; }
    boolean isSmsNotifications() { return smsNotifications; }
    String getLanguage() { return language; }
    String getSecurityQuestion1() { return securityQuestion1; }
    String getSecurityQuestion2() { return securityQuestion2; }

    void setAccountActive(boolean accountActive) { this.accountActive = accountActive; }
    void setPasswordExpired(boolean passwordExpired) { this.passwordExpired = passwordExpired; }
    void setLoginAttempts(int loginAttempts) { this.loginAttempts = loginAttempts; }
    void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    void setThemePreference(String themePreference) { this.themePreference = themePreference; }
    void setEmailNotifications(boolean emailNotifications) { this.emailNotifications = emailNotifications; }
    void setSmsNotifications(boolean smsNotifications) { this.smsNotifications = smsNotifications; }
    void setLanguage(String language) { this.language = language; }
}