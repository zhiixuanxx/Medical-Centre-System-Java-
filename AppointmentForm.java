package view;

import model.Appointment;
import model.Customer;
import model.Doctor;
import util.FileStorage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class AppointmentForm extends JFrame {
    // Private fields - encapsulation
    private final Customer customer;
    private final JComboBox<String> specialtyComboBox;
    private final JComboBox<Doctor> doctorComboBox;
    private final JComboBox<String> slotComboBox;
    private final JButton bookButton;
    private final JLabel statusLabel;
    
    // Constructor
    public AppointmentForm(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        this.customer = customer;
        this.specialtyComboBox = new JComboBox<>();
        this.doctorComboBox = new JComboBox<>();
        this.slotComboBox = new JComboBox<>();
        this.bookButton = new JButton("Book Appointment");
        this.statusLabel = new JLabel(" ");
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadInitialData();
        setVisible(true);
    }
    
    // Initialize UI components - single responsibility
    private void initializeComponents() {
        setTitle("Book Appointment - " + customer.getName());
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // Style components
        bookButton.setBackground(new Color(0, 120, 215));
        bookButton.setForeground(Color.WHITE);
        bookButton.setFocusPainted(false);
        bookButton.setEnabled(false); // Initially disabled
        
        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Set custom renderer for doctor combo box
        doctorComboBox.setRenderer(new DoctorListCellRenderer());
    }
    
    // Setup layout - single responsibility
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // Title panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Book New Appointment", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 100, 150));
        titlePanel.add(titleLabel);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Specialty selection
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Select Specialty:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(specialtyComboBox, gbc);
        
        // Doctor selection
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Select Doctor:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(doctorComboBox, gbc);
        
        // Slot selection
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Select Time Slot:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(slotComboBox, gbc);
        
        // Status label
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(statusLabel, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            dispose(); // Close current form
            new CustomerDashboard(customer); // Go back to dashboard
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(backButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(bookButton);
        
        // Add panels to frame
        add(titlePanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    // Setup event handlers - single responsibility
    private void setupEventHandlers() {
        specialtyComboBox.addActionListener(e -> handleSpecialtySelection());
        doctorComboBox.addActionListener(e -> handleDoctorSelection());
        slotComboBox.addActionListener(e -> validateForm());
        bookButton.addActionListener(new BookingActionListener());
    }
    
    // Load initial data - single responsibility
    private void loadInitialData() {
        try {
            loadSpecialties();
            clearStatus();
        } catch (Exception e) {
            showError("Error loading specialties: " + e.getMessage());
        }
    }
    
    // Load specialties into combo box
    private void loadSpecialties() {
        specialtyComboBox.removeAllItems();
        specialtyComboBox.addItem("-- Select Specialty --");
        
        List<String> specialties = FileStorage.getAllSpecialties();
        for (String specialty : specialties) {
            specialtyComboBox.addItem(specialty);
        }

    }
    
    // Handle specialty selection
    private void handleSpecialtySelection() {
        clearDoctors();
        clearSlots();
        clearStatus();
        
        String selectedSpecialty = (String) specialtyComboBox.getSelectedItem();
        if (selectedSpecialty != null && !selectedSpecialty.startsWith("--")) {
            try {
                updateDoctorsBySpecialty(selectedSpecialty);
            } catch (Exception e) {
                showError("Error loading doctors: " + e.getMessage());
            }
        }
        validateForm();
    }
    
    private void updateDoctorsBySpecialty(String specialty) {
        List<Doctor> doctors = FileStorage.getDoctorsBySpecialty(specialty);
        doctorComboBox.addItem(null); // Add placeholder

        for (Doctor doctor : doctors) {
            // ✅ Only show clinical doctors
            if ("Clinical".equalsIgnoreCase(doctor.getDoctorType()) || 
                "Clinical Doctor".equalsIgnoreCase(doctor.getDoctorType())) {
                doctorComboBox.addItem(doctor);
            }
        }
    }

    
    // Handle doctor selection
    private void handleDoctorSelection() {
        clearSlots();
        clearStatus();
        
        Doctor selectedDoctor = (Doctor) doctorComboBox.getSelectedItem();
        if (selectedDoctor != null) {
            try {
                updateSlotsForDoctor(selectedDoctor);
            } catch (Exception e) {
                showError("Error loading time slots: " + e.getMessage());
            }
        }
        validateForm();
    }
    
    // Update time slots for selected doctor
    private void updateSlotsForDoctor(Doctor doctor) {
    List<String> slots = FileStorage.getAvailableSlotsByDoctorId(doctor.getId());
    slotComboBox.addItem("-- Select Time Slot --");

    LocalDateTime now = LocalDateTime.now();
    
    boolean anyFuture = false;
    for (String slot : slots) {
        try {
            // Parse the slot format: "DD-MM-YYYY HH:MM" 
            String[] parts = slot.split(" ");
            if (parts.length == 2) {
                String dateStr = parts[0]; // DD-MM-YYYY
                String timeStr = parts[1]; // HH:MM
                
                // Convert DD-MM-YYYY to LocalDate
                String[] dateParts = dateStr.split("-");
                if (dateParts.length == 3) {
                    int year = Integer.parseInt(dateParts[0]);
                    int month = Integer.parseInt(dateParts[1]);
                    int day = Integer.parseInt(dateParts[2]);

                    LocalDate slotDate = LocalDate.parse(dateStr); 

                    
                    // Parse time
                    String[] timeParts = timeStr.split(":");
                    if (timeParts.length == 2) {
                        int hour = Integer.parseInt(timeParts[0]);
                        int minute = Integer.parseInt(timeParts[1]);
                        
                        LocalDateTime slotDateTime = slotDate.atTime(hour, minute);
                        
                        // Only add future slots
                        if (slotDateTime.isAfter(now)) {
                            // Format for display: "DD/MM/YYYY at HH:MM"
                            String displayFormat = String.format("%02d/%02d/%d at %s", 
                                    day, month, year, timeStr);
                            // Store only the original slot format for processing (no duplicate)
                            slotComboBox.addItem(displayFormat);
                            anyFuture = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing slot: " + slot + " - " + e.getMessage());
        }
    }

    if (!anyFuture) {
        showError("No available future slots for this doctor.");
    }
}


    
    // Validate form and enable/disable book button
    private void validateForm() {
        boolean isValid = specialtyComboBox.getSelectedItem() != null &&
                         !specialtyComboBox.getSelectedItem().toString().startsWith("--") &&
                         doctorComboBox.getSelectedItem() != null &&
                         slotComboBox.getSelectedItem() != null &&
                         !slotComboBox.getSelectedItem().toString().startsWith("--");
        
        bookButton.setEnabled(isValid);
        
        if (isValid) {
            clearStatus();
        }
    }
    
    // Inner class for booking action - encapsulation
    private class BookingActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                handleBooking();
            } catch (Exception ex) {
                showError("Booking failed: " + ex.getMessage());
            }
        }
    }
    
    // Handle appointment booking
    // Fixed handleBooking method in AppointmentForm.java
private void handleBooking() {
    Doctor selectedDoctor = (Doctor) doctorComboBox.getSelectedItem();
    String selectedDisplaySlot = (String) slotComboBox.getSelectedItem();
    
    if (!validateBookingData(selectedDoctor, selectedDisplaySlot)) {
        return;
    }
    
    if (selectedDisplaySlot.startsWith("--")) {
        showError("Please select a valid time slot.");
        return;
    }
    
    // Parse the display format "DD/MM/YYYY at HH:MM" back to original format
    try {
        // Extract date and time from display format
        String[] displayParts = selectedDisplaySlot.split(" at ");
        if (displayParts.length != 2) {
            showError("Invalid slot format.");
            return;
        }
        
        String displayDate = displayParts[0]; // DD/MM/YYYY
        String time = displayParts[1];        // HH:MM
        
        // Convert DD/MM/YYYY to DD-MM-YYYY for processing
        String dateInDDMMYYYY = displayDate.replace("/", "-");
        
        // Convert DD-MM-YYYY to YYYY-MM-DD for schedule removal
        String[] dateParts = dateInDDMMYYYY.split("-");
        String scheduleDate = String.format("%s-%02d-%02d", 
                dateParts[2], 
                Integer.parseInt(dateParts[1]), 
                Integer.parseInt(dateParts[0]));
        
        // Create appointment with DD/MM/YYYY format for storage
        String appointmentDate = displayDate;
        
        // Create appointment
        String appointmentId = FileStorage.generateAppointmentID();
        Appointment appointment = new Appointment(
            appointmentId,
            customer.getId(),
            customer.getName(),
            selectedDoctor.getId(),
            selectedDoctor.getName(),
            appointmentDate, // Store in DD/MM/YYYY format
            time,
            "Upcoming",
            "Unpaid"
        );
        
        // Save appointment and update availability
        FileStorage.writeAppointment(appointment);
        FileStorage.removeSlot(selectedDoctor.getId(), scheduleDate, time); // Use YYYY-MM-DD for schedule
        
        showSuccess("Appointment booked successfully!\nAppointment ID: " + appointmentId);
        dispose();
        new CustomerDashboard(customer).setVisible(true);
        
    } catch (Exception e) {
        showError("Error processing appointment: " + e.getMessage());
        e.printStackTrace();
    }
}

    
    // Validate booking data
    private boolean validateBookingData(Doctor doctor, String slot) {
        if (doctor == null) {
            showError("Please select a doctor.");
            return false;
        }
        
        if (slot == null || slot.isEmpty() || slot.startsWith("--")) {
            showError("Please select a time slot.");
            return false;
        }
        
        return true;
    }
    
    // UI helper methods - encapsulation
    private void clearDoctors() {
        doctorComboBox.removeAllItems();
        doctorComboBox.addItem(null); // Add placeholder
    }
    
    private void clearSlots() {
        slotComboBox.removeAllItems();
    }
    
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(Color.RED);
    }
    
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void clearStatus() {
        statusLabel.setText(" ");
    }
    
    // Custom renderer for doctor combo box
    private static class DoctorListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value == null) {
                setText("-- Select Doctor --");
                setForeground(Color.GRAY);
            } else if (value instanceof Doctor) {
                Doctor doctor = (Doctor) value;
                setText(doctor.getName());
            }
            
            return this;
        }
    }
}