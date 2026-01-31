package view;

import model.Manager;
import util.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CrudManagerPage extends JFrame {
    private JTable managerTable;
    private DefaultTableModel tableModel;

    private JTextField idField, nameField, emailField, phoneField, passwordField, dobField;
    private JButton addButton, updateButton, deleteButton, backButton, viewDetailButton;
    private JComboBox<String> genderCombo;

    private Manager loggedInManager;

    public CrudManagerPage(Manager manager) {
        this.loggedInManager = manager;

        setTitle("CRUD Manager Page");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== Table =====
        tableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Gender", "Email", "Phone", "Password", "DOB"}, 0);
        managerTable = new JTable(tableModel);
        loadManagersToTable();

        JScrollPane scrollPane = new JScrollPane(managerTable);

        // ===== Form Fields =====
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Manager Details"));

        idField = new JTextField(IDGenerator.generateID("manager"));
        idField.setEditable(false);

        nameField = new JTextField();
        emailField = new JTextField();
        phoneField = new JTextField();
        passwordField = new JTextField();
        dobField = new JTextField();

        genderCombo = new JComboBox<>(new String[]{"--Select Gender--", "Male", "Female"});

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
        formPanel.add(new JLabel("DOB(dd/mm/yyyy):"));
        formPanel.add(dobField);

        // ===== Buttons =====
        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        viewDetailButton = new JButton("View Detail");
        backButton = new JButton("Back");
        JButton resetButton = new JButton("Reset"); 

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(viewDetailButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(backButton);

        // ===== Layout =====
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());

        // ===== Events =====
        managerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && managerTable.getSelectedRow() != -1) {
                int row = managerTable.getSelectedRow();
                idField.setText(tableModel.getValueAt(row, 0).toString());
                nameField.setText(tableModel.getValueAt(row, 1).toString());
                genderCombo.setSelectedItem(tableModel.getValueAt(row, 2).toString());
                emailField.setText(tableModel.getValueAt(row, 3).toString());
                phoneField.setText(tableModel.getValueAt(row, 4).toString());
                passwordField.setText(tableModel.getValueAt(row, 5).toString());
                dobField.setText(tableModel.getValueAt(row, 6).toString());
            }
        });

        addButton.addActionListener(e -> addManager());
        updateButton.addActionListener(e -> updateManager());
        deleteButton.addActionListener(e -> deleteManager());
        resetButton.addActionListener(e -> clearForm());

        // NEW: View detail
        viewDetailButton.addActionListener(e -> viewDetail());

        backButton.addActionListener(e -> {
            dispose();
            new ManagerDashboard(loggedInManager);
        });

        setVisible(true);
    }

    private void addManager() {
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

        // Normalize email
        if (!email.contains("@")) {
            email += "@manager.apu.my";
        }
        email = email.trim().toLowerCase();
        phone = phone.trim();

        // 🔑 Duplication check
        List<Manager> managerList = FileStorage.getAllManagers();
        for (Manager m : managerList) {
            if (m.getEmail().trim().equalsIgnoreCase(email)) {
                JOptionPane.showMessageDialog(this, "Email already exists. Please use another.");
                return;
            }
            if (m.getPhone().trim().equals(phone)) {
                JOptionPane.showMessageDialog(this, "Phone number already exists. Please use another.");
                return;
            }
        }

        // Validate format
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

        // Save manager
        String newId = IDGenerator.generateID("manager");
        Manager newManager = new Manager(newId, name, gender, email, phone, password, dob);
        FileStorage.saveManager(newManager);

        loadManagersToTable();
        clearForm();
        JOptionPane.showMessageDialog(this, "Manager added successfully.");
    }

    private void updateManager() {
        int selectedRow = managerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a manager to update.");
            return;
        }

        String id = idField.getText();
        String name = nameField.getText().trim();
        String gender = (String) genderCombo.getSelectedItem();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText().trim();
        String dob = dobField.getText().trim();

        Manager updatedManager = new Manager(id, name, gender, email, phone, password, dob);
        FileStorage.updateManager(updatedManager);

        loadManagersToTable();
        clearForm();
        JOptionPane.showMessageDialog(this, "Manager updated successfully.");
    }

    private void deleteManager() {
        int selectedRow = managerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a manager to delete.");
            return;
        }

        String id = tableModel.getValueAt(selectedRow, 0).toString();
        String name = tableModel.getValueAt(selectedRow, 1).toString();

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete Manager: " + name + " (ID: " + id + ")?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            FileStorage.deleteManager(id);

            loadManagersToTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Manager deleted successfully.");
        }
    }


    private void viewDetail() {
        int selectedRow = managerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a manager to view details.");
            return;
        }

        String detail = "ID: " + tableModel.getValueAt(selectedRow, 0) + "\n"
                + "Name: " + tableModel.getValueAt(selectedRow, 1) + "\n"
                + "Gender: " + tableModel.getValueAt(selectedRow, 2) + "\n"
                + "Email: " + tableModel.getValueAt(selectedRow, 3) + "\n"
                + "Phone: " + tableModel.getValueAt(selectedRow, 4) + "\n"
                + "Password: " + tableModel.getValueAt(selectedRow, 5) + "\n"
                + "DOB: " + tableModel.getValueAt(selectedRow, 6);

        JOptionPane.showMessageDialog(this, detail, "Manager Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearForm() {
        idField.setText(IDGenerator.generateID("manager"));
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        passwordField.setText("");
        dobField.setText("");
        genderCombo.setSelectedIndex(0);

        SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());
    }

    private void loadManagersToTable() {
        tableModel.setRowCount(0);
        List<Manager> managers = FileStorage.getAllManagers();
        for (Manager m : managers) {
            tableModel.addRow(new Object[]{
                    m.getId(),
                    m.getName(),
                    m.getGender(),
                    m.getEmail(),
                    m.getPhone(),
                    m.getPassword(),
                    m.getDob()
            });
        }
    }
}
