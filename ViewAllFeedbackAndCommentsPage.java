package GUI;
import java.awt.Color;
import java.awt.Font;
import model.Feedback;
import model.Manager;
import model.Appointment;
import util.FileStorage;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.stream.Collectors;

public class ViewAllFeedbackAndCommentsPage extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ViewAllFeedbackAndCommentsPage.class.getName());
    private final Manager loggedInManager;
    private DefaultTableModel tableModel;

    public ViewAllFeedbackAndCommentsPage(Manager manager) {
        this.loggedInManager = manager;
        initComponents();
        initializeForm();
        
        //Customize bg
        getContentPane().setBackground(new java.awt.Color(230, 245, 255));
        jPanel1.setBackground(new java.awt.Color(0, 51, 102));
        
        //Label
        jLabel1.setForeground(Color.WHITE);
        jLabel1.setFont(new Font("Perpetua", Font.PLAIN, 35));
        
        //button
        searchBtn.setBackground(new java.awt.Color(72, 103, 150)); 
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFont(new Font("Perpetua", Font.BOLD, 15));
        viewDetailBtn.setBackground(new java.awt.Color(72, 103, 150)); 
        viewDetailBtn.setForeground(Color.WHITE);
        viewDetailBtn.setFont(new Font("Perpetua", Font.BOLD, 15));
        resetBtn.setBackground(new java.awt.Color(72, 103, 150)); 
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setFont(new Font("Perpetua", Font.BOLD, 15));
        backBtn.setBackground(new java.awt.Color(72, 103, 150)); 
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("Perpetua", Font.BOLD, 15));
        

    }
    
    private void initializeForm() {
        
        // Initialize table model
        tableModel = (DefaultTableModel) feedbackTable.getModel();
        tableModel.setColumnIdentifiers(new Object[]{
            "Appointment ID", "Customer ID", 
            "Doctor Feedback", "Doctor Rating", 
            "Staff Feedback", "Staff Rating"
        });
        
        // Initialize rating filter
        ratingFilter.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"All", "1", "2", "3", "4", "5"}));
        
        // Load feedback data
        loadFeedback(FileStorage.getAllFeedback());
    }
    
    
    private void loadFeedback(List<Feedback> feedbackList) {
        tableModel.setRowCount(0);
        for (Feedback f : feedbackList) {
            tableModel.addRow(new Object[]{
                f.getAppointmentId(),
                f.getCustomerId(),
                f.getDoctorFeedback(),
                f.getDoctorRating() == -1 ? "N/A" : f.getDoctorRating(),
                f.getStaffFeedback(),
                f.getStaffRating() == -1 ? "N/A" : f.getStaffRating()
            });
        }
    }
    
    
    private void searchFeedback() {
        String keyword = searchField.getText().trim().toLowerCase();
        String selectedRating = ratingFilter.getSelectedItem().toString();

        List<Feedback> allFeedback = FileStorage.getAllFeedback();
        List<Feedback> filtered = allFeedback.stream()
                .filter(f ->
                        keyword.isEmpty()
                                || f.getAppointmentId().toLowerCase().contains(keyword)
                                || f.getCustomerId().toLowerCase().contains(keyword)
                                || FileStorage.getAppointmentById(f.getAppointmentId())
                                   .getDoctorName().toLowerCase().contains(keyword))
                .filter(f -> {
                    if (selectedRating.equals("All")) return true;
                    int r = Integer.parseInt(selectedRating);
                    return f.getDoctorRating() == r || f.getStaffRating() == r;
                })
                .collect(Collectors.toList());

        loadFeedback(filtered);
    }
    
    
    private void showDetail() {
        int selectedRow = feedbackTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row first.");
            return;
        }

        String apptId = tableModel.getValueAt(selectedRow, 0).toString();
        String custId = tableModel.getValueAt(selectedRow, 1).toString();
        String doctorFeedback = tableModel.getValueAt(selectedRow, 2).toString();
        String doctorRating = tableModel.getValueAt(selectedRow, 3).toString();
        String staffFeedback = tableModel.getValueAt(selectedRow, 4).toString();
        String staffRating = tableModel.getValueAt(selectedRow, 5).toString();

        Appointment appt = FileStorage.getAppointmentById(apptId);
        String doctorName = appt != null ? appt.getDoctorName() : "N/A";
        String customerName = appt != null ? appt.getCustomerName() : "N/A";
        String date = appt != null ? appt.getDate() : "N/A";
        String time = appt != null ? appt.getTime() : "N/A";

        double charges = 0.0;
        if (FileStorage.getAppointmentDetailsById(apptId) != null) {
            charges = FileStorage.getAppointmentDetailsById(apptId).getCharges();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Appointment ID: ").append(apptId).append("\n");
        sb.append("Customer ID: ").append(custId).append("\n");
        sb.append("Customer Name: ").append(customerName).append("\n");
        sb.append("Doctor Name: ").append(doctorName).append("\n");
        sb.append("Date & Time: ").append(date).append(" ").append(time).append("\n");
        sb.append("Charges: RM").append(String.format("%.2f", charges)).append("\n\n");

        sb.append("Doctor Feedback: ").append(doctorFeedback).append("\n");
        sb.append("Doctor Rating: ").append(doctorRating).append("\n\n");
        sb.append("Staff Feedback: ").append(staffFeedback).append("\n");
        sb.append("Staff Rating: ").append(staffRating).append("\n");

        JOptionPane.showMessageDialog(this, sb.toString(),
                "Feedback Detail", JOptionPane.INFORMATION_MESSAGE);
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
        jLabel3 = new javax.swing.JLabel();
        ratingFilter = new javax.swing.JComboBox<>();
        searchBtn = new javax.swing.JButton();
        viewDetailBtn = new javax.swing.JButton();
        resetBtn = new javax.swing.JButton();
        backBtn = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        feedbackTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Feedback and comments");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(217, 217, 217)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 469, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(114, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addComponent(jLabel1)
                .addContainerGap(45, Short.MAX_VALUE))
        );

        jLabel2.setText("Search (Appt ID/ Cust ID/ Doctor name) :");

        searchField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchFieldActionPerformed(evt);
            }
        });

        jLabel3.setText("Filter by Rating: ");

        ratingFilter.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        ratingFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ratingFilterActionPerformed(evt);
            }
        });

        searchBtn.setText("Search");
        searchBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchBtnActionPerformed(evt);
            }
        });

        viewDetailBtn.setText("View details");
        viewDetailBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewDetailBtnActionPerformed(evt);
            }
        });

        resetBtn.setText("Reset");
        resetBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetBtnActionPerformed(evt);
            }
        });

        backBtn.setText("Back");
        backBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backBtnActionPerformed(evt);
            }
        });

        feedbackTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Appointment ID", "Customer ID", "Doctor Feedback", "Doctor Rating", "Staff Feedback", "Staff Rating"
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
        jScrollPane1.setViewportView(feedbackTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(138, 138, 138)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(viewDetailBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(78, 78, 78)
                                        .addComponent(resetBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(26, 26, 26)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(ratingFilter, 0, 205, Short.MAX_VALUE)
                                            .addComponent(searchField)))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(112, 112, 112)
                                        .addComponent(backBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(269, 269, 269)
                        .addComponent(searchBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(118, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(ratingFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(searchBtn)
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(resetBtn)
                    .addComponent(backBtn)
                    .addComponent(viewDetailBtn))
                .addGap(39, 39, 39)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(120, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void searchFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_searchFieldActionPerformed

    private void backBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backBtnActionPerformed
        dispose();
        new ManagerDashboard(loggedInManager).setVisible(true);
    }//GEN-LAST:event_backBtnActionPerformed

    private void ratingFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ratingFilterActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ratingFilterActionPerformed

    private void searchBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchBtnActionPerformed
        searchFeedback();
    }//GEN-LAST:event_searchBtnActionPerformed

    private void viewDetailBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewDetailBtnActionPerformed
        showDetail();
    }//GEN-LAST:event_viewDetailBtnActionPerformed

    private void resetBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetBtnActionPerformed
        searchField.setText("");
        ratingFilter.setSelectedIndex(0);
        loadFeedback(FileStorage.getAllFeedback());
    }//GEN-LAST:event_resetBtnActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backBtn;
    private javax.swing.JTable feedbackTable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox<String> ratingFilter;
    private javax.swing.JButton resetBtn;
    private javax.swing.JButton searchBtn;
    private javax.swing.JTextField searchField;
    private javax.swing.JButton viewDetailBtn;
    // End of variables declaration//GEN-END:variables
}
