package GUI;
import java.awt.Color;
import java.awt.Font;
import model.Appointment;
import model.AppointmentDetails;
import model.Doctor;
import model.Staff;
import util.FileStorage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.util.List;

public class AssignAppointmentPage extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(AssignAppointmentPage.class.getName());
    private final Staff loggedInStaff;
    private Appointment selectedAppointment;
    private DefaultTableModel apptModel;

    public AssignAppointmentPage(Staff staff) {
        this.loggedInStaff = staff;
        initComponents();
        initializeComponents();
        setupEventHandlers();
        loadFollowUpAppointments();
        loadSpecialties(); 
        statusLabel.setText("Pending"); // Default status
        
        //customize
        getContentPane().setBackground(new java.awt.Color(230, 245, 255));
        
         //Panel
        jPanel2.setBackground(Color.WHITE);
        jPanel1.setBackground(new java.awt.Color(0, 51, 102));
        
        //Label
        jLabel1.setForeground(Color.WHITE);
        jLabel1.setFont(new Font("Perpetua", Font.PLAIN, 35));
        jLabel2.setForeground(Color.black);
        jLabel2.setFont(new Font("Perpetua", Font.BOLD, 15));
        jLabel7.setForeground(Color.black);
        jLabel7.setFont(new Font("Perpetua", Font.BOLD, 15));
        statusLabel.setForeground(Color.black);
        statusLabel.setFont(new Font("Perpetua", Font.BOLD, 15));
        
        //Buttons
        searchButton.setBackground(new java.awt.Color(72, 103, 150)); 
        searchButton.setForeground(Color.WHITE);
        searchButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        assignButton.setBackground(new java.awt.Color(72, 103, 150)); 
        assignButton.setForeground(Color.WHITE);
        assignButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        backButton.setBackground(new java.awt.Color(0, 51, 102)); 
        backButton.setForeground(Color.WHITE);
        backButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        
        resetButton.setBackground(new java.awt.Color(72, 103, 150)); 
        resetButton.setForeground(Color.WHITE);
        resetButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        
    }
    
    private void initializeComponents() {
        // Initialize the table model (non-editable)
        apptModel = new DefaultTableModel(
                new Object[]{"Appt ID", "Customer", "Doctor", "Date", "Diagnosis"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // make table cells non-editable
            }
        };
        apptTable.setModel(apptModel);
        apptTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Prevent column reordering and resizing (optional but safer)
        apptTable.getTableHeader().setReorderingAllowed(false);
        apptTable.getTableHeader().setResizingAllowed(false);

        // Initialize combo boxes
        specialityComboBox.removeAllItems();
        doctorComboBox.removeAllItems();
        slotComboBox.removeAllItems();

        // Set up status label
        statusLabel.setText(" ");
        statusLabel.setForeground(java.awt.Color.RED);
    }


    private void setupEventHandlers() {
        // Search button handler
        searchButton.addActionListener((ActionEvent e) -> {
            searchAppointments();
        });

        // Table selection handler
        apptTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && apptTable.getSelectedRow() != -1) {
                int row = apptTable.getSelectedRow();
                String apptId = apptModel.getValueAt(row, 0).toString();

                selectedAppointment = FileStorage.readAppointments().stream()
                        .filter(a -> a.getAppointmentId().equals(apptId))
                        .findFirst().orElse(null);

                if (selectedAppointment != null) {
                    statusLabel.setText("Selected follow-up for " + selectedAppointment.getCustomerName());
                }
            }
        });

        // Combo box handlers
        specialityComboBox.addActionListener((ActionEvent e) -> {
            handleSpecialtySelection();
        });

        doctorComboBox.addActionListener((ActionEvent e) -> {
            handleDoctorSelection();
        });

        slotComboBox.addActionListener((ActionEvent e) -> {
            validateForm();
        });
    }

    private void loadFollowUpAppointments() {
        apptModel.setRowCount(0);
        List<Appointment> allAppointments = FileStorage.readAppointments();
        List<AppointmentDetails> details = FileStorage.readAppointmentDetails();

        for (AppointmentDetails det : details) {
            if (det.isFollowUpNeeded()) {
                Appointment a = allAppointments.stream()
                        .filter(ap -> ap.getAppointmentId().equals(det.getAppointmentId()))
                        .findFirst().orElse(null);

                if (a != null) {
                    apptModel.addRow(new Object[]{
                            a.getAppointmentId(),
                            a.getCustomerName(),
                            a.getDoctorName(),
                            a.getDate(),
                            det.getDiagnosis()
                    });
                }
            }
        }
    }

    private void searchAppointments() {
        String keyword = searchField.getText().trim().toLowerCase();
        for (int i = apptModel.getRowCount() - 1; i >= 0; i--) {
            String apptId = apptModel.getValueAt(i, 0).toString().toLowerCase();
            String cust = apptModel.getValueAt(i, 1).toString().toLowerCase();
            String doc = apptModel.getValueAt(i, 2).toString().toLowerCase();
            if (!(apptId.contains(keyword) || cust.contains(keyword) || doc.contains(keyword))) {
                apptModel.removeRow(i);
            }
        }
    }

    private void loadSpecialties() {
        specialityComboBox.removeAllItems();
        specialityComboBox.addItem("-- Select Specialty --");
        for (String s : FileStorage.getAllSpecialties()) {
            if (!s.equalsIgnoreCase("Emergency")) {
                specialityComboBox.addItem(s);
            }
        }
    }

    private void handleSpecialtySelection() {
    doctorComboBox.removeAllItems();
    slotComboBox.removeAllItems();
    String specialty = (String) specialityComboBox.getSelectedItem();
    if (specialty != null) {
        for (Doctor d : FileStorage.getAllDoctors()) { 
            if (specialty.equals(d.getSpecialty())) { 
                doctorComboBox.addItem(d.getName());
            }
        }
    }
    validateForm();
}

    private void handleDoctorSelection() {
        slotComboBox.removeAllItems();
        String doctorName = (String) doctorComboBox.getSelectedItem();

        if (doctorName != null && !doctorName.isEmpty()) {
            Doctor doctor = FileStorage.getAllDoctors().stream()
                    .filter(d -> d.getName().equals(doctorName))
                    .findFirst()
                    .orElse(null);

            if (doctor != null && "Clinical Doctor".equalsIgnoreCase(doctor.getDoctorType())) {
                List<String> rawSlots = FileStorage.getAvailableSlotsByDoctorId(doctor.getId());

                java.time.format.DateTimeFormatter formatter =
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

                java.time.LocalDateTime now = java.time.LocalDateTime.now();

                for (String raw : rawSlots) {
                    try {
                        // Expecting raw like: "dd/MM/yyyy HH:mm"
                        java.time.LocalDateTime slotDateTime =
                                java.time.LocalDateTime.parse(raw, formatter);

                        if (slotDateTime.isAfter(now)) { //only future slots
                            String[] parts = raw.split(" ");
                            if (parts.length == 2) {
                                String displayDate = parts[0];
                                slotComboBox.addItem(displayDate + " at " + parts[1]);
                            } else {
                                slotComboBox.addItem(raw); // fallback
                            }
                        }
                    } catch (Exception ex) {
                        logger.warning("Invalid slot format: " + raw);
                    }
                }

                if (slotComboBox.getItemCount() == 0) {
                    slotComboBox.addItem("No future slots available");
                }
            }
        }
        validateForm();
    }


    private void validateForm() {
    boolean isValid = selectedAppointment != null &&
            specialityComboBox.getSelectedItem() != null &&
            !"-- Select Specialty --".equals(specialityComboBox.getSelectedItem()) &&
            doctorComboBox.getSelectedItem() != null &&
            slotComboBox.getSelectedItem() != null;
    
    assignButton.setEnabled(isValid);
}

    private void handleAssign() {
        if (selectedAppointment == null) {
            statusLabel.setText("Please select an appointment first.");
            return;
        }

        String doctorName = (String) doctorComboBox.getSelectedItem();
        String selectedSlot = (String) slotComboBox.getSelectedItem();

        if (doctorName == null || selectedSlot == null) {
            statusLabel.setText("Please complete all fields.");
            return;
        }

        Doctor selectedDoctor = FileStorage.getAllDoctors().stream()
                .filter(d -> d.getName().equals(doctorName))
                .findFirst()
                .orElse(null);

        if (selectedDoctor == null) {
            statusLabel.setText("Doctor not found.");
            return;
        }

        String[] parts = selectedSlot.split(" at ");
        if (parts.length != 2) {
            statusLabel.setText("Invalid slot format.");
            return;
        }

        String displayDate = parts[0]; // dd/MM/yyyy
        String time = parts[1];

        //scheduleDate stays in dd/MM/yyyy now
        String scheduleDate = displayDate;

        String appointmentId = FileStorage.generateAppointmentID();
        Appointment newAppt = new Appointment(
                appointmentId,
                selectedAppointment.getCustomerId(),
                selectedAppointment.getCustomerName(),
                selectedDoctor.getId(),
                selectedDoctor.getName(),
                displayDate,   // keep dd/MM/yyyy
                time,
                "Upcoming",
                "Unpaid"
        );

        FileStorage.writeAppointment(newAppt);
        FileStorage.removeSlot(selectedDoctor.getId(), scheduleDate, time);

        AppointmentDetails oldDetails = FileStorage.getAllAppointmentDetailsMap().get(selectedAppointment.getAppointmentId());
        if (oldDetails != null) {
            AppointmentDetails updated = new AppointmentDetails(
                    oldDetails.getAppointmentId(),
                    oldDetails.getCharges(),
                    oldDetails.getDiagnosis(),
                    oldDetails.getNotes(),
                    false // no more follow-up needed
            );
            FileStorage.saveOrUpdateAppointmentDetails(updated);
        } 

        JOptionPane.showMessageDialog(this,
                "Follow-up booked for " + selectedAppointment.getCustomerName() +
                        "\nDoctor: " + selectedDoctor.getName() +
                        "\nDate: " + displayDate +
                        " at " + time +
                        "\nStatus: Upcoming",
                "Success", JOptionPane.INFORMATION_MESSAGE);

        selectedAppointment = null;
        apptTable.clearSelection();
        specialityComboBox.setSelectedIndex(0);
        doctorComboBox.removeAllItems();
        slotComboBox.removeAllItems();
        statusLabel.setText("Form cleared. You can assign another follow-up.");
        assignButton.setEnabled(false);

        // reload the follow-up appointments list
        loadFollowUpAppointments();
    }

    
    private void resetForm() {
        // Clear search field
        searchField.setText("");

        // Reload all follow-up appointments
        loadFollowUpAppointments();

        // Reset selections
        selectedAppointment = null;
        apptTable.clearSelection();

        specialityComboBox.setSelectedIndex(0); // back to -- Select Specialty --
        doctorComboBox.removeAllItems();
        slotComboBox.removeAllItems();

        // Reset status label
        statusLabel.setText("Form reset. All follow-up appointments loaded.");

        // Disable assign button until valid selection
        assignButton.setEnabled(false);
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
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        specialityComboBox = new javax.swing.JComboBox<>();
        doctorComboBox = new javax.swing.JComboBox<>();
        slotComboBox = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        apptTable = new javax.swing.JTable();
        statusLabel = new javax.swing.JLabel();
        assignButton = new javax.swing.JButton();
        backButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Assign Appointment to Doctor");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 459, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(126, 126, 126))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addComponent(jLabel1)
                .addContainerGap(44, Short.MAX_VALUE))
        );

        jLabel2.setText("Assign next appointment");

        jLabel3.setText("Speciality:");

        jLabel4.setText("Doctor:");

        jLabel5.setText("Time Slot:");

        specialityComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        specialityComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                specialityComboBoxActionPerformed(evt);
            }
        });

        doctorComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        doctorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doctorComboBoxActionPerformed(evt);
            }
        });

        slotComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        slotComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                slotComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE))
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(49, 49, 49)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(specialityComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(doctorComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(slotComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 8, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel2)
                .addGap(31, 31, 31)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(specialityComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(38, 38, 38)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(doctorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(slotComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel6.setText("Search:");

        searchField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchFieldActionPerformed(evt);
            }
        });

        searchButton.setText("Search");
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        jLabel7.setText("follow-up appointment");

        apptTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Appt ID", "Customer", "Doctor", "Date", "Diagnosis"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        jScrollPane1.setViewportView(apptTable);

        statusLabel.setText("Status:");

        assignButton.setText("Assign Follow-up");
        assignButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                assignButtonActionPerformed(evt);
            }
        });

        backButton.setText("Back");
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });

        resetButton.setText("Reset");
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        jLabel8.setText("(ApptID, CusName, DrName)");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(assignButton, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 463, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 496, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(resetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(85, 85, 85)
                        .addComponent(backButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(54, 54, 54))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel8)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(31, 31, 31)
                                        .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(searchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 0, Short.MAX_VALUE))))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(searchButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel8)
                        .addGap(52, 52, 52)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(statusLabel)
                        .addGap(40, 40, 40)
                        .addComponent(assignButton)
                        .addGap(93, 93, 93)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(backButton)
                            .addComponent(resetButton))
                        .addGap(144, 144, 144))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void searchFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_searchFieldActionPerformed

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        searchAppointments();
    }//GEN-LAST:event_searchButtonActionPerformed

    private void specialityComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_specialityComboBoxActionPerformed
        handleSpecialtySelection();
    }//GEN-LAST:event_specialityComboBoxActionPerformed

    private void doctorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doctorComboBoxActionPerformed
        handleDoctorSelection();
    }//GEN-LAST:event_doctorComboBoxActionPerformed

    private void slotComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_slotComboBoxActionPerformed
        validateForm();
    }//GEN-LAST:event_slotComboBoxActionPerformed

    private void assignButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_assignButtonActionPerformed
        handleAssign();
    }//GEN-LAST:event_assignButtonActionPerformed

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
        dispose();
        new StaffDashboard(loggedInStaff).setVisible(true);
    }//GEN-LAST:event_backButtonActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        resetForm();
    }//GEN-LAST:event_resetButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable apptTable;
    private javax.swing.JButton assignButton;
    private javax.swing.JButton backButton;
    private javax.swing.JComboBox<String> doctorComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton resetButton;
    private javax.swing.JButton searchButton;
    private javax.swing.JTextField searchField;
    private javax.swing.JComboBox<String> slotComboBox;
    private javax.swing.JComboBox<String> specialityComboBox;
    private javax.swing.JLabel statusLabel;
    // End of variables declaration//GEN-END:variables
}
