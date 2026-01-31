package GUI;


import GUI.*;
import GUI.GenericEditProfileFrame1;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import model.Customer;
import java.awt.BorderLayout;
import GUI.LoginGUI;
import GUI.History;
import GUI.LoginGUI;
import GUI.ProvideFeedbackFrame;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Ewen
 */
public class CustomerDashboard1 extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(CustomerDashboard1.class.getName());
    
    private Customer customer;

    
    public CustomerDashboard1(Customer customer) {
        this.customer = customer;
        initComponents();
        welcomeLabel.setText("Welcome, " + customer.getName());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    public void refreshWelcomeLabel() {
        welcomeLabel.setText("Welcome, " + customer.getName());
        setTitle("Customer Dashboard - " + customer.getName());
    }

   
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        headerPanel = new javax.swing.JPanel();
        appTitle = new javax.swing.JLabel();
        welcomeLabel = new javax.swing.JLabel();
        buttonPanel = new javax.swing.JPanel();
        makeAppointmentBtn = new javax.swing.JButton();
        appointmentManagementBtn = new javax.swing.JButton();
        editProfileBtn = new javax.swing.JButton();
        provideFeedbackBtn = new javax.swing.JButton();
        logoutBtn = new javax.swing.JButton();
        viewHistoryBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        headerPanel.setBackground(new java.awt.Color(230, 245, 250));
        headerPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        headerPanel.setAlignmentX(2.0F);
        headerPanel.setAlignmentY(1.0F);
        headerPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        headerPanel.setMinimumSize(new java.awt.Dimension(100, 100));
        headerPanel.setLayout(new java.awt.GridBagLayout());

        appTitle.setFont(new java.awt.Font("Perpetua", 0, 36)); // NOI18N
        appTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        appTitle.setText("APU MEDICAL CENTRE");
        appTitle.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(26, 266, 0, 229);
        headerPanel.add(appTitle, gridBagConstraints);

        welcomeLabel.setFont(new java.awt.Font("Perpetua", 0, 18)); // NOI18N
        welcomeLabel.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 293;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 295, 37, 0);
        headerPanel.add(welcomeLabel, gridBagConstraints);

        buttonPanel.setBackground(new java.awt.Color(248, 249, 250));
        buttonPanel.setAutoscrolls(true);
        buttonPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        buttonPanel.setLayout(new java.awt.GridBagLayout());

        makeAppointmentBtn.setBackground(new java.awt.Color(153, 153, 255));
        makeAppointmentBtn.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        makeAppointmentBtn.setForeground(new java.awt.Color(255, 255, 255));
        makeAppointmentBtn.setText("Make Appointment");
        makeAppointmentBtn.setMaximumSize(new java.awt.Dimension(228, 24));
        makeAppointmentBtn.setMinimumSize(new java.awt.Dimension(228, 24));
        makeAppointmentBtn.setPreferredSize(new java.awt.Dimension(218, 24));
        makeAppointmentBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                makeAppointmentBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 65;
        gridBagConstraints.ipady = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(76, 299, 0, 264);
        buttonPanel.add(makeAppointmentBtn, gridBagConstraints);

        appointmentManagementBtn.setBackground(new java.awt.Color(102, 153, 255));
        appointmentManagementBtn.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        appointmentManagementBtn.setForeground(new java.awt.Color(255, 255, 255));
        appointmentManagementBtn.setText("Manage Upcoming Appointment");
        appointmentManagementBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                appointmentManagementBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 56;
        gridBagConstraints.ipady = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 299, 0, 0);
        buttonPanel.add(appointmentManagementBtn, gridBagConstraints);

        editProfileBtn.setBackground(new java.awt.Color(255, 204, 0));
        editProfileBtn.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        editProfileBtn.setForeground(new java.awt.Color(255, 255, 255));
        editProfileBtn.setText("Edit Profile ");
        editProfileBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editProfileBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 181;
        gridBagConstraints.ipady = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 299, 0, 0);
        buttonPanel.add(editProfileBtn, gridBagConstraints);

        provideFeedbackBtn.setBackground(new java.awt.Color(255, 153, 0));
        provideFeedbackBtn.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        provideFeedbackBtn.setForeground(new java.awt.Color(255, 255, 255));
        provideFeedbackBtn.setText("Provide Feedback");
        provideFeedbackBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                provideFeedbackBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.ipadx = 145;
        gridBagConstraints.ipady = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 299, 0, 0);
        buttonPanel.add(provideFeedbackBtn, gridBagConstraints);

        logoutBtn.setBackground(new java.awt.Color(255, 102, 102));
        logoutBtn.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        logoutBtn.setForeground(new java.awt.Color(255, 255, 255));
        logoutBtn.setText("Logout");
        logoutBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logoutBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.ipadx = 210;
        gridBagConstraints.ipady = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 299, 55, 0);
        buttonPanel.add(logoutBtn, gridBagConstraints);

        viewHistoryBtn.setBackground(new java.awt.Color(0, 204, 102));
        viewHistoryBtn.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        viewHistoryBtn.setForeground(new java.awt.Color(255, 255, 255));
        viewHistoryBtn.setText("View Appointment History");
        viewHistoryBtn.setMaximumSize(new java.awt.Dimension(228, 24));
        viewHistoryBtn.setMinimumSize(new java.awt.Dimension(228, 24));
        viewHistoryBtn.setPreferredSize(new java.awt.Dimension(228, 24));
        viewHistoryBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewHistoryBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 54;
        gridBagConstraints.ipady = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 299, 0, 0);
        buttonPanel.add(viewHistoryBtn, gridBagConstraints);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(buttonPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(headerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 856, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(headerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void viewHistoryBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewHistoryBtnActionPerformed
        new History(customer).setVisible(true);
        dispose();
    }//GEN-LAST:event_viewHistoryBtnActionPerformed

    private void logoutBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutBtnActionPerformed
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            new LoginGUI().setVisible(true);
        }
    }//GEN-LAST:event_logoutBtnActionPerformed

    private void provideFeedbackBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_provideFeedbackBtnActionPerformed
        new ProvideFeedbackFrame(customer).setVisible(true);
        dispose();
    }//GEN-LAST:event_provideFeedbackBtnActionPerformed

    private void editProfileBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editProfileBtnActionPerformed
        new GenericEditProfileFrame1(customer, this).setVisible(true);
        dispose();
    }//GEN-LAST:event_editProfileBtnActionPerformed

    private void appointmentManagementBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_appointmentManagementBtnActionPerformed
        new AppointmentManagement(customer).setVisible(true);
        dispose();
    }//GEN-LAST:event_appointmentManagementBtnActionPerformed

    private void makeAppointmentBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_makeAppointmentBtnActionPerformed
        new AppointmentForm1(customer).setVisible(true);
        dispose();
    }//GEN-LAST:event_makeAppointmentBtnActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel appTitle;
    private javax.swing.JButton appointmentManagementBtn;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton editProfileBtn;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JButton logoutBtn;
    private javax.swing.JButton makeAppointmentBtn;
    private javax.swing.JButton provideFeedbackBtn;
    private javax.swing.JButton viewHistoryBtn;
    private javax.swing.JLabel welcomeLabel;
    // End of variables declaration//GEN-END:variables
}