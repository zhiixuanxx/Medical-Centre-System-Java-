package GUI;
import java.awt.Color;
import java.awt.Font;
import model.Doctor;
import model.Manager;
import util.FileStorage;
import util.IDGenerator;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class CrudDoctorPage extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(CrudDoctorPage.class.getName());
    private Manager loggedInManager;
    private DefaultTableModel tableModel;

    /**
     * Creates new form CrudDoctorPage
     * @param manager
     */
    public CrudDoctorPage(Manager manager) {
        this.loggedInManager = manager;
        initComponents();
        initializeForm();

        
        //Customize bg
        getContentPane().setBackground(new java.awt.Color(230, 245, 255));
        jPanel1.setBackground(new java.awt.Color(0, 51, 102));
        
        //Label
        jLabel3.setForeground(Color.WHITE);
        jLabel3.setFont(new Font("Perpetua", Font.PLAIN, 35));
        jLabel1.setFont(new Font("Perpetua", Font.BOLD, 16));
        jLabel2.setFont(new Font("Perpetua", Font.BOLD, 16));
        jLabel4.setFont(new Font("Perpetua", Font.BOLD, 16));
        jLabel5.setFont(new Font("Perpetua", Font.BOLD, 16));
        jLabel6.setFont(new Font("Perpetua", Font.BOLD, 16));
        jLabel7.setFont(new Font("Perpetua", Font.BOLD, 16));
        jLabel8.setFont(new Font("Perpetua", Font.BOLD, 16));
        
        //buttons
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
        resetButton.setBackground(new java.awt.Color(72, 103, 150)); 
        resetButton.setForeground(Color.WHITE);
        resetButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        backButton.setBackground(new java.awt.Color(0, 51, 102)); 
        backButton.setForeground(Color.WHITE);
        backButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        
        
    }
    
    private void initializeForm() {
        // Initialize table model
        tableModel = (DefaultTableModel) doctorTable.getModel();
        tableModel.setColumnIdentifiers(new Object[]{"ID", "Name", "Gender", "Email", "Phone", "Password", "Specialty", "Doctor Type", "DOB"});
        
        // Initialize combo boxes
        genderCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"--Select Gender--", "Male", "Female"}));
        doctorTypeCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Clinical Doctor", "On-call Doctor"}));
        loadSpecialtiesToCombo();
        
        // Set initial ID
        idField.setText(IDGenerator.generateID("doctor"));
        idField.setEditable(false);
        
        // Load docs to table
        loadDoctorsToTable();
        
        // Add selection listener to table
        doctorTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && doctorTable.getSelectedRow() != -1) {
                int row = doctorTable.getSelectedRow();
                idField.setText(tableModel.getValueAt(row, 0).toString());
                nameField.setText(tableModel.getValueAt(row, 1).toString());
                genderCombo.setSelectedItem(tableModel.getValueAt(row, 2).toString());
                emailField.setText(tableModel.getValueAt(row, 3).toString());
                phoneField.setText(tableModel.getValueAt(row, 4).toString());
                passwordField.setText(tableModel.getValueAt(row, 5).toString());
                specialityCombo.setSelectedItem(tableModel.getValueAt(row, 6).toString());
                doctorTypeCombo.setSelectedItem(tableModel.getValueAt(row, 7).toString());
                dobField.setText(tableModel.getValueAt(row, 8).toString());
            }
        });
        
        // Add doctor type change listener
        doctorTypeCombo.addActionListener(e -> {
            String type = (String) doctorTypeCombo.getSelectedItem();
            if ("On-call Doctor".equalsIgnoreCase(type)) {
                specialityCombo.setSelectedItem("Emergency");
                specialityCombo.setEnabled(false);
            } else {
                specialityCombo.setEnabled(true);
            }
        });
    }
    
    
    private void loadSpecialtiesToCombo() {
        specialityCombo.removeAllItems();
        specialityCombo.addItem("-- Select Specialty --");
        for (String s : FileStorage.getAllSpecialties()) {
            specialityCombo.addItem(s);
        }
    }
    
    private void loadDoctorsToTable() {
        tableModel.setRowCount(0);
        List<Doctor> doctors = FileStorage.getAllDoctors();
        for (Doctor d : doctors) {
            tableModel.addRow(new Object[]{
                d.getId(),
                d.getName(),
                d.getGender(),
                d.getEmail(),
                d.getPhone(),
                d.getPassword(),
                d.getSpecialty(),
                d.getDoctorType(),
                d.getDob()
            });
        }
    }
    
    private void clearForm() {
        idField.setText(IDGenerator.generateID("doctor"));
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        passwordField.setText("");
        dobField.setText("");
        specialityCombo.setSelectedIndex(0);
        genderCombo.setSelectedIndex(0);
        doctorTypeCombo.setSelectedIndex(0);
        specialityCombo.setEnabled(true);
    }
    
    private void addDoctor() {
        String name = nameField.getText().trim();
        if (!name.toLowerCase().startsWith("dr.")) {
            name = "Dr. " + name;
        }
        String gender = (String) genderCombo.getSelectedItem();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText().trim();
        String specialty = (String) specialityCombo.getSelectedItem();
        String doctorType = (String) doctorTypeCombo.getSelectedItem();
        String dob = dobField.getText().trim();

        if (name.isEmpty() || "--Select Gender--".equals(gender) || email.isEmpty() ||
                phone.isEmpty() || password.isEmpty() || doctorType == null || dob.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.");
            return;
        }
        
        if (!phone.matches("\\d{10,11}")) {
            JOptionPane.showMessageDialog(this, "Phone number must be 10–11 digits.");
            return;
        }
        
        

        if (!email.contains("@")) {
            email += "@doctor.apu.my";
        }
        email = email.trim().toLowerCase();
        phone = phone.trim();

        if (!name.matches("^[a-zA-Z\\s]+$")) {
            JOptionPane.showMessageDialog(this, "Name must only contain letters and spaces.");
            return;
        }
        //Duplication check
        List<Doctor> doctors = FileStorage.getAllDoctors();
        for (Doctor d : doctors) {
            if (d.getEmail().trim().equalsIgnoreCase(email)) {
                JOptionPane.showMessageDialog(this, "Email already exists. Please use another.");
                return;
            }
            if (d.getPhone().trim().equals(phone)) {
                JOptionPane.showMessageDialog(this, "Phone number already exists. Please use another.");
                return;
            }
        }

        if (!phone.matches("\\d{10,11}")) {
            JOptionPane.showMessageDialog(this, "Phone number must be 10–11 digits.");
            return;
        }
        // Validate DOB strictly
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

        if ("On-call Doctor".equals(doctorType)) {
            specialty = "Emergency";
        } else {
            if (specialty == null || "-- Select Specialty --".equals(specialty)) {
                JOptionPane.showMessageDialog(this, "Specialty is required for Clinical Doctors.");
                return;
            }
        }

        String newId = IDGenerator.generateID("doctor");
        Doctor newDoctor = new Doctor(newId, name, gender, email, phone, password, specialty, doctorType, dob);
        FileStorage.saveDoctor(newDoctor);

        loadDoctorsToTable();
        clearForm();
        JOptionPane.showMessageDialog(this, "Doctor added successfully.");
    }
    
    private void updateDoctor() {
        int selectedRow = doctorTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a doctor to update.");
            return;
        }

        // Retrieve form values
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String gender = (String) genderCombo.getSelectedItem();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText().trim();
        String specialty = (String) specialityCombo.getSelectedItem();
        String doctorType = (String) doctorTypeCombo.getSelectedItem();
        String dob = dobField.getText().trim();

        // --- Basic required field checks ---
        if (name.isEmpty() || "--Select Gender--".equals(gender) || email.isEmpty()
                || phone.isEmpty() || password.isEmpty()
                || doctorType == null || dob.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.");
            return;
        }

        // --- Name validation ---
        if (!name.matches("^[a-zA-Z\\s]+$")) {
            JOptionPane.showMessageDialog(this, "Name must contain only letters and spaces.");
            return;
        }
        if (!name.toLowerCase().startsWith("dr.")) {
            name = "Dr. " + name;
        }

        // --- Email formatting and validation ---
        if (!email.contains("@")) {
            email += "@doctor.apu.my";
        }
        email = email.toLowerCase();
        if (!email.matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            JOptionPane.showMessageDialog(this, "Invalid email format.");
            return;
        }

        // --- Phone format validation ---
        if (!phone.matches("\\d{10,11}")) {
            JOptionPane.showMessageDialog(this, "Phone number must be 10–11 digits.");
            return;
        }

        // --- DOB validation ---
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu")
                .withResolverStyle(ResolverStyle.STRICT);
        LocalDate parsedDob;
        try {
            parsedDob = LocalDate.parse(dob, formatter);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use dd/MM/yyyy.");
            return;
        }
        if (parsedDob.isAfter(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "Date of Birth cannot be in the future.");
            return;
        }

        // --- Password strength check ---
        if (!password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$")) {
            JOptionPane.showMessageDialog(this,
                    "Password must include upper, lower, digit, symbol, and be at least 8 characters long.");
            return;
        }

        // --- Doctor Type / Specialty validation ---
        if ("On-call Doctor".equals(doctorType)) {
            specialty = "Emergency";
        } else if (specialty == null || "-- Select Specialty --".equals(specialty)) {
            JOptionPane.showMessageDialog(this, "Please select a specialty for Clinical Doctors.");
            return;
        }

        // --- Duplication check (exclude current doctor) ---
        for (Doctor d : FileStorage.getAllDoctors()) {
            if (!d.getId().equals(id)) {
                if (d.getEmail().equalsIgnoreCase(email)) {
                    JOptionPane.showMessageDialog(this, "Email already exists for another doctor.");
                    return;
                }
                if (d.getPhone().equals(phone)) {
                    JOptionPane.showMessageDialog(this, "Phone number already exists for another doctor.");
                    return;
                }
            }
        }

        // --- Create updated Doctor object ---
        Doctor updatedDoctor = new Doctor(id, name, gender, email, phone, password, specialty, doctorType, dob);

        // --- Save changes ---
        FileStorage.updateDoctor(updatedDoctor);
        loadDoctorsToTable();
        clearForm();
        JOptionPane.showMessageDialog(this, "Doctor information updated successfully.");
    }

    
    private void deleteDoctor() {
        int selectedRow = doctorTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a doctor to delete.");
            return;
        }

        String id = tableModel.getValueAt(selectedRow, 0).toString();
        String name = tableModel.getValueAt(selectedRow, 1).toString();
        String type = tableModel.getValueAt(selectedRow, 7).toString();

        //Prevent deleting less than 3 On-call Doctors 
        if ("On-call Doctor".equalsIgnoreCase(type)) {
            long oncallCount = FileStorage.getAllDoctors().stream()
                    .filter(d -> "On-call Doctor".equalsIgnoreCase(d.getDoctorType()))
                    .count();

            if (oncallCount <= 3) {
                JOptionPane.showMessageDialog(this,
                        "Cannot delete. At least 3 on-call doctors must remain in the system.");
                return;
            }
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete Doctor: " + name + " (ID: " + id + ")?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            FileStorage.deleteDoctor(id);
            loadDoctorsToTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Doctor deleted successfully.");
        }
    }
    
    private void viewDetail() {
        int selectedRow = doctorTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a doctor to view details.");
            return;
        }

        String detail = "ID: " + tableModel.getValueAt(selectedRow, 0) + "\n"
                + "Name: " + tableModel.getValueAt(selectedRow, 1) + "\n"
                + "Gender: " + tableModel.getValueAt(selectedRow, 2) + "\n"
                + "Email: " + tableModel.getValueAt(selectedRow, 3) + "\n"
                + "Phone: " + tableModel.getValueAt(selectedRow, 4) + "\n"
                + "Password: " + tableModel.getValueAt(selectedRow, 5) + "\n"
                + "Specialty: " + tableModel.getValueAt(selectedRow, 6) + "\n"
                + "Doctor Type: " + tableModel.getValueAt(selectedRow, 7) + "\n"
                + "DOB: " + tableModel.getValueAt(selectedRow, 8);

        JOptionPane.showMessageDialog(this, detail, "Doctor Details", JOptionPane.INFORMATION_MESSAGE);
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
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        idField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        emailField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        genderCombo = new javax.swing.JComboBox<>();
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
        doctorTable = new javax.swing.JTable();
        resetButton = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        specialityCombo = new javax.swing.JComboBox<>();
        doctorTypeCombo = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel3.setText("Doctor Details");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(320, 320, 320)
                .addComponent(jLabel3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(46, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addGap(38, 38, 38))
        );

        jLabel1.setText("ID:");

        idField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                idFieldActionPerformed(evt);
            }
        });

        jLabel2.setText("Name:");

        nameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameFieldActionPerformed(evt);
            }
        });

        jLabel4.setText("Email (no domain name needed):");

        emailField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                emailFieldActionPerformed(evt);
            }
        });

        jLabel5.setText("Gender:");

        genderCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        genderCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                genderComboActionPerformed(evt);
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

        viewDetailButton.setText("View Details");
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

        doctorTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Name", "Gender", "Email", "Phone", "Password", "Speciality", "Doctor Type", "DOB"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        });
        jScrollPane1.setViewportView(doctorTable);

        resetButton.setText("Reset");
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        jLabel9.setText("Speciality:");

        jLabel10.setText("Doctor Type:");

        specialityCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        specialityCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                specialityComboActionPerformed(evt);
            }
        });

        doctorTypeCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        doctorTypeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doctorTypeComboActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(updateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(viewDetailButton, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(resetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(backButton, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
            .addGroup(layout.createSequentialGroup()
                .addGap(82, 82, 82)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(idField)
                    .addComponent(nameField)
                    .addComponent(genderCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(emailField)
                    .addComponent(phoneField)
                    .addComponent(passwordField, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addComponent(dobField)
                    .addComponent(specialityCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(doctorTypeCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(167, 167, 167))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(idField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(genderCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
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
                    .addComponent(jLabel9)
                    .addComponent(specialityCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(doctorTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(dobField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(40, 40, 40)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addButton)
                    .addComponent(updateButton)
                    .addComponent(deleteButton)
                    .addComponent(viewDetailButton)
                    .addComponent(backButton)
                    .addComponent(resetButton))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void idFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_idFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_idFieldActionPerformed

    private void nameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_nameFieldActionPerformed

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

    private void specialityComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_specialityComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_specialityComboActionPerformed

    private void doctorTypeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doctorTypeComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_doctorTypeComboActionPerformed

    private void dobFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dobFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_dobFieldActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        addDoctor();
    }//GEN-LAST:event_addButtonActionPerformed

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        updateDoctor();
    }//GEN-LAST:event_updateButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        deleteDoctor();
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void viewDetailButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewDetailButtonActionPerformed
       viewDetail();
    }//GEN-LAST:event_viewDetailButtonActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        clearForm();
    }//GEN-LAST:event_resetButtonActionPerformed

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
        dispose();
        new ManagerDashboard(loggedInManager).setVisible(true);
    }//GEN-LAST:event_backButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton backButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JTextField dobField;
    private javax.swing.JTable doctorTable;
    private javax.swing.JComboBox<String> doctorTypeCombo;
    private javax.swing.JTextField emailField;
    private javax.swing.JComboBox<String> genderCombo;
    private javax.swing.JTextField idField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField nameField;
    private javax.swing.JTextField passwordField;
    private javax.swing.JTextField phoneField;
    private javax.swing.JButton resetButton;
    private javax.swing.JComboBox<String> specialityCombo;
    private javax.swing.JButton updateButton;
    private javax.swing.JButton viewDetailButton;
    // End of variables declaration//GEN-END:variables
}
