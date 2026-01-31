package GUI;

import java.awt.Color;
import java.awt.Font;
import model.Appointment;
import model.Customer;
import model.Doctor;
import model.Staff;
import util.FileStorage;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AppointmentFormForStaff extends javax.swing.JFrame {
    
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Staff loggedInStaff;
    private final DefaultTableModel customerTableModel;
    private Customer selectedCustomer;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(AppointmentFormForStaff.class.getName());

    /**
     * Creates new form AppointmentFormForStaff
     * @param staff
     */
    public AppointmentFormForStaff(Staff staff) {
        this.loggedInStaff = staff;
        initComponents();
        setLocationRelativeTo(null);
        
        //Override table model to block editing
        customerTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] { "ID", "Name", "Email", "Phone" }
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 🚫 block all edits
            }
        });

    
        // Initialize components
        customerTableModel = (DefaultTableModel) customerTable.getModel();
        statusLabel.setText(" ");
        
        // Setup event handlers
        setupEventHandlers();
        
        // Load data
        loadAllCustomers();
        loadSpecialties();
        
        // Disable book button initially
        bookButton.setEnabled(false);
        
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
        statusLabel.setFont(new Font("Perpetua", Font.BOLD, 17));
        
        
        //buttons
        searchButton.setBackground(new java.awt.Color(72, 103, 150)); 
        searchButton.setForeground(Color.WHITE);
        searchButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        bookButton.setBackground(new java.awt.Color(72, 103, 150)); 
        bookButton.setForeground(Color.WHITE);
        bookButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        resetButton.setBackground(new java.awt.Color(0, 51, 102)); 
        resetButton.setForeground(Color.WHITE);
        resetButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        backButton.setBackground(new java.awt.Color(0, 51, 102)); 
        backButton.setForeground(Color.WHITE);
        backButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        
        
    }
    
    private void setupEventHandlers() {
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
    }
    
    private void loadSpecialties() {
        specialtyComboBox.removeAllItems();
        specialtyComboBox.addItem("-- Select Specialty --");
        for (String specialty : FileStorage.getAllSpecialties()) {
            if (!specialty.equalsIgnoreCase("Emergency")) { // ✅ skip emergency
                specialtyComboBox.addItem(specialty);
            }
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
            List<Doctor> doctors = FileStorage.getClinicalDoctorsBySpecialty(specialty);
            for (Doctor d : doctors) {
                doctorComboBox.addItem(d.getId() + " - " + d.getName()); // ✅ store as String
            }

            if (doctors.isEmpty()) {
                statusLabel.setText("No doctors available for " + specialty);
            } else {
                statusLabel.setText(doctors.size() + " doctors found for " + specialty);
            }
        }
        validateForm();
    }

    private void handleDoctorSelection() {
        slotComboBox.removeAllItems();
        String selectedDoctorInfo = (String) doctorComboBox.getSelectedItem();

        if (selectedDoctorInfo != null) {
            // Extract doctorId from "id - name"
            String doctorId = selectedDoctorInfo.split(" - ")[0];
            Doctor doctor = FileStorage.getDoctorById(doctorId);

            if (doctor != null) {
                List<String> rawSlots = FileStorage.getAvailableSlotsByDoctorId(doctor.getId());

                LocalDate today = LocalDate.now();
                java.time.LocalTime nowTime = java.time.LocalTime.now();

                for (String raw : rawSlots) {
                    String[] parts = raw.split(" ");
                    if (parts.length == 2) {
                        String dateStr = parts[0]; // dd/MM/yyyy
                        String time = parts[1];    // HH:mm

                        // Parse using dd/MM/yyyy
                        LocalDate date = LocalDate.parse(dateStr, FILE_DATE_FORMAT);
                        java.time.LocalTime slotTime = java.time.LocalTime.parse(time);

                        if (date.isBefore(today)) continue;
                        if (date.isEqual(today) && slotTime.isBefore(nowTime)) continue;

                        String displayDate = date.format(FILE_DATE_FORMAT);
                        slotComboBox.addItem(displayDate + " at " + time);
                    }
                }
            }
        }
        validateForm();
    }

    private void validateForm() {
        boolean isValid = selectedCustomer != null &&
            specialtyComboBox.getSelectedItem() != null &&
            !specialtyComboBox.getSelectedItem().toString().startsWith("--") &&
            doctorComboBox.getSelectedItem() != null &&
            slotComboBox.getSelectedItem() != null;
            bookButton.setEnabled(isValid);
    }

    private void handleBooking() {
        if (selectedCustomer == null) {
        statusLabel.setText("Please select a customer first.");
        return;
    }

    String selectedDoctorInfo = (String) doctorComboBox.getSelectedItem();
    String selectedSlot = (String) slotComboBox.getSelectedItem();

    if (selectedDoctorInfo == null || selectedSlot == null) {
        statusLabel.setText("Please complete all fields.");
        return;
    }

    // Extract doctor ID from format string
    String doctorId = selectedDoctorInfo.split(" - ")[0];
    
    // Find doc by ID
    Doctor selectedDoctor = FileStorage.getDoctorById(doctorId);

    if (selectedDoctor == null) {
        statusLabel.setText("Doctor not found.");
        return;
    }
    
    String[] parts = selectedSlot.split(" at ");
    String displayDate = parts[0];  // dd/MM/yyyy
    String time = parts[1];         // HH:mm

    // Directly use dd/MM/yyyy for schedule file
    String scheduleDate = displayDate;

    FileStorage.removeSlot(selectedDoctor.getId(), scheduleDate, time);

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
    
    JOptionPane.showMessageDialog(this,
            "Appointment booked successfully for " + selectedCustomer.getName()
                    + "\nAppointment ID: " + appointmentId,
            "Success", JOptionPane.INFORMATION_MESSAGE);

    handleDoctorSelection(); // reload available slots for the selected doctor
    validateForm();
    statusLabel.setText("Appointment booked. You may book another.");
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
        specialtyComboBox = new javax.swing.JComboBox<>();
        doctorComboBox = new javax.swing.JComboBox<>();
        slotComboBox = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        customerTable = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        backButton = new javax.swing.JButton();
        statusLabel = new javax.swing.JLabel();
        resetButton = new javax.swing.JButton();
        bookButton = new javax.swing.JButton();
        searchLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Manage Walk-in Booking");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 587, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(55, 55, 55))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(44, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(43, 43, 43))
        );

        jLabel2.setText("Appointment Details");

        jLabel3.setText("Speciality:");

        jLabel4.setText("Doctor:");

        jLabel5.setText("Time Slot:");

        specialtyComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        specialtyComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                specialtyComboBoxActionPerformed(evt);
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
                .addGap(19, 19, 19)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(67, 67, 67)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(specialtyComboBox, 0, 129, Short.MAX_VALUE)
                            .addComponent(doctorComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(slotComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(130, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel2)
                .addGap(46, 46, 46)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(specialtyComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(doctorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(slotComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel6.setText("Search Customer:");

        searchButton.setText("Search");
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        customerTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "ID", "Name", "Email", "Phone"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }

        });
        jScrollPane1.setViewportView(customerTable);

        jLabel7.setText("Selected Customer:");

        backButton.setText("Back");
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });

        statusLabel.setText("Status:");

        resetButton.setText("Reset");
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        bookButton.setText("Book Appointment");
        bookButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bookButtonActionPerformed(evt);
            }
        });

        searchLabel.setText("(CustID, Name, Email, Phone)");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(108, 108, 108)
                        .addComponent(bookButton, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(resetButton)
                        .addGap(33, 33, 33)
                        .addComponent(backButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel6)
                                        .addGap(18, 18, 18)
                                        .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(searchLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(31, 31, 31)
                                .addComponent(searchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 495, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 352, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jLabel7)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(46, 46, 46)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(searchButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(statusLabel)
                        .addGap(9, 9, 9)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(backButton)
                            .addComponent(resetButton)
                            .addComponent(bookButton))
                        .addContainerGap(172, Short.MAX_VALUE))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bookButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bookButtonActionPerformed
        handleBooking();
    }//GEN-LAST:event_bookButtonActionPerformed

    private void specialtyComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_specialtyComboBoxActionPerformed
        handleSpecialtySelection();
    }//GEN-LAST:event_specialtyComboBoxActionPerformed

    private void doctorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doctorComboBoxActionPerformed
        handleDoctorSelection();
    }//GEN-LAST:event_doctorComboBoxActionPerformed

    private void slotComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_slotComboBoxActionPerformed
        validateForm();
    }//GEN-LAST:event_slotComboBoxActionPerformed

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        searchCustomers();
    }//GEN-LAST:event_searchButtonActionPerformed

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
        dispose();
        new StaffDashboard(loggedInStaff).setVisible(true);
    }//GEN-LAST:event_backButtonActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed

    customerTable.clearSelection();
    selectedCustomer = null;

    specialtyComboBox.setSelectedIndex(0);
    doctorComboBox.removeAllItems();
    slotComboBox.removeAllItems();

    searchField.setText("");
    loadAllCustomers();

    statusLabel.setText("Form reset. Please start again.");

    bookButton.setEnabled(false);
    }//GEN-LAST:event_resetButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backButton;
    private javax.swing.JButton bookButton;
    private javax.swing.JTable customerTable;
    private javax.swing.JComboBox<String> doctorComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton resetButton;
    private javax.swing.JButton searchButton;
    private javax.swing.JTextField searchField;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JComboBox<String> slotComboBox;
    private javax.swing.JComboBox<String> specialtyComboBox;
    private javax.swing.JLabel statusLabel;
    // End of variables declaration//GEN-END:variables
}
