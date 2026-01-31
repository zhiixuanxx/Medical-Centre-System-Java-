package view;

import model.Appointment;
import model.Doctor;
import model.MedicalCertificate;
import util.FileStorage;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Dialog for doctors to issue a Medical Certificate (MC).
 * "Preview" shows an official formatted certificate (no system print).
 */
public class IssueMC extends JDialog {
    private final Appointment appointment;
    private final Doctor doctor;

    private JTextField tfIssueDate, tfRestFrom, tfRestTo;
    private JTextArea taRemarks;
    private JButton btnSave, btnPreview, btnClose;

    public IssueMC(JFrame parent, Doctor doctor, Appointment appt) {
        super(parent, "Issue Medical Certificate (MC) - " + appt.getAppointmentId(), true);
        this.appointment = appt;
        this.doctor = doctor;

        initUI();
        setSize(520, 420);
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setLayout(new BorderLayout(10,10));
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,8,6,8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.gridy = 0;

        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Appointment ID (read-only)
        form.add(new JLabel("Appointment ID:"), c); c.gridx=1;
        JTextField tfAppt = new JTextField(appointment.getAppointmentId()); tfAppt.setEditable(false);
        form.add(tfAppt, c);

        // Customer
        c.gridx=0; c.gridy++;
        form.add(new JLabel("Customer ID:"), c); c.gridx=1;
        JTextField tfCust = new JTextField(appointment.getCustomerId()); tfCust.setEditable(false);
        form.add(tfCust, c);

        // Doctor
        c.gridx=0; c.gridy++;
        form.add(new JLabel("Doctor ID:"), c); c.gridx=1;
        JTextField tfDoc = new JTextField(doctor.getId()); tfDoc.setEditable(false);
        form.add(tfDoc, c);

        // Issue Date
        c.gridx=0; c.gridy++;
        form.add(new JLabel("Issue Date (yyyy-MM-dd):"), c); c.gridx=1;
        tfIssueDate = new JTextField(LocalDate.now().format(df));
        form.add(tfIssueDate, c);

        // Rest From
        c.gridx=0; c.gridy++;
        form.add(new JLabel("Rest From (yyyy-MM-dd):"), c); c.gridx=1;
        tfRestFrom = new JTextField(LocalDate.now().format(df));
        form.add(tfRestFrom, c);

        // Rest To
        c.gridx=0; c.gridy++;
        form.add(new JLabel("Rest To (yyyy-MM-dd):"), c); c.gridx=1;
        tfRestTo = new JTextField(LocalDate.now().plusDays(1).format(df));
        form.add(tfRestTo, c);

        // Remarks
        c.gridx=0; c.gridy++; c.gridwidth=2;
        form.add(new JLabel("Remarks (optional):"), c);
        c.gridy++;
        taRemarks = new JTextArea(5, 30);
        taRemarks.setLineWrap(true); taRemarks.setWrapStyleWord(true);
        form.add(new JScrollPane(taRemarks), c);

        add(form, BorderLayout.CENTER);

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSave = new JButton("Save");
        btnPreview = new JButton("Preview MC"); // no print, just display
        btnClose = new JButton("Close");
        buttons.add(btnSave); buttons.add(btnPreview); buttons.add(btnClose);
        add(buttons, BorderLayout.SOUTH);

        btnSave.addActionListener(e -> onSave());
        btnPreview.addActionListener(e -> onPreview());
        btnClose.addActionListener(e -> dispose());
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
            FileStorage.saveMedicalCertificate(mc);
            JOptionPane.showMessageDialog(this, "Medical Certificate saved.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
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
}
