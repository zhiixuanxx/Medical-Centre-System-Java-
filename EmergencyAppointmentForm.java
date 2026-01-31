package view;

import model.Appointment;
import model.Customer;
import model.Doctor;
import model.Staff;
import util.FileStorage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class EmergencyAppointmentForm extends JFrame {

    private final Staff loggedInStaff;

    private JTextField searchField;
    private JButton searchButton;
    private JTable customerTable;
    private DefaultTableModel customerTableModel;
    private JLabel statusLabel;
    private JButton confirmButton;

    private Customer selectedCustomer;

    public EmergencyAppointmentForm(Staff staff) {
        this.loggedInStaff = staff;

        setTitle("Emergency Appointment");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadAllCustomers();

        setVisible(true);
    }

    private void initializeComponents() {
        searchField = new JTextField(20);
        searchButton = new JButton("Search");

        customerTableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Email", "Phone"}, 0);
        customerTable = new JTable(customerTableModel);
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        confirmButton = new JButton("Confirm Emergency Appointment");
    }

    private void setupLayout() {
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search Customer (Optional):"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Customer table
        JScrollPane customerScroll = new JScrollPane(customerTable);
        customerScroll.setBorder(BorderFactory.createTitledBorder("Select Customer (Optional)"));

        // Bottom panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton backButton = new JButton("Back");
        JButton resetButton = new JButton("Reset"); 
        backButton.addActionListener(e -> {
            dispose();
            new StaffDashboard(loggedInStaff);
        });
        resetButton.addActionListener(e -> resetForm()); 
        bottomPanel.add(backButton);
        bottomPanel.add(resetButton);
        bottomPanel.add(confirmButton);

        add(searchPanel, BorderLayout.NORTH);
        add(customerScroll, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.PAGE_END);
    }

    private void setupEventHandlers() {
        searchButton.addActionListener(e -> searchCustomers());
        customerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && customerTable.getSelectedRow() != -1) {
                int row = customerTable.getSelectedRow();
                String id = customerTableModel.getValueAt(row, 0).toString();
                selectedCustomer = FileStorage.getCustomerById(id);
                statusLabel.setText("Selected: " + selectedCustomer.getName());
            }
        });

        confirmButton.addActionListener(e -> handleEmergencyBooking());
    }

    private void searchCustomers() {
        String keyword = searchField.getText().trim().toLowerCase();
        List<Customer> customers = FileStorage.getAllCustomers();

        List<Customer> filtered;
        if(keyword.isEmpty()){
            filtered=customers;
        }else{
            filtered = customers.stream()
                .filter(c -> c.getId().toLowerCase().contains(keyword)
                        || c.getName().toLowerCase().contains(keyword)
                        || c.getEmail().toLowerCase().contains(keyword)
                        || c.getPhone().toLowerCase().contains(keyword))
                .collect(Collectors.toList());
        }

        customerTableModel.setRowCount(0);
        for (Customer c : filtered) {
            customerTableModel.addRow(new Object[]{c.getId(), c.getName(), c.getEmail(), c.getPhone()});
        }

        if (filtered.isEmpty()) {
            statusLabel.setText("No customers found.");
        } else {
            statusLabel.setText(filtered.size() + " customers found.");
        }
    }

    private void handleEmergencyBooking() {
        Doctor oncallDoctor = FileStorage.getOncallDoctorForNow();
        if (oncallDoctor == null) {
            JOptionPane.showMessageDialog(this,
                    "No on-call doctor available right now!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Customer bookingCustomer;

        if (selectedCustomer == null) {
            // Auto-register new walk-in with temp credentials
            String newId = util.IDGenerator.generateID("customer"); // you must have or add this in FileStorage
            String name = JOptionPane.showInputDialog(this, "Enter patient name:", "Walk-in Patient");
            if (name == null || name.trim().isEmpty()) name = "Walk-in Patient";

            String email = "walkin" + newId.toLowerCase() + "@apu.my";
            String phone = "0000000000";
            String password = "Temp123!"; // temp password
            String dob = "01/01/1970";    // placeholder DOB

            bookingCustomer = new Customer(newId, name, "Unknown", email, phone, password, dob);
            FileStorage.saveCustomer(bookingCustomer);

            JOptionPane.showMessageDialog(this,
                "Walk-in patient registered!\n\n" +
                "Login credentials:\n" +
                "Email: " + email + "\n" +
                "Password: " + password + "\n\n" +
                "Please advise patient to update profile and change password.",
                "Walk-in Registered", JOptionPane.INFORMATION_MESSAGE);

        } else {
            bookingCustomer = selectedCustomer;
        }

        // Create emergency appointment
        String appointmentId = FileStorage.generateAppointmentID();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        Appointment appointment = new Appointment(
                appointmentId,
                bookingCustomer.getId(),
                bookingCustomer.getName(),
                oncallDoctor.getId(),
                oncallDoctor.getName(),
                today,
                nowTime,
                "Emergency",
                "Unpaid"
        );

        FileStorage.writeAppointment(appointment);

        JOptionPane.showMessageDialog(this,
                "Emergency appointment assigned to " + oncallDoctor.getName()
                        + " for " + bookingCustomer.getName(),
                "Success", JOptionPane.INFORMATION_MESSAGE);

        dispose();
        new StaffDashboard(loggedInStaff);
    }

    private void loadAllCustomers() {
        customerTableModel.setRowCount(0); // clear table
        List<Customer> customers = FileStorage.getAllCustomers();
        for (Customer c : customers) {
            customerTableModel.addRow(new Object[]{
                    c.getId(), c.getName(), c.getEmail(), c.getPhone()
            });
        }
        statusLabel.setText(customers.size() + " customers loaded.");
    }
    
    private void resetForm() {
        searchField.setText("");
        selectedCustomer = null;
        customerTable.clearSelection();
        loadAllCustomers();
        statusLabel.setText("Form reset. All customers loaded.");
    }

}
