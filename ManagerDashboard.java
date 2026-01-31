package GUI;
import java.awt.Color;
import java.awt.Font;
import model.Manager;
import javax.swing.JOptionPane;


public class ManagerDashboard extends javax.swing.JFrame {
    
    private Manager loggedInManager; 


    /**
     * Creates new form ManagerDashboard
     * @param manager
     */
    public ManagerDashboard(Manager manager) {
        this.loggedInManager = manager;
        initComponents();
        setSize(800, 650);
        setResizable(false);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new java.awt.Color(230, 245, 255));
        //Panel
        jPanel1.setBackground(Color.WHITE);
        jPanel2.setBackground(new java.awt.Color(0, 51, 102));
        
        //Buttons
        crudManagerBtn.setBackground(new java.awt.Color(72, 103, 150)); 
        crudManagerBtn.setForeground(Color.WHITE);
        crudManagerBtn.setFont(new Font("Perpetua", Font.BOLD, 17));
        appointmentBtn.setBackground(new java.awt.Color(72, 103, 150)); 
        appointmentBtn.setForeground(Color.WHITE);
        appointmentBtn.setFont(new Font("Perpetua", Font.BOLD, 17));
        feedbackBtn.setBackground(new java.awt.Color(72, 103, 150)); 
        feedbackBtn.setForeground(Color.WHITE);
        feedbackBtn.setFont(new Font("Perpetua", Font.BOLD, 17));
        reportBtn.setBackground(new java.awt.Color(72, 103, 150)); 
        reportBtn.setForeground(Color.WHITE);
        reportBtn.setFont(new Font("Perpetua", Font.BOLD, 17));
        crudStaffBtn.setBackground(new java.awt.Color(72, 103, 150)); 
        crudStaffBtn.setForeground(Color.WHITE);
        crudStaffBtn.setFont(new Font("Perpetua", Font.BOLD, 17));
        crudDoctorBtn.setBackground(new java.awt.Color(72, 103, 150)); 
        crudDoctorBtn.setForeground(Color.WHITE);
        crudDoctorBtn.setFont(new Font("Perpetua", Font.BOLD, 17));
        doctorScheduleBtn.setBackground(new java.awt.Color(72, 103, 150)); 
        doctorScheduleBtn.setForeground(Color.WHITE);
        doctorScheduleBtn.setFont(new Font("Perpetua", Font.BOLD, 17));
        editProfileBtn.setBackground(new java.awt.Color(72, 103, 150)); 
        editProfileBtn.setForeground(Color.WHITE);
        editProfileBtn.setFont(new Font("Perpetua", Font.BOLD, 17));
        logoutBtn.setBackground(new java.awt.Color(0, 51, 102)); 
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFont(new Font("Perpetua", Font.BOLD, 17));
        
