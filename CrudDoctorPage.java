package view;

import model.Doctor;
import model.Manager;
import util.FileStorage;
import util.IDGenerator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CrudDoctorPage extends JFrame {
    private JTable doctorTable;
    private DefaultTableModel tableModel;

    private JTextField idField, nameField, emailField, phoneField, passwordField, dobField;
    private JComboBox<String> specialtyCombo, genderCombo, doctorTypeCombo;
    private JButton addButton, updateButton, deleteButton, backButton, viewDetailButton;

    private Manager loggedInManager;

    public CrudDoctorPage(Manager manager) {
        this.loggedInManager = manager;

        setTitle("CRUD Doctor Page");
        setSize(950, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== Table =====
        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Name", "Gender", "Email", "Phone", "Password", "Specialty", "Doctor Type", "DOB"}, 0
        );
        doctorTable = new JTable(tableModel);
        loadDoctorsToTable();

        JScrollPane scrollPane = new JScrollPane(doctorTable);

        // ===== Form Fields =====
        JPanel formPanel = new JPanel(new GridLayout(9, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Doctor Details"));

        idField = new JTextField(IDGenerator.generateID("doctor"));
        idField.setEditable(false);

        nameField = new JTextField();
        emailField = new JTextField();
        phoneField = new JTextField();
        passwordField = new JTextField();
        dobField = new JTextField();

        genderCombo = new JComboBox<>(new String[]{"--Select Gender--","Male", "Female"});
        specialtyCombo = new JComboBox<>();
        loadSpecialtiesToCombo();

        doctorTypeCombo = new JComboBox<>(new String[]{"Clinical Doctor", "On-call Doctor"});

        formPanel.add(new JLabel("ID:"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Gender:"));
        formPanel.add(genderCombo);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Phone:"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);
        formPanel.add(new JLabel("Specialty:"));
        formPanel.add(specialtyCombo);
        formPanel.add(new JLabel("Doctor Type:"));
        formPanel.add(doctorTypeCombo);
        formPanel.add(new JLabel("DOB:"));
        formPanel.add(dobField);

        // ===== Buttons =====
        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        viewDetailButton = new JButton("View Detail");
        JButton resetButton = new JButton("Reset");
        backButton = new JButton("Back");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(viewDetailButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(backButton);

        add(scrollPane, BorderLayout.CENTER);
        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());

        // ===== Events =====
        doctorTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && doctorTable.getSelectedRow() != -1) {
                int row = doctorTable.getSelectedRow();
                idField.setText(tableModel.getValueAt(row, 0).toString());
                nameField.setText(tableModel.getValueAt(row, 1).toString());
                genderCombo.setSelectedItem(tableModel.getValueAt(row, 2).toString());
                emailField.setText(tableModel.getValueAt(row, 3).toString());
                phoneField.setText(tableModel.getValueAt(row, 4).toString());
                passwordField.setText(tableModel.getValueAt(row, 5).toString());
                specialtyCombo.setSelectedItem(tableModel.getValueAt(row, 6).toString());
                doctorTypeCombo.setSelectedItem(tableModel.getValueAt(row, 7).toString());
                dobField.setText(tableModel.getValueAt(row, 8).toString());
            }
        });

        addButton.addActionListener(e -> addDoctor());
        updateButton.addActionListener(e -> updateDoctor());
        deleteButton.addActionListener(e -> deleteDoctor());
        viewDetailButton.addActionListener(e -> viewDetail());
        resetButton.addActionListener(e -> clearForm());
        backButton.addActionListener(e -> {
            dispose();
            new ManagerDashboard(loggedInManager);
        });

        // If On-call doctor is chosen, force specialty = Emergency
        doctorTypeCombo.addActionListener(e -> {
            String type = (String) doctorTypeCombo.getSelectedItem();
            if ("On-call Doctor".equalsIgnoreCase(type)) {
                specialtyCombo.setSelectedItem("Emergency");
                specialtyCombo.setEnabled(false);
            } else {
                specialtyCombo.setEnabled(true);
            }
        });

        setVisible(true);
    }

    private void loadSpecialtiesToCombo() {
        specialtyCombo.removeAllItems();
        specialtyCombo.addItem("-- Select Specialty --");
        for (String s : FileStorage.getAllSpecialties()) {
            specialtyCombo.addItem(s);
        }
        specialtyCombo.addItem("Emergency"); // ensure Emergency is always available
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
        String specialty = (String) specialtyCombo.getSelectedItem();
        String doctorType = (String) doctorTypeCombo.getSelectedItem();
        String dob = dobField.getText().trim();

        if (name.isEmpty() || gender.isEmpty() || email.isEmpty() ||
                phone.isEmpty() || password.isEmpty() || doctorType == null || dob.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.");
            return;
        }

        if (!email.contains("@")) {
            email += "@doctor.apu.my";
        }
        email = email.trim().toLowerCase();
        phone = phone.trim();

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
        if (!dob.matches("\\d{2}/\\d{2}/\\d{4}")) {
            JOptionPane.showMessageDialog(this, "DOB must be in format dd/MM/yyyy.");
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
            if (specialty == null || specialty.equals("-- Select Specialty --")) {
                JOptionPane.showMessageDialog(this, "Specialty is required for Clinical Doctors.");
                return;
            }
        }

        String newId = IDGenerator.generateID("doctor");
        Doctor newDoctor = new Doctor(newId, name, gender, email, phone, password, specialty, doctorType, dob);
        FileStorage.saveDoctor(newDoctor);

        loadDoctorsToTable();
        clearForm();
        idField.setText(IDGenerator.generateID("doctor"));
        JOptionPane.showMessageDialog(this, "Doctor added successfully.");
    }

    private void updateDoctor() {
        int selectedRow = doctorTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a doctor to update.");
            return;
        }

        String id = idField.getText();
        String name = nameField.getText().trim();
        if (!name.toLowerCase().startsWith("dr.")) {
        name = "Dr. " + name;
        }
        String gender = (String) genderCombo.getSelectedItem();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText().trim();
        String specialty = (String) specialtyCombo.getSelectedItem();
        String doctorType = (String) doctorTypeCombo.getSelectedItem();
        String dob = dobField.getText().trim();

        if (!email.contains("@")) {
            email += "@doctor.apu.my";
        }
        email = email.trim().toLowerCase();
        phone = phone.trim();

        //Duplication check (ignore self)
        List<Doctor> doctors = FileStorage.getAllDoctors();
        for (Doctor d : doctors) {
            if (!d.getId().equals(id)) {
                if (d.getEmail().trim().equalsIgnoreCase(email)) {
                    JOptionPane.showMessageDialog(this, "Email already exists.");
                    return;
                }
                if (d.getPhone().trim().equals(phone)) {
                    JOptionPane.showMessageDialog(this, "Phone already exists.");
                    return;
                }
            }
        }

        if ("On-call Doctor".equals(doctorType)) {
            specialty = "Emergency";
        }

        Doctor updatedDoctor = new Doctor(id, name, gender, email, phone, password, specialty, doctorType, dob);
        FileStorage.updateDoctor(updatedDoctor);

        loadDoctorsToTable();
        clearForm();
        JOptionPane.showMessageDialog(this, "Doctor updated successfully.");
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

        //Prevent deleting if fewer than 3 On-call Doctors remain
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

    private void clearForm() {
        idField.setText(IDGenerator.generateID("doctor"));
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        passwordField.setText("");
        dobField.setText("");
        specialtyCombo.setSelectedIndex(0);
        genderCombo.setSelectedIndex(0);
        doctorTypeCombo.setSelectedIndex(0);

        SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());
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
}
