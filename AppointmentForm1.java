package GUI;

import GUI.CustomerDashboard1;
import GUI.CustomerDashboard1;
import GUI.LoginGUI;
import model.Customer;
import model.Doctor;
import model.Appointment;
import util.FileStorage;
import java.awt.Color;
import java.awt.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;


/**
 *
 * @author Ewen
 */
public class AppointmentForm1 extends JFrame {
    private static final java.util.logging.Logger logger =
    java.util.logging.Logger.getLogger(AppointmentForm1.class.getName());
    private Customer customer;
    
    public AppointmentForm1(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        
        this.customer = customer;
        initComponents();
        setTitle("Book Appointment - " + customer.getName());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        loadInitialData();   // populate specialties
    }
    
    private void loadInitialData() {
        try {
            loadSpecialties();
            clearStatus();
        } catch (Exception e) {
            showError("Error loading specialties: " + e.getMessage());
        }
    }
    
    private void loadSpecialties() {
        specialtyComboBox.removeAllItems();
        specialtyComboBox.addItem("-- Select Specialty --");

        List<String> specialties = FileStorage.getAllSpecialties();
        for (String specialty : specialties) {
            if (!"Emergency".equalsIgnoreCase(specialty)) {
                specialtyComboBox.addItem(specialty);
            }
        }
    }

    private void handleSpecialtySelection() {
        clearDoctors();
        clearSlots();
        clearStatus();

        String selectedSpecialty = (String) specialtyComboBox.getSelectedItem();
        if (selectedSpecialty != null && !selectedSpecialty.startsWith("--")) {
            try {
                List<Doctor> doctors = FileStorage.getDoctorsBySpecialty(selectedSpecialty);
                doctorComboBox.removeAllItems();

                // Only allow Clinical doctors
                for (Doctor doctor : doctors) {
                    String type = doctor.getDoctorType();
                    if (type != null && type.toLowerCase().contains("clinical")) {
                        doctorComboBox.addItem(doctor);
                    }
                }

                // ✅ Auto-select first doctor if available
                if (doctorComboBox.getItemCount() > 0) {
                    doctorComboBox.setSelectedIndex(0);
                    handleDoctorSelection(); // auto-load slots
                } else {
                    showError("No clinical doctors available for " + selectedSpecialty);
                }
            } catch (Exception e) {
                showError("Error loading doctors: " + e.getMessage());
            }
        }

        validateForm();
    }
    
    private void updateDoctorsBySpecialty(String specialty) {
        List<Doctor> doctors = FileStorage.getDoctorsBySpecialty(specialty);

        doctorComboBox.removeAllItems(); 
        doctorComboBox.addItem(null); // placeholder

        for (Doctor doctor : doctors) {
            String type = doctor.getDoctorType();
            // ✅ Only allow Clinical doctors (block On-call/Emergency)
            if (type != null && type.toLowerCase().contains("clinical")) {
                doctorComboBox.addItem(doctor);
            }
        }
    }


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
    
