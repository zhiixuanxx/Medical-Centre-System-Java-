package GUI;

import model.Doctor;
import model.Appointment;
import model.AppointmentDetails;
import util.FileStorage;
import javax.swing.*;
import javax.swing.JFrame;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EnterChargesDiagnosis1 extends JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(EnterChargesDiagnosis1.class.getName());
    private List<Appointment> doctorAppointments;
    private Doctor doctor;
    private DoctorDashboard1 parentDashboard;
    private DefaultTableModel appointmentModel;
    private boolean suppressSelection = false;

    // Date formatter (consistent everywhere)
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    
    public EnterChargesDiagnosis1() {
        initComponents(); 
        this.parentDashboard = null;
        appointmentModel = (DefaultTableModel) appointmentTable.getModel();
    }
    
    // Constructor with doctor parameter
    public EnterChargesDiagnosis1(Doctor doctor, DoctorDashboard1 parentDashboard) {
        this.doctor = doctor;
        this.parentDashboard = parentDashboard;
        this.doctorAppointments = new ArrayList<>();
        initComponents();
        
        appointmentModel = (DefaultTableModel) appointmentTable.getModel();

        //update title AFTER doctor is assigned
        topPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
            null,
            "Appointments - " + doctor.getName(),
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new java.awt.Font("Perpetua", 1, 14)
        ));

        loadDoctorAppointments();

        appointmentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && appointmentTable.getSelectedRow() != -1) {
            int row = appointmentTable.getSelectedRow();
            String appointmentId = appointmentTable.getValueAt(row, 0).toString();
            String patientName = appointmentTable.getValueAt(row, 1).toString();

            appointmentIdField.setText(appointmentId);
            patientNameField.setText(patientName);
    }
        });

        searchField.addActionListener(e -> searchPatient());
        cancelButton.addActionListener(e -> clearForm());
        
        setLocationRelativeTo(null);
    }


    private void loadDoctorAppointments() {
        try {
            List<Appointment> all = FileStorage.readAppointments();
            appointmentModel.setRowCount(0);
            doctorAppointments.clear();

            LocalDate today = LocalDate.now();

            for (Appointment a : all) {
                if (!a.getDoctorId().equals(doctor.getId())) continue;

                try {
                    LocalDate apptDate = LocalDate.parse(a.getDate().trim(), DATE_FORMATTER);

                    String status = a.getStatus() != null ? a.getStatus().trim() : "";
                    String payment = a.getpaymentStatus() != null ? a.getpaymentStatus().trim() : "";

                    boolean isUnpaid = !"Paid".equalsIgnoreCase(payment);

                    if (!doctor.isOnCall() 
                            && apptDate.equals(today) 
                            && "Upcoming".equalsIgnoreCase(status) 
                            && isUnpaid) {

                        addRow(a);
                    }

                    if (doctor.isOnCall() 
                            && apptDate.equals(today) 
                            && "Emergency".equalsIgnoreCase(status) 
                            && isUnpaid) {

                        addRow(a);
                    }
                } catch (Exception ex) {
                    logger.warning("Skipping appointment due to invalid date: " + a.getDate());
                }
            }

            noAppointmentsLabel.setVisible(appointmentModel.getRowCount() == 0);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading appointments: " + ex.getMessage(),
                    "Load Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

private void addRow(Appointment a) {
    doctorAppointments.add(a);
    appointmentModel.addRow(new Object[]{
        a.getAppointmentId(),
        a.getCustomerName(),
        a.getDate(),
        a.getTime(),
        a.getStatus(),
        a.getpaymentStatus()
    });
}
    
    private void onSelectRow() {
        if (suppressSelection) return; 
        int row = appointmentTable.getSelectedRow();
        if (row < 0) return;

        String apptId = String.valueOf(appointmentModel.getValueAt(row, 0));
        String patient = String.valueOf(appointmentModel.getValueAt(row, 1));
        String status = String.valueOf(appointmentModel.getValueAt(row, 4));

        if (!(status.equalsIgnoreCase("Upcoming")
           || status.equalsIgnoreCase("Completed")
           || status.equalsIgnoreCase("Emergency"))) {
            JOptionPane.showMessageDialog(this,
                "Only 'Upcoming', 'Completed' or 'Emergency' appointments can be edited.",
                "Invalid Selection", JOptionPane.WARNING_MESSAGE);
            appointmentTable.clearSelection();
            return;
        }

        appointmentIdField.setText(apptId);
        patientNameField.setText(patient);
        appointmentIdField.setEditable(false);
        patientNameField.setEditable(false);

        submitButton.setEnabled(true);
        issueMcButton.setEnabled(true);

        loadExistingDetails(apptId);

        diagnosisField.requestFocus();
    }

    private void loadExistingDetails(String appointmentId) {
        try {
            AppointmentDetails det = FileStorage.getAppointmentDetailsById(appointmentId);
            if (det != null) {
                chargeAmountField.setText(det.getCharges() == 0 ? "" : String.format("%.2f", det.getCharges()));
                diagnosisField.setText(det.getDiagnosis() == null ? "" : det.getDiagnosis());
                notesArea.setText(det.getNotes() == null ? "" : det.getNotes());
                followUpCheckBox.setSelected(det.isFollowUpNeeded());
            } else {
                clearForm();
            }
        } catch (Exception ex) {
            clearForm();
        }
    }

    private void searchPatient() {
        if (appointmentModel == null) {
            appointmentModel = (DefaultTableModel) appointmentTable.getModel();
        }

        String q = searchField.getText();
        q = (q == null) ? "" : q.trim().toLowerCase();

        if (q.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a name or appointment ID to search.",
                    "Search Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        appointmentTable.clearSelection();
        int foundIndex = -1;

        for (int i = 0; i < appointmentModel.getRowCount(); i++) {
            String name = String.valueOf(appointmentModel.getValueAt(i, 1));
            String id   = String.valueOf(appointmentModel.getValueAt(i, 0));
            name = (name == null) ? "" : name.toLowerCase();
            id   = (id   == null) ? "" : id.toLowerCase();

            if (name.contains(q) || id.contains(q)) {
                foundIndex = i;
                break;
            }
        }

        if (foundIndex >= 0) {
            appointmentTable.getSelectionModel().setSelectionInterval(foundIndex, foundIndex);
            appointmentTable.scrollRectToVisible(appointmentTable.getCellRect(foundIndex, 0, true));
        } else {
            JOptionPane.showMessageDialog(this,
                    "No matches found for: " + q,
                    "Search Result", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void submitData() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment first.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            System.out.println("submitData() returned early: No row selected.");
            return;
        }

        String appointmentId = appointmentIdField.getText().trim();
        String diagnosis = diagnosisField.getText().trim();
        String chargeAmount = chargeAmountField.getText().trim();
        String notes = notesArea.getText().trim();
        boolean followUpNeeded = followUpCheckBox.isSelected();

        if (diagnosis.isEmpty() || chargeAmount.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both diagnosis and charge amount.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(chargeAmount);
            if (amount < 0) throw new NumberFormatException("negative");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid non-negative number for charges.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        submitButton.setEnabled(false);
        issueMcButton.setEnabled(false);

        try {
            AppointmentDetails det = new AppointmentDetails(
                    appointmentId, amount, diagnosis, notes, followUpNeeded);
            FileStorage.saveOrUpdateAppointmentDetails(det);
            FileStorage.updateAppointmentStatus(appointmentId, "Completed");

            JOptionPane.showMessageDialog(this,
                "Saved. Follow-up needed: " + (followUpNeeded ? "YES" : "NO"),
                "Success", JOptionPane.INFORMATION_MESSAGE);

            suppressSelection = true;
            clearForm();
            appointmentModel.removeRow(selectedRow);
            suppressSelection = false;

            doctorAppointments.removeIf(a -> a.getAppointmentId().equals(appointmentId));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving data: " + ex.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            
            submitButton.setEnabled(true);
            issueMcButton.setEnabled(true);
        }
    }

    private void openMcDialogForCurrentSelection() {
        int row = appointmentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment first.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String apptId = String.valueOf(appointmentModel.getValueAt(row, 0));
        Appointment appt = FileStorage.getAppointmentById(apptId);
        if (appt == null) {
            JOptionPane.showMessageDialog(this, "Could not retrieve the selected appointment.",
                    "Missing Appointment", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new IssueMC(this, doctor, appt).setVisible(true);
    }
    
    private void clearForm() {
        suppressSelection = true;
        appointmentTable.clearSelection();
        suppressSelection = false;
        
        appointmentIdField.setText("");
        patientNameField.setText("");
        diagnosisField.setText("");
        chargeAmountField.setText("");
        notesArea.setText("");
        followUpCheckBox.setSelected(false);
        searchField.setText("");
        submitButton.setEnabled(false);
        issueMcButton.setEnabled(false);
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        topPanel = new javax.swing.JPanel();
        searchPanel = new javax.swing.JPanel();
        searchLabel = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        searchBtn = new javax.swing.JButton();
        noAppointmentsLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        formPanel = new javax.swing.JPanel();
        appointmentLabel = new javax.swing.JLabel();
        patientLabel = new javax.swing.JLabel();
        diagnosisLabel = new javax.swing.JLabel();
        chargeAmountLabel = new javax.swing.JLabel();
        notesLabel = new javax.swing.JLabel();
        patientNameField = new javax.swing.JTextField();
        diagnosisField = new javax.swing.JTextField();
        chargeAmountField = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        notesArea = new javax.swing.JTextArea();
        followUpCheckBox = new javax.swing.JCheckBox();
        appointmentIdField = new javax.swing.JTextField();
        submitButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        issueMcButton = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        appointmentTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Enter Charges, Diagnosis & Notes");
        setBackground(new java.awt.Color(230, 245, 250));
        setForeground(new java.awt.Color(255, 255, 255));
        setSize(new java.awt.Dimension(1200, 800));

        topPanel.setBackground(new java.awt.Color(235, 245, 250));
        topPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Appointments ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Perpetua", 1, 14))); // NOI18N

        searchPanel.setBackground(new java.awt.Color(235, 245, 250));
        searchPanel.setFont(new java.awt.Font("Perpetua", 0, 14)); // NOI18N

        searchLabel.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        searchLabel.setText("Search (Name / ID): ");

        searchBtn.setBackground(new java.awt.Color(153, 153, 153));
        searchBtn.setFont(new java.awt.Font("Perpetua", 0, 14)); // NOI18N
        searchBtn.setForeground(new java.awt.Color(255, 255, 255));
        searchBtn.setText("Search");
        searchBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout searchPanelLayout = new javax.swing.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(searchLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 90, Short.MAX_VALUE)
                .addComponent(searchBtn)
                .addGap(49, 49, 49))
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addGap(370, 370, 370)
                .addComponent(noAppointmentsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, searchPanelLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchLabel)
                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchBtn))
                .addGap(12, 12, 12)
                .addComponent(noAppointmentsLabel)
                .addContainerGap(12, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout topPanelLayout = new javax.swing.GroupLayout(topPanel);
        topPanel.setLayout(topPanelLayout);
        topPanelLayout.setHorizontalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(searchPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        topPanelLayout.setVerticalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(searchPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        formPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Diagnosis, Charges & Notes", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Perpetua", 1, 14))); // NOI18N
        formPanel.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N

        appointmentLabel.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        appointmentLabel.setText("Appointment ID : ");

        patientLabel.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        patientLabel.setText("Patient Name : ");

        diagnosisLabel.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        diagnosisLabel.setText("Diagnosis : ");

        chargeAmountLabel.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        chargeAmountLabel.setText("Charges : ");

        notesLabel.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        notesLabel.setText("Notes : ");

        patientNameField.setEditable(false);

        notesArea.setColumns(20);
        notesArea.setRows(5);
        jScrollPane2.setViewportView(notesArea);

        followUpCheckBox.setFont(new java.awt.Font("Perpetua", 0, 14)); // NOI18N
        followUpCheckBox.setText(" Follow-up Required");

        appointmentIdField.setEditable(false);

        submitButton.setBackground(new java.awt.Color(102, 153, 255));
        submitButton.setFont(new java.awt.Font("Perpetua", 0, 14)); // NOI18N
        submitButton.setForeground(new java.awt.Color(255, 255, 255));
        submitButton.setText("Submit");
        submitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitButtonActionPerformed(evt);
            }
        });

        cancelButton.setBackground(new java.awt.Color(255, 102, 102));
        cancelButton.setFont(new java.awt.Font("Perpetua", 0, 14)); // NOI18N
        cancelButton.setForeground(new java.awt.Color(255, 255, 255));
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        issueMcButton.setBackground(new java.awt.Color(255, 153, 0));
        issueMcButton.setFont(new java.awt.Font("Perpetua", 0, 14)); // NOI18N
        issueMcButton.setForeground(new java.awt.Color(255, 255, 255));
        issueMcButton.setText("Issue MC");
        issueMcButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                issueMcButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout formPanelLayout = new javax.swing.GroupLayout(formPanel);
        formPanel.setLayout(formPanelLayout);
        formPanelLayout.setHorizontalGroup(
            formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(formPanelLayout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(formPanelLayout.createSequentialGroup()
                        .addComponent(diagnosisLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(diagnosisField, javax.swing.GroupLayout.PREFERRED_SIZE, 395, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(formPanelLayout.createSequentialGroup()
                        .addComponent(patientLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(patientNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 395, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(formPanelLayout.createSequentialGroup()
                        .addComponent(appointmentLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(appointmentIdField, javax.swing.GroupLayout.PREFERRED_SIZE, 395, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(formPanelLayout.createSequentialGroup()
                        .addComponent(chargeAmountLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(chargeAmountField, javax.swing.GroupLayout.PREFERRED_SIZE, 395, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(formPanelLayout.createSequentialGroup()
                            .addComponent(followUpCheckBox)
                            .addGap(82, 82, 82)
                            .addComponent(submitButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(issueMcButton, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(34, 34, 34)
                            .addComponent(cancelButton))
                        .addGroup(formPanelLayout.createSequentialGroup()
                            .addComponent(notesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(jScrollPane2))))
                .addContainerGap(38, Short.MAX_VALUE))
        );
        formPanelLayout.setVerticalGroup(
            formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(formPanelLayout.createSequentialGroup()
                .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(formPanelLayout.createSequentialGroup()
                        .addGap(230, 230, 230)
                        .addComponent(notesLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(formPanelLayout.createSequentialGroup()
                        .addContainerGap(31, Short.MAX_VALUE)
                        .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(appointmentIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(appointmentLabel))
                        .addGap(18, 18, 18)
                        .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(patientNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(patientLabel))
                        .addGap(18, 18, 18)
                        .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(diagnosisField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(diagnosisLabel))
                        .addGap(18, 18, 18)
                        .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(chargeAmountField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(chargeAmountLabel))
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27)))
                .addGroup(formPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(followUpCheckBox)
                    .addComponent(submitButton)
                    .addComponent(issueMcButton)
                    .addComponent(cancelButton))
                .addGap(38, 38, 38))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(formPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(formPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        appointmentTable.setFont(new java.awt.Font("Perpetua", 0, 12)); // NOI18N
        appointmentTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Appointment ID", "Patient Name", "Date ", "Time", "Status", "Payment"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        appointmentTable.setMaximumSize(new java.awt.Dimension(600, 100));
        jScrollPane3.setViewportView(appointmentTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane3)
                        .addContainerGap())))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(topPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 629, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(topPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void searchBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchBtnActionPerformed
        searchPatient();
    }//GEN-LAST:event_searchBtnActionPerformed

    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitButtonActionPerformed
        submitData();
    }//GEN-LAST:event_submitButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        clearForm();
        this.dispose();                 // close this frame
        if (parentDashboard != null) {
        parentDashboard.setVisible(true);
        } // show dashboard again
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void issueMcButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_issueMcButtonActionPerformed
        openMcDialogForCurrentSelection();
    }//GEN-LAST:event_issueMcButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField appointmentIdField;
    private javax.swing.JLabel appointmentLabel;
    private javax.swing.JTable appointmentTable;
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextField chargeAmountField;
    private javax.swing.JLabel chargeAmountLabel;
    private javax.swing.JTextField diagnosisField;
    private javax.swing.JLabel diagnosisLabel;
    private javax.swing.JCheckBox followUpCheckBox;
    private javax.swing.JPanel formPanel;
    private javax.swing.JButton issueMcButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel noAppointmentsLabel;
    private javax.swing.JTextArea notesArea;
    private javax.swing.JLabel notesLabel;
    private javax.swing.JLabel patientLabel;
    private javax.swing.JTextField patientNameField;
    private javax.swing.JButton searchBtn;
    private javax.swing.JTextField searchField;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JButton submitButton;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables
}

