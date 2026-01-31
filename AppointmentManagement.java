package GUI;

import GUI.CustomerDashboard1;
import model.Appointment;
import model.Doctor;
import model.Customer;
import util.FileStorage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.JOptionPane;

public class AppointmentManagement extends javax.swing.JFrame {
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(AppointmentManagement.class.getName());
    
    private Customer customer;  // <-- Add this field

    // Constructor that accepts a Customer
    public AppointmentManagement(Customer customer) {
        this.customer = customer;
        initComponents();
        loadAppointments();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    // Keep the no-argument constructor too (optional, if needed)
    public AppointmentManagement() {
        initComponents();
    }
    
    private void loadAppointments() {
        try {
            List<Appointment> customerAppointments = FileStorage.getAppointmentsByCustomer(customer.getId());

            // filter: only upcoming (exclude Cancelled, Completed, Emergency)
            List<Appointment> activeAppointments = new ArrayList<>();
            for (Appointment a : customerAppointments) {
                String status = a.getStatus() != null ? a.getStatus().trim().toLowerCase() : "";

                if (status.equals("upcoming")) {
                    activeAppointments.add(a);
                }
            }

            updateTable(activeAppointments);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading appointments: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable(List<Appointment> appointments) {
        //skip cancelled records
        List<Appointment> filtered = new ArrayList<>();
        for (Appointment a : appointments) {
            if (!"Cancelled".equalsIgnoreCase(a.getStatus())) {
                filtered.add(a);
            }
        }

        String[] columns = {"ID", "Doctor", "Specialty", "Date", "Time", "Status"};
        Object[][] data = new Object[filtered.size()][6];

        for (int i = 0; i < filtered.size(); i++) {
            Appointment apt = filtered.get(i);
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
    
    
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonPanel = new javax.swing.JPanel();
        viewButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        rescheduleButton = new javax.swing.JButton();
        refreshButton = new javax.swing.JButton();
        backButton = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        appointmentTable = new javax.swing.JTable();
        titleLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(230, 245, 255));
        setSize(new java.awt.Dimension(850, 800));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        viewButton.setBackground(new java.awt.Color(51, 153, 255));
        viewButton.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        viewButton.setForeground(new java.awt.Color(255, 255, 255));
        viewButton.setText("View Details");
        viewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewButtonActionPerformed(evt);
            }
        });

        cancelButton.setBackground(new java.awt.Color(255, 51, 51));
        cancelButton.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        cancelButton.setForeground(new java.awt.Color(255, 255, 255));
        cancelButton.setText("Cancel Appointment");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        rescheduleButton.setBackground(new java.awt.Color(255, 165, 0));
        rescheduleButton.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        rescheduleButton.setForeground(new java.awt.Color(255, 255, 255));
        rescheduleButton.setText("Reschedule Appointment");
        rescheduleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rescheduleButtonActionPerformed(evt);
            }
        });

        refreshButton.setBackground(new java.awt.Color(50, 150, 50));
        refreshButton.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        refreshButton.setForeground(new java.awt.Color(255, 255, 255));
        refreshButton.setText("Refresh");
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        backButton.setBackground(new java.awt.Color(102, 102, 255));
        backButton.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        backButton.setForeground(new java.awt.Color(255, 255, 255));
        backButton.setText("Back");
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout buttonPanelLayout = new javax.swing.GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(viewButton)
                .addGap(18, 18, 18)
                .addComponent(cancelButton)
                .addGap(18, 18, 18)
                .addComponent(rescheduleButton)
                .addGap(18, 18, 18)
                .addComponent(refreshButton)
                .addGap(18, 18, 18)
                .addComponent(backButton))
        );
        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(viewButton)
            .addComponent(cancelButton)
            .addComponent(rescheduleButton)
            .addComponent(refreshButton)
            .addComponent(backButton)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(33, 86, 49, 0);
        getContentPane().add(buttonPanel, gridBagConstraints);

        jScrollPane3.setPreferredSize(new java.awt.Dimension(800, 400));

        appointmentTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Doctor", "Specialty", "Date", "Time", "Status"
            }
        ));
        jScrollPane3.setViewportView(appointmentTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 726;
        gridBagConstraints.ipady = 456;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(33, 54, 0, 54);
        getContentPane().add(jScrollPane3, gridBagConstraints);

        titleLabel.setFont(new java.awt.Font("Perpetua", 0, 36)); // NOI18N
        titleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        titleLabel.setText("MY APPOINTMENT");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(23, 280, 0, 0);
        getContentPane().add(titleLabel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void viewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewButtonActionPerformed
        int row = appointmentTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment first.");
            return;
        }
        String id = appointmentTable.getValueAt(row, 0).toString();
        try {
            Appointment apt = FileStorage.getAppointmentById(id);
            if (apt != null) {
                Doctor doctor = FileStorage.getDoctorById(apt.getDoctorId());
                JOptionPane.showMessageDialog(this,
                        "Appointment Details:\n\n" +
                                "ID: " + apt.getAppointmentId() +
                                "\nDoctor: " + (doctor != null ? doctor.getName() : "Unknown") +
                                "\nSpecialty: " + (doctor != null ? doctor.getSpecialty() : "Unknown") +
                                "\nDate: " + apt.getDate() +
                                "\nTime: " + apt.getTime() +
                                "\nStatus: " + apt.getStatus());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_viewButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        int row = appointmentTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to cancel.");
            return;
        }
        String id = appointmentTable.getValueAt(row, 0).toString();

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure to cancel?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Appointment apt = FileStorage.getAppointmentById(id);
                if (apt != null) {
                    // ✅ return the slot back
                    returnOldSlotToSchedule(apt.getDoctorId(), apt.getDate(), apt.getTime());
                }

                FileStorage.cancelAppointment(id);
                JOptionPane.showMessageDialog(this, "Cancelled successfully!");

                //Instead of reloading from file, just update this row in the table
                appointmentTable.setValueAt("Cancelled", row, 5);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void rescheduleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rescheduleButtonActionPerformed
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
    }//GEN-LAST:event_rescheduleButtonActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        loadAppointments();
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
        this.dispose();
        CustomerDashboard1 customerDashboard1 = new CustomerDashboard1(customer);
        customerDashboard1.setVisible(true);
    }//GEN-LAST:event_backButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable appointmentTable;
    private javax.swing.JButton backButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton rescheduleButton;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JButton viewButton;
    // End of variables declaration//GEN-END:variables

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
    
    private void showAvailableSlotsAndReschedule(String appointmentId, Doctor doctor) {
        try {
            List<String> availableSlots = FileStorage.getAvailableSlotsByDoctorId(doctor.getId());

            if (availableSlots.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                        "No available slots for Dr. " + doctor.getName() + ".");
                return;
            }

            List<String> displayOptions = new ArrayList<>();
            List<String> actualSlots = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            for (String slot : availableSlots) {
                String[] parts = slot.trim().split(" ");
                if (parts.length >= 2) {
                    String dateStr = parts[0]; // dd/MM/yyyy
                    String time = parts[1];    // HH:mm or HH:mm-HH:mm

                    // Skip on-call ranges like 00:00-08:00
                    if (time.contains("-")) {
                        continue;
                    }

                    try {
                        LocalDate slotDate = LocalDate.parse(dateStr, DATE_FORMAT);
                        LocalTime slotTime = LocalTime.parse(time);
                        LocalDateTime slotDateTime = slotDate.atTime(slotTime);

                        if (slotDateTime.isAfter(now)) {
                            displayOptions.add(dateStr + " at " + time);
                            actualSlots.add(slot);
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing slot for display: " + slot + " - " + e.getMessage());
                    }
                }
            }

            if (displayOptions.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No valid future slots found for Dr. " + doctor.getName() + ".");
                return;
            }

            // Pair slots together before sorting
            List<String[]> pairedSlots = new ArrayList<>();
            for (int i = 0; i < displayOptions.size(); i++) {
                pairedSlots.add(new String[]{displayOptions.get(i), actualSlots.get(i)});
            }

            // Sort by display string
            pairedSlots.sort(Comparator.comparing(a -> a[0]));

            // Extract sorted display options
            String[] sortedDisplay = pairedSlots.stream().map(a -> a[0]).toArray(String[]::new);

            String selectedSlot = (String) JOptionPane.showInputDialog(
                    this,
                    "Select a new date and time for Dr. " + doctor.getName() + ":",
                    "Choose Appointment Slot",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    sortedDisplay,
                    sortedDisplay[0]
            );

            if (selectedSlot == null) return;

            // Find the actual slot that matches
            String[] chosenPair = pairedSlots.stream()
                    .filter(p -> p[0].equals(selectedSlot))
                    .findFirst()
                    .orElse(null);

            if (chosenPair == null) return;

            String[] parts = chosenPair[1].split(" ");
            String scheduleDate = parts[0];  // dd/MM/yyyy
            String newTime = parts[1];       // HH:mm

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Reschedule appointment to:\nDoctor: " + doctor.getName() +
                            "\nDate: " + scheduleDate + "\nTime: " + newTime + "?",
                    "Confirm Reschedule",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                rescheduleAppointmentInSystem(appointmentId, doctor, scheduleDate, newTime, scheduleDate);
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
            String scheduleDate = appointmentDate;

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

}
