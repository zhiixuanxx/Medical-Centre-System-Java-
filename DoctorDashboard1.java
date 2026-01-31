package GUI;

import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import model.Doctor;

import GUI.EnterChargesDiagnosis1;
import GUI.GenericEditProfileFrame1;
import GUI.LoginGUI;
import GUI.ViewPatientHistory1; 
import GUI.DoctorViewAppointments1;
import GUI.DoctorViewAppointments1;
import GUI.EnterChargesDiagnosis1;
import GUI.GenericEditProfileFrame1;
import GUI.LoginGUI;
import GUI.ViewPatientHistory1;

/**
 *
 * @author Ewen
 */
public class DoctorDashboard1 extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(DoctorDashboard1.class.getName());

    public Doctor doctor;

    public DoctorDashboard1(Doctor doctor) {
        this.doctor = doctor;
        initComponents();
        
        setTitle("Doctor Dashboard - " + doctor.getName());
        setSize(650, 550);
        setLocationRelativeTo(null);
        
        welcomeLabel.setText("Welcome Dr. " + doctor.getName());
        specialtyLabel.setText("Specialty: " + doctor.getSpecialty());
        footerLabel.setText("ID: " + doctor.getId());
        
        addHoverEffect(viewAppointmentsBtn);
        addHoverEffect(enterChargesBtn);
        addHoverEffect(viewHistoryBtn);
        addHoverEffect(editProfileBtn);
        addHoverEffect(logoutBtn);
    }
    

    
     private void addHoverEffect(JButton button) {
        Color originalColor = button.getBackground();
        Color hoverColor = originalColor.darker();

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor);
            }
        });
    }

    private void openEditProfileDialog() {
        try {
            GenericEditProfileFrame1 editFrame = new GenericEditProfileFrame1(doctor, this);
            editFrame.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Error opening profile editor: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    // Refresh header after profile updates
    public void refreshWelcomeLabel() {
        SwingUtilities.invokeLater(() -> {
            welcomeLabel.setText("Welcome, Dr. " + doctor.getName());
            setTitle("Doctor Dashboard - " + doctor.getName());

            JPanel headerPanel = (JPanel) ((JPanel) getContentPane().getComponent(0)).getComponent(0);
            JLabel specialtyLabel = (JLabel) headerPanel.getComponent(1);
            specialtyLabel.setText("Specialty: " + doctor.getSpecialty());

            repaint();
        });
    }

    private void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            JOptionPane.showMessageDialog(
                null,
                "Logout successful. Thank you, " + doctor.getName() + "!",
                "Logout",
                JOptionPane.INFORMATION_MESSAGE
            );
            new LoginGUI().setVisible(true); 
        }
    }

    public Doctor getDoctor() { return doctor; }

    public void updateDoctor(Doctor updatedDoctor) {
        this.doctor = updatedDoctor;
        refreshWelcomeLabel();
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        headerPanel = new javax.swing.JPanel();
        welcomeLabel = new javax.swing.JLabel();
        specialtyLabel = new javax.swing.JLabel();
        buttonPanel = new javax.swing.JPanel();
        viewAppointmentsBtn = new javax.swing.JButton();
        enterChargesBtn = new javax.swing.JButton();
        viewHistoryBtn = new javax.swing.JButton();
        editProfileBtn = new javax.swing.JButton();
        logoutBtn = new javax.swing.JButton();
        footerLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        headerPanel.setBackground(new java.awt.Color(230, 245, 255));
        headerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        welcomeLabel.setFont(new java.awt.Font("Perpetua", 0, 18)); // NOI18N
        welcomeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        welcomeLabel.setText(" ");

        specialtyLabel.setFont(new java.awt.Font("Perpetua", 0, 14)); // NOI18N
        specialtyLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        specialtyLabel.setText(" ");

        javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addGap(216, 216, 216)
                .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(welcomeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 241, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(specialtyLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(246, 246, 246))
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, headerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(welcomeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(specialtyLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        getContentPane().add(headerPanel, gridBagConstraints);

        buttonPanel.setBackground(new java.awt.Color(248, 249, 250));
        buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        viewAppointmentsBtn.setBackground(new java.awt.Color(52, 152, 219));
        viewAppointmentsBtn.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        viewAppointmentsBtn.setForeground(new java.awt.Color(255, 255, 255));
        viewAppointmentsBtn.setText("View Appointments");
        viewAppointmentsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewAppointmentsBtnActionPerformed(evt);
            }
        });

        enterChargesBtn.setBackground(new java.awt.Color(46, 204, 113));
        enterChargesBtn.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        enterChargesBtn.setForeground(new java.awt.Color(255, 255, 255));
        enterChargesBtn.setText("Enter Charges & Diagnosis");
        enterChargesBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enterChargesBtnActionPerformed(evt);
            }
        });

        viewHistoryBtn.setBackground(new java.awt.Color(153, 102, 255));
        viewHistoryBtn.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        viewHistoryBtn.setForeground(new java.awt.Color(255, 255, 255));
        viewHistoryBtn.setText("View Patient History");
        viewHistoryBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewHistoryBtnActionPerformed(evt);
            }
        });

        editProfileBtn.setBackground(new java.awt.Color(255, 204, 0));
        editProfileBtn.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        editProfileBtn.setForeground(new java.awt.Color(255, 255, 255));
        editProfileBtn.setText("Edit Profile");
        editProfileBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editProfileBtnActionPerformed(evt);
            }
        });

        logoutBtn.setBackground(new java.awt.Color(231, 76, 60));
        logoutBtn.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        logoutBtn.setForeground(new java.awt.Color(255, 255, 255));
        logoutBtn.setText("Logout");
        logoutBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logoutBtnActionPerformed(evt);
            }
        });

        footerLabel.setFont(new java.awt.Font("Perpetua", 0, 14)); // NOI18N
        footerLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        footerLabel.setText(" ");

        javax.swing.GroupLayout buttonPanelLayout = new javax.swing.GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addGap(246, 246, 246)
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(footerLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                    .addComponent(viewAppointmentsBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                    .addComponent(viewHistoryBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                    .addComponent(editProfileBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                    .addComponent(logoutBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                    .addComponent(enterChargesBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addGap(82, 82, 82)
                .addComponent(viewAppointmentsBtn)
                .addGap(18, 18, 18)
                .addComponent(enterChargesBtn)
                .addGap(18, 18, 18)
                .addComponent(viewHistoryBtn)
                .addGap(18, 18, 18)
                .addComponent(editProfileBtn)
                .addGap(18, 18, 18)
                .addComponent(logoutBtn)
                .addGap(32, 32, 32)
                .addComponent(footerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(60, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 249;
        gridBagConstraints.ipady = 54;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        getContentPane().add(buttonPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void viewAppointmentsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewAppointmentsBtnActionPerformed
        DoctorViewAppointments1 frame = new DoctorViewAppointments1(doctor);
        frame.setVisible(true);
    }//GEN-LAST:event_viewAppointmentsBtnActionPerformed

    private void enterChargesBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enterChargesBtnActionPerformed
        EnterChargesDiagnosis1 dialog = new EnterChargesDiagnosis1(doctor, this);
        dialog.setVisible(true);
    }//GEN-LAST:event_enterChargesBtnActionPerformed

    private void viewHistoryBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewHistoryBtnActionPerformed
        ViewPatientHistory1 dialog = new ViewPatientHistory1(doctor);
        dialog.setVisible(true);
    }//GEN-LAST:event_viewHistoryBtnActionPerformed

    private void editProfileBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editProfileBtnActionPerformed
        openEditProfileDialog();
    }//GEN-LAST:event_editProfileBtnActionPerformed

    private void logoutBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutBtnActionPerformed
        handleLogout();
    }//GEN-LAST:event_logoutBtnActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton editProfileBtn;
    private javax.swing.JButton enterChargesBtn;
    private javax.swing.JLabel footerLabel;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JButton logoutBtn;
    private javax.swing.JLabel specialtyLabel;
    private javax.swing.JButton viewAppointmentsBtn;
    private javax.swing.JButton viewHistoryBtn;
    private javax.swing.JLabel welcomeLabel;
    // End of variables declaration//GEN-END:variables
}
