package view;

import model.Customer;
import model.Staff;
import util.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CrudCustomerPage extends JFrame {
    private JTable customerTable;
    private DefaultTableModel tableModel;

    private JTextField idField, nameField, emailField, phoneField, passwordField, dobField;
    private JButton addButton, updateButton, deleteButton, backButton, viewDetailButton;
    private JComboBox<String> genderCombo;

    private Staff loggedInStaff;

    public CrudCustomerPage(Staff staff) {
        this.loggedInStaff = staff;

        setTitle("CRUD Customer Page");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== Table =====
        tableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Gender", "Email", "Phone", "Password", "DOB"}, 0);
        customerTable = new JTable(tableModel);
        loadCustomersToTable();

        JScrollPane scrollPane = new JScrollPane(customerTable);

        // ===== Form Fields =====
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Customer Details"));

        idField = new JTextField(IDGenerator.generateID("customer"));
        idField.setEditable(false);

        nameField = new JTextField();
        emailField = new JTextField();
        phoneField = new JTextField();
        passwordField = new JTextField();
        dobField = new JTextField();

        genderCombo = new JComboBox<>(new String[]{"Male", "Female"});

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
        formPanel.add(new JLabel("DOB:"));
        formPanel.add(dobField);

        // ===== Buttons =====
        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        viewDetailButton = new JButton("View Detail");
        backButton = new JButton("Back");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(viewDetailButton);
        buttonPanel.add(backButton);

        // ===== Layout =====
        add(scrollPane, BorderLayout.CENTER);
        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());

        // ===== Events =====
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

        addButton.addActionListener(e -> addCustomer());
        updateButton.addActionListener(e -> updateCustomer());
        deleteButton.addActionListener(e -> deleteCustomer());
        viewDetailButton.addActionListener(e -> viewDetail());
        backButton.addActionListener(e -> {
            dispose();
            new StaffDashboard(loggedInStaff); // staff goes back to staff dashboard
        });

        setVisible(true);
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

        if (!email.contains("@")) {
            email += "@customer.apu.my";
        }

        if (FileStorage.isEmailOrPhoneDuplicate(email, phone)) {
            JOptionPane.showMessageDialog(this, "Email or phone is already registered.");
            return;
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

        // ✅ Duplication check excluding current customer
        if (FileStorage.isEmailDuplicateForUpdate(email, id)) {
            JOptionPane.showMessageDialog(this, "This email is already registered by another user.");
            return;
        }
        // Custom phone duplication check
        for (Customer c : FileStorage.getAllCustomers()) {
            if (!c.getId().equals(id) && c.getPhone().equals(phone)) {
                JOptionPane.showMessageDialog(this, "This phone number is already registered by another user.");
                return;
            }
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

        // ✅ Show confirmation dialog
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

        String detail = "ID: " + tableModel.getValueAt(selectedRow, 0) + "\n"
                + "Name: " + tableModel.getValueAt(selectedRow, 1) + "\n"
                + "Gender: " + tableModel.getValueAt(selectedRow, 2) + "\n"
                + "Email: " + tableModel.getValueAt(selectedRow, 3) + "\n"
                + "Phone: " + tableModel.getValueAt(selectedRow, 4) + "\n"
                + "Password: " + tableModel.getValueAt(selectedRow, 5) + "\n"
                + "DOB: " + tableModel.getValueAt(selectedRow, 6);

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

        SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());
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
}
