package GUI;
import java.awt.Color;
import java.awt.Font;
import model.Customer;
import model.Staff;
import util.FileStorage;
import util.IDGenerator;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class CrudCustomerPage extends javax.swing.JFrame {
    private final Staff loggedInStaff;
    private DefaultTableModel tableModel;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(CrudCustomerPage.class.getName());

    /**
     * Creates new form CrudCustomerPage
     * @param staff
     */
    public CrudCustomerPage(Staff staff) {
        this.loggedInStaff = staff;
        initComponents();
        setLocationRelativeTo(null);
        setResizable(false);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new java.awt.Color(230, 245, 255));
        //Panel
        jPanel1.setBackground(new java.awt.Color(0, 51, 102));
        
        //Label
        jLabel1.setForeground(Color.WHITE);
        jLabel1.setFont(new Font("Perpetua", Font.PLAIN, 35));
        
        //Buttons
        addButton.setBackground(new java.awt.Color(72, 103, 150)); 
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        updateButton.setBackground(new java.awt.Color(72, 103, 150)); 
        updateButton.setForeground(Color.WHITE);
        updateButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        viewDetailButton.setBackground(new java.awt.Color(72, 103, 150)); 
        viewDetailButton.setForeground(Color.WHITE);
        viewDetailButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        deleteButton.setBackground(new java.awt.Color(72, 103, 150)); 
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        backButton.setBackground(new java.awt.Color(0, 51, 102)); 
        backButton.setForeground(Color.WHITE);
        backButton.setFont(new Font("Perpetua", Font.BOLD, 17));
        
        
        // Initialize table model
        tableModel = (DefaultTableModel) customerTable.getModel();
        loadCustomersToTable();
        
        // Set auto-generated ID
        idField.setText(IDGenerator.generateID("customer"));
        idField.setEditable(false);
        
        // Set gender combo box items
        genderCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Male", "Female"}));
        
        // Add table selection listener
        customerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && customerTable.getSelectedRow() != -1) {
                int row = customerTable.getSelectedRow();
                idField.setText(tableModel.getValueAt(row, 0).toString());
                nameField.setText(tableModel.getValueAt(row, 1).toString());
                genderCombo.setSelectedItem(tableModel.getValueAt(row, 2).toString());
                emailField.setText(tableModel.getValueAt(row, 3).toString());
                phoneField.setText(tableModel.getValueAt(row, 4).toString());
                passwordField.setText(tableModel.getValueAt(row, 5).toString());
                dobField.setText(tableModel.getValueAt(row, 6).toString());
            }
        });
    }
    
    private void addCustomer() {
        String name = nameField.getText().trim();
        String gender = (String) genderCombo.getSelectedItem();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText().trim();
        String dob = dobField.getText().trim();

        if (name.isEmpty() || gender.isEmpty() || email.isEmpty() ||
                phone.isEmpty() || password.isEmpty() || dob.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.");
            return;
        }
        
        if (!name.matches("^[a-zA-Z\\s]+$")) {
            JOptionPane.showMessageDialog(this, "Name must only contain letters and spaces.");
            return;
        }

        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            JOptionPane.showMessageDialog(this, "Invalid email format. Please enter a valid email (e.g., maria@gmail.com).");
            return;
        }
        
        if (!phone.matches("\\d{10,11}")) {
                    JOptionPane.showMessageDialog(this, "Phone number must be 10–11 digits.");
                    return;
                }
        
        
        if (FileStorage.isEmailOrPhoneDuplicate(email, phone)) {
            JOptionPane.showMessageDialog(this, "Email or phone is already registered.");
            return;
        }

        
        // DOB strict validation
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu")
                                                       .withResolverStyle(ResolverStyle.STRICT);
        try {
            LocalDate parsedDob = LocalDate.parse(dob, formatter);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid DOB. Please enter a valid date in format dd/MM/yyyy.");
            return;
        }
        if (!password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$")) {
            JOptionPane.showMessageDialog(this,
                    "Password must have upper, lower, digit, special char, min 8 chars.");
            return;
        }

        String newId = IDGenerator.generateID("customer");
        Customer newCustomer = new Customer(newId, name, gender, email, phone, password, dob);
        FileStorage.saveCustomer(newCustomer);

        loadCustomersToTable();
        clearForm();
        JOptionPane.showMessageDialog(this, "Customer added successfully.");
    }
    
    private void updateCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a customer to update.");
            return;
        }

        String id = idField.getText();
        String name = nameField.getText().trim();
        String gender = (String) genderCombo.getSelectedItem();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText().trim();
        String dob = dobField.getText().trim();

        if (name.isEmpty() || gender.isEmpty() || email.isEmpty() ||
            phone.isEmpty() || password.isEmpty() || dob.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.");
            return;
        }
        
        if (!name.matches("^[a-zA-Z\\s]+$")) {
            JOptionPane.showMessageDialog(this, "Name must only contain letters and spaces.");
            return;
        }
        
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            JOptionPane.showMessageDialog(this, "Invalid email format. Please enter a valid email (e.g., maria@gmail.com).");
            return;
        }
        
        if (!phone.matches("\\d{10,11}")) {
            JOptionPane.showMessageDialog(this, "Phone number must be 10–11 digits.");
            return;
        }
        
        if (FileStorage.isEmailDuplicateForUpdate(email, id)) {
            JOptionPane.showMessageDialog(this, "This email is already registered by another user.");
            return;
        }
        
        if (!password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$")) {
            JOptionPane.showMessageDialog(this,
                    "Password must contain uppercase, lowercase, number, special character, and be at least 8 characters long.");
            return;
        }
        // Custom phone duplication check
        for (Customer c : FileStorage.getAllCustomers()) {
            if (!c.getId().equals(id) && c.getPhone().equals(phone)) {
                JOptionPane.showMessageDialog(this, "This phone number is already registered by another user.");
                return;
            }
        }
        
        // DOB strict validation
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu")
                                                       .withResolverStyle(ResolverStyle.STRICT);
        try {
            LocalDate parsedDob = LocalDate.parse(dob, formatter);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid DOB. Please enter a valid date in format dd/MM/yyyy.");
            return;
        }

        Customer updatedCustomer = new Customer(id, name, gender, email, phone, password, dob);
        FileStorage.updateCustomer(updatedCustomer);

        loadCustomersToTable();
        clearForm();
        JOptionPane.showMessageDialog(this, "Customer updated successfully.");
    }
    
    private void deleteCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a customer to delete.");
            return;
        }

        String id = tableModel.getValueAt(selectedRow, 0).toString();
        String name = tableModel.getValueAt(selectedRow, 1).toString();
        
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete customer: " + name + " (" + id + ")?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            FileStorage.deleteCustomer(id);
            loadCustomersToTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Customer deleted successfully.");
        }
    }
    
    
    private void viewDetail() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer to view details.");
            return;
        }

        String id = tableModel.getValueAt(selectedRow, 0).toString();
        String name = tableModel.getValueAt(selectedRow, 1).toString();

        String detail = "ID: " + tableModel.getValueAt(selectedRow, 0) + "\n"
                + "Name: " + tableModel.getValueAt(selectedRow, 1) + "\n"
                + "Gender: " + tableModel.getValueAt(selectedRow, 2) + "\n"
                + "Email: " + tableModel.getValueAt(selectedRow, 3) + "\n"
                + "Phone: " + tableModel.getValueAt(selectedRow, 4) + "\n"
                + "Password: " + tableModel.getValueAt(selectedRow, 5) + "\n"
                + "DOB: " + tableModel.getValueAt(selectedRow, 6);

        //If this is a Walk-in Patient, also show emergency_details.txt data
        if ("Walk-in Patient".equalsIgnoreCase(name.trim())) {
            StringBuilder emergencyInfo = new StringBuilder("\n\n--- Emergency Details ---\n");

            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader("emergency_details.txt"))) {
                String line;
                boolean found = false;
                while ((line = br.readLine()) != null) {
                    // File format: appointmentId,bedNo,timestamp
                    String[] parts = line.split(",");
                    if (parts.length >= 3) {
                        String apptId = parts[0];
                        String bedNo = parts[1];
                        String timestamp = parts[2];

                        // cross-check if this appointment belongs to the Walk-in patient
                        // by scanning appointments.txt
                        for (String apptLine : java.nio.file.Files.readAllLines(java.nio.file.Paths.get("appointments.txt"))) {
                            if (apptLine.startsWith(apptId + "," + id + ",")) {
                                emergencyInfo.append("Appointment ID: ").append(apptId)
                                        .append("\nBed Number: ").append(bedNo)
                                        .append("\nTimestamp: ").append(timestamp)
                                        .append("\n\n");
                                found = true;
                            }
                        }
                    }
                }
                if (!found) {
                    emergencyInfo.append("No emergency records found.\n");
                }
            } catch (Exception ex) {
                emergencyInfo.append("Error reading emergency_details.txt: ").append(ex.getMessage());
            }

            detail += emergencyInfo.toString();
        }

        JOptionPane.showMessageDialog(this, detail, "Customer Details", JOptionPane.INFORMATION_MESSAGE);
    }

    
    
    
    private void clearForm() {
        idField.setText(IDGenerator.generateID("customer"));
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        passwordField.setText("");
        dobField.setText("");
        genderCombo.setSelectedIndex(0);
    }
    
    
    private void loadCustomersToTable() {
        tableModel.setRowCount(0);
        List<Customer> customerList = FileStorage.getAllCustomers();
        for (Customer c : customerList) {
            tableModel.addRow(new Object[]{
                    c.getId(),
                    c.getName(),
                    c.getGender(),
                    c.getEmail(),
                    c.getPhone(),
                    c.getPassword(),
                    c.getDob()
            });
        }
    }




    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        idField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        genderCombo = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        emailField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        phoneField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        passwordField = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        dobField = new javax.swing.JTextField();
        addButton = new javax.swing.JButton();
        updateButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        viewDetailButton = new javax.swing.JButton();
        backButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        customerTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Customer Details");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(263, 263, 263)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 377, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(45, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(42, 42, 42))
        );

        jLabel2.setText("ID:");

        jLabel3.setText("Name:");

        jLabel4.setText("Gender:");

        genderCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        genderCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                genderComboActionPerformed(evt);
            }
        });

        jLabel5.setText("Email:");

        emailField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                emailFieldActionPerformed(evt);
            }
        });

        jLabel6.setText("Phone:");

        phoneField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phoneFieldActionPerformed(evt);
            }
        });

        jLabel7.setText("Password:");

        passwordField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwordFieldActionPerformed(evt);
            }
        });

        jLabel8.setText("DOB (dd/mm/yyyy):");

        dobField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dobFieldActionPerformed(evt);
            }
        });

        addButton.setText("Add");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        updateButton.setText("Update");
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });

        deleteButton.setText("Delete");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        viewDetailButton.setText("View Detail");
        viewDetailButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewDetailButtonActionPerformed(evt);
            }
        });

        backButton.setText("Back");
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });

        customerTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Name", "Gender", "Email", "Phone", "Password", "DOB"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        });
        jScrollPane1.setViewportView(customerTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE))
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(76, 76, 76)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(idField, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
                            .addComponent(nameField)
                            .addComponent(genderCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(emailField)
                            .addComponent(phoneField)
                            .addComponent(passwordField)
                            .addComponent(dobField))
                        .addGap(46, 46, 46))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(33, 33, 33)
                        .addComponent(updateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(36, 36, 36)
                        .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(35, 35, 35)
                        .addComponent(viewDetailButton, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addComponent(backButton, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(59, Short.MAX_VALUE))
            .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(idField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(genderCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(emailField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(phoneField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(dobField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(47, 47, 47)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addButton)
                    .addComponent(updateButton)
                    .addComponent(deleteButton)
                    .addComponent(viewDetailButton)
                    .addComponent(backButton))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
       addCustomer();
    }//GEN-LAST:event_addButtonActionPerformed

    private void viewDetailButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewDetailButtonActionPerformed
        viewDetail();
    }//GEN-LAST:event_viewDetailButtonActionPerformed

    private void dobFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dobFieldActionPerformed

        
    }//GEN-LAST:event_dobFieldActionPerformed

    private void genderComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_genderComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_genderComboActionPerformed

    private void emailFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_emailFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_emailFieldActionPerformed

    private void phoneFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phoneFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_phoneFieldActionPerformed

    private void passwordFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_passwordFieldActionPerformed

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        updateCustomer();
    }//GEN-LAST:event_updateButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        deleteCustomer();
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
        dispose();
        new StaffDashboard(loggedInStaff).setVisible(true);
    }//GEN-LAST:event_backButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton backButton;
    private javax.swing.JTable customerTable;
    private javax.swing.JButton deleteButton;
    private javax.swing.JTextField dobField;
    private javax.swing.JTextField emailField;
    private javax.swing.JComboBox<String> genderCombo;
    private javax.swing.JTextField idField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField nameField;
    private javax.swing.JTextField passwordField;
    private javax.swing.JTextField phoneField;
    private javax.swing.JButton updateButton;
    private javax.swing.JButton viewDetailButton;
    // End of variables declaration//GEN-END:variables
}
