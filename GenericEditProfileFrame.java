package view;

import model.User;
import model.ProfileEditable;
import util.FileStorage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class GenericEditProfileFrame extends JFrame {
    private final User user;
    private final JFrame parentFrame;
    private final Map<String, JTextField> fieldComponents;
    private final Map<String, String> originalValues;
    private JButton saveBtn;
    private JButton showPasswordBtn;
    private boolean passwordVisible = false;
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    public GenericEditProfileFrame(User user, JFrame parentFrame) {
        this.user = user;
        this.parentFrame = parentFrame;
        this.fieldComponents = new HashMap<>();
        this.originalValues = new HashMap<>();
        
        setTitle("Edit Profile - " + user.getName());
        setSize(480, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        initUI();
        
        SwingUtilities.invokeLater(() -> {
            JTextField firstEditable = getFirstEditableField();
            if (firstEditable != null) {
                firstEditable.requestFocusInWindow();
            }
        });
    }
    
    private void initUI() {
        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        
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

        String userType = user instanceof model.Doctor ? "Doctor" : "Customer";
        JLabel subtitleLabel = new JLabel("Update your " + userType.toLowerCase() + " account information");
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
        
        // Define all possible fields in order
        String[] allFields = {"name", "gender", "email", "phone", "password", "specialty", "dob"};
        int row = 0;
        
        for (String field : allFields) {
            try {
                String value = user.getFieldValue(field);
                boolean isEditable = user.isFieldEditable(field);
                
                // Store original values
                originalValues.put(field, value);
                
                gbc.gridx = 0; 
                gbc.gridy = row;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.insets = new Insets(5, 0, 5, 10);
                gbc.fill = GridBagConstraints.NONE;
                gbc.weightx = 0;
                
                String labelText = getFieldDisplayName(field);
                if (isEditable) {
                    labelText += " *";
                }
                mainPanel.add(new JLabel(labelText), gbc);
                
                gbc.gridx = 1; 
                gbc.fill = GridBagConstraints.HORIZONTAL; 
                gbc.weightx = 1.0;
                
                JTextField fieldComponent;
                if (field.equals("password")) {
                    fieldComponent = createPasswordField(value, isEditable);
                } else if (isEditable) {
                    fieldComponent = createEditableField(value);
                } else {
                    fieldComponent = createNonEditableField(value);
                }
                
                fieldComponents.put(field, fieldComponent);
                
                if (field.equals("password") && isEditable) {
                    // Special handling for password field with show/hide button
                    JPanel passwordPanel = new JPanel(new BorderLayout());
                    passwordPanel.add(fieldComponent, BorderLayout.CENTER);
                    
                    showPasswordBtn = new JButton("👁");
                    showPasswordBtn.setPreferredSize(new Dimension(35, 30));
                    showPasswordBtn.setFont(new Font("Arial", Font.PLAIN, 12));
                    showPasswordBtn.addActionListener(e -> togglePasswordVisibility());
                    passwordPanel.add(showPasswordBtn, BorderLayout.EAST);
                    
                    mainPanel.add(passwordPanel, gbc);
                } else {
                    mainPanel.add(fieldComponent, gbc);
                }
                
                // Add confirm password field for password
                if (field.equals("password") && isEditable) {
                    row++;
                    gbc.gridx = 0; 
                    gbc.gridy = row;
                    gbc.fill = GridBagConstraints.NONE;
                    gbc.weightx = 0;
                    mainPanel.add(new JLabel("Confirm Password: *"), gbc);
                    
                    gbc.gridx = 1;
                    gbc.fill = GridBagConstraints.HORIZONTAL;
                    gbc.weightx = 1.0;
                    JPasswordField confirmField = new JPasswordField();
                    confirmField.setFont(new Font("Arial", Font.PLAIN, 14));
                    confirmField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                        BorderFactory.createEmptyBorder(5, 8, 5, 8)
                    ));
                    fieldComponents.put("confirmPassword", confirmField);
                    mainPanel.add(confirmField, gbc);
                }
                
                row++;
            } catch (IllegalArgumentException e) {
                // Field doesn't exist for this user type, skip it
                continue;
            }
        }
        
        return mainPanel;
    }
    
    private String getFieldDisplayName(String field) {
        switch (field.toLowerCase()) {
            case "name": return "Name:";
            case "gender": return "Gender:";
            case "email": return "Email:";
            case "phone": return "Phone:";
            case "password": return "Password:";
            case "specialty": return "Specialty:";
            case "dob": return "Date of Birth:";
            default: return field.substring(0, 1).toUpperCase() + field.substring(1) + ":";
        }
    }
    
    private JTextField createEditableField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return field;
    }
    
    private JTextField createPasswordField(String value, boolean isEditable) {
        JPasswordField field = new JPasswordField(value);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        field.setEditable(isEditable);
        if (!isEditable) {
            field.setBackground(new Color(245, 245, 245));
        }
        return field;
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
    
    private JTextField getFirstEditableField() {
        for (String field : user.getEditableFields()) {
            JTextField component = fieldComponents.get(field);
            if (component != null && component.isEditable()) {
                return component;
            }
        }
        return null;
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
        saveBtn.setPreferredSize(new Dimension(150, 35));
        saveBtn.setBackground(new Color(0, 120, 215));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        cancelBtn.addActionListener(e -> {
            if (parentFrame != null) {
                parentFrame.setVisible(true);   // bring dashboard back
            }
            dispose(); // close edit frame
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
            "Non-editable fields are shown in gray" +
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
        
        for (String field : user.getEditableFields()) {
            JTextField component = fieldComponents.get(field);
            if (component != null) {
                component.addKeyListener(changeListener);
            }
        }
        
        // Also add to confirm password field if it exists
        JTextField confirmField = fieldComponents.get("confirmPassword");
        if (confirmField != null) {
            confirmField.addKeyListener(changeListener);
        }
    }
    
    private void checkForChanges() {
        boolean hasChanges = false;
        
        for (String field : user.getEditableFields()) {
            JTextField component = fieldComponents.get(field);
            if (component != null) {
                String currentValue = component.getText();
                String originalValue = originalValues.get(field);
                if (!currentValue.equals(originalValue)) {
                    hasChanges = true;
                    break;
                }
            }
        }
        
        if (hasChanges) {
            saveBtn.setBackground(new Color(0, 150, 0));
            saveBtn.setText("Save Changes *");
        } else {
            saveBtn.setBackground(new Color(0, 120, 215));
            saveBtn.setText("Save Changes");
        }
    }
    
    private void togglePasswordVisibility() {
        JPasswordField passwordField = (JPasswordField) fieldComponents.get("password");
        if (passwordField != null) {
            if (passwordVisible) {
                passwordField.setEchoChar('•');
                showPasswordBtn.setText("👁");
            } else {
                passwordField.setEchoChar((char) 0);
                showPasswordBtn.setText("🙈");
            }
            passwordVisible = !passwordVisible;
        }
    }
    
    private void onSave() {
        // Validate all editable fields
        for (String field : user.getEditableFields()) {
            JTextField component = fieldComponents.get(field);
            if (component != null) {
                String value = component.getText().trim();
                
                if (!validateField(field, value)) {
                    component.requestFocus();
                    return;
                }
            }
        }
        
        // Check password confirmation if password is editable
        if (user.isFieldEditable("password")) {
            JPasswordField passwordField = (JPasswordField) fieldComponents.get("password");
            JPasswordField confirmField = (JPasswordField) fieldComponents.get("confirmPassword");
            
            if (passwordField != null && confirmField != null) {
                String password = new String(passwordField.getPassword());
                String confirmPassword = new String(confirmField.getPassword());
                
                boolean passwordChanged = !password.equals(originalValues.get("password"));
                
                if (passwordChanged && !password.equals(confirmPassword)) {
                    showValidationError("Passwords do not match. Please confirm your new password.");
                    confirmField.requestFocus();
                    return;
                }
            }
        }
        
        try {
            // Update all editable fields
            for (String field : user.getEditableFields()) {
                JTextField component = fieldComponents.get(field);
                if (component != null) {
                    String newValue = component.getText().trim();
                    String originalValue = originalValues.get(field);
                    
                    // Only update if value has changed
                    if (!newValue.equals(originalValue)) {
                        user.updateEditableField(field, newValue);
                    }
                }
            }
            
            // Save to file based on user type
            if (user instanceof model.Customer) {
                FileStorage.updateCustomer((model.Customer) user);
            } else if (user instanceof model.Doctor) {
                // You'll need to add updateDoctor method to FileStorage
                FileStorage.updateDoctor((model.Doctor) user);
            }
            
            showSuccessMessage("Profile updated successfully!");
            if (parentFrame != null) {
                parentFrame.setVisible(true);   // return to dashboard
                try {
                    parentFrame.getClass().getMethod("refreshWelcomeLabel").invoke(parentFrame);
                } catch (Exception ignored) {}
            }
            dispose();
            
            // Refresh parent if it has a refresh method
            if (parentFrame != null) {
                // Try to call refresh method if available
                try {
                    parentFrame.getClass().getMethod("refreshWelcomeLabel").invoke(parentFrame);
                } catch (Exception e) {
                    // Method doesn't exist, ignore
                }
            }
            
            dispose();
        } catch (Exception e) {
            showErrorMessage("Error updating profile: " + e.getMessage());
        }
    }
    
    private boolean validateField(String field, String value) {
        switch (field.toLowerCase()) {
            case "email":
                if (value.isEmpty()) {
                    showValidationError("Email cannot be empty.");
                    return false;
                }
                if (!EMAIL_PATTERN.matcher(value).matches()) {
                    showValidationError("Please enter a valid email address.");
                    return false;
                }
                if (FileStorage.isEmailDuplicateForUpdate(value, user.getId())) {
                    showValidationError("This email is already registered by another user.");
                    return false;
                }
                break;
            case "password":
                String originalPassword = originalValues.get("password");
                boolean passwordChanged = !value.equals(originalPassword);
                
                if (passwordChanged) {
                    if (value.isEmpty()) {
                        showValidationError("Password cannot be empty.");
                        return false;
                    }
                    if (value.length() < 6) {
                        showValidationError("Password must be at least 6 characters long.");
                        return false;
                    }
                }
                break;
            case "specialty":
                if (value.isEmpty()) {
                    showValidationError("Specialty cannot be empty.");
                    return false;
                }
                break;
        }
        return true;
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