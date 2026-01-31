package view;

import model.*;
import util.*;

import javax.swing.*;
import java.awt.*;

public class RegisterManager extends JFrame {
    private JTextField nameField, emailField, phoneField, dobField;
    private JComboBox<String> genderCombo;
    private JPasswordField passwordField, confirmPasswordField;
    private JLabel emailGuidelineLabel, phoneGuidelineLabel, dobGuidelineLabel, passwordStrengthLabel;
    private JCheckBox showPasswordCheck;
    private JButton registerButton, backButton;

    public RegisterManager() {
        setTitle("Manager Registration");
        setSize(450, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Title
        JLabel titleLabel = new JLabel("Manager Registration", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(50, 50, 150));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createTitledBorder("Fill in your details"));

        nameField = new JTextField();
        genderCombo = new JComboBox<>(new String[]{"Male", "Female"});
        emailField = new JTextField();
        phoneField = new JTextField();
        dobField = new JTextField();

        passwordField = new JPasswordField();
        confirmPasswordField = new JPasswordField();

        passwordStrengthLabel = new JLabel("");
        passwordStrengthLabel.setForeground(Color.RED);
        emailGuidelineLabel = new JLabel("");
        emailGuidelineLabel.setForeground(Color.RED);
        phoneGuidelineLabel = new JLabel("");
        phoneGuidelineLabel.setForeground(Color.RED);
        dobGuidelineLabel = new JLabel("");
        dobGuidelineLabel.setForeground(Color.RED);

        formPanel.add(new JLabel("Name:"));                formPanel.add(nameField);
        formPanel.add(new JLabel("Gender:"));              formPanel.add(genderCombo);
        formPanel.add(new JLabel("Email:"));               formPanel.add(emailField);
        formPanel.add(new JLabel(""));                     formPanel.add(emailGuidelineLabel);
        formPanel.add(new JLabel("Phone Number:"));        formPanel.add(phoneField);
        formPanel.add(new JLabel(""));                     formPanel.add(phoneGuidelineLabel);
        formPanel.add(new JLabel("Date of Birth:"));       formPanel.add(dobField);
        formPanel.add(new JLabel(""));                     formPanel.add(dobGuidelineLabel);
        formPanel.add(new JLabel("Password:"));            formPanel.add(passwordField);
        formPanel.add(new JLabel(""));                     formPanel.add(passwordStrengthLabel);
        formPanel.add(new JLabel("Re-enter Password:"));   formPanel.add(confirmPasswordField);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Bottom panel with Show Password and Register
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        showPasswordCheck = new JCheckBox("Show Password");
        showPasswordCheck.addActionListener(e -> {
            char echo = showPasswordCheck.isSelected() ? (char) 0 : '●';
            passwordField.setEchoChar(echo);
            confirmPasswordField.setEchoChar(echo);
        });

        registerButton = new JButton("Register");
        registerButton.setEnabled(false);
        registerButton.setBackground(new Color(0, 120, 215));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);

        backButton = new JButton("Back");
        backButton.setBackground(new Color(0, 120, 215));
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);

        registerButton.addActionListener(e -> register());
        backButton.addActionListener(e -> {
            dispose();
            new Login();
        });

        bottomPanel.add(showPasswordCheck);
        bottomPanel.add(Box.createVerticalStrut(10));
        bottomPanel.add(registerButton);
        bottomPanel.add(backButton);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Live validation listeners
        nameField.getDocument().addDocumentListener(new SimpleListener(this::checkAllValid));
        emailField.getDocument().addDocumentListener(new SimpleListener(this::validateEmail));
        phoneField.getDocument().addDocumentListener(new SimpleListener(this::validatePhone));
        dobField.getDocument().addDocumentListener(new SimpleListener(this::validateDob));
        passwordField.getDocument().addDocumentListener(new SimpleListener(this::checkPasswordStrength));
        confirmPasswordField.getDocument().addDocumentListener(new SimpleListener(this::checkAllValid));

        add(mainPanel);
        setVisible(true);
    }

    private void validateEmail() {
        String email = emailField.getText().trim();
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$")) {
            emailGuidelineLabel.setText("Invalid format. Must be like name@manager.apu.my");
        } else if (!email.endsWith("@manager.apu.my")) {
            emailGuidelineLabel.setText("Manager must use @manager.apu.my");
        } else {
            emailGuidelineLabel.setText("");
        }
        checkAllValid();
    }

    private void validateDob() {
        String dob = dobField.getText().trim();
        if (!dob.matches("^\\d{2}/\\d{2}/\\d{4}$")) {
            dobGuidelineLabel.setText("Date format must be dd/MM/yyyy");
        } else {
            dobGuidelineLabel.setText("");
        }
        checkAllValid();
    }

    private void validatePhone() {
        String phone = phoneField.getText().trim();
        if (!phone.matches("^\\d{10,11}$")) {
            phoneGuidelineLabel.setText("Must be 10-11 digits with no spaces");
        } else {
            phoneGuidelineLabel.setText("");
        }
        checkAllValid();
    }

    private void checkPasswordStrength() {
        String pwd = new String(passwordField.getPassword());
        if (!pwd.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{9,}$")) {
            passwordStrengthLabel.setText("Weak password: min 9 chars, upper, lower, digit, special");
        } else {
            passwordStrengthLabel.setText("");
        }
        checkAllValid();
    }

    private void checkAllValid() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String dob = dobField.getText().trim();
        String pwd = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());

        boolean isEmailValid = email.matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$") && email.endsWith("@manager.apu.my");
        boolean isPhoneValid = phone.matches("^\\d{10,11}$");
        boolean isDobValid = dob.matches("^\\d{2}/\\d{2}/\\d{4}$");
        boolean isPwdStrong = pwd.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{9,}$");
        boolean isPwdMatch = pwd.equals(confirm);

        boolean allValid = !name.isEmpty() &&
                isEmailValid &&
                isPhoneValid &&
                isDobValid &&
                isPwdStrong &&
                isPwdMatch;

        registerButton.setEnabled(allValid);
    }

    private void register() {
        String name = nameField.getText().trim();
        String gender = genderCombo.getSelectedItem().toString();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String dob = dobField.getText().trim();
        String pwd = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());

        if (FileStorage.isEmailOrPhoneDuplicate(email, phone)) {
            int option = JOptionPane.showOptionDialog(
                    this,
                    "Email or phone number already exists. Try to login.",
                    "Already Registered",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    new String[]{"OK"},
                    "OK"
            );

            if (option == 0) {
                dispose();
                new Login();
            }
            return;
        }

        String id = IDGenerator.generateID("manager");
        FileStorage.saveManager(new Manager(id, name, gender, email, phone, pwd, dob));

        JOptionPane.showMessageDialog(this, "Registration successful. Please login.");
        dispose();
        new Login();
    }

    private class SimpleListener implements javax.swing.event.DocumentListener {
        private final Runnable action;
        public SimpleListener(Runnable action) { this.action = action; }
        public void insertUpdate(javax.swing.event.DocumentEvent e) { action.run(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e) { action.run(); }
        public void changedUpdate(javax.swing.event.DocumentEvent e) { action.run(); }
    }
}
