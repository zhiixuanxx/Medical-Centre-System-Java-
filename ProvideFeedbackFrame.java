package GUI;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import model.Appointment;
import model.Customer;
import model.Feedback;
import util.FileStorage;
import view.CustomerDashboard;

/**
 *
 * @author Ewen
 */
public class ProvideFeedbackFrame extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ProvideFeedbackFrame.class.getName());

    private Appointment selectedAppointment;
    private final Customer customer;
    private DefaultTableModel tableModel;
    
    public ProvideFeedbackFrame(Customer customer) {
        this.customer = customer;
        setTitle("Provide Feedback");
        setSize(850, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initComponents();

        String[] columnNames = { "Appointment ID", "Doctor", "Date", "Time", "Status" };

        // Create a non-editable table model
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // make table read-only
            }
        };

        // Attach model to table
        appointmentTable.setModel(tableModel);

        // Load completed appointments into the table
        loadCompletedAppointments();

        // Allow only single selection
        appointmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appointmentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectAppointment();
            }
        });

        // Initially disable bottom section
        setFeedbackSectionEnabled(false);
    }

    private void loadCompletedAppointments() {
        List<Appointment> allAppointments = FileStorage.getAppointmentsByCustomerId(customer.getId());
        List<Appointment> completedAppointments = new ArrayList<>();

        for (Appointment apt : allAppointments) {
            if ("Completed".equalsIgnoreCase(apt.getStatus())) {
                Feedback existingFeedback = FileStorage.getFeedbackByAppointmentId(apt.getAppointmentId());
                if (existingFeedback == null) {
                    completedAppointments.add(apt);
                }
            }
        }

        tableModel.setRowCount(0);

        for (Appointment apt : completedAppointments) {
            Object[] rowData = {
                apt.getAppointmentId(),
                apt.getDoctorName(),
                apt.getDate(),
                apt.getTime(),
                apt.getStatus()
            };
            tableModel.addRow(rowData);
        }

        if (completedAppointments.isEmpty()) {
            JLabel noDataLabel = new JLabel("No completed appointments available for feedback");
            noDataLabel.setHorizontalAlignment(SwingConstants.CENTER);
            noDataLabel.setFont(noDataLabel.getFont().deriveFont(Font.ITALIC));

            Container parent = appointmentTable.getParent().getParent();
            parent.removeAll();
            parent.add(noDataLabel);
            parent.revalidate();
            parent.repaint();
        }
    }

    private void selectAppointment() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow >= 0) {
            String appointmentId = (String) appointmentTable.getValueAt(selectedRow, 0);
            selectedAppointment = FileStorage.getAppointmentById(appointmentId);

            if (selectedAppointment != null) {
                selectedInfoLabel.setText(
                        selectedAppointment.getAppointmentId() + " - " +
                        selectedAppointment.getDoctorName() + " (" +
                        selectedAppointment.getDate() + " " +
                        selectedAppointment.getTime() + ")"
                );

                setFeedbackSectionEnabled(true);

                // 🔍 Check if feedback already exists
                Feedback existingFeedback = FileStorage.getFeedbackByAppointmentId(appointmentId);

                if (existingFeedback != null) {
                    // Prefill with existing values
                    doctorCommentsArea.setText(existingFeedback.getDoctorFeedback());
                    staffCommentsArea.setText(existingFeedback.getStaffFeedback());

                    doctorRatingBox.setSelectedItem(String.valueOf(existingFeedback.getDoctorRating()));
                    staffRatingBox.setSelectedItem(String.valueOf(existingFeedback.getStaffRating()));

                    // Optional: disable editing if you want feedback to be read-only
                    // setFeedbackSectionEnabled(false);
                    // submitBtn.setEnabled(false);
                } else {
                    // If no feedback yet → clear everything
                    doctorCommentsArea.setText("");
                    staffCommentsArea.setText("");
                    doctorRatingBox.setSelectedIndex(0);
                    staffRatingBox.setSelectedIndex(0);
                }
            }
        }
    }


    private void setFeedbackSectionEnabled(boolean enabled) {
        doctorCommentsArea.setEnabled(enabled);
        staffCommentsArea.setEnabled(enabled);
        doctorRatingBox.setEnabled(enabled);
        staffRatingBox.setEnabled(enabled);
        bottomPanel.setEnabled(enabled);
        submitBtn.setEnabled(enabled);
    }

    private void submitFeedback() {
        if (selectedAppointment == null) {
            JOptionPane.showMessageDialog(this, "Please select an appointment.");
            return;
        }

        String doctorComments = doctorCommentsArea.getText().trim();
        int doctorRating = Integer.parseInt((String) doctorRatingBox.getSelectedItem());
        String staffComments = staffCommentsArea.getText().trim();
        int staffRating = Integer.parseInt((String) staffRatingBox.getSelectedItem());

        if (doctorComments.isEmpty() && staffComments.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter at least one comment.");
            return;
        }

        Feedback feedback = new Feedback(
                selectedAppointment.getAppointmentId(),
                customer.getId(),
                doctorComments.isEmpty() ? "No comments" : doctorComments,
                doctorRating,
                staffComments.isEmpty() ? "No comments" : staffComments,
                staffRating
        );

        FileStorage.saveFeedback(feedback);
        JOptionPane.showMessageDialog(this, "Thank you! Your feedback has been recorded.");

        loadCompletedAppointments();

        // Reset feedback section
        selectedAppointment = null;
        setFeedbackSectionEnabled(false);
        doctorCommentsArea.setText("");
        staffCommentsArea.setText("");
        selectedInfoLabel.setText("None selected");
    }

    

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        topPanel = new javax.swing.JPanel();
        tableScrollPane = new javax.swing.JScrollPane();
        appointmentTable = new javax.swing.JTable();
        instructionLabel = new javax.swing.JLabel();
        bottomPanel = new javax.swing.JPanel();
        selectedLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        doctorCommentsArea = new javax.swing.JTextArea();
        doctorRatingBox = new javax.swing.JComboBox<>();
        staffRatingBox = new javax.swing.JComboBox<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        staffCommentsArea = new javax.swing.JTextArea();
        selectedInfoLabel = new javax.swing.JLabel();
        backBtn = new javax.swing.JButton();
        submitBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Provide Feedback");
        setBackground(new java.awt.Color(230, 245, 255));
        setFont(new java.awt.Font("Perpetua", 0, 12)); // NOI18N
        setSize(new java.awt.Dimension(850, 800));

        topPanel.setBackground(new java.awt.Color(230, 245, 255));
        topPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Select Completed Appointment", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Perpetua", 1, 18))); // NOI18N
        topPanel.setFont(new java.awt.Font("Perpetua", 0, 14)); // NOI18N

        appointmentTable.setFont(new java.awt.Font("Perpetua", 0, 14)); // NOI18N
        appointmentTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Appointment ID", "Doctor", "Date", "Time", "Status"
            }
        ));
        appointmentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                appointmentTableMouseClicked(evt);
            }
        });
        tableScrollPane.setViewportView(appointmentTable);

        instructionLabel.setFont(new java.awt.Font("Perpetua", 2, 14)); // NOI18N
        instructionLabel.setText("Click on an appointment to provide your feedback ^_^");

        javax.swing.GroupLayout topPanelLayout = new javax.swing.GroupLayout(topPanel);
        topPanel.setLayout(topPanelLayout);
        topPanelLayout.setHorizontalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, topPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tableScrollPane)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, topPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(instructionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(265, 265, 265))
        );
        topPanelLayout.setVerticalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(tableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
                .addGap(21, 21, 21)
                .addComponent(instructionLabel)
                .addContainerGap())
        );

        bottomPanel.setBackground(new java.awt.Color(230, 245, 255));
        bottomPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Provide Feedback", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Perpetua", 1, 18))); // NOI18N

        selectedLabel.setFont(new java.awt.Font("Perpetua", 1, 16)); // NOI18N
        selectedLabel.setText("Selected Appointment: ");

        jLabel2.setFont(new java.awt.Font("Perpetua", 1, 16)); // NOI18N
        jLabel2.setText("Doctor Comments : ");

        jLabel3.setFont(new java.awt.Font("Perpetua", 1, 16)); // NOI18N
        jLabel3.setText("Doctor Ratings ( 1 - 5 ) : ");

        jLabel4.setFont(new java.awt.Font("Perpetua", 1, 16)); // NOI18N
        jLabel4.setText("Staff Comments : ");

        jLabel5.setFont(new java.awt.Font("Perpetua", 1, 16)); // NOI18N
        jLabel5.setText("Staff Ratings ( 1 - 5 ) :  ");

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        doctorCommentsArea.setColumns(20);
        doctorCommentsArea.setLineWrap(true);
        doctorCommentsArea.setRows(5);
        doctorCommentsArea.setWrapStyleWord(true);
        jScrollPane1.setViewportView(doctorCommentsArea);

        doctorRatingBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4", "5" }));
        doctorRatingBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doctorRatingBoxActionPerformed(evt);
            }
        });

        staffRatingBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4", "5" }));
        staffRatingBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                staffRatingBoxActionPerformed(evt);
            }
        });

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        staffCommentsArea.setColumns(20);
        staffCommentsArea.setRows(5);
        jScrollPane2.setViewportView(staffCommentsArea);

        selectedInfoLabel.setFont(new java.awt.Font("Perpetua", 0, 14)); // NOI18N
        selectedInfoLabel.setText("None Selected");

        backBtn.setBackground(new java.awt.Color(0, 153, 204));
        backBtn.setFont(new java.awt.Font("Perpetua", 0, 14)); // NOI18N
        backBtn.setForeground(new java.awt.Color(255, 255, 255));
        backBtn.setText("Back");
        backBtn.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        backBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backBtnActionPerformed(evt);
            }
        });

        submitBtn.setBackground(new java.awt.Color(51, 204, 0));
        submitBtn.setFont(new java.awt.Font("Perpetua", 0, 14)); // NOI18N
        submitBtn.setForeground(new java.awt.Color(255, 255, 255));
        submitBtn.setText("Submit Feedback");
        submitBtn.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        submitBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout bottomPanelLayout = new javax.swing.GroupLayout(bottomPanel);
        bottomPanel.setLayout(bottomPanelLayout);
        bottomPanelLayout.setHorizontalGroup(
            bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bottomPanelLayout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(selectedLabel)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(bottomPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(backBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(62, 62, 62)
                        .addComponent(submitBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(bottomPanelLayout.createSequentialGroup()
                        .addGap(203, 203, 203)
                        .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 372, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(selectedInfoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(doctorRatingBox, javax.swing.GroupLayout.PREFERRED_SIZE, 372, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 372, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(staffRatingBox, javax.swing.GroupLayout.PREFERRED_SIZE, 372, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 32, Short.MAX_VALUE))
        );
        bottomPanelLayout.setVerticalGroup(
            bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bottomPanelLayout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectedLabel)
                    .addComponent(selectedInfoLabel))
                .addGap(31, 31, 31)
                .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35)
                .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(doctorRatingBox, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(bottomPanelLayout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(bottomPanelLayout.createSequentialGroup()
                        .addGap(41, 41, 41)
                        .addComponent(jLabel4)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(staffRatingBox, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(45, 45, 45)
                .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(submitBtn)
                    .addComponent(backBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(38, 38, 38))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(topPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(bottomPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(topPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bottomPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void submitBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitBtnActionPerformed
        submitFeedback();
    }//GEN-LAST:event_submitBtnActionPerformed

    private void backBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backBtnActionPerformed
        dispose();
        new CustomerDashboard1(customer).setVisible(true);
    }//GEN-LAST:event_backBtnActionPerformed

    private void staffRatingBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_staffRatingBoxActionPerformed

    }//GEN-LAST:event_staffRatingBoxActionPerformed

    private void doctorRatingBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doctorRatingBoxActionPerformed

    }//GEN-LAST:event_doctorRatingBoxActionPerformed

    private void appointmentTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_appointmentTableMouseClicked
        selectAppointment();
    }//GEN-LAST:event_appointmentTableMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable appointmentTable;
    private javax.swing.JButton backBtn;
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JTextArea doctorCommentsArea;
    private javax.swing.JComboBox<String> doctorRatingBox;
    private javax.swing.JLabel instructionLabel;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel selectedInfoLabel;
    private javax.swing.JLabel selectedLabel;
    private javax.swing.JTextArea staffCommentsArea;
    private javax.swing.JComboBox<String> staffRatingBox;
    private javax.swing.JButton submitBtn;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables
}
