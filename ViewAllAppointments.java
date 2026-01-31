package GUI;
import java.awt.Color;
import java.awt.Font;
import model.*;
import util.FileStorage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class ViewAllAppointments extends javax.swing.JFrame {
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ViewAllAppointments.class.getName());
    private final Manager loggedInManager;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private boolean initializing = true;

    /**
     * Creates new form ViewAllAppointments
     * @param manager
     */
    public ViewAllAppointments(Manager manager) {
        this.loggedInManager = manager;
        initComponents();
        setupStatusFilter();
        initializing = false;
        loadAppointments(FileStorage.readAppointments());
        setSize(800, 550);
        setResizable(false);
        setLocationRelativeTo(null);
        //Customization
        getContentPane().setBackground(new java.awt.Color(230, 245, 255));
        jPanel1.setBackground(new java.awt.Color(0, 51, 102));
        
        //Label
        jLabel1.setForeground(Color.WHITE);
        jLabel1.setFont(new Font("Perpetua", Font.PLAIN, 35));
        
        jLabel2.setFont(new Font("Perpetua", Font.BOLD, 17));
        jLabel3.setFont(new Font("Perpetua", Font.BOLD, 17));
        jLabel4.setFont(new Font("Perpetua", Font.BOLD, 17));
        jLabel5.setFont(new Font("Perpetua", Font.BOLD, 17));
        jLabel6.setFont(new Font("Perpetua", Font.BOLD, 17));
        
        
        //
        
        //Buttons
        searchButton.setBackground(new java.awt.Color(72, 103, 150)); 
        searchButton.setForeground(Color.WHITE);
        searchButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        viewDetailButton.setBackground(new java.awt.Color(72, 103, 150)); 
        viewDetailButton.setForeground(Color.WHITE);
        viewDetailButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        resetButton.setBackground(new java.awt.Color(72, 103, 150)); 
        resetButton.setForeground(Color.WHITE);
        resetButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        backButton.setBackground(new java.awt.Color(0, 51, 102)); 
        backButton.setForeground(Color.WHITE);
        backButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        
        //Table
        table.setBackground(Color.WHITE);
        table.setForeground(Color.black);
        table.setGridColor(new Color(200, 200, 200));
        table.setShowGrid(true);
        table.setFont(new Font("Perpetua", Font.PLAIN, 15));
        
        
    }
    
    private void setupStatusFilter() {
       java.awt.event.ActionListener[] listeners = statusFilter.getActionListeners();
    for (java.awt.event.ActionListener listener : listeners) {
        statusFilter.removeActionListener(listener);
    }
    
    statusFilter.removeAllItems();
    statusFilter.addItem("All");
    statusFilter.addItem("Upcoming");
    statusFilter.addItem("Completed");
    statusFilter.addItem("Cancelled");
    statusFilter.addItem("Paid");
    statusFilter.addItem("Emergency");
    statusFilter.setSelectedIndex(0);
    
    for (java.awt.event.ActionListener listener : listeners) {
        statusFilter.addActionListener(listener);
    }
   }
    
    private void loadAppointments(List<Appointment> appointments) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        
        for (Appointment a : appointments) {
            model.addRow(new Object[]{
                a.getAppointmentId(),
                a.getCustomerId(),
                a.getCustomerName(),
                a.getDoctorId(),
                a.getDate(),
                a.getTime(),
                a.getStatus(),
                a.getpaymentStatus()
            });
        }
    }
    
        private void doSearch() {
            if (initializing) return;

            String keyword = searchField.getText().trim().toLowerCase();
            String selectedStatus = statusFilter.getSelectedItem().toString();
            String fromDateStr = fromDateField.getText().trim();
            String toDateStr = toDateField.getText().trim();

            LocalDate tmpFrom = LocalDate.MIN;
            LocalDate tmpTo = LocalDate.MAX;

            //validate once before filtering
            try {
                if (!fromDateStr.isEmpty()) {
                    tmpFrom = LocalDate.parse(fromDateStr, DATE_FORMAT);
                }
                if (!toDateStr.isEmpty()) {
                    tmpTo = LocalDate.parse(toDateStr, DATE_FORMAT);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Invalid date format! Please use DD/MM/YYYY",
                    "Date Error", JOptionPane.ERROR_MESSAGE);
                return; // stop search
            }

            //eclare as final (effectively final now)
            final LocalDate from = tmpFrom;
            final LocalDate to = tmpTo;

            List<Appointment> all = FileStorage.readAppointments();
            List<Appointment> filtered = all.stream()
                    .filter(a ->
                            keyword.isEmpty() ||
                            a.getAppointmentId().toLowerCase().contains(keyword) ||
                            a.getCustomerName().toLowerCase().contains(keyword) ||
                            a.getDoctorName().toLowerCase().contains(keyword))
                    .filter(a -> {
                        if (selectedStatus.equals("All")) return true;
                        if (selectedStatus.equalsIgnoreCase("Paid")) {
                            return "Paid".equalsIgnoreCase(a.getpaymentStatus());
                        }
                        return a.getStatus().equalsIgnoreCase(selectedStatus);
                    })
                    .filter(a -> {
                        try {
                            LocalDate date = LocalDate.parse(a.getDate(), DATE_FORMAT);
                            return !date.isBefore(from) && !date.isAfter(to);
                        } catch (Exception ex) {
                            return false; // skip invalid stored date
                        }
                    })
                    .collect(Collectors.toList());

            loadAppointments(filtered);
        }


    private void viewDetail() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment first.");
            return;
        }

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        String detail = """
                        Appointment Details:
                        
                        Appointment ID: """ + model.getValueAt(selectedRow, 0) + "\n"
                + "Customer ID: " + model.getValueAt(selectedRow, 1) + "\n"
                + "Customer Name: " + model.getValueAt(selectedRow, 2) + "\n"
                + "Doctor ID: " + model.getValueAt(selectedRow, 3) + "\n"
                + "Date: " + model.getValueAt(selectedRow, 4) + "\n"
                + "Time: " + model.getValueAt(selectedRow, 5) + "\n"
                + "Status: " + model.getValueAt(selectedRow, 6) + "\n"
                + "Payment Status: " + model.getValueAt(selectedRow, 7);

        JOptionPane.showMessageDialog(this, detail, "Appointment Detail", JOptionPane.INFORMATION_MESSAGE);
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
        fromDateField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        toDateField = new javax.swing.JTextField();
        statusFilter = new javax.swing.JComboBox<>();
        searchButton = new javax.swing.JButton();
        viewDetailButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        backButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("All Appointments");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(278, 278, 278)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 451, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(46, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(41, 41, 41))
        );

        jLabel2.setText("Search & Filter:");

        searchField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchFieldActionPerformed(evt);
            }
        });

        jLabel3.setText("From (DD/MM/YYYY): ");

        fromDateField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fromDateFieldActionPerformed(evt);
            }
        });

        jLabel4.setText("Status:");

        jLabel5.setText("To (DD/MM/YYYY):");

        toDateField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toDateFieldActionPerformed(evt);
            }
        });

        statusFilter.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        statusFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statusFilterActionPerformed(evt);
            }
        });

        searchButton.setText("Search");
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        viewDetailButton.setText("View Details");
        viewDetailButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewDetailButtonActionPerformed(evt);
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

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Appt ID", "Patient ID", "Patient Name", "Doctor ID", "Date", "Time", "Status", "Payment Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        });
        jScrollPane1.setViewportView(table);

        jLabel6.setText("(ApptID, CustName, DrName)");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addGap(276, 276, 276)
                .addComponent(searchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addComponent(viewDetailButton, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(75, 75, 75)
                .addComponent(resetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(backButton, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(128, 128, 128))
            .addGroup(layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 6, Short.MAX_VALUE))
                    .addComponent(fromDateField))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(45, 45, 45))
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(statusFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(toDateField, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(102, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(statusFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(fromDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(toDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(searchButton)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(viewDetailButton)
                    .addComponent(resetButton)
                    .addComponent(backButton))
                .addGap(35, 35, 35)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 22, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
        this.dispose();
        new ManagerDashboard(loggedInManager).setVisible(true);
    }//GEN-LAST:event_backButtonActionPerformed

    private void searchFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchFieldActionPerformed
        doSearch();
    }//GEN-LAST:event_searchFieldActionPerformed

    private void fromDateFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fromDateFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_fromDateFieldActionPerformed

    private void statusFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statusFilterActionPerformed
        doSearch();
    }//GEN-LAST:event_statusFilterActionPerformed

    private void toDateFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toDateFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_toDateFieldActionPerformed

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        doSearch();
    }//GEN-LAST:event_searchButtonActionPerformed

    private void viewDetailButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewDetailButtonActionPerformed
        viewDetail();
    }//GEN-LAST:event_viewDetailButtonActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        searchField.setText("");
        statusFilter.setSelectedIndex(0);
        fromDateField.setText("");
        toDateField.setText("");
        loadAppointments(FileStorage.readAppointments());
    }//GEN-LAST:event_resetButtonActionPerformed

    /**
     * @param args the command line arguments
     */


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backButton;
    private javax.swing.JTextField fromDateField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton resetButton;
    private javax.swing.JButton searchButton;
    private javax.swing.JTextField searchField;
    private javax.swing.JComboBox<String> statusFilter;
    private javax.swing.JTable table;
    private javax.swing.JTextField toDateField;
    private javax.swing.JButton viewDetailButton;
    // End of variables declaration//GEN-END:variables
}
