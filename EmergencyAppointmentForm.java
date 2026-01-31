package GUI;

import java.awt.Color;
import java.awt.Font;
import model.Appointment;
import model.Customer;
import model.Doctor;
import model.Staff;
import util.FileStorage;
import util.IDGenerator;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;


/**
 *
 * @author User
 */
public class EmergencyAppointmentForm extends javax.swing.JFrame {
    
    // Always use dd/MM/yyyy for dates
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    // Always use HH:mm for times
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    private final Staff loggedInStaff;
    private final DefaultTableModel customerTableModel;
    private Customer selectedCustomer;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(EmergencyAppointmentForm.class.getName());

    /**
     * Creates new form EmergencyAppointmentForm
     * @param staff
     */
    public EmergencyAppointmentForm(Staff staff) {
        this.loggedInStaff = staff;
        initComponents();
        setLocationRelativeTo(null);
        // Initialize components
        customerTableModel = (DefaultTableModel) customerTable.getModel();
        statusLabel.setText(" ");
        
        // Setup event handlers
        setupEventHandlers();
        
        // Load data
        loadAllCustomers();
        
         //customize
        getContentPane().setBackground(new java.awt.Color(230, 245, 255));
        
         //Panel
        jPanel1.setBackground(new java.awt.Color(0, 51, 102));
        
        //Label
        jLabel1.setForeground(Color.WHITE);
        jLabel1.setFont(new Font("Perpetua", Font.PLAIN, 35));
        statusLabel.setFont(new Font("Perpetua", Font.BOLD, 17));
        
        
        //Buttons
        searchButton.setBackground(new java.awt.Color(72, 103, 150)); 
        searchButton.setForeground(Color.WHITE);
        searchButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        confirmButton.setBackground(new java.awt.Color(72, 103, 150)); 
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        resetButton.setBackground(new java.awt.Color(72, 103, 150)); 
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
            }
        });
    }

    private void searchCustomers() {
        String keyword = searchField.getText().trim().toLowerCase();
        List<Customer> customers = FileStorage.getAllCustomers();

        List<Customer> filtered;
        if (keyword.isEmpty()) {
            filtered = customers;
        } else {
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
            String newId = IDGenerator.generateID("customer");
            String name = JOptionPane.showInputDialog(this, "Enter patient name:", "Walk-in Patient");
            if (name == null || name.trim().isEmpty()) name = "Walk-in Patient";

            String email = "walkin" + newId.toLowerCase() + "@gmail.com";
            String phone = "0000000000";
            String password = "Temp" + newId.toLowerCase() + "!"; 
            String dob = "01/01/1970"; // fixed default in dd/MM/yyyy

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
        String today = LocalDate.now().format(DATE_FORMATTER);
        String nowTime = LocalTime.now().format(TIME_FORMATTER);

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

        // Ask staff for bed number
        String bedNo = JOptionPane.showInputDialog(this, "Enter Bed Number:", "");
        if (bedNo == null || bedNo.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Emergency booking cancelled (no bed assigned).",
                    "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Save emergency details
        try (java.io.FileWriter fw = new java.io.FileWriter("emergency_details.txt", true);
             java.io.BufferedWriter bw = new java.io.BufferedWriter(fw);
             java.io.PrintWriter out = new java.io.PrintWriter(bw)) {

            String timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

            out.println(appointmentId + "," + bedNo + "," + timestamp);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error saving emergency details: " + ex.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }

        JOptionPane.showMessageDialog(this,
                "Emergency appointment assigned to " + oncallDoctor.getName()
                        + " for " + bookingCustomer.getName(),
                "Success", JOptionPane.INFORMATION_MESSAGE);

       resetForm();
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
        jLabel2 = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        customerTable = new javax.swing.JTable();
        confirmButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        backButton = new javax.swing.JButton();
        statusLabel = new javax.swing.JLabel();
        searchLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Emergency Appointment");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 415, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(160, 160, 160))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(44, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(43, 43, 43))
        );

        jLabel2.setText("Search Customer (optional) :");

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

            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        jScrollPane1.setViewportView(customerTable);

        confirmButton.setText("Confirm Emergency Appointment");
        confirmButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmButtonActionPerformed(evt);
            }
        });

        resetButton.setText("Reset");
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        backButton.setText("Back");
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });

        statusLabel.setText("Status:");

        searchLabel.setText("(CustID, Name, email, phone)");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(195, 195, 195)
                        .addComponent(jLabel2)
                        .addGap(40, 40, 40)
                        .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(225, 225, 225)
                        .addComponent(resetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(61, 61, 61)
                        .addComponent(backButton, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(47, 47, 47)
                        .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 700, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(181, 181, 181)
                        .addComponent(confirmButton, javax.swing.GroupLayout.PREFERRED_SIZE, 387, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(195, 195, 195)
                        .addComponent(searchLabel)))
                .addContainerGap(53, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(searchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(254, 254, 254))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchLabel)
                .addGap(15, 15, 15)
                .addComponent(searchButton)
                .addGap(64, 64, 64)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(statusLabel)
                .addGap(11, 11, 11)
                .addComponent(confirmButton)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(resetButton)
                    .addComponent(backButton))
                .addGap(0, 37, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void searchFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchFieldActionPerformed
        
    }//GEN-LAST:event_searchFieldActionPerformed

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        searchCustomers();
    }//GEN-LAST:event_searchButtonActionPerformed

    private void confirmButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confirmButtonActionPerformed
        handleEmergencyBooking();
    }//GEN-LAST:event_confirmButtonActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        resetForm();
    }//GEN-LAST:event_resetButtonActionPerformed

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
        dispose();
        new StaffDashboard(loggedInStaff).setVisible(true);
    }//GEN-LAST:event_backButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backButton;
    private javax.swing.JButton confirmButton;
    private javax.swing.JTable customerTable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton resetButton;
    private javax.swing.JButton searchButton;
    private javax.swing.JTextField searchField;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JLabel statusLabel;
    // End of variables declaration//GEN-END:variables
}
