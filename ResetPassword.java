package view;

import model.User;
import util.FileStorage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ResetPassword extends JFrame {
    private JTextField emailOrPhoneField;
    private JPasswordField newPasswordField, confirmPasswordField;
    private JLabel statusLabel, passwordErrorLabel;
    private JCheckBox showPasswordCheck;
    private JButton verifyButton, resetButton;

    private User user;

    public ResetPassword() {
        setTitle("Reset Password");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(8, 1, 5, 5));

        emailOrPhoneField = new JTextField();
        newPasswordField = new JPasswordField();
        confirmPasswordField = new JPasswordField();

        statusLabel = new JLabel("");
        statusLabel.setForeground(Color.RED);

        passwordErrorLabel = new JLabel("");
        passwordErrorLabel.setForeground(Color.RED);

        showPasswordCheck = new JCheckBox("Show Password");
        showPasswordCheck.addActionListener(e -> {
            char echo = showPasswordCheck.isSelected() ? (char) 0 : '●';
            newPasswordField.setEchoChar(echo);
            confirmPasswordField.setEchoChar(echo);
        });

        verifyButton = new JButton("Verify");
        resetButton = new JButton("Reset Password");
        resetButton.setEnabled(false);

        panel.add(new JLabel("Email or Phone:"));
        panel.add(emailOrPhoneField);
        panel.add(statusLabel);
        panel.add(verifyButton);

        panel.add(new JLabel("New Password:"));
        panel.add(newPasswordField);
        panel.add(new JLabel("Confirm New Password:"));
        panel.add(confirmPasswordField);
        panel.add(passwordErrorLabel);
        panel.add(showPasswordCheck);
        panel.add(resetButton);

        add(panel);

        verifyButton.addActionListener(e -> {
            String input = emailOrPhoneField.getText().trim();
            user = FileStorage.findUserByEmailOrPhone(input);
            if (user != null) {
                statusLabel.setText("Account found. Enter new password.");
                resetButton.setEnabled(true);
            } else {
                statusLabel.setText("Account not found.");
                resetButton.setEnabled(false);
            }
        });

        resetButton.addActionListener(e -> {
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            passwordErrorLabel.setText("");

            User latestUser = FileStorage.findUserByEmailOrPhone(emailOrPhoneField.getText().trim());
            
            if (!newPassword.equals(confirmPassword)) {
                passwordErrorLabel.setText("Passwords do not match.");
                return;
            }

            if (latestUser != null && newPassword.equals(latestUser.getPassword())) {
                passwordErrorLabel.setText("New password cannot be the same as old password.");
                return;
            }

            if (!newPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{9,}$")) {
                passwordErrorLabel.setText("Weak password. Use 9+ chars, upper, lower, digit, special char.");
                return;
            }

            FileStorage.updateUserPassword(latestUser.getId(), newPassword);
            JOptionPane.showMessageDialog(this, "Password reset successfully.");
            dispose();
            new Login();
        });


        setVisible(true);
    }
}