        //Label
        jLabel1.setForeground(Color.black);
        jLabel1.setFont(new Font("Perpetua", Font.BOLD, 15));
        jLabel2.setForeground(Color.WHITE);
        jLabel2.setFont(new Font("Perpetua", Font.PLAIN, 35));
        
        
        if (manager != null) {
            setTitle("Manager Dashboard - " + manager.getName());
            jLabel1.setText("Welcome, " + manager.getName());
        } else {
            setTitle("Manager Dashboard");
        }
        
    }
    
    public ManagerDashboard(){
        throw new IllegalStateException("Must pass a Manager when opening dashboard");   
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuItem1 = new javax.swing.JMenuItem();
        jColorChooser1 = new javax.swing.JColorChooser();
        jColorChooser2 = new javax.swing.JColorChooser();
        jColorChooser3 = new javax.swing.JColorChooser();
        jPanel3 = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel1 = new javax.swing.JPanel();
        crudManagerBtn = new javax.swing.JButton();
        appointmentBtn = new javax.swing.JButton();
        feedbackBtn = new javax.swing.JButton();
        reportBtn = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        crudStaffBtn = new javax.swing.JButton();
        crudDoctorBtn = new javax.swing.JButton();
        doctorScheduleBtn = new javax.swing.JButton();
        editProfileBtn = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        logoutBtn = new javax.swing.JButton();

        jMenuItem1.setText("jMenuItem1");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        crudManagerBtn.setText("Manage Managers");
        crudManagerBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                crudManagerBtnActionPerformed(evt);
            }
        });

        appointmentBtn.setText("View All appointments");
        appointmentBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                appointmentBtnActionPerformed(evt);
            }
        });

        feedbackBtn.setText("View Feedbacks & Comments");
        feedbackBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                feedbackBtnActionPerformed(evt);
            }
        });

        reportBtn.setText("Generate Analysed Reports");
        reportBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reportBtnActionPerformed(evt);
            }
        });

        jLabel1.setText("Manager Dashboard");

        crudStaffBtn.setText("Manage Staff");
        crudStaffBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                crudStaffBtnActionPerformed(evt);
            }
        });

        crudDoctorBtn.setText("Manage Doctors");
        crudDoctorBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                crudDoctorBtnActionPerformed(evt);
            }
        });

        doctorScheduleBtn.setText("Doctor Schedule Management");
        doctorScheduleBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doctorScheduleBtnActionPerformed(evt);
            }
        });

        editProfileBtn.setText("Edit Profile");
        editProfileBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editProfileBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(224, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(219, 219, 219))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(67, 67, 67)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(editProfileBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(reportBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(doctorScheduleBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(feedbackBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(crudDoctorBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(crudStaffBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(appointmentBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(crudManagerBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(67, 67, 67))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(crudManagerBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(crudStaffBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(crudDoctorBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                .addComponent(appointmentBtn)
                .addGap(18, 18, 18)
                .addComponent(feedbackBtn)
                .addGap(18, 18, 18)
                .addComponent(doctorScheduleBtn)
                .addGap(18, 18, 18)
                .addComponent(reportBtn)
                .addGap(18, 18, 18)
                .addComponent(editProfileBtn)
                .addGap(36, 36, 36))
        );

        jPanel2.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
                jPanel2AncestorRemoved(evt);
            }
        });

        jLabel2.setText("APU Medical Centre");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addGap(243, 243, 243))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
        );

        logoutBtn.setText("Logout");
        logoutBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logoutBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(124, 124, 124)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(333, 333, 333)
                        .addComponent(logoutBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(124, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 91, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(logoutBtn)
                .addGap(44, 44, 44))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void appointmentBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_appointmentBtnActionPerformed
       new ViewAllAppointments(loggedInManager).setVisible(true);
       this.dispose();

    }//GEN-LAST:event_appointmentBtnActionPerformed

    private void logoutBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutBtnActionPerformed
            int choice = JOptionPane.showConfirmDialog(
                 this,
                 "Are you sure you want to logout?",
                 "Confirm Logout",
                 JOptionPane.YES_NO_OPTION,
                 JOptionPane.QUESTION_MESSAGE
         );
         if (choice == JOptionPane.YES_OPTION) {
             String name = (loggedInManager != null) ? loggedInManager.getName() : "User";
             JOptionPane.showMessageDialog(
                     this,
                     "Logout successful. Thank you, " + name + "!",
                     "Logout",
                     JOptionPane.INFORMATION_MESSAGE
             );

             // ✅ Show login first, then close dashboard
             new LoginGUI().setVisible(true);
             this.dispose();
         }
    }//GEN-LAST:event_logoutBtnActionPerformed

    private void jPanel2AncestorRemoved(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_jPanel2AncestorRemoved
        // TODO add your handling code here:
    }//GEN-LAST:event_jPanel2AncestorRemoved

    private void crudManagerBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_crudManagerBtnActionPerformed
       new CrudManagerPage(loggedInManager).setVisible(true);
       this.dispose();       
    }//GEN-LAST:event_crudManagerBtnActionPerformed

    private void crudStaffBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_crudStaffBtnActionPerformed
       new CrudStaffPage(loggedInManager).setVisible(true);
       this.dispose();
    }//GEN-LAST:event_crudStaffBtnActionPerformed

    private void crudDoctorBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_crudDoctorBtnActionPerformed
    dispose();
        new CrudDoctorPage(loggedInManager).setVisible(true);
    }//GEN-LAST:event_crudDoctorBtnActionPerformed

    private void feedbackBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_feedbackBtnActionPerformed
    dispose();
        new ViewAllFeedbackAndCommentsPage(loggedInManager).setVisible(true);
    }//GEN-LAST:event_feedbackBtnActionPerformed

    private void doctorScheduleBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doctorScheduleBtnActionPerformed
     dispose();
        new DoctorScheduleManagement(loggedInManager).setVisible(true);
    }//GEN-LAST:event_doctorScheduleBtnActionPerformed

    private void reportBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reportBtnActionPerformed
     dispose();
        new AnalyzedReportPage(loggedInManager).setVisible(true);
    }//GEN-LAST:event_reportBtnActionPerformed

    private void editProfileBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editProfileBtnActionPerformed
    if (loggedInManager == null) {
        JOptionPane.showMessageDialog(
            this,
            "No manager data loaded. Please log in again.",
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
        return;
    }
    new GenericEditProfileFrame1(loggedInManager, this).setVisible(true);
    }//GEN-LAST:event_editProfileBtnActionPerformed

 // test code for login   
    public static void main(String args[]) {
    java.awt.EventQueue.invokeLater(() -> {
        // Create a mock manager for testing until login is ready
        Manager mockManager = new Manager(
            "manager01",           // id
            "John Doe",            // name
            "Male",                // gender
            "john.doe@example.com", // email
            "123456789",           // phone
            "password",            // password
            "01/01/1980"           // dob (date of birth)
        );
        new ManagerDashboard(mockManager).setVisible(true);
    });
}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton appointmentBtn;
    private javax.swing.JButton crudDoctorBtn;
    private javax.swing.JButton crudManagerBtn;
    private javax.swing.JButton crudStaffBtn;
    private javax.swing.JButton doctorScheduleBtn;
    private javax.swing.JButton editProfileBtn;
    private javax.swing.JButton feedbackBtn;
    private javax.swing.JColorChooser jColorChooser1;
    private javax.swing.JColorChooser jColorChooser2;
    private javax.swing.JColorChooser jColorChooser3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton logoutBtn;
    private javax.swing.JButton reportBtn;
    // End of variables declaration//GEN-END:variables
}
