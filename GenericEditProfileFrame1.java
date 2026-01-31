package GUI;

import model.User;
import util.FileStorage;
import model.ProfileEditable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import view.CustomerDashboard;

public class GenericEditProfileFrame1 extends JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(GenericEditProfileFrame1.class.getName());
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile( "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    
    private final User user;
    private final JFrame parentFrame;
    private final Map<String, JTextField> fieldComponents;
    private final Map<String, String> originalValues;

    private boolean passwordVisible = false;
    
    public GenericEditProfileFrame1(User user, JFrame parentFrame) {
        this.user = user;
        this.parentFrame = parentFrame;
        this.fieldComponents = new HashMap<>();
        this.originalValues = new HashMap<>();
        initComponents();
        setLocationRelativeTo(null);
        setUserDetails();
        
        SwingUtilities.invokeLater(() -> {
        JTextField firstEditable = getFirstEditableField();
        if (firstEditable != null) {
            firstEditable.requestFocusInWindow();
            }
        });
    }
    
    private JTextField createEditableField(String value) {
        JTextField field = new JTextField(value);
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

    private JTextField createNonEditableField(String value) {
        JTextField field = new JTextField(value);
        field.setEditable(false);
        return field;
    }

    private void setUserDetails() {
        // ----- Non-editable fields -----
        nameField.setText(user.getName());
        nameField.setEditable(false);

        genderField.setText(user.getGender());
        genderField.setEditable(false);

        phoneField.setText(user.getPhone());
        phoneField.setEditable(false);

        dobField.setText(formatDob(user.getDob()));
        dobField.setEditable(false);

        // ----- Email (domain locked for staff, doctor, manager) -----
        String email = user.getEmail();
        if (user instanceof model.Doctor) {
            emailField.setText(email.split("@")[0]);  // only username
            emailField.setEditable(true);
            emailLabel.setText("Email (Doctor) *");
        } else if (user instanceof model.Staff) {
            emailField.setText(email.split("@")[0]);
            emailField.setEditable(true);
            emailLabel.setText("Email (Staff) *");
        } else if (user instanceof model.Manager) {
            emailField.setText(email.split("@")[0]);
            emailField.setEditable(true);
            emailLabel.setText("Email (Manager) *");
        } else {
            // Customers can edit the full email
            emailField.setText(email);
            emailField.setEditable(true);
        }

        passwordField.setText(user.getPassword());
        passwordField.setEditable(true);

        confirmField.setText(user.getPassword());
        confirmField.setEditable(true);

        // ----- Specialty (only for Doctor) -----
        if (user instanceof model.Doctor) {
            specialtyPanel.setVisible(true);
            specialtyField.setText(((model.Doctor) user).getSpecialty());
            specialtyField.setEditable(true);
        } else {
            specialtyPanel.setVisible(false); // completely hide it for non-doctors
        }

        // ----- Store original values -----
        originalValues.put("name", user.getName());
        originalValues.put("gender", user.getGender());
        originalValues.put("email", user.getEmail());
        originalValues.put("phone", user.getPhone());
        originalValues.put("password", user.getPassword());
        originalValues.put("dob", user.getDob());

        if (user instanceof model.Doctor) {
            originalValues.put("specialty", ((model.Doctor) user).getSpecialty());
        }

        // ----- Track only editable fields -----
        fieldComponents.clear();
        fieldComponents.put("email", emailField);
        fieldComponents.put("password", passwordField);
        fieldComponents.put("confirmPassword", confirmField);

        if (user instanceof model.Doctor) {
            fieldComponents.put("specialty", specialtyField);
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
            
            String finalEmail = emailField.getText().trim();

            if (user instanceof model.Doctor) {
                finalEmail = finalEmail + "@doctor.apu.my";
            } else if (user instanceof model.Staff) {
                finalEmail = finalEmail + "@staff.apu.my";
            } else if (user instanceof model.Manager) {
                finalEmail = finalEmail + "@manager.apu.my";
            }

            user.updateEditableField("email", finalEmail);


            
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
                String fullEmail = value;
                if (user instanceof model.Doctor) {
                    fullEmail = value + "@doctor.apu.my";
                } else if (user instanceof model.Staff) {
                    fullEmail = value + "@staff.apu.my";
                } else if (user instanceof model.Manager) {
                    fullEmail = value + "@manager.apu.my";
                }
                if (!EMAIL_PATTERN.matcher(fullEmail).matches()) {
                    showValidationError("Please enter a valid email.");
                    return false;
                }
                if (FileStorage.isEmailDuplicateForUpdate(fullEmail, user.getId())) {
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
    
    private void enableSaveButtonOnChanges() {
        KeyAdapter changeListener = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                checkForChanges();
            }
        };
        emailField.addKeyListener(changeListener);
        passwordField.addKeyListener(changeListener);
        confirmField.addKeyListener(changeListener);
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
        if (passwordVisible) {
            // Hide both fields
            passwordField.setEchoChar('•'); 
            confirmField.setEchoChar('•');
            showPasswordBtn.setText("👁");
        } else {
            // Show both fields
            passwordField.setEchoChar((char) 0); 
            confirmField.setEchoChar((char) 0);
            showPasswordBtn.setText("🙈");
        }
        passwordVisible = !passwordVisible;
    }
    
    private String formatDob(String dob) {
        try {
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            java.time.LocalDate date = java.time.LocalDate.parse(dob, fmt);
            return date.format(fmt); // keep same format
        } catch (Exception e) {
            return dob; // fallback if something weird slips in
        }
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        headerPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        mainPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        emailLabel = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        passwordLabel = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        genderField = new javax.swing.JTextField();
        emailField = new javax.swing.JTextField();
        phoneField = new javax.swing.JTextField();
        passwordField = new javax.swing.JPasswordField();
        confirmField = new javax.swing.JPasswordField();
        dobField = new javax.swing.JTextField();
        showPasswordBtn = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        specialtyPanel = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        specialtyField = new javax.swing.JTextField();
        buttonPanel = new javax.swing.JPanel();
        saveBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Edit Profile - " + user.getName());
        setBackground(new java.awt.Color(204, 204, 204));
        setLocation(new java.awt.Point(0, 0));
        setMinimumSize(new java.awt.Dimension(480, 580));
        setResizable(false);
        setSize(new java.awt.Dimension(480, 580));

        headerPanel.setBackground(new java.awt.Color(230, 245, 255));
        headerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jLabel1.setFont(new java.awt.Font("Perpetua", 1, 28)); // NOI18N
        jLabel1.setText("EDIT PROFILE ");

        jLabel2.setFont(new java.awt.Font("Perpetua", 0, 16)); // NOI18N
        jLabel2.setText("Update your account information !");

        javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(headerPanelLayout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 360, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(headerPanelLayout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(484, 484, 484))))
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jLabel3.setFont(new java.awt.Font("Perpetua", 1, 18)); // NOI18N
        jLabel3.setText("Name:");

        jLabel4.setFont(new java.awt.Font("Perpetua", 1, 18)); // NOI18N
        jLabel4.setText("Gender: ");

        emailLabel.setFont(new java.awt.Font("Perpetua", 1, 18)); // NOI18N
        emailLabel.setText("Email: * ");

        jLabel6.setFont(new java.awt.Font("Perpetua", 1, 18)); // NOI18N
        jLabel6.setText("Phone: ");

        passwordLabel.setFont(new java.awt.Font("Perpetua", 1, 18)); // NOI18N
        passwordLabel.setText("Password: *");

        jLabel8.setFont(new java.awt.Font("Perpetua", 1, 18)); // NOI18N
        jLabel8.setText("Confirm Password: *");

        jLabel9.setFont(new java.awt.Font("Perpetua", 1, 18)); // NOI18N
        jLabel9.setText("Date Of Birth: ");

        nameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameFieldActionPerformed(evt);
            }
        });

        genderField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                genderFieldActionPerformed(evt);
            }
        });

        emailField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                emailFieldActionPerformed(evt);
            }
        });

        phoneField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phoneFieldActionPerformed(evt);
            }
        });

        passwordField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwordFieldActionPerformed(evt);
            }
        });

        confirmField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmFieldActionPerformed(evt);
            }
        });

        dobField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dobFieldActionPerformed(evt);
            }
        });

        showPasswordBtn.setText("👁");
        showPasswordBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showPasswordBtnActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Perpetua", 0, 14)); // NOI18N
        jLabel5.setText("* Editable fields");

        jTextField1.setBackground(new java.awt.Color(242, 242, 242));
        jTextField1.setFont(new java.awt.Font("Perpetua", 1, 18)); // NOI18N
        jTextField1.setText("Specialty: *");
        jTextField1.setBorder(null);
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        specialtyField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                specialtyFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout specialtyPanelLayout = new javax.swing.GroupLayout(specialtyPanel);
        specialtyPanel.setLayout(specialtyPanelLayout);
        specialtyPanelLayout.setHorizontalGroup(
            specialtyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(specialtyPanelLayout.createSequentialGroup()
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 52, Short.MAX_VALUE)
                .addComponent(specialtyField, javax.swing.GroupLayout.PREFERRED_SIZE, 347, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        specialtyPanelLayout.setVerticalGroup(
            specialtyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(specialtyPanelLayout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addGroup(specialtyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(specialtyField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGap(54, 54, 54)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(emailLabel)
                            .addComponent(jLabel6)
                            .addComponent(passwordLabel)
                            .addComponent(jLabel8)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(genderField)
                                .addComponent(emailField)
                                .addComponent(nameField, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                                .addComponent(phoneField))
                            .addComponent(confirmField, javax.swing.GroupLayout.PREFERRED_SIZE, 347, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 301, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(showPasswordBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(specialtyPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(dobField, javax.swing.GroupLayout.PREFERRED_SIZE, 347, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(75, 75, 75))))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(genderField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(emailLabel)
                    .addComponent(emailField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(phoneField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordLabel)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(showPasswordBtn))
                .addGap(18, 18, 18)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addComponent(confirmField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(specialtyPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dobField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 56, Short.MAX_VALUE)
                .addComponent(jLabel5))
        );

        buttonPanel.setBackground(new java.awt.Color(255, 255, 255));

        saveBtn.setBackground(new java.awt.Color(230, 245, 255));
        saveBtn.setFont(new java.awt.Font("Perpetua", 0, 18)); // NOI18N
        saveBtn.setText("Save Changes");
        saveBtn.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        saveBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveBtnActionPerformed(evt);
            }
        });

        cancelBtn.setBackground(new java.awt.Color(204, 204, 204));
        cancelBtn.setFont(new java.awt.Font("Perpetua", 0, 18)); // NOI18N
        cancelBtn.setText("Cancel");
        cancelBtn.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout buttonPanelLayout = new javax.swing.GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addGap(322, 322, 322)
                .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37)
                .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(headerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(buttonPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(headerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(26, 26, 26)
                .addComponent(buttonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void nameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameFieldActionPerformed
        nameField = createNonEditableField(user.getName());
    }//GEN-LAST:event_nameFieldActionPerformed

    private void genderFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_genderFieldActionPerformed
        genderField = createNonEditableField(user.getGender());
    }//GEN-LAST:event_genderFieldActionPerformed

    private void emailFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_emailFieldActionPerformed
        emailField = createEditableField(user.getEmail());
    }//GEN-LAST:event_emailFieldActionPerformed

    private void phoneFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phoneFieldActionPerformed
        phoneField = createNonEditableField(user.getPhone());
    }//GEN-LAST:event_phoneFieldActionPerformed

    private void passwordFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordFieldActionPerformed
        confirmField.requestFocusInWindow();
    }//GEN-LAST:event_passwordFieldActionPerformed

    private void confirmFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confirmFieldActionPerformed
        onSave();
    }//GEN-LAST:event_confirmFieldActionPerformed

    private void dobFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dobFieldActionPerformed
        dobField = createNonEditableField(user.getDob());
    }//GEN-LAST:event_dobFieldActionPerformed

    private void showPasswordBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showPasswordBtnActionPerformed
        togglePasswordVisibility();
    }//GEN-LAST:event_showPasswordBtnActionPerformed

    private void saveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed
        onSave();
    }//GEN-LAST:event_saveBtnActionPerformed

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
            if (parentFrame != null) {
                parentFrame.setVisible(true);   // bring dashboard back
            }
            dispose(); // close edit frame
    }//GEN-LAST:event_cancelBtnActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void specialtyFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_specialtyFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_specialtyFieldActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JPasswordField confirmField;
    private javax.swing.JTextField dobField;
    private javax.swing.JTextField emailField;
    private javax.swing.JLabel emailLabel;
    private javax.swing.JTextField genderField;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JTextField nameField;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JTextField phoneField;
    private javax.swing.JButton saveBtn;
    private javax.swing.JButton showPasswordBtn;
    private javax.swing.JTextField specialtyField;
    private javax.swing.JPanel specialtyPanel;
    // End of variables declaration//GEN-END:variables

}
