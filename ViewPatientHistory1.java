package GUI;

import model.Doctor;
import model.Appointment;
import model.AppointmentDetails;
import model.Customer;
import util.FileStorage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;



public class ViewPatientHistory1 extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger =
    java.util.logging.Logger.getLogger(ViewPatientHistory1.class.getName());

    // Data models
    private DefaultTableModel patientTableModel;
    private DefaultTableModel appointmentHistoryModel;

    // Data lists & maps
    private List<Customer> allCustomers = new ArrayList<>();
    private Map<String, AppointmentDetails> appointmentDetailsMap = new HashMap<>();
    
    private Doctor doctor;
    private Customer customer;

    public ViewPatientHistory1(Customer customer) {
        this.customer = customer;
        initComponents();
        setLocationRelativeTo(null);
    }

    public ViewPatientHistory1() {
        initComponents();
        
        // Initialize table models
        patientTableModel = (DefaultTableModel) patientTable.getModel();
        appointmentHistoryModel = (DefaultTableModel) appointmentHistoryTable.getModel();
        
        setupPatientSelectionEvents();
        loadCustomerData();
        loadAppointmentDetails();
        loadPatientsWithAppointments();
    }

    public ViewPatientHistory1(Doctor doctor) {
        this();
        this.doctor = doctor;
        loadPatientsWithAppointments();
    }

    private void setupPatientSelectionEvents() {
        searchBtn.addActionListener(e -> searchPatients());
        searchField.addActionListener(e -> searchPatients());

        patientTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && patientTable.getSelectedRow() >= 0) {
                viewHistoryBtn.setEnabled(true);
            }
        });

        //viewHistoryBtn.addActionListener(e -> viewSelectedPatientHistory());

        // Double-click patient also loads history
        patientTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) viewSelectedPatientHistory();
            }
        });

        //when selecting an appointment, show feedback
        appointmentHistoryTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && appointmentHistoryTable.getSelectedRow() >= 0) {
                showFeedbackForSelectedAppointment();
            }
        });
    }
    
    private void showFeedbackForSelectedAppointment() {
        int row = appointmentHistoryTable.getSelectedRow();
        if (row < 0) return;

        // Get appointment date/time from table
        String date = (String) appointmentHistoryTable.getValueAt(row, 0);
        String time = (String) appointmentHistoryTable.getValueAt(row, 1);

        // Find the appointment ID by matching date + time
        String appointmentId = null;
        for (Appointment apt : FileStorage.readAppointments()) {
            if (apt.getDate().equals(date) && apt.getTime().equals(time)) {
                appointmentId = apt.getAppointmentId();
                break;
            }
        }

        if (appointmentId == null) {
            feedbackArea.setText("No appointment found.");
            return;
        }

        // Load feedback for this appointment
        try (BufferedReader reader = new BufferedReader(new FileReader("feedback.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    String feedbackAppointmentId = parts[0].trim();
                    if (feedbackAppointmentId.equals(appointmentId)) {
                        feedbackArea.setText(
                            "Appointment ID: " + feedbackAppointmentId + "\n" +
                            "Doctor Feedback: " + parts[2].trim() + " (Rating: " + parts[3].trim() + "/5)\n" +
                            "Staff Feedback: " + parts[4].trim() + " (Rating: " + parts[5].trim() + "/5)\n"
                        );
                        return;
                    }
                }
            }
            feedbackArea.setText("No feedback found for this appointment.");
        } catch (IOException ex) {
            feedbackArea.setText("Error reading feedback: " + ex.getMessage());
        }
    }


    private void loadCustomerData() {
        try {
            allCustomers = FileStorage.getAllCustomers();
            loadPatientsWithAppointments();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading customer data: " + e.getMessage(),
                    "Load Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void loadPatientsWithAppointments() {
        patientTableModel.setRowCount(0);

        List<Appointment> allAppointments = FileStorage.readAppointments();
        Map<String, Integer> patientAppointmentCount = new HashMap<>();
        Map<String, Boolean> patientHasDiagnosis = new HashMap<>();

        for (Appointment appointment : allAppointments) {
            if (doctor == null || appointment.getDoctorId().equals(doctor.getId())) {
                String customerId = appointment.getCustomerId();

                // Check if diagnosis exists for this appointment
                AppointmentDetails details = appointmentDetailsMap.get(appointment.getAppointmentId());
                if (details != null && details.getDiagnosis() != null && !details.getDiagnosis().trim().isEmpty()) {
                    patientHasDiagnosis.put(customerId, true);
                }

                // Count only Completed+Paid and Upcoming
                if (appointment.getStatus().equalsIgnoreCase("Completed") 
                        || appointment.getStatus().equalsIgnoreCase("Upcoming")) {
                    patientAppointmentCount.put(
                        customerId,
                        patientAppointmentCount.getOrDefault(customerId, 0) + 1
                    );
                }
            }
        }

        for (Customer customer : allCustomers) {
            // Show only if patient has at least one diagnosed appointment
            if (patientHasDiagnosis.containsKey(customer.getId())) {
                patientTableModel.addRow(new Object[]{
                    customer.getId(),
                    customer.getName(),
                    customer.getEmail(),
                    customer.getPhone(),
                    patientAppointmentCount.getOrDefault(customer.getId(), 0)
                });
            }
        }
    }

    // Use FileStorage helper (reads ID, charges, diagnosis, notes; backward-compatible)
    private void loadAppointmentDetails() {
        this.appointmentDetailsMap = FileStorage.getAllAppointmentDetailsMap();
    }

    private void searchPatients() {
        String searchText = searchField.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            loadPatientsWithAppointments();
            return;
        }

        patientTableModel.setRowCount(0);

        List<Appointment> allAppointments = FileStorage.readAppointments();
        Map<String, Integer> patientAppointmentCount = new HashMap<>();

        for (Appointment appointment : allAppointments) {
            if (doctor == null || appointment.getDoctorId().equals(doctor.getId())) {
                AppointmentDetails details = appointmentDetailsMap.get(appointment.getAppointmentId());
                if (details != null && details.getDiagnosis() != null && !details.getDiagnosis().trim().isEmpty()) {
                    String customerId = appointment.getCustomerId();
                    patientAppointmentCount.put(
                        customerId,
                        patientAppointmentCount.getOrDefault(customerId, 0) + 1
                    );
                }
            }
        }

        for (Customer customer : allCustomers) {
            boolean matches = customer.getName().toLowerCase().contains(searchText)
                    || customer.getId().toLowerCase().contains(searchText);

            if (patientAppointmentCount.containsKey(customer.getId()) && matches) {
                patientTableModel.addRow(new Object[]{
                        customer.getId(),
                        customer.getName(),
                        customer.getEmail(),
                        customer.getPhone(),
                        patientAppointmentCount.get(customer.getId())
                });
            }
        }
    }

    private void viewSelectedPatientHistory() {
        int selectedRow = patientTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a patient first");
            return;
        }

        String customerId = (String) patientTableModel.getValueAt(selectedRow, 0);
        String customerName = (String) patientTableModel.getValueAt(selectedRow, 1);

        selectedPatientLabel.setText("Patient History: " + customerName + " (ID: " + customerId + ")");

        //Only load appointment history here
        loadAppointmentHistory(customerId);

        feedbackArea.setText("Select an appointment to view feedback...");
    }

    private void loadAppointmentHistory(String customerId) {
        appointmentHistoryModel.setRowCount(0);

        List<Appointment> allAppointments = FileStorage.readAppointments();

        for (Appointment appointment : allAppointments) {
            if (appointment.getCustomerId().equals(customerId)
                    && appointment.getDoctorId().equals(doctor.getId())&& appointment.getStatus().equalsIgnoreCase("Completed")) {

                String appointmentId = appointment.getAppointmentId();
                AppointmentDetails details = appointmentDetailsMap.get(appointmentId);

                String diagnosis = (details != null) ? details.getDiagnosis() : "Not recorded";
                String charges   = (details != null) ? String.format("%.2f", details.getCharges()) : "0.00";
                String notes     = (details != null) ? details.getNotes() : ""; // <-- show notes

                appointmentHistoryModel.addRow(new Object[]{
                        appointment.getDate(),
                        appointment.getTime(),
                        appointment.getStatus(),
                        diagnosis,
                        charges,
                        notes
                });
            }
        }
    }

    private void loadPatientFeedback(String customerId) {
        StringBuilder feedbackText = new StringBuilder();
        feedbackText.append("PATIENT FEEDBACK HISTORY\n");
        feedbackText.append("==================================================\n\n");

        try (BufferedReader reader = new BufferedReader(new FileReader("feedback.txt"))) {
            String line;
            boolean hasFeedback = false;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String appointmentId = parts[0].trim();
                    String feedbackCustomerId = parts[1].trim();

                    if (feedbackCustomerId.equals(customerId)) {
                        String appointmentDate = getAppointmentDate(appointmentId);

                        feedbackText.append("Date: ").append(appointmentDate).append("\n");
                        feedbackText.append("Appointment ID: ").append(appointmentId).append("\n");

                        // Feedback text
                        String feedback = parts[2].trim();
                        feedbackText.append("Feedback: ").append(feedback).append("\n");
                        feedbackText.append("----------------------------------------------\n\n");
                        hasFeedback = true;
                    }
                }
            }

            if (!hasFeedback) {
                feedbackText.append("No feedback records found for this patient.");
            }

        } catch (FileNotFoundException e) {
            feedbackText.append("Feedback file not found.");
        } catch (IOException e) {
            feedbackText.append("Error loading feedback: ").append(e.getMessage());
        }

        // ✅ Always clear and reset text
        feedbackArea.setText(feedbackText.toString());
        feedbackArea.setCaretPosition(0);
    }


    private String getAppointmentDate(String appointmentId) {
        List<Appointment> appointments = FileStorage.readAppointments();
        for (Appointment appointment : appointments) {
            if (appointment.getAppointmentId().equals(appointmentId)) {
                return appointment.getDate();
            }
        }
        return "Unknown";
    }

    // Public refresh hook
    public void refreshData() {
        loadCustomerData();
        loadAppointmentDetails();

        appointmentHistoryModel.setRowCount(0);
        feedbackArea.setText("");
        selectedPatientLabel.setText("Select a patient to view history");
        viewHistoryBtn.setEnabled(false);
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        searchPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        searchBtn = new javax.swing.JButton();
        viewHistoryBtn = new javax.swing.JButton();
        createHistoryDisplayPanel = new javax.swing.JPanel();
        selectedPatientLabel = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        createAppointmentHistoryPanel = new javax.swing.JPanel();
        historyScrollPane = new javax.swing.JScrollPane();
        appointmentHistoryTable = new javax.swing.JTable();
        feedbackPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        feedbackArea = new javax.swing.JTextArea();
        bottomPanel = new javax.swing.JPanel();
        cancelBtn = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        patientScrollPane = new javax.swing.JScrollPane();
        patientTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(230, 245, 250));
        setSize(new java.awt.Dimension(1000, 750));

        searchPanel.setBackground(new java.awt.Color(230, 245, 250));
        searchPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Patient Selection", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Perpetua", 1, 14))); // NOI18N

        jLabel1.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        jLabel1.setText("Search Name / ID : ");

        searchField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchFieldActionPerformed(evt);
            }
        });

        searchBtn.setBackground(new java.awt.Color(0, 153, 204));
        searchBtn.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        searchBtn.setForeground(new java.awt.Color(255, 255, 255));
        searchBtn.setText("Search Patient");
        searchBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchBtnActionPerformed(evt);
            }
        });

        viewHistoryBtn.setBackground(new java.awt.Color(0, 102, 204));
        viewHistoryBtn.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        viewHistoryBtn.setForeground(new java.awt.Color(255, 255, 255));
        viewHistoryBtn.setText("View History");
        viewHistoryBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewHistoryBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout searchPanelLayout = new javax.swing.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addComponent(jLabel1)
                .addGap(39, 39, 39)
                .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(71, 71, 71)
                .addComponent(searchBtn)
                .addGap(37, 37, 37)
                .addComponent(viewHistoryBtn)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchBtn)
                    .addComponent(viewHistoryBtn))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        createHistoryDisplayPanel.setBackground(new java.awt.Color(230, 245, 250));

        selectedPatientLabel.setFont(new java.awt.Font("Perpetua", 1, 16)); // NOI18N
        selectedPatientLabel.setText("Please select a patient to view history");

        jSplitPane1.setBackground(new java.awt.Color(230, 245, 250));

        createAppointmentHistoryPanel.setBackground(new java.awt.Color(230, 245, 250));

        appointmentHistoryTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Date", "Time", "Status", "Diagnosis", "Charges", "Notes"
            }
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 🔒 all cells locked
            }
        }
    );
    historyScrollPane.setViewportView(appointmentHistoryTable);

    javax.swing.GroupLayout createAppointmentHistoryPanelLayout = new javax.swing.GroupLayout(createAppointmentHistoryPanel);
    createAppointmentHistoryPanel.setLayout(createAppointmentHistoryPanelLayout);
    createAppointmentHistoryPanelLayout.setHorizontalGroup(
        createAppointmentHistoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 659, Short.MAX_VALUE)
        .addGroup(createAppointmentHistoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, createAppointmentHistoryPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(historyScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 647, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap()))
    );
    createAppointmentHistoryPanelLayout.setVerticalGroup(
        createAppointmentHistoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 403, Short.MAX_VALUE)
        .addGroup(createAppointmentHistoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, createAppointmentHistoryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(historyScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
                .addContainerGap()))
    );

    jSplitPane1.setLeftComponent(createAppointmentHistoryPanel);

    feedbackPanel.setBackground(new java.awt.Color(230, 245, 250));

    jScrollPane1.setBackground(new java.awt.Color(230, 245, 250));

    feedbackArea.setColumns(20);
    feedbackArea.setRows(5);
    jScrollPane1.setViewportView(feedbackArea);

    javax.swing.GroupLayout feedbackPanelLayout = new javax.swing.GroupLayout(feedbackPanel);
    feedbackPanel.setLayout(feedbackPanelLayout);
    feedbackPanelLayout.setHorizontalGroup(
        feedbackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(feedbackPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 558, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    feedbackPanelLayout.setVerticalGroup(
        feedbackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(feedbackPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
            .addContainerGap())
    );

    jSplitPane1.setRightComponent(feedbackPanel);

    bottomPanel.setBackground(new java.awt.Color(230, 245, 250));

    cancelBtn.setBackground(new java.awt.Color(153, 153, 153));
    cancelBtn.setFont(new java.awt.Font("Perpetua", 0, 14)); // NOI18N
    cancelBtn.setForeground(new java.awt.Color(255, 255, 255));
    cancelBtn.setText("Cancel");
    cancelBtn.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    cancelBtn.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            cancelBtnActionPerformed(evt);
        }
    });

    resetButton.setBackground(new java.awt.Color(102, 153, 255));
    resetButton.setFont(new java.awt.Font("Perpetua", 0, 14)); // NOI18N
    resetButton.setForeground(new java.awt.Color(255, 255, 255));
    resetButton.setText("Reset");
    resetButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            resetButtonActionPerformed(evt);
        }
    });

    javax.swing.GroupLayout bottomPanelLayout = new javax.swing.GroupLayout(bottomPanel);
    bottomPanel.setLayout(bottomPanelLayout);
    bottomPanelLayout.setHorizontalGroup(
        bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, bottomPanelLayout.createSequentialGroup()
            .addContainerGap(963, Short.MAX_VALUE)
            .addComponent(resetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(51, 51, 51)
            .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
    );
    bottomPanelLayout.setVerticalGroup(
        bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(bottomPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(resetButton)
                .addComponent(cancelBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap(14, Short.MAX_VALUE))
    );

    javax.swing.GroupLayout createHistoryDisplayPanelLayout = new javax.swing.GroupLayout(createHistoryDisplayPanel);
    createHistoryDisplayPanel.setLayout(createHistoryDisplayPanelLayout);
    createHistoryDisplayPanelLayout.setHorizontalGroup(
        createHistoryDisplayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(createHistoryDisplayPanelLayout.createSequentialGroup()
            .addGroup(createHistoryDisplayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(createHistoryDisplayPanelLayout.createSequentialGroup()
                    .addGap(14, 14, 14)
                    .addComponent(selectedPatientLabel))
                .addGroup(createHistoryDisplayPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(bottomPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1241, Short.MAX_VALUE)
    );
    createHistoryDisplayPanelLayout.setVerticalGroup(
        createHistoryDisplayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(createHistoryDisplayPanelLayout.createSequentialGroup()
            .addGap(9, 9, 9)
            .addComponent(selectedPatientLabel)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 403, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(bottomPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(16, Short.MAX_VALUE))
    );

    patientTable.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null}
        },
        new String [] {
            "Customer ID", "Name", "Email", "Phone", "Total Appointment"
        }
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // 🔒 all cells locked
        }
    });
    patientScrollPane.setViewportView(patientTable);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addGap(6, 6, 6)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(searchPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(patientScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 1231, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(createHistoryDisplayPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addGap(6, 6, 6)
            .addComponent(searchPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(patientScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(18, 18, 18)
            .addComponent(createHistoryDisplayPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    pack();
    }// </editor-fold>//GEN-END:initComponents

    private void searchBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchBtnActionPerformed
        searchPatients();
    }//GEN-LAST:event_searchBtnActionPerformed

    private void viewHistoryBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewHistoryBtnActionPerformed
        viewSelectedPatientHistory();
    }//GEN-LAST:event_viewHistoryBtnActionPerformed

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        dispose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    private void searchFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchFieldActionPerformed
        searchPatients();
    }//GEN-LAST:event_searchFieldActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        searchField.setText("");
        refreshData(); 
    }//GEN-LAST:event_resetButtonActionPerformed

public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        
        // Add code to create and show the frame
        java.awt.EventQueue.invokeLater(() -> {
            new ViewPatientHistory1().setVisible(true);
        });
    }

        

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable appointmentHistoryTable;
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JPanel createAppointmentHistoryPanel;
    private javax.swing.JPanel createHistoryDisplayPanel;
    private javax.swing.JTextArea feedbackArea;
    private javax.swing.JPanel feedbackPanel;
    private javax.swing.JScrollPane historyScrollPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JScrollPane patientScrollPane;
    private javax.swing.JTable patientTable;
    private javax.swing.JButton resetButton;
    private javax.swing.JButton searchBtn;
    private javax.swing.JTextField searchField;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JLabel selectedPatientLabel;
    private javax.swing.JButton viewHistoryBtn;
    // End of variables declaration//GEN-END:variables
}
