package GUI;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Window;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import model.Appointment;
import util.FileStorage;
import model.Doctor;
import model.MedicalCertificate;


public class IssueMC extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(IssueMC.class.getName());
    private final Appointment appointment;
    private final Doctor doctor;

   
    public IssueMC(JFrame parent, Doctor doctor, Appointment appt) {
           this.appointment = appt;
           this.doctor = doctor;

           initComponents();
           setTitle("Issue Medical Certificate (MC) - " + appt.getAppointmentId());
           setLocationRelativeTo(parent);

           // Pre-fill values
           tfAppointmentId.setText(appt.getAppointmentId());
           tfCustomerId.setText(appt.getCustomerId());
           tfDoctorId.setText(doctor.getId());
           tfIssueDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

           tfAppointmentId.setEditable(false);
            tfCustomerId.setEditable(false);
            tfDoctorId.setEditable(false);
       }

    private void onSave() {
        try {
            MedicalCertificate mc = new MedicalCertificate(
                appointment.getAppointmentId(),
                appointment.getCustomerId(),
                tfIssueDate.getText().trim(),
                tfRestFrom.getText().trim(),
                tfRestTo.getText().trim(),
                doctor.getId(),
                taRemarks.getText().trim()
            );
            if (
                !normalizeAndSetDate(tfRestFrom) ||
                !normalizeAndSetDate(tfRestTo)) {
                JOptionPane.showMessageDialog(this, "Please enter all dates in DD/MM/YYYY format.");
                return;
        }
            FileStorage.saveMedicalCertificate(mc);
            JOptionPane.showMessageDialog(this, "Medical Certificate saved.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isValidDate(String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy")
                    .withResolverStyle(java.time.format.ResolverStyle.STRICT);

            // Parse only (will throw exception if invalid)
            LocalDate.parse(date, formatter);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean normalizeAndSetDate(javax.swing.JTextField field) {
        try {
            String raw = field.getText().trim();

            DateTimeFormatter inFmt = DateTimeFormatter.ofPattern("d/M/uuuu")
                    .withResolverStyle(java.time.format.ResolverStyle.STRICT);
            DateTimeFormatter outFmt = DateTimeFormatter.ofPattern("dd/MM/uuuu");

            LocalDate parsed = LocalDate.parse(raw, inFmt);
            String reformatted = parsed.format(outFmt);

            field.setText(reformatted);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Shows an official-looking MC as a styled, read-only preview dialog. */
    private void onPreview() {
        String html = buildCertificateHtml(
            safe(appointment.getAppointmentId()),
            safe(appointment.getCustomerId()),
            safe(tryGet(appointment.getCustomerName())),
            safe(doctor.getId()),
            safe(doctor.getName()),
            safe(tfIssueDate.getText().trim()),
            safe(tfRestFrom.getText().trim()),
            safe(tfRestTo.getText().trim()),
            safe(taRemarks.getText().trim())
        );
        showHtmlDialog(this, "Medical Certificate Preview", html, 720, 840);
        
    }

    // ===== Helpers =====

    private static String safe(String s) {
        if (s == null) return "";
        // Minimal HTML escape
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
                .replace("\"","&quot;").replace("'","&#39;");
    }

    private static String tryGet(String v) {
        return v == null ? "" : v;
    }

    private static void showHtmlDialog(Window parent, String title, String html, int w, int h) {
        JDialog dlg = new JDialog(parent, title, Dialog.ModalityType.APPLICATION_MODAL);
        JEditorPane pane = new JEditorPane("text/html", html);
        pane.setEditable(false);
        pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        JScrollPane sp = new JScrollPane(pane);
        sp.setBorder(BorderFactory.createEmptyBorder());
        dlg.getContentPane().add(sp, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton close = new JButton("Close");
        south.add(close);
        dlg.getContentPane().add(south, BorderLayout.SOUTH);

        close.addActionListener(e -> dlg.dispose());

        dlg.setSize(w, h);
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
    }

    /** Build a clean, printable-style HTML for the certificate (no system print invoked). */
    private static String buildCertificateHtml(
            String apptId, String custId, String custName,
            String docId, String docName,
            String issueDate, String restFrom, String restTo,
            String remarks
    ) {
        // Inline CSS for portability inside JEditorPane
        String css = """
        <style>
          body { font-family: 'Segoe UI', Arial, sans-serif; color:#111; margin:32px; }
          .sheet { max-width: 680px; margin: 0 auto; padding: 32px; border: 1px solid #C9CED6; border-radius: 12px; background:#fff; }
          .hdr { text-align:center; margin-bottom: 8px; }
          .brand { font-size: 22px; font-weight: 700; letter-spacing: 0.3px; }
          .sub { color:#555; font-size: 12px; margin-top: 2px; }
          .title { text-align:center; font-size: 18px; font-weight: 700; margin: 20px 0 16px; text-transform: uppercase; letter-spacing: 1px; }
          .divider { height: 1px; background: #E6E9EF; margin: 10px 0 20px; }
          .row { display: flex; gap: 16px; margin-bottom: 10px; }
          .col { flex: 1; }
          .label { font-size: 11px; color:#666; text-transform: uppercase; letter-spacing: .6px; }
          .val { font-size: 14px; font-weight: 600; margin-top: 2px; }
          .box { background:#F8F9FB; border:1px solid #E6E9EF; border-radius:10px; padding:12px 14px; margin: 14px 0; }
          .sign { display:flex; justify-content: space-between; align-items: end; margin-top: 30px; }
          .sigline { width: 260px; border-top:1px solid #333; height: 1px; }
          .muted { color:#666; font-size: 12px; }
          .note { font-size: 13px; line-height: 1.45; }
        </style>
        """;

        String body = """
        <div class="sheet">
          <div class="hdr">
            <div class="brand">APU Medical Centre</div>
            <div class="sub">Lot 1, Jalan Teknologi, 57000 Kuala Lumpur • +60 3-1234 5678</div>
          </div>

          <div class="title">Medical Certificate</div>
          <div class="divider"></div>

          <div class="row">
            <div class="col">
              <div class="label">Appointment ID</div>
              <div class="val">%s</div>
            </div>
            <div class="col">
              <div class="label">Issue Date</div>
              <div class="val">%s</div>
            </div>
          </div>

          <div class="row">
            <div class="col">
              <div class="label">Patient Name</div>
              <div class="val">%s</div>
            </div>
            <div class="col">
              <div class="label">Patient ID</div>
              <div class="val">%s</div>
            </div>
          </div>

          <div class="box">
            <div class="row">
              <div class="col">
                <div class="label">Rest From</div>
                <div class="val">%s</div>
              </div>
              <div class="col">
                <div class="label">Rest To</div>
                <div class="val">%s</div>
              </div>
            </div>
            <div class="row">
              <div class="col">
                <div class="label">Remarks</div>
                <div class="note">%s</div>
              </div>
            </div>
          </div>

          <div class="sign">
            <div>
              <div class="muted">Certified by</div>
              <div class="val">%s</div>
              <div class="muted">Doctor ID: %s</div>
            </div>
            <div class="sigline"></div>
          </div>

          <p class="muted" style="margin-top:16px;">This certificate is issued for work/school absence purposes as indicated.</p>
        </div>
        """.formatted(apptId, issueDate, custName.isEmpty() ? "—" : custName, custId,
                      restFrom, restTo, remarks.isEmpty() ? "—" : remarks, docName, docId);

        return "<html><head>"+css+"</head><body>"+body+"</body></html>";
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        tfAppointmentId = new javax.swing.JTextField();
        tfCustomerId = new javax.swing.JTextField();
        tfDoctorId = new javax.swing.JTextField();
        tfIssueDate = new javax.swing.JTextField();
        tfRestFrom = new javax.swing.JTextField();
        tfRestTo = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        taRemarks = new javax.swing.JTextArea();
        buttonPanel = new javax.swing.JPanel();
        saveButton = new javax.swing.JButton();
        previewMCButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        titleLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setSize(new java.awt.Dimension(520, 480));

        jPanel1.setBackground(new java.awt.Color(248, 249, 250));

        jLabel1.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        jLabel1.setText("Appointment ID : ");

        jLabel2.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        jLabel2.setText("Customer ID : ");

        jLabel3.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        jLabel3.setText("Doctor ID : ");

        jLabel4.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        jLabel4.setText("Issue Date ( DD/MM/YYYY ) : ");

        jLabel5.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        jLabel5.setText("Rest From ( DD/MM/YYYY ) : ");

        jLabel6.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        jLabel6.setText("Rest To ( DD/MM/YYYY ) :");

        jLabel7.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        jLabel7.setText("Remarks : ");

        taRemarks.setColumns(20);
        taRemarks.setRows(5);
        jScrollPane1.setViewportView(taRemarks);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7))
                        .addGap(103, 103, 103)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tfIssueDate)
                            .addComponent(tfRestFrom)
                            .addComponent(tfRestTo)
                            .addComponent(tfAppointmentId, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tfCustomerId)
                            .addComponent(tfDoctorId))))
                .addContainerGap(45, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(tfAppointmentId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(22, 22, 22)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(tfCustomerId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(28, 28, 28)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(tfDoctorId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(tfIssueDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(27, 27, 27)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(tfRestFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(35, 35, 35)
                        .addComponent(jLabel6))
                    .addComponent(tfRestTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(37, 37, 37)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(13, Short.MAX_VALUE))
        );

        saveButton.setBackground(new java.awt.Color(0, 204, 102));
        saveButton.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        saveButton.setForeground(new java.awt.Color(255, 255, 255));
        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        previewMCButton.setBackground(new java.awt.Color(102, 204, 255));
        previewMCButton.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        previewMCButton.setForeground(new java.awt.Color(255, 255, 255));
        previewMCButton.setText("Preview MC");
        previewMCButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previewMCButtonActionPerformed(evt);
            }
        });

        closeButton.setBackground(new java.awt.Color(255, 102, 102));
        closeButton.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        closeButton.setForeground(new java.awt.Color(255, 255, 255));
        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout buttonPanelLayout = new javax.swing.GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addContainerGap(221, Short.MAX_VALUE)
                .addComponent(saveButton)
                .addGap(46, 46, 46)
                .addComponent(previewMCButton)
                .addGap(46, 46, 46)
                .addComponent(closeButton)
                .addGap(46, 46, 46))
        );
        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, buttonPanelLayout.createSequentialGroup()
                .addContainerGap(14, Short.MAX_VALUE)
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveButton)
                    .addComponent(previewMCButton)
                    .addComponent(closeButton))
                .addGap(23, 23, 23))
        );

        titleLabel.setFont(new java.awt.Font("Perpetua", 1, 36)); // NOI18N
        titleLabel.setText("ISSUE MC");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(19, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(buttonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(titleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(240, 240, 240))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(20, Short.MAX_VALUE)
                .addComponent(titleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        
        tfRestFrom.setName("RestFrom");
        tfRestTo.setName("RestTo");
        onSave();
    }//GEN-LAST:event_saveButtonActionPerformed

    private void previewMCButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previewMCButtonActionPerformed
        onPreview();
    }//GEN-LAST:event_previewMCButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton previewMCButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JTextArea taRemarks;
    private javax.swing.JTextField tfAppointmentId;
    private javax.swing.JTextField tfCustomerId;
    private javax.swing.JTextField tfDoctorId;
    private javax.swing.JTextField tfIssueDate;
    private javax.swing.JTextField tfRestFrom;
    private javax.swing.JTextField tfRestTo;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
}
