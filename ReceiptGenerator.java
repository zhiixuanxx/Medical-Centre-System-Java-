package util;

import model.Appointment;
import model.AppointmentDetails;
import model.Staff;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReceiptGenerator extends JDialog {
    private final Appointment appt;
    private final AppointmentDetails details;
    private final Staff staff;
    private final String paymentId;
    private final String paymentMethod;
    private final double paidAmount;
    private final double returnAmount;

    private JButton btnClose, btnDownload;
    private JEditorPane previewPane;

    public ReceiptGenerator(JFrame parent,
                            Appointment appt,
                            AppointmentDetails details,
                            Staff staff,
                            String paymentId,
                            String paymentMethod,
                            double paidAmount,
                            double returnAmount) {
        super(parent, "Receipt Preview - " + paymentId, true);
        this.appt = appt;
        this.details = details;
        this.staff = staff;
        this.paymentId = paymentId;
        this.paymentMethod = paymentMethod;
        this.paidAmount = paidAmount;
        
        double total = details.getCharges();
        double tax = total * 0.06;
        double grandTotal = total + tax;
        this.returnAmount = paidAmount - grandTotal;

        initUI();
        setSize(720, 640);
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setLayout(new BorderLayout());

        previewPane = new JEditorPane("text/html", buildReceiptHtml());
        previewPane.setEditable(false);
        previewPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        add(new JScrollPane(previewPane), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnDownload = new JButton("Download PDF");
        JButton btnPrint = new JButton("Print Receipt"); 
        btnClose = new JButton("Close");
        bottom.add(btnDownload);
        bottom.add(btnPrint);
        bottom.add(btnClose);
        add(bottom, BorderLayout.SOUTH);

        btnClose.addActionListener(e -> dispose());
        btnPrint.addActionListener(e -> onPrint());
        btnDownload.addActionListener(e -> onDownload());
    }

    private void onPrint() {
        JOptionPane.showMessageDialog(
                this,
                "Please connect to a printer to print the receipt.",
                "Print Receipt",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
    /** Build HTML receipt (styled like invoice/receipt). */
    private String buildReceiptHtml() {
        String css = """
        <style>
          body { font-family: 'Segoe UI', Arial, sans-serif; color:#111; margin:32px; }
          .sheet { max-width: 640px; margin: 0 auto; padding: 32px; border: 1px solid #C9CED6; border-radius: 12px; background:#fff; }
          .hdr { text-align:center; margin-bottom: 8px; }
          .brand { font-size: 22px; font-weight: 700; letter-spacing: 0.3px; }
          .sub { color:#555; font-size: 12px; margin-top: 2px; }
          .title { text-align:center; font-size: 18px; font-weight: 700; margin: 20px 0 16px; text-transform: uppercase; letter-spacing: 1px; }
          .divider { height: 1px; background: #E6E9EF; margin: 10px 0 20px; }
          .row { display: flex; justify-content: space-between; margin-bottom: 8px; }
          .label { font-size: 12px; color:#666; }
          .val { font-size: 14px; font-weight: 600; }
          .amounts { margin-top:20px; border-top:1px solid #ddd; padding-top:10px; }
          .muted { color:#666; font-size: 12px; }
        </style>
        """;

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        double total = details.getCharges();
        double tax = total * 0.06; // 6% tax
        double grandTotal = total + tax;

        String html = """
        <div class="sheet">
          <div class="hdr">
            <div class="brand">APU Medical Centre</div>
            <div class="sub">Lot 1, Jalan Teknologi, 57000 Kuala Lumpur • +60 3-1234 5678</div>
          </div>

          <div class="title">Receipt</div>
          <div class="divider"></div>

          <div class="row"><div class="label">Receipt ID:</div><div class="val">%s</div></div>
          <div class="row"><div class="label">Date/Time:</div><div class="val">%s</div></div>
          <div class="row"><div class="label">Staff:</div><div class="val">%s</div></div>
          <div class="row"><div class="label">Customer:</div><div class="val">%s</div></div>
          <div class="row"><div class="label">Doctor:</div><div class="val">%s</div></div>
          <div class="row"><div class="label">Appointment ID:</div><div class="val">%s</div></div>
          <div class="row"><div class="label">Diagnosis:</div><div class="val">%s</div></div>

          <div class="amounts">
            <div class="row"><div class="label">Charges:</div><div class="val">RM %.2f</div></div>
            <div class="row"><div class="label">Tax (6%%):</div><div class="val">RM %.2f</div></div>
            <div class="row"><div class="label">Total:</div><div class="val">RM %.2f</div></div>
            <div class="divider"></div>
            <div class="row"><div class="label">Payment Method:</div><div class="val">%s</div></div>
        """.formatted(
                paymentId,
                now,
                staff.getName(),
                appt.getCustomerName(),
                appt.getDoctorName(),
                appt.getAppointmentId(),
                details.getDiagnosis(),
                total, tax, grandTotal,
                paymentMethod
        );

        if ("Cash".equalsIgnoreCase(paymentMethod)) {
            html += """
            <div class="row"><div class="label">Paid:</div><div class="val">RM %.2f</div></div>
            <div class="row"><div class="label">Change:</div><div class="val">RM %.2f</div></div>
            """.formatted(paidAmount, returnAmount);
        }

        html += """
          </div>
          <p class="muted" style="margin-top:20px;">This is a system generated receipt. Thank you for your payment.</p>
        </div>
        """;

        return "<html><head>" + css + "</head><body>" + html + "</body></html>";
    }

    private void onDownload() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Receipt PDF");
        chooser.setSelectedFile(new java.io.File("Receipt_" + paymentId + ".pdf"));

        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                // Call your PDF writer utility
                util.PDFReceiptWriter.writeReceiptPdf(
                        chooser.getSelectedFile().getAbsolutePath(),
                        appt, details, staff,
                        paymentId, paymentMethod, paidAmount, returnAmount
                );
                JOptionPane.showMessageDialog(this,
                        "Receipt saved to:\n" + chooser.getSelectedFile().getAbsolutePath(),
                        "PDF Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error saving PDF: " + ex.getMessage(),
                        "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}
 