package view;

import model.Customer;
import util.FileStorage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

public class EditProfileFrame extends JFrame {
    private final Customer customer;
    private final CustomerDashboard parent;
    private JTextField nameField;
    private JTextField genderField;
    private JTextField emailField;
    private JTextField phoneField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField dobField;
    private JButton saveBtn;
    private JButton showPasswordBtn;
    private boolean passwordVisible = false;

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    public EditProfileFrame(Customer customer) {
        this(customer, null);
    }

    public EditProfileFrame(Customer customer, CustomerDashboard parent) {
        this.customer = customer;
        this.parent = parent;
        setTitle("Edit Profile - " + customer.getName());
        setSize(480, 580); // Increased height to accommodate buttons
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        initUI();
        SwingUtilities.invokeLater(() -> emailField.requestFocusInWindow());
    }

    private void initUI() {
        // Use a single main panel with BoxLayout for better control
        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        
        // Add all components
        mainContainer.add(createHeaderPanel());
        mainContainer.add(createMainPanel());
        mainContainer.add(createButtonPanel());
        mainContainer.add(createFooterPanel());
        
        add(mainContainer);
        
        enableSaveButtonOnChanges();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(240, 248, 255));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("Edit Profile");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(25, 25, 112));

        JLabel subtitleLabel = new JLabel("Update your account information");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.GRAY);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        return headerPanel;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();

        // Name (non-editable)
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 0, 5, 10);
        mainPanel.add(new JLabel("Name:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        nameField = createNonEditableField(customer.getName());
        mainPanel.add(nameField, gbc);

        // Gender (non-editable)
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Gender:"), gbc);

        gbc.gridx = 1;
        genderField = createNonEditableField(customer.getGender());
        mainPanel.add(genderField, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel emailLabel = new JLabel("Email: *");
        mainPanel.add(emailLabel, gbc);

        gbc.gridx = 1;
        emailField = new JTextField(customer.getEmail());
        emailField.setFont(new Font("Arial", Font.PLAIN, 14));
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        mainPanel.add(emailField, gbc);

        // Phone (non-editable)
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Phone:"), gbc);

        gbc.gridx = 1;
        phoneField = createNonEditableField(customer.getPhone());
        mainPanel.add(phoneField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel passwordLabel = new JLabel("Password: *");
        mainPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordField = new JPasswordField(customer.getPassword());
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        showPasswordBtn = new JButton("👁");
        showPasswordBtn.setPreferredSize(new Dimension(35, 30)); // Slightly wider
        showPasswordBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        showPasswordBtn.addActionListener(e -> togglePasswordVisibility());

        passwordPanel.add(passwordField, BorderLayout.CENTER);
        passwordPanel.add(showPasswordBtn, BorderLayout.EAST);
        mainPanel.add(passwordPanel, gbc);

        // Confirm Password
        gbc.gridx = 0; gbc.gridy = 5;
        JLabel confirmLabel = new JLabel("Confirm Password: *");
        mainPanel.add(confirmLabel, gbc);

        gbc.gridx = 1;
        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 14));
        confirmPasswordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        mainPanel.add(confirmPasswordField, gbc);

        // DOB (non-editable)
        gbc.gridx = 0; gbc.gridy = 6;
        mainPanel.add(new JLabel("Date of Birth:"), gbc);

        gbc.gridx = 1;
        dobField = createNonEditableField(customer.getDob());
        mainPanel.add(dobField, gbc);

        return mainPanel;
    }

    private JTextField createNonEditableField(String value) {
        JTextField field = new JTextField(value);
        field.setEditable(false);
        field.setBackground(new Color(245, 245, 245));
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return field;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        cancelBtn.setPreferredSize(new Dimension(100, 35));
        cancelBtn.setBackground(new Color(128, 128, 128));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFocusPainted(false);

        saveBtn = new JButton("Save Changes");
        saveBtn.setFont(new Font("Arial", Font.BOLD, 14));
        saveBtn.setPreferredSize(new Dimension(150, 35)); // Increased width
        saveBtn.setBackground(new Color(0, 120, 215));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setEnabled(true); // Always enabled

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        cancelBtn.addActionListener(e -> {
            if (parent != null) {
                parent.setVisible(true);}
                dispose(); // close edit profile window
                });
        saveBtn.addActionListener(e -> onSave());

        // ESC key handler
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });

        return buttonPanel;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(new Color(250, 250, 250));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));

        JLabel instructionLabel = new JLabel("<html><center>" +
            "<span style='color: green;'>* Editable fields</span><br>" +
            "Name, Gender, Phone, and Date of Birth cannot be modified" +
            "</center></html>");
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);

        footerPanel.add(instructionLabel);
        return footerPanel;
    }

    private void enableSaveButtonOnChanges() {
        KeyAdapter changeListener = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                checkForChanges();
            }
        };
        emailField.addKeyListener(changeListener);
        passwordField.addKeyListener(changeListener);
        confirmPasswordField.addKeyListener(changeListener);
    }

    private void checkForChanges() {
        String currentEmail = emailField.getText().trim();
        String currentPassword = new String(passwordField.getPassword());
        boolean hasChanges = !currentEmail.equals(customer.getEmail()) ||
                             !currentPassword.equals(customer.getPassword());
        
        if (hasChanges) {
            saveBtn.setBackground(new Color(0, 150, 0));
            saveBtn.setText("Save Changes *");
        } else {
            saveBtn.setBackground(new Color(0, 120, 215));
            saveBtn.setText("Save Changes");
        }
    }

    private void togglePasswordVisibility() {
        if (passwordVisible) {
            // Hide password
            passwordField.setEchoChar('•'); // Use bullet character
            showPasswordBtn.setText("👁");
        } else {
            // Show password
            passwordField.setEchoChar((char) 0); // Show actual characters
            showPasswordBtn.setText("🙈");
        }
        passwordVisible = !passwordVisible;
    }

    private void onSave() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (email.isEmpty()) {
            showValidationError("Email cannot be empty.");
            emailField.requestFocus();
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showValidationError("Please enter a valid email address.");
            emailField.requestFocus();
            return;
        }

        // Check if password was modified
        boolean passwordChanged = !password.equals(customer.getPassword());
        
        if (passwordChanged) {
            // Only validate password fields if password was actually changed
            if (password.isEmpty()) {
                showValidationError("Password cannot be empty.");
                passwordField.requestFocus();
                return;
            }

            if (password.length() < 6) {
                showValidationError("Password must be at least 6 characters long.");
                passwordField.requestFocus();
                return;
            }

            if (!password.equals(confirmPassword)) {
                showValidationError("Passwords do not match. Please confirm your new password.");
                confirmPasswordField.requestFocus();
                return;
            }
        }

        if (FileStorage.isEmailDuplicateForUpdate(email, customer.getId())) {
            showValidationError("This email is already registered by another user.");
            emailField.requestFocus();
            return;
        }

        try {
            customer.setEmail(email);
            // Only update password if it was changed
            if (passwordChanged) {
                customer.setPassword(password);
            }
            FileStorage.updateCustomer(customer);
            showSuccessMessage("Profile updated successfully!");
            if (parent != null) {
                parent.refreshWelcomeLabel();
                parent.setVisible(true);
            }
            dispose();
        } catch (Exception e) {
            showErrorMessage("Error updating profile: " + e.getMessage());
        }
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}