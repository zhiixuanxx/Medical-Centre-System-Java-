package GUI;
import model.Appointment;
import model.AppointmentDetails;
import model.Staff;
import util.PDFReceiptWriter;
import util.FileStorage;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import util.ReceiptGenerator;

public class CollectPaymentPage extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(CollectPaymentPage.class.getName());
    private final Staff loggedInStaff;
    private Appointment selectedAppt;
    private AppointmentDetails selectedDetails;
    
    

    /**
     * Creates new form CollecPaymentPage
     * @param staff
     */
    public CollectPaymentPage(Staff staff) {
        this.loggedInStaff = staff;
        initComponents();
        setupEvents();
        loadCompletedAppointments();
        setLocationRelativeTo(null);
        paymentMethodCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-- Select Payment Method --", "Cash", "Debit/Credit Card", "TNG eWallet" }));
        //customize
        getContentPane().setBackground(new java.awt.Color(230, 245, 255));
        
         //Panel
        jPanel2.setBackground(Color.WHITE);
        jPanel1.setBackground(new java.awt.Color(0, 51, 102));
        
        //Label
        jLabel1.setForeground(Color.WHITE);
        jLabel1.setFont(new Font("Perpetua", Font.PLAIN, 35));
        jLabel4.setForeground(Color.black);
        jLabel4.setFont(new Font("Perpetua", Font.BOLD, 15));
        jLabel3.setForeground(Color.black);
        jLabel3.setFont(new Font("Perpetua", Font.BOLD, 15));
        statusLabel.setForeground(Color.black);
        statusLabel.setFont(new Font("Perpetua", Font.BOLD, 17));
        
        //Button
        searchButton.setBackground(new java.awt.Color(72, 103, 150)); 
        searchButton.setForeground(Color.WHITE);
        searchButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        processBtn.setBackground(new java.awt.Color(72, 103, 150)); 
        processBtn.setForeground(Color.WHITE);
        processBtn.setFont(new Font("Perpetua", Font.BOLD, 15));
        backBtn.setBackground(new java.awt.Color(0, 51, 102)); 
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("Perpetua", Font.BOLD, 15));
        
    }

    
    private void setupEvents() {
        searchButton.addActionListener(e -> searchAppointments());

        appTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && appTable.getSelectedRow() != -1) {
                int row = appTable.getSelectedRow();
                String apptId = appTable.getValueAt(row, 0).toString();

                selectedAppt = FileStorage.readAppointments().stream()
                        .filter(a -> a.getAppointmentId().equals(apptId))
                        .findFirst().orElse(null);

                selectedDetails = FileStorage.readAppointmentDetails().stream()
                        .filter(d -> d.getAppointmentId().equals(apptId))
                        .findFirst().orElse(null);

                if (selectedAppt != null && selectedDetails != null) {
                    statusLabel.setText("Selected appointment " + apptId + " for " + selectedAppt.getCustomerName());
                    processBtn.setEnabled(true);
                }
            }
        });
        
        paymentMethodCombo.addActionListener(e -> {
            
            if (selectedDetails == null) return;
            double charges = selectedDetails.getCharges();
            double tax = charges * 0.06;
            double totalWithTax = charges + tax;

            String method = (String) paymentMethodCombo.getSelectedItem();
            if ("Cash".equalsIgnoreCase(method)) {
                amountPaidField.setText("");
                amountPaidField.setEditable(true);
            } else if (!method.startsWith("--")) {
                amountPaidField.setText(String.format("%.2f", totalWithTax));
                amountPaidField.setEditable(false);
            }
        });

      
    }

    private void loadCompletedAppointments() {
        DefaultTableModel model = (DefaultTableModel) appTable.getModel();
        model.setRowCount(0);
        List<Appointment> all = FileStorage.readAppointments();
        List<AppointmentDetails> details = FileStorage.readAppointmentDetails();

        for (Appointment a : all) {
            if ("Completed".equalsIgnoreCase(a.getStatus()) &&
            !"Paid".equalsIgnoreCase(a.getpaymentStatus())) {
                AppointmentDetails det = details.stream()
                        .filter(d -> d.getAppointmentId().equals(a.getAppointmentId()))
                        .findFirst().orElse(null);

                if (det != null) {
                    model.addRow(new Object[]{
                            a.getAppointmentId(),
                            a.getCustomerName(),
                            a.getDoctorName(),
                            a.getDate(),
                            det.getDiagnosis(),
                            String.format("RM %.2f", det.getCharges())
                    });
                }
            }
        }
    }

    private void searchAppointments() {
        String keyword = searchField.getText().trim().toLowerCase();
        DefaultTableModel model = (DefaultTableModel) appTable.getModel();
        
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            String apptId = model.getValueAt(i, 0).toString().toLowerCase();
            String cust = model.getValueAt(i, 1).toString().toLowerCase();
            if (!(apptId.contains(keyword) || cust.contains(keyword))) {
                model.removeRow(i);
            }
        }
    }

    private void handleProcessPayment() {
        if (selectedAppt == null || selectedDetails == null) {
            statusLabel.setText("Select an appointment first.");
            return;
        }

        String method = (String) paymentMethodCombo.getSelectedItem();
        if (method == null || method.startsWith("--")) {
            statusLabel.setText("Please select a payment method.");
            return;
        }

        double charges = selectedDetails.getCharges();
        double tax = charges * 0.06;
        double totalWithTax = charges + tax;

        double paidAmount;
        double change = 0.0;

        if ("Cash".equalsIgnoreCase(method)) {
            try {
                paidAmount = Double.parseDouble(amountPaidField.getText().trim());
            } catch (NumberFormatException e) {
                statusLabel.setText("Invalid paid amount.");
                return;
            }

            if (paidAmount < totalWithTax) {
                statusLabel.setText("Amount paid is less than total (RM " 
                                    + String.format("%.2f", totalWithTax) + ")!");
                return;
            }
            change = paidAmount - totalWithTax;

        } else {
            // auto full payment
            paidAmount = totalWithTax;
            change = 0.0;
        }

        String receiptId = "R" + System.currentTimeMillis();
        String timestamp = java.time.LocalDateTime.now()
        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        // Save payment record
        FileStorage.writePayment(
                receiptId,
                selectedAppt,
                selectedDetails,
                loggedInStaff,
                method,
                paidAmount,
                change,
                timestamp
        );

        // Update appointment status
        FileStorage.updatePaymentStatus(selectedAppt.getAppointmentId(), "Paid");

       ReceiptGenerator receiptDialog = new ReceiptGenerator(
                this,
                selectedAppt,
                selectedDetails,
                loggedInStaff,
                receiptId,
                method,
                paidAmount,
                change
        );
        receiptDialog.setVisible(true);

        JOptionPane.showMessageDialog(this,
                "Payment successful. Receipt generated.\nChange: RM " + String.format("%.2f", change),
                "Success", JOptionPane.INFORMATION_MESSAGE);

        // Reset all fields
        paymentMethodCombo.setSelectedIndex(0);
        amountPaidField.setText("");
        amountPaidField.setEditable(true);
        statusLabel.setText(" ");
        processBtn.setEnabled(false);
        appTable.clearSelection();

        loadCompletedAppointments();
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
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        paymentMethodCombo = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        amountPaidField = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        appTable = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        processBtn = new javax.swing.JButton();
        backBtn = new javax.swing.JButton();
        statusLabel = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Collect Payment & Generate Receipt");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 482, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(87, 87, 87))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addComponent(jLabel1)
                .addContainerGap(44, Short.MAX_VALUE))
        );

        jLabel4.setText("Payment Details");

        jLabel5.setText("Payment Method:");

        paymentMethodCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        paymentMethodCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paymentMethodComboActionPerformed(evt);
            }
        });

        jLabel6.setText("Amount Paid:");

        amountPaidField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                amountPaidFieldActionPerformed(evt);
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
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(paymentMethodCombo, 0, 155, Short.MAX_VALUE)
                            .addComponent(amountPaidField)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addGap(60, 60, 60)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(paymentMethodCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(amountPaidField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(323, Short.MAX_VALUE))
        );

        appTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Appt ID", "Customer", "Doctor", "Date", "Diagnosis", "Charges"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int row, int column) {
                return false;
            }

        });
        jScrollPane1.setViewportView(appTable);

        jLabel2.setText("Search:");

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

        jLabel3.setText("Completed Appointments (Unpaid)");

        processBtn.setText("Process Payment & Generate Receipt");
        processBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                processBtnActionPerformed(evt);
            }
        });

        backBtn.setText("Back");
        backBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backBtnActionPerformed(evt);
            }
        });

        statusLabel.setText("Status:");

        jLabel7.setText("(ApptID, CustName)");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(30, 30, 30)
                                .addComponent(searchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(92, 92, 92))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 352, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jScrollPane1))
                                .addContainerGap())))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(204, 204, 204)
                                .addComponent(backBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(85, 85, 85)
                                .addComponent(processBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(searchButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7)
                        .addGap(36, 36, 36)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(statusLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(processBtn)
                        .addGap(18, 18, 18)
                        .addComponent(backBtn)
                        .addGap(41, 41, 41))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void processBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_processBtnActionPerformed
        handleProcessPayment();
    }//GEN-LAST:event_processBtnActionPerformed

    private void backBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backBtnActionPerformed
        new StaffDashboard(loggedInStaff).setVisible(true);
        dispose();
    }//GEN-LAST:event_backBtnActionPerformed

    private void searchFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchFieldActionPerformed
        searchAppointments();
    }//GEN-LAST:event_searchFieldActionPerformed

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        searchAppointments();
    }//GEN-LAST:event_searchButtonActionPerformed

    private void amountPaidFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_amountPaidFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_amountPaidFieldActionPerformed

    private void paymentMethodComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paymentMethodComboActionPerformed
        
        if (selectedDetails == null) return;
        double charges = selectedDetails.getCharges();
        double tax = charges * 0.06;
        double totalWithTax = charges + tax;

        String method = (String) paymentMethodCombo.getSelectedItem();
        if ("Cash".equalsIgnoreCase(method)) {
            amountPaidField.setText("");
            amountPaidField.setEditable(true);
        } else if (!method.startsWith("--")) {
            amountPaidField.setText(String.format("%.2f", totalWithTax));
            amountPaidField.setEditable(false);
        }
    }//GEN-LAST:event_paymentMethodComboActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField amountPaidField;
    private javax.swing.JTable appTable;
    private javax.swing.JButton backBtn;
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
    private javax.swing.JComboBox<String> paymentMethodCombo;
    private javax.swing.JButton processBtn;
    private javax.swing.JButton searchButton;
    private javax.swing.JTextField searchField;
    private javax.swing.JLabel statusLabel;
    // End of variables declaration//GEN-END:variables
}
