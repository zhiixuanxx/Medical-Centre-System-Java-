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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AppointmentFormForStaff extends JFrame {

    private final Staff loggedInStaff;

    private JTextField searchField;
    private JButton searchButton;
    private JTable customerTable;
    private DefaultTableModel customerTableModel;

    private JComboBox<String> specialtyComboBox;
    private JComboBox<Doctor> doctorComboBox;
    private JComboBox<String> slotComboBox;
    private JButton bookButton;
    private JLabel statusLabel;

    private Customer selectedCustomer;

    public AppointmentFormForStaff(Staff staff) {
        this.loggedInStaff = staff;

        setTitle("Assist Customer - Book Appointment");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadAllCustomers();
        loadSpecialties();

        setVisible(true);
    }

    private void initializeComponents() {
        searchField = new JTextField(20);
        searchButton = new JButton("Search");

        customerTableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Email", "Phone"}, 0);
        customerTable = new JTable(customerTableModel);
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        specialtyComboBox = new JComboBox<>();
        doctorComboBox = new JComboBox<>();
        slotComboBox = new JComboBox<>();

        bookButton = new JButton("Book Appointment");
        bookButton.setEnabled(false);

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void setupLayout() {
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search Customer:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Customer table (left side)
        JScrollPane customerScroll = new JScrollPane(customerTable);
        customerScroll.setBorder(BorderFactory.createTitledBorder("Select Customer"));

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(searchPanel, BorderLayout.NORTH);
        leftPanel.add(customerScroll, BorderLayout.CENTER);

        // Appointment details (right side)
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Appointment Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; rightPanel.add(new JLabel("Specialty:"), gbc);
        gbc.gridx = 1; rightPanel.add(specialtyComboBox, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; rightPanel.add(new JLabel("Doctor:"), gbc);
        gbc.gridx = 1; rightPanel.add(doctorComboBox, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; rightPanel.add(new JLabel("Time Slot:"), gbc);
        gbc.gridx = 1; rightPanel.add(slotComboBox, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; rightPanel.add(statusLabel, gbc);

        // Split
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.5);

        // Bottom buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            dispose();
            new StaffDashboard(loggedInStaff);
        });
        buttonPanel.add(backButton);
        buttonPanel.add(bookButton);

        add(splitPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        searchButton.addActionListener(e -> searchCustomers());
        customerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && customerTable.getSelectedRow() != -1) {
                int row = customerTable.getSelectedRow();
                String id = customerTableModel.getValueAt(row, 0).toString();
                selectedCustomer = FileStorage.getCustomerById(id);
                statusLabel.setText("Selected: " + selectedCustomer.getName());
                validateForm();
            }
        });

        specialtyComboBox.addActionListener(e -> handleSpecialtySelection());
        doctorComboBox.addActionListener(e -> handleDoctorSelection());
        slotComboBox.addActionListener(e -> validateForm());
        bookButton.addActionListener(e -> handleBooking());
    }

    private void loadSpecialties() {
        specialtyComboBox.addItem("-- Select Specialty --");
        for (String specialty : FileStorage.getAllSpecialties()) {
            specialtyComboBox.addItem(specialty);
        }
    }

    private void searchCustomers() {
        String keyword = searchField.getText().trim().toLowerCase();
        List<Customer> customers = FileStorage.getAllCustomers();

        List<Customer> filtered = customers.stream()
                .filter(c -> c.getId().toLowerCase().contains(keyword)
                        || c.getName().toLowerCase().contains(keyword)
                        || c.getEmail().toLowerCase().contains(keyword)
                        || c.getPhone().toLowerCase().contains(keyword))
                .collect(Collectors.toList());

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

    private void handleSpecialtySelection() {
        doctorComboBox.removeAllItems();
        slotComboBox.removeAllItems();
        String specialty = (String) specialtyComboBox.getSelectedItem();
        if (specialty != null && !specialty.startsWith("--")) {
            for (Doctor d : FileStorage.getClinicalDoctorsBySpecialty(specialty)) { // ✅ Only clinical doctors
                doctorComboBox.addItem(d);
            }
        }
        validateForm();
    }

    private void handleDoctorSelection() {
        slotComboBox.removeAllItems();
        Doctor doctor = (Doctor) doctorComboBox.getSelectedItem();

        if (doctor != null) {
            List<String> rawSlots = FileStorage.getAvailableSlotsByDoctorId(doctor.getId());

            LocalDate today = LocalDate.now();
            java.time.LocalTime nowTime = java.time.LocalTime.now();

            for (String raw : rawSlots) {
                String[] parts = raw.split(" ");
                if (parts.length == 2) {
                    String dateISO = parts[0]; // yyyy-MM-dd
                    String time = parts[1];    // HH:mm

                    LocalDate date = LocalDate.parse(dateISO);
                    java.time.LocalTime slotTime = java.time.LocalTime.parse(time);

                    // ✅ Skip past dates, and also skip earlier times today
                    if (date.isBefore(today)) {
                        continue;
                    }
                    if (date.isEqual(today) && slotTime.isBefore(nowTime)) {
                        continue;
                    }

                    String displayDate = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    slotComboBox.addItem(displayDate + " at " + time);
                }
            }
        }
        validateForm();
    }

    private void validateForm() {
        boolean isValid = selectedCustomer != null &&
                specialtyComboBox.getSelectedItem() != null &&
                doctorComboBox.getSelectedItem() != null &&
                slotComboBox.getSelectedItem() != null;
        bookButton.setEnabled(isValid);
    }

    private void handleBooking() {
        if (selectedCustomer == null) {
            statusLabel.setText("Please select a customer first.");
            return;
        }

        Doctor selectedDoctor = (Doctor) doctorComboBox.getSelectedItem();
        String selectedSlot = (String) slotComboBox.getSelectedItem();

        if (selectedDoctor == null || selectedSlot == null) {
            statusLabel.setText("Please complete all fields.");
            return;
        }

        String[] parts = selectedSlot.split(" at ");
        String displayDate = parts[0];  // dd/MM/yyyy
        String time = parts[1];         // HH:mm

        String[] dateParts = displayDate.split("/");
        String scheduleDate = String.format("%s-%02d-%02d",
                dateParts[2],
                Integer.parseInt(dateParts[1]),
                Integer.parseInt(dateParts[0]));

        String appointmentId = FileStorage.generateAppointmentID();
        Appointment appointment = new Appointment(
                appointmentId,
                selectedCustomer.getId(),
                selectedCustomer.getName(),
                selectedDoctor.getId(),
                selectedDoctor.getName(),
                displayDate,
                time,
                "Upcoming",
                "Unpaid"
        );

        FileStorage.writeAppointment(appointment);
        FileStorage.removeSlot(selectedDoctor.getId(), scheduleDate, time);

        JOptionPane.showMessageDialog(this,
                "Appointment booked successfully for " + selectedCustomer.getName()
                        + "\nAppointment ID: " + appointmentId,
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
}
