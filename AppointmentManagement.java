package view;

import model.Customer;
import model.Appointment;
import util.FileStorage;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.List;
import model.Doctor;

public class AppointmentManagement extends JFrame {
    private final Customer customer;
    private JTable appointmentTable;
    private JButton viewButton, cancelButton, rescheduleButton, refreshButton, backButton;

    public AppointmentManagement(Customer customer) {
        this.customer = customer;
        initializeComponents();
        setupLayout();
        loadAppointments();
        setVisible(true);
    }

    private void initializeComponents() {
        setTitle("Manage Appointments - " + customer.getName());
        setSize(850, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Table
        String[] columns = {"ID", "Doctor", "Specialty", "Date", "Time", "Status"};
        appointmentTable = new JTable(new Object[0][0], columns);
        appointmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Buttons
        viewButton = new JButton("View Details");
        cancelButton = new JButton("Cancel Appointment");
        rescheduleButton = new JButton("Reschedule Appointment");
        refreshButton = new JButton("Refresh");
        backButton = new JButton("Back");

        // Colors
        viewButton.setBackground(new Color(0, 120, 215));
        viewButton.setForeground(Color.WHITE);
        cancelButton.setBackground(new Color(200, 50, 50));
        cancelButton.setForeground(Color.WHITE);
        rescheduleButton.setBackground(new Color(255, 165, 0));
        rescheduleButton.setForeground(Color.WHITE);
        refreshButton.setBackground(new Color(50, 150, 50));
        refreshButton.setForeground(Color.WHITE);
        backButton.setBackground(new Color(102,102,255)); 
        backButton.setForeground(Color.WHITE);            


        // Listeners
        viewButton.addActionListener(e -> viewAppointmentDetails());
        cancelButton.addActionListener(e -> cancelAppointment());
        rescheduleButton.addActionListener(e -> rescheduleAppointment());
        refreshButton.addActionListener(e -> loadAppointments());
        backButton.addActionListener(e -> {
        dispose(); // Close this frame
        new CustomerDashboard(customer); // Return to dashboard
        });
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Title
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("My Appointments", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titlePanel.add(titleLabel);

        // Table scroll
        JScrollPane scrollPane = new JScrollPane(appointmentTable);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
  
    
        buttonPanel.add(viewButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(rescheduleButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(backButton);

        add(titlePanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadAppointments() {
        try {
            List<Appointment> customerAppointments = FileStorage.getAppointmentsByCustomer(customer.getId());
            updateTable(customerAppointments);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading appointments: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable(List<Appointment> appointments) {
        String[] columns = {"ID", "Doctor", "Specialty", "Date", "Time", "Status"};
        Object[][] data = new Object[appointments.size()][6];

        for (int i = 0; i < appointments.size(); i++) {
            Appointment apt = appointments.get(i);

            Doctor doctor = FileStorage.getDoctorById(apt.getDoctorId());

            data[i][0] = apt.getAppointmentId();
            data[i][1] = doctor != null ? doctor.getName() : "Unknown";
            data[i][2] = doctor != null ? doctor.getSpecialty() : "Unknown";
            data[i][3] = apt.getDate();
            data[i][4] = apt.getTime();
            data[i][5] = apt.getStatus();
        }

        appointmentTable.setModel(new javax.swing.table.DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        });
    }

    private void viewAppointmentDetails() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to view.");
            return;
        }

        try {
            String appointmentId = appointmentTable.getValueAt(selectedRow, 0).toString();
            String doctor = appointmentTable.getValueAt(selectedRow, 1).toString();
            String specialty = appointmentTable.getValueAt(selectedRow, 2).toString();
            String date = appointmentTable.getValueAt(selectedRow, 3).toString();
            String time = appointmentTable.getValueAt(selectedRow, 4).toString();
            String status = appointmentTable.getValueAt(selectedRow, 5).toString();

            String details = String.format(
                    "Appointment Details:\n\n" +
                            "ID: %s\nDoctor: %s\nSpecialty: %s\nDate: %s\nTime: %s\nStatus: %s\nPatient: %s",
                    appointmentId, doctor, specialty, date, time, status, customer.getName()
            );

            JOptionPane.showMessageDialog(this, details, "Appointment Details", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error viewing appointment details: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelAppointment() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to cancel.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel this appointment?",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String appointmentId = appointmentTable.getValueAt(selectedRow, 0).toString();
                FileStorage.cancelAppointment(appointmentId);
                JOptionPane.showMessageDialog(this, "Appointment cancelled successfully!");
                loadAppointments();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error cancelling appointment: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void rescheduleAppointment() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to reschedule.");
            return;
        }

        try {
            String appointmentId = appointmentTable.getValueAt(selectedRow, 0).toString();
            String currentDoctorName = appointmentTable.getValueAt(selectedRow, 1).toString();
            
            // Get the current appointment details
            List<Appointment> appointments = FileStorage.readAppointments();
            Appointment currentAppointment = null;
            for (Appointment apt : appointments) {
                if (apt.getAppointmentId().equals(appointmentId)) {
                    currentAppointment = apt;
                    break;
                }
            }
            
            if (currentAppointment == null) {
                JOptionPane.showMessageDialog(this, "Appointment not found.");
                return;
            }

            // Ask if they want to keep the same doctor or choose a different one
            String[] options = {"Keep same doctor (" + currentDoctorName + ")", "Choose different doctor"};
            int choice = JOptionPane.showOptionDialog(this,
                    "Would you like to keep the same doctor or choose a different one?",
                    "Reschedule Appointment",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

            Doctor selectedDoctor;
            if (choice == 0) {
                // Keep same doctor
                selectedDoctor = FileStorage.getDoctorById(currentAppointment.getDoctorId());
            } else if (choice == 1) {
                // Choose different doctor
                selectedDoctor = selectNewDoctor();
                if (selectedDoctor == null) {
                    return; // User cancelled doctor selection
                }
            } else {
                return; // User cancelled
            }

            // Show available dates and times for the selected doctor
            showAvailableSlotsAndReschedule(appointmentId, selectedDoctor);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error rescheduling appointment: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Doctor selectNewDoctor() {
        List<String> specialties = FileStorage.getAllSpecialties();
        
        // Let user choose specialty first
        String selectedSpecialty = (String) JOptionPane.showInputDialog(
                this,
                "Select a specialty:",
                "Choose Specialty",
                JOptionPane.QUESTION_MESSAGE,
                null,
                specialties.toArray(),
                specialties.get(0)
        );
        
        if (selectedSpecialty == null) {
            return null; // User cancelled
        }
        
        // Get doctors for the selected specialty
        List<Doctor> doctors = FileStorage.getDoctorsBySpecialty(selectedSpecialty);
        if (doctors.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No doctors available for this specialty.");
            return null;
        }
        
        // Let user choose doctor
        String[] doctorNames = doctors.stream()
                .map(d -> d.getName() + " (" + d.getSpecialty() + ")")
                .toArray(String[]::new);
        
        String selectedDoctorName = (String) JOptionPane.showInputDialog(
                this,
                "Select a doctor:",
                "Choose Doctor",
                JOptionPane.QUESTION_MESSAGE,
                null,
                doctorNames,
                doctorNames[0]
        );
        
        if (selectedDoctorName == null) {
            return null; // User cancelled
        }
        
        // Find the selected doctor
        int doctorIndex = java.util.Arrays.asList(doctorNames).indexOf(selectedDoctorName);
        return doctors.get(doctorIndex);
    }

    private void showAvailableSlotsAndReschedule(String appointmentId, Doctor doctor) {
    try {
        // Get all available slots for the doctor
        List<String> availableSlots = FileStorage.getAvailableSlotsByDoctorId(doctor.getId());
        
        if (availableSlots.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "No available slots for Dr. " + doctor.getName() + ".");
            return;
        }
        
        // Create display options from available slots
        List<String> displayOptions = new ArrayList<>();
        List<String> actualSlots = new ArrayList<>();
        
        LocalDateTime now = LocalDateTime.now();
        
        for (String slot : availableSlots) {
            String[] parts = slot.trim().split(" ");
            if (parts.length >= 2) {
                String dateStr = parts[0]; // DD-MM-YYYY format
                String time = parts[1];    // HH:MM format
                
                try {
                    // Parse to check if it's a future slot
                    String[] dateParts = dateStr.split("-");
                    if (dateParts.length == 3) {
                        int day = Integer.parseInt(dateParts[0]);
                        int month = Integer.parseInt(dateParts[1]);
                        int year = Integer.parseInt(dateParts[2]);
                        
                        LocalDate slotDate = LocalDate.of(year, month, day);
                        String[] timeParts = time.split(":");
                        
                        if (timeParts.length == 2) {
                            int hour = Integer.parseInt(timeParts[0]);
                            int minute = Integer.parseInt(timeParts[1]);
                            
                            LocalDateTime slotDateTime = slotDate.atTime(hour, minute);
                            
                            // Only add future slots
                            if (slotDateTime.isAfter(now)) {
                                // Display format: DD/MM/YYYY at HH:MM
                                String displayDate = String.format("%02d/%02d/%d", day, month, year);
                                displayOptions.add(displayDate + " at " + time);
                                actualSlots.add(slot); // Keep original DD-MM-YYYY HH:MM format
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing slot for display: " + slot + " - " + e.getMessage());
                }
            }
        }
        
        if (displayOptions.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "No valid future time slots found for Dr. " + doctor.getName() + ".");
            return;
        }
        
        // Sort display options by date and time
        Collections.sort(displayOptions);
        
        // Let user choose from available slots
        String selectedSlot = (String) JOptionPane.showInputDialog(
                this,
                "Select a new date and time for Dr. " + doctor.getName() + ":",
                "Choose Appointment Slot",
                JOptionPane.QUESTION_MESSAGE,
                null,
                displayOptions.toArray(),
                displayOptions.get(0)
        );
        
        if (selectedSlot == null) {
            return; // User cancelled
        }
        
        // Get the actual slot format
        int selectedIndex = displayOptions.indexOf(selectedSlot);
        String actualSlot = actualSlots.get(selectedIndex);
        String[] parts = actualSlot.split(" ");
        String originalDateFormat = parts[0]; // DD-MM-YYYY format
        String newTime = parts[1];            // HH:MM format
        
        // Convert DD-MM-YYYY to DD/MM/YYYY for appointment storage
        String appointmentDate = originalDateFormat.replace("-", "/");
        
        // Convert DD-MM-YYYY to YYYY-MM-DD for schedule operations
        String[] dateParts = originalDateFormat.split("-");
        String scheduleDate = String.format("%s-%02d-%02d", 
                dateParts[2], 
                Integer.parseInt(dateParts[1]), 
                Integer.parseInt(dateParts[0]));
        
        // Confirm the reschedule
        int confirm = JOptionPane.showConfirmDialog(this,
                "Reschedule appointment to:\nDoctor: " + doctor.getName() + 
                "\nDate: " + appointmentDate + "\nTime: " + newTime + "?",
                "Confirm Reschedule",
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Perform the reschedule
            rescheduleAppointmentInSystem(appointmentId, doctor, appointmentDate, newTime, scheduleDate);
            JOptionPane.showMessageDialog(this, "Appointment rescheduled successfully!");
            loadAppointments();
        }
        
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error rescheduling appointment: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rescheduleAppointmentInSystem(String appointmentId, Doctor newDoctor, 
                                             String newDate, String newTime, String scheduleDate) throws Exception {
        List<Appointment> appointments = FileStorage.readAppointments();
        Appointment targetAppointment = null;
        
        // Find and update the appointment
        for (Appointment apt : appointments) {
            if (apt.getAppointmentId().equals(appointmentId)) {
                targetAppointment = apt;
                
                // Return old slot to schedule if it's a different doctor or different time
                if (!apt.getDoctorId().equals(newDoctor.getId()) || 
                    !apt.getDate().equals(newDate) || !apt.getTime().equals(newTime)) {
                    returnOldSlotToSchedule(apt.getDoctorId(), apt.getDate(), apt.getTime());
                }
                
                // Update appointment details
                apt.setDoctorId(newDoctor.getId());
                apt.setDoctorName(newDoctor.getName());
                apt.setDate(newDate);
                apt.setTime(newTime);
                break;
            }
        }
        
        if (targetAppointment == null) {
            throw new Exception("Appointment not found");
        }
        
        // Remove the selected slot from schedule
        FileStorage.removeSlot(newDoctor.getId(), scheduleDate, newTime);
        
        // Save updated appointments
        FileStorage.overwriteAppointments(appointments);
    }

    private void returnOldSlotToSchedule(String doctorId, String appointmentDate, String time) {
    try {
        // Convert appointment date from DD/MM/YYYY to YYYY-MM-DD format
        String scheduleDate = convertDateForSchedule(appointmentDate);
        
        List<String> lines = new ArrayList<>();
        boolean found = false;

        try (BufferedReader br = new BufferedReader(new FileReader("schedule.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 3);

                if (parts.length >= 3 && parts[0].equals(doctorId) && parts[1].equals(scheduleDate)) {
                    found = true;
                    List<String> times = new ArrayList<>(Arrays.asList(parts[2].split(";")));
                    
                    // Only add if not already present
                    if (!times.contains(time)) {
                        times.add(time);
                    }

                    // Sort times
                    times.sort((t1, t2) -> {
                        try {
                            return LocalTime.parse(t1).compareTo(LocalTime.parse(t2));
                        } catch (Exception e) {
                            return t1.compareTo(t2);
                        }
                    });

                    lines.add(parts[0] + "," + parts[1] + "," + String.join(";", times));
                } else {
                    lines.add(line);
                }
            }
        }

        // If no existing entry found, create new one with just the time
        if (!found) {
            lines.add(doctorId + "," + scheduleDate + "," + time);
        }

        // Write back to file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("schedule.txt"))) {
            for (String l : lines) {
                bw.write(l);
                bw.newLine();
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

    // Helper methods for date conversion
    private String convertDateForDisplay(String scheduleDate) {
        // Convert YYYY-MM-DD to DD/MM/YYYY
        String[] parts = scheduleDate.split("-");
        if (parts.length == 3) {
            return parts[2] + "/" + parts[1] + "/" + parts[0];
        }
        return scheduleDate;
    }

    private String convertDateForAppointment(String scheduleDate) {
        // Convert YYYY-MM-DD to DD/MM/YYYY
        return convertDateForDisplay(scheduleDate);
    }

    private String convertDateForSchedule(String appointmentDate) {
        // Convert DD/MM/YYYY to YYYY-MM-DD
        String[] parts = appointmentDate.split("/");
        if (parts.length == 3) {
            return parts[2] + "-" + String.format("%02d", Integer.parseInt(parts[1])) + 
                   "-" + String.format("%02d", Integer.parseInt(parts[0]));
        }
        return appointmentDate;
    }
}