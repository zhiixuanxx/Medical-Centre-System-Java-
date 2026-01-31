
package GUI;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import model.Customer;
import model.Manager;
import util.FileStorage;
import util.IDGenerator;
import GUI.LoginGUI;

public class RegisterCustomer extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(RegisterCustomer.class.getName());
    private String role;
    
    public RegisterCustomer() {
        setTitle("Register");
        setSize(650, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        initComponents();
        
        nameField.getDocument().addDocumentListener(new SimpleListener(this::checkAllValid));
        emailField.getDocument().addDocumentListener(new SimpleListener(this::validateEmail));
        phoneField.getDocument().addDocumentListener(new SimpleListener(this::validatePhone));
        dobField.getDocument().addDocumentListener(new SimpleListener(this::validateDob));
        passwordField.getDocument().addDocumentListener(new SimpleListener(this::checkPasswordStrength));
        confirmPasswordField.getDocument().addDocumentListener(new SimpleListener(this::checkAllValid));
        
    }
    private void validateEmail() {
        String email = emailField.getText().trim();

        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$")) {
            emailGuidelineLabel.setText("Invalid format. Must be like name@example.com");
            role = "invalid";
        } else if (email.endsWith(".com")) {
            emailGuidelineLabel.setText("");
            role = "customer";
        } else if (email.endsWith("@manager.apu.my")) {
            emailGuidelineLabel.setText("");
            role = "manager";
        } else {
            emailGuidelineLabel.setText("Invalid email domain");
            role = "invalid";
        }

        checkAllValid();
    }

    private void validateDob() {
        String dob = dobField.getText().trim();
        try {
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            java.time.LocalDate parsed = java.time.LocalDate.parse(dob, fmt);
            // Optional: ensure DOB is not in the future
            if (parsed.isAfter(java.time.LocalDate.now())) {
                dobGuidelineLabel.setText("Date of Birth cannot be in the future");
            } else {
                dobGuidelineLabel.setText("");
            }
        } catch (Exception e) {
            dobGuidelineLabel.setText("Invalid date. Must be real dd/MM/yyyy");
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
        if (!pwd.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$")) {
            passwordStrengthLabel.setText("Password must be strong (min 8 chars, upper, lower, digit, special)");
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

        boolean isEmailFormatValid = email.matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$");
        boolean isAllowedDomain = email.endsWith("@manager.apu.my") || email.endsWith(".com");
        boolean isEmailValid = isEmailFormatValid && isAllowedDomain;

        boolean isPhoneValid = phone.matches("^\\d{10,11}$");
        boolean isDobValid = false;
        try {
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            java.time.LocalDate parsed = java.time.LocalDate.parse(dob, fmt);
            isDobValid = !parsed.isAfter(java.time.LocalDate.now());
        } catch (Exception e) {
            isDobValid = false;
        }
        dobGuidelineLabel.setText(isDobValid ? "" : "Format must be dd/MM/yyyy");

        boolean isPwdStrong = pwd.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{9,}$");
        boolean isPwdMatch = pwd.equals(confirm);

        if (email.endsWith("@manager.apu.my")) role = "manager";
        else if (email.endsWith(".com")) role = "customer";
        else role = "invalid";

        boolean allValid = !name.isEmpty() &&
                isEmailValid &&
                isPhoneValid &&
                isDobValid &&
                isPwdStrong &&
                isPwdMatch &&
                !role.equals("invalid");

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

        if (name.isEmpty() || gender.isEmpty() || email.isEmpty() || phone.isEmpty() || dob.isEmpty() || pwd.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled.");
            return;
        }

        if (!name.matches("^[a-zA-Z\\s]+$")) {
            JOptionPane.showMessageDialog(this, "Name must only contain letters and spaces.");
            return;
        }

        if (!pwd.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.");
            return;
        }

        if (!pwd.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{9,}$")) {
            JOptionPane.showMessageDialog(this, "Password is not strong enough.");
            return;
        }

        if (email.endsWith("@manager.apu.my")) role = "manager";
        else if (email.endsWith(".com")) role = "customer";
        else {
            JOptionPane.showMessageDialog(this, "Invalid email domain.");
            return;
        }

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
                new LoginGUI();
            }
            return;
        }

        String id = IDGenerator.generateID("customer");
        FileStorage.saveCustomer(new Customer(id, name, gender, email, phone, pwd, dob));

        JOptionPane.showMessageDialog(this, "Registration successful. Please login.");
        dispose();
        new LoginGUI();
    }

    private class SimpleListener implements javax.swing.event.DocumentListener {
        private final Runnable action;
        public SimpleListener(Runnable action) {
            this.action = action;
        }
        public void insertUpdate(javax.swing.event.DocumentEvent e) { action.run(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e) { action.run(); }
        public void changedUpdate(javax.swing.event.DocumentEvent e) { action.run(); }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        formPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        emailGuidelineLabel = new javax.swing.JLabel();
        showPasswordCheck = new javax.swing.JCheckBox();
        emailField = new javax.swing.JTextField();
        phoneField = new javax.swing.JTextField();
        phoneGuidelineLabel = new javax.swing.JLabel();
        dobField = new javax.swing.JTextField();
        dobGuidelineLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        confirmPasswordField = new javax.swing.JPasswordField();
        passwordStrengthLabel = new javax.swing.JLabel();
        genderCombo = new javax.swing.JComboBox<>();
        nameField = new javax.swing.JTextField();
        registerButton = new javax.swing.JButton();
        backButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        mainPanel.setBackground(new java.awt.Color(230, 245, 250));

        titleLabel.setFont(new java.awt.Font("Perpetua", 1, 28)); // NOI18N
        titleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        titleLabel.setText("Customer Registration");

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addContainerGap(220, Short.MAX_VALUE)
                .addComponent(titleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 311, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(198, 198, 198))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(titleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        formPanel.setBackground(new java.awt.Color(248, 249, 250));
        formPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Fill in your details", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Perpetua", 1, 14))); // NOI18N

        jLabel1.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        jLabel1.setText("Name : ");

        jLabel2.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        jLabel2.setText("Gender : ");

        jLabel3.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        jLabel3.setText("Email : ");

        jLabel4.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        jLabel4.setText("Phone Number : ");

        jLabel5.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        jLabel5.setText("Date Of Birth : ");

        jLabel6.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        jLabel6.setText("Password : ");

        jLabel7.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        jLabel7.setText("Re-enter Password : ");

        emailGuidelineLabel.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        emailGuidelineLabel.setForeground(new java.awt.Color(255, 51, 51));
        emailGuidelineLabel.setText("   ");

        showPasswordCheck.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        showPasswordCheck.setText("Show Password");
        showPasswordCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showPasswordCheckActionPerformed(evt);
            }
        });

        emailField.setFont(new java.awt.Font("Perpetua", 0, 14)); // NOI18N
        emailField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                emailFieldActionPerformed(evt);
            }
        });

        phoneField.setFont(new java.awt.Font("Perpetua", 0, 14)); // NOI18N
        phoneField.setText("    ");
        phoneField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phoneFieldActionPerformed(evt);
            }
        });

        phoneGuidelineLabel.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        phoneGuidelineLabel.setForeground(new java.awt.Color(255, 51, 51));
        phoneGuidelineLabel.setText("    ");

        dobField.setFont(new java.awt.Font("Perpetua", 0, 14)); // NOI18N
        dobField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dobFieldActionPerformed(evt);
            }
        });

        dobGuidelineLabel.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        dobGuidelineLabel.setForeground(new java.awt.Color(255, 51, 51));
        dobGuidelineLabel.setText("    ");

        passwordField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwordFieldActionPerformed(evt);
            }
        });

        passwordStrengthLabel.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        passwordStrengthLabel.setForeground(new java.awt.Color(255, 51, 51));
        passwordStrengthLabel.setText("    ");

        genderCombo.setFont(new java.awt.Font("Perpetua", 0, 14)); // NOI18N
        genderCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Male", "Female" }));
        genderCombo.setToolTipText("");

        nameField.setFont(new java.awt.Font("Perpetua", 0, 14)); // NOI18N

        javax.swing.GroupLayout formPanelLayout = new javax.swing.GroupLayout(formPanel);
        formPanel.setLayout(formPanelLayout);
        formPanelLayout.setHorizontalGroup(
            formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(formPanelLayout.createSequentialGroup()
                .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(formPanelLayout.createSequentialGroup()
                        .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(formPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, formPanelLayout.createSequentialGroup()
                                .addGap(56, 56, 56)
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(genderCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(nameField)))
                    .addGroup(formPanelLayout.createSequentialGroup()
                        .addGap(56, 56, 56)
                        .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(formPanelLayout.createSequentialGroup()
                                .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(showPasswordCheck, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(formPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(phoneGuidelineLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(phoneField, javax.swing.GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE))))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(formPanelLayout.createSequentialGroup()
                                .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(emailField)
                                    .addComponent(dobField)
                                    .addComponent(dobGuidelineLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(passwordField)
                                    .addComponent(confirmPasswordField)
                                    .addComponent(passwordStrengthLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(emailGuidelineLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))))
                .addContainerGap())
        );
        formPanelLayout.setVerticalGroup(
            formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(formPanelLayout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(genderCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(emailField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(emailGuidelineLabel)
                .addGap(16, 16, 16)
                .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(phoneField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(phoneGuidelineLabel)
                .addGap(12, 12, 12)
                .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(dobField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(dobGuidelineLabel)
                .addGap(9, 9, 9)
                .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addComponent(passwordStrengthLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(confirmPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(showPasswordCheck)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        registerButton.setBackground(new java.awt.Color(153, 153, 255));
        registerButton.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        registerButton.setForeground(new java.awt.Color(255, 255, 255));
        registerButton.setText("Register");
        registerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                registerButtonActionPerformed(evt);
            }
        });

        backButton.setBackground(new java.awt.Color(255, 102, 102));
        backButton.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        backButton.setForeground(new java.awt.Color(255, 255, 255));
        backButton.setText("Back");
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(formPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(registerButton, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(79, 79, 79)
                        .addComponent(backButton, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(formPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(backButton)
                    .addComponent(registerButton))
                .addGap(0, 18, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void showPasswordCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showPasswordCheckActionPerformed
            char echo = showPasswordCheck.isSelected() ? (char) 0 : '●';
            passwordField.setEchoChar(echo);
            confirmPasswordField.setEchoChar(echo);
    }//GEN-LAST:event_showPasswordCheckActionPerformed

    private void emailFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_emailFieldActionPerformed
        validateEmail();
    }//GEN-LAST:event_emailFieldActionPerformed

    private void phoneFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phoneFieldActionPerformed
        validatePhone();
    }//GEN-LAST:event_phoneFieldActionPerformed

    private void dobFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dobFieldActionPerformed
        validateDob();
    }//GEN-LAST:event_dobFieldActionPerformed

    private void passwordFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordFieldActionPerformed
        checkPasswordStrength();
    }//GEN-LAST:event_passwordFieldActionPerformed

    private void registerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_registerButtonActionPerformed
        checkAllValid();
        register();
    }//GEN-LAST:event_registerButtonActionPerformed

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
        dispose();
        LoginGUI login = new LoginGUI();
        login.setLocationRelativeTo(null); // center on screen
        login.setVisible(true);  
    }//GEN-LAST:event_backButtonActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backButton;
    private javax.swing.JPasswordField confirmPasswordField;
    private javax.swing.JTextField dobField;
    private javax.swing.JLabel dobGuidelineLabel;
    private javax.swing.JTextField emailField;
    private javax.swing.JLabel emailGuidelineLabel;
    private javax.swing.JPanel formPanel;
    private javax.swing.JComboBox<String> genderCombo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JTextField nameField;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordStrengthLabel;
    private javax.swing.JTextField phoneField;
    private javax.swing.JLabel phoneGuidelineLabel;
    private javax.swing.JButton registerButton;
    private javax.swing.JCheckBox showPasswordCheck;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
}