    private void updateSlotsForDoctor(Doctor doctor) {
        List<String> slots = FileStorage.getAvailableSlotsByDoctorId(doctor.getId());
        slotComboBox.removeAllItems();
        slotComboBox.addItem("-- Select Time Slot --");

        LocalDateTime now = LocalDateTime.now();
        boolean anyFuture = false;

        for (String slot : slots) {
            try {
                // Expect slots in "dd/MM/yyyy HH:mm"
                String[] parts = slot.split(" ");
                if (parts.length == 2) {
                    String dateStr = parts[0]; // dd/MM/yyyy
                    String timeStr = parts[1]; // HH:mm

                    // Parse dd/MM/yyyy → LocalDate
                    String[] dateParts = dateStr.split("/");
                    int day = Integer.parseInt(dateParts[0]);
                    int month = Integer.parseInt(dateParts[1]);
                    int year = Integer.parseInt(dateParts[2]);

                    LocalDate slotDate = LocalDate.of(year, month, day);

                    // Parse time
                    String[] timeParts = timeStr.split(":");
                    int hour = Integer.parseInt(timeParts[0]);
                    int minute = Integer.parseInt(timeParts[1]);

                    LocalDateTime slotDateTime = slotDate.atTime(hour, minute);

                    if (slotDateTime.isAfter(now)) {
                        // ✅ Store/display exactly dd/MM/yyyy HH:mm
                        String displayFormat = String.format("%02d/%02d/%d %s", day, month, year, timeStr);
                        slotComboBox.addItem(displayFormat);
                        anyFuture = true;
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


    private void handleBooking() {
        Doctor selectedDoctor = (Doctor) doctorComboBox.getSelectedItem();
        String selectedSlot = (String) slotComboBox.getSelectedItem();

        if (!validateBookingData(selectedDoctor, selectedSlot)) {
            return;
        }
        if (selectedSlot.startsWith("--")) {
            showError("Please select a valid time slot.");
            return;
        }

        try {
            // Expect "dd/MM/yyyy HH:mm"
            String[] parts = selectedSlot.split(" ");
            String appointmentDate = parts[0]; // dd/MM/yyyy
            String time = parts[1];            // HH:mm

            // ✅ Create appointment directly in dd/MM/yyyy format
            String appointmentId = FileStorage.generateAppointmentID();
            Appointment appointment = new Appointment(
                appointmentId,
                customer.getId(),
                customer.getName(),
                selectedDoctor.getId(),
                selectedDoctor.getName(),
                appointmentDate, // dd/MM/yyyy everywhere
                time,
                "Upcoming",
                "Unpaid"
            );

            FileStorage.writeAppointment(appointment);

            // Remove slot from schedule (your FileStorage.removeSlot must also expect dd/MM/yyyy)
            FileStorage.removeSlot(selectedDoctor.getId(), appointmentDate, time);

            showSuccess("Appointment booked successfully!\nAppointment ID: " + appointmentId);
            dispose();
            new CustomerDashboard1(customer).setVisible(true);

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

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        titlePanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        formPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        specialtyComboBox = new javax.swing.JComboBox<>();
        doctorComboBox = new javax.swing.JComboBox<>();
        slotComboBox = new javax.swing.JComboBox<>();
        statusLabel = new javax.swing.JLabel();
        buttonPanel = new javax.swing.JPanel();
        backButton = new javax.swing.JButton();
        bookButton = new javax.swing.JButton();

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        titlePanel.setBackground(new java.awt.Color(230, 240, 250));

        titleLabel.setFont(new java.awt.Font("Perpetua", 0, 36)); // NOI18N
        titleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        titleLabel.setText("Book New Appointment");

        javax.swing.GroupLayout titlePanelLayout = new javax.swing.GroupLayout(titlePanel);
        titlePanel.setLayout(titlePanelLayout);
        titlePanelLayout.setHorizontalGroup(
            titlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(titleLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        titlePanelLayout.setVerticalGroup(
            titlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(titlePanelLayout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(titleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(33, Short.MAX_VALUE))
        );

        formPanel.setBackground(new java.awt.Color(248, 249, 250));
        formPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

        jLabel1.setFont(new java.awt.Font("Perpetua", 0, 18)); // NOI18N
        jLabel1.setText("Select Specialty :");

        jLabel2.setFont(new java.awt.Font("Perpetua", 0, 18)); // NOI18N
        jLabel2.setText("Select Doctor : ");

        jLabel3.setFont(new java.awt.Font("Perpetua", 0, 18)); // NOI18N
        jLabel3.setText("Select Time Slot : ");

        specialtyComboBox.setFont(new java.awt.Font("Perpetua", 0, 14)); // NOI18N
        specialtyComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-- Select Specialty --" }));
        specialtyComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                specialtyComboBoxActionPerformed(evt);
            }
        });

        doctorComboBox.setFont(new java.awt.Font("Perpetua", 0, 14)); // NOI18N
        doctorComboBox.setToolTipText("");
        doctorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doctorComboBoxActionPerformed(evt);
            }
        });

        slotComboBox.setFont(new java.awt.Font("Perpetua", 0, 14)); // NOI18N
        slotComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-- Select Time Slot --" }));
        slotComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                slotComboBoxActionPerformed(evt);
            }
        });

        statusLabel.setFont(new java.awt.Font("Perpetua", 0, 14)); // NOI18N
        statusLabel.setText(".");

        buttonPanel.setBackground(new java.awt.Color(248, 249, 250));

        backButton.setFont(new java.awt.Font("Perpetua", 0, 16)); // NOI18N
        backButton.setText("Back");
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });

        bookButton.setFont(new java.awt.Font("Perpetua", 0, 16)); // NOI18N
        bookButton.setText("Book Appointment");
        bookButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bookButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout buttonPanelLayout = new javax.swing.GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addGap(118, 118, 118)
                .addComponent(backButton)
                .addGap(38, 38, 38)
                .addComponent(bookButton)
                .addContainerGap(57, Short.MAX_VALUE))
        );
        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bookButton)
                    .addComponent(backButton))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout formPanelLayout = new javax.swing.GroupLayout(formPanel);
        formPanel.setLayout(formPanelLayout);
        formPanelLayout.setHorizontalGroup(
            formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, formPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(buttonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(formPanelLayout.createSequentialGroup()
                .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(formPanelLayout.createSequentialGroup()
                        .addGap(116, 116, 116)
                        .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)
                            .addComponent(jLabel1))
                        .addGap(86, 86, 86)
                        .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(specialtyComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(doctorComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(slotComboBox, 0, 244, Short.MAX_VALUE))))
                    .addGroup(formPanelLayout.createSequentialGroup()
                        .addGap(70, 70, 70)
                        .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 277, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(209, Short.MAX_VALUE))
        );
        formPanelLayout.setVerticalGroup(
            formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(formPanelLayout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(specialtyComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(42, 42, 42)
                .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(doctorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(43, 43, 43)
                .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(slotComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(67, 67, 67)
                .addComponent(buttonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(titlePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(formPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(titlePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(formPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
        dispose();
        CustomerDashboard1 customerDashboard1 = new CustomerDashboard1(customer);
        customerDashboard1.setVisible(true);
    }//GEN-LAST:event_backButtonActionPerformed

    private void bookButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bookButtonActionPerformed
        handleBooking();
    }//GEN-LAST:event_bookButtonActionPerformed

    private void slotComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_slotComboBoxActionPerformed
        validateForm();
    }//GEN-LAST:event_slotComboBoxActionPerformed

    private void specialtyComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_specialtyComboBoxActionPerformed
        handleSpecialtySelection();
    }//GEN-LAST:event_specialtyComboBoxActionPerformed

    private void doctorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doctorComboBoxActionPerformed
        handleDoctorSelection();
    }//GEN-LAST:event_doctorComboBoxActionPerformed

    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backButton;
    private javax.swing.JButton bookButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JComboBox<Doctor> doctorComboBox;
    private javax.swing.JPanel formPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JComboBox<String> slotComboBox;
    private javax.swing.JComboBox<String> specialtyComboBox;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JPanel titlePanel;
    // End of variables declaration//GEN-END:variables
}
