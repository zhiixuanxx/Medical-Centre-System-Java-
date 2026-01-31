package view;

import model.*;
import util.FileStorage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class Login extends JFrame {
    private JTextField emailOrPhoneField;
    private JLabel emailFormatLabel;
    private JPasswordField passwordField;
    private JLabel passwordErrorLabel;
    private JCheckBox showPasswordCheck;
    private JButton loginButton, registerButton, forgotPasswordButton;

    public Login() {
        setTitle("Login");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(8, 1, 5, 5));

        emailOrPhoneField = new JTextField();
        emailFormatLabel = new JLabel("");
        emailFormatLabel.setForeground(Color.RED);

        emailOrPhoneField.getDocument().addDocumentListener(new SimpleListener(this::validateEmailFormat));

        passwordField = new JPasswordField();
        passwordErrorLabel = new JLabel("");
        passwordErrorLabel.setForeground(Color.RED);

        showPasswordCheck = new JCheckBox("Show Password");
        showPasswordCheck.addActionListener(e -> {
            char echo = showPasswordCheck.isSelected() ? (char) 0 : '●';
            passwordField.setEchoChar(echo);
        });

        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        forgotPasswordButton = new JButton("Forgot Password");

        panel.add(new JLabel("Email or Phone:"));
        panel.add(emailOrPhoneField);
        panel.add(emailFormatLabel);

        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(passwordErrorLabel);
        panel.add(showPasswordCheck);

        JPanel btnPanel = new JPanel();
        btnPanel.add(loginButton);
        btnPanel.add(registerButton);
        btnPanel.add(forgotPasswordButton);

        panel.add(btnPanel);
        add(panel);

        loginButton.addActionListener(e -> handleLogin());
        registerButton.addActionListener(e -> {
            dispose();
            new RoleSelection();
        });
        forgotPasswordButton.addActionListener(e -> {
            dispose();
            new ResetPassword();
        });

        setVisible(true);
    }

    private void handleLogin() {
        String input = emailOrPhoneField.getText().trim();
        String password = new String(passwordField.getPassword());
        passwordErrorLabel.setText("");

        if (input.isEmpty() || password.isEmpty()) {
            passwordErrorLabel.setText("Email/Phone and Password cannot be empty.");
            return;
        }

        List<User> allUsers = FileStorage.getAllUsers();
        User matchedUser = null;

        for (User user : allUsers) {
            if (user.getEmail().equalsIgnoreCase(input) || user.getPhone().equals(input)) {
                matchedUser = user;
                break;
            }
        }

        if (matchedUser == null) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Account not found. Do you want to register a new account?",
                    "Account Not Found",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                dispose();
                new RoleSelection(); // go to registration page
            }
            // if NO, just stay on login page
            return;
        }

        // Try both original and trimmed comparison
        if (!matchedUser.getPassword().equals(password) && 
            !matchedUser.getPassword().trim().equals(password.trim())) {
            passwordErrorLabel.setText("Incorrect password.");
            passwordField.setText("");
            return;
        }

        // Successful login
        JOptionPane.showMessageDialog(this, "Login successful!");
        dispose();

        if (matchedUser instanceof Manager) {
            new ManagerDashboard((Manager) matchedUser);
        } 
        else if (matchedUser instanceof Staff) {
            new StaffDashboard((Staff) matchedUser);
        } 
        else if (matchedUser instanceof Doctor) {
            new DoctorDashboard((Doctor) matchedUser);
        } 
        else if (matchedUser instanceof Customer) {
            new CustomerDashboard((Customer) matchedUser).setVisible(true);
        } 
        else {
            JOptionPane.showMessageDialog(this, "Unrecognized user role.");
        }
    }

    private class SimpleListener implements javax.swing.event.DocumentListener {
        private final Runnable action;

        public SimpleListener(Runnable action) {
            this.action = action;
        }

        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            action.run();
        }

        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            action.run();
        }

        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            action.run();
        }
    }

    private void validateEmailFormat() {
        String input = emailOrPhoneField.getText().trim();
        if (!input.matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$") && !input.matches("^\\d{10,11}$")) {
            emailFormatLabel.setText("Invalid email or phone format");
        } else {
            emailFormatLabel.setText("");
        }
    }
}
