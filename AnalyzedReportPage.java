package GUI;
import model.Appointment;
import model.AppointmentDetails;
import model.Feedback;
import model.Manager;
import util.FileStorage;
import util.PDFReportWriter;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import model.Doctor;
import java.awt.print.*;

public class AnalyzedReportPage extends javax.swing.JFrame {
    
    private final Manager loggedInManager;
    private List<Appointment> allAppointments;
    private Map<String, AppointmentDetails> detailsMap;
    private List<Feedback> allFeedback;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(AnalyzedReportPage.class.getName());
    
   
    public AnalyzedReportPage(Manager manager) {
        this.loggedInManager = manager;
        initComponents();
        initData();
        setupComboBoxes();
        setSize(800,550);
        
         //Customize bg
        getContentPane().setBackground(new java.awt.Color(230, 245, 255));
        jPanel1.setBackground(new java.awt.Color(0, 51, 102));
        
        //Label
        jLabel1.setForeground(Color.WHITE);
        jLabel1.setFont(new Font("Perpetua", Font.PLAIN, 35));
        
        previewBtn.setBackground(new java.awt.Color(72, 103, 150)); 
        previewBtn.setForeground(Color.WHITE);
        previewBtn.setFont(new Font("Perpetua", Font.BOLD, 15));
        resetBtn.setBackground(new java.awt.Color(72, 103, 150)); 
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setFont(new Font("Perpetua", Font.BOLD, 15));
        backBtn.setBackground(new java.awt.Color(72, 103, 150)); 
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("Perpetua", Font.BOLD, 15));
        
    }
    
    private void initData() {
        allAppointments = FileStorage.readAppointments();
        detailsMap = FileStorage.getAllAppointmentDetailsMap();
        try {
            allFeedback = FileStorage.getAllFeedback();
        } catch (Throwable t) {
            allFeedback = new ArrayList<>();
        }
    }
    
    private void setupComboBoxes() {
        // Report types
    reportTypeCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{
        "Select Report Type", "Financial Report", "Appointment Statistics Report", "Performance Report"
    }));
    
    List<Integer> years = inferYearsFromAppointments();
    List<String> yearOptions = new ArrayList<>();
    yearOptions.add("Select Year"); // sentinel
    
    // Convert integer years to strings
    for (Integer year : years) {
        yearOptions.add(year.toString());
    }
    
    year1Combo.setModel(new javax.swing.DefaultComboBoxModel<>(yearOptions.toArray(String[]::new)));
    year2Combo.setModel(new javax.swing.DefaultComboBoxModel<>(yearOptions.toArray(String[]::new)));
    
    year1Combo.setSelectedIndex(0);
    year2Combo.setSelectedIndex(0);
    
    // Months
    String[] months = {
        "Select Month", "01 - January", "02 - February", "03 - March", "04 - April",
        "05 - May", "06 - June", "07 - July", "08 - August", "09 - September",
        "10 - October", "11 - November", "12 - December"
    };
    month1Combo.setModel(new javax.swing.DefaultComboBoxModel<>(months));
    month2Combo.setModel(new javax.swing.DefaultComboBoxModel<>(months));
    }
    

    
    private void showPreviewDialog(String reportType, String html) {
        JDialog dlg = new JDialog(this, "Report Preview", true);
        dlg.setLayout(new BorderLayout());

        JEditorPane pane = new JEditorPane("text/html", html);
        pane.setEditable(false);
        JScrollPane sp = new JScrollPane(pane);
        dlg.add(sp, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton exportBtn = new JButton("Export CSV");
        JButton exportPdfBtn = new JButton("Download PDF");   
        JButton printBtn = new JButton("Print Report");      
        JButton closeBtn = new JButton("Close");

        exportBtn.addActionListener(e -> exportCsv(reportType));
        exportPdfBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File(reportType.replace(" ", "_") + ".pdf"));
            int res = chooser.showSaveDialog(this);

            if (res == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (file != null) {
                    try {
                        PDFReportWriter.writeReportPdf(
                                file.getAbsolutePath(),
                                reportType,
                                html,
                                loggedInManager.getName()
                        );
                        JOptionPane.showMessageDialog(this, "PDF exported successfully.");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage());
                    }
                }
            } else {
                System.out.println("PDF export cancelled by user.");
            }
        }); 
        printBtn.addActionListener(e -> onPrintReport());              
        closeBtn.addActionListener(e -> dlg.dispose());

        south.add(exportBtn);
        south.add(exportPdfBtn);
        south.add(printBtn);
        south.add(closeBtn);

        dlg.add(south, BorderLayout.SOUTH);
        dlg.setSize(900, 700);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void onPrintReport() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Analyzed Report");

        // Use a Printable implementation to print the report content
        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) {
                return Printable.NO_SUCH_PAGE;
            }

            this.printAll(graphics);
            return Printable.PAGE_EXISTS;
        });

        try {
            if (job.printDialog()) {  // shows printer dialog
                job.print();
                JOptionPane.showMessageDialog(this, "Report sent to printer.");
            } else {
                JOptionPane.showMessageDialog(this, "Printing cancelled.");
            }
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(
                this,
                "No printer found or printing failed: " + ex.getMessage(),
                "Print Error",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void exportCsv(String reportType) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(reportType.replace(" ", "_") + "_RawData.csv"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter w = new PrintWriter(new FileWriter(chooser.getSelectedFile()))) {

                switch (reportType) {
                    case "Financial Report" -> exportPaymentsRaw(w);
                    case "Appointment Statistics Report" -> exportAppointmentsRaw(w);
                    case "Performance Report" -> exportFeedbackRaw(w);
                    default -> JOptionPane.showMessageDialog(this, "Unsupported report type for raw export.");
                }

                JOptionPane.showMessageDialog(this, "CSV exported successfully.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage());
            }
        }
    }


    private String buildFinancialReportHtml(Integer y1, Integer m1, Integer y2, Integer m2) {
        FinancialStats s1 = computeFinancial(y1, m1);
        FinancialStats s2 = (y2 != null) ? computeFinancial(y2, m2) : null;

        String period = buildPeriodLine(y1, m1, y2, m2);

        StringBuilder sb = new StringBuilder(standardHtmlHead());
        sb.append("<div class='sheet'><div class='hdr'><div class='brand'>APU Medical Centre</div></div>");
        sb.append("<div class='title'>Financial Report</div>");
        sb.append("<div class='period'>").append(escape(period)).append("</div>");
        sb.append("<table class='tbl'><tr><th>Metric</th><th>").append(sideLabel(y1, m1)).append("</th>");
        if (s2 != null) sb.append("<th>").append(sideLabel(y2, m2)).append("</th><th>Change</th>");
        sb.append("</tr>");

        // Existing report
        sb.append(row("Total Revenue (RM)", fmtMoney(s1.totalRevenue),
                s2 != null ? fmtMoney(s2.totalRevenue) : null,
                s2 != null ? diffPct(s1.totalRevenue, s2.totalRevenue) : null));

        sb.append(row("Completed Appointments", String.valueOf(s1.completedCount),
                s2 != null ? String.valueOf(s2.completedCount) : null,
                s2 != null ? diffNum(s1.completedCount, s2.completedCount) : null));

        sb.append(row("Average Revenue / Appt (RM)", fmtMoney(s1.avgRevenuePerAppt),
                s2 != null ? fmtMoney(s2.avgRevenuePerAppt) : null,
                s2 != null ? diffPct(s1.avgRevenuePerAppt, s2.avgRevenuePerAppt) : null));

        // New
        sb.append(row("Top Doctor by Revenue", s1.topDoctorName + " (" + fmtMoney(s1.topDoctorRevenue) + ")",
                (s2 != null ? s2.topDoctorName + " (" + fmtMoney(s2.topDoctorRevenue) + ")" : null),
                null));

        sb.append(row("Clinical Revenue (RM)", fmtMoney(s1.clinicalRevenue),
                s2 != null ? fmtMoney(s2.clinicalRevenue) : null,
                s2 != null ? diffPct(s1.clinicalRevenue, s2.clinicalRevenue) : null));

        sb.append(row("On-Call Revenue (RM)", fmtMoney(s1.oncallRevenue),
                s2 != null ? fmtMoney(s2.oncallRevenue) : null,
                s2 != null ? diffPct(s1.oncallRevenue, s2.oncallRevenue) : null));

        sb.append("</table><p class='summary'>Summary: ");
        if (s2 == null) {
            sb.append("For ").append(escape(sideLabel(y1, m1))).append(", total revenue was ")
                    .append(fmtMoney(s1.totalRevenue)).append(" with top doctor ")
                    .append(s1.topDoctorName).append(".");
        } else {
            sb.append("Revenue moved ").append(diffPct(s1.totalRevenue, s2.totalRevenue))
                    .append(" from ").append(escape(sideLabel(y1, m1)))
                    .append(" to ").append(escape(sideLabel(y2, m2)))
                    .append(". Top doctor in ").append(sideLabel(y1, m1)).append(" was ")
                    .append(s1.topDoctorName).append(".");
        }
        sb.append("</p></div>");
        return sb.toString();
    }

    private String buildAppointmentStatsReportHtml(Integer y1, Integer m1, Integer y2, Integer m2) {
    ApptStats a1 = computeAppointmentStats(y1, m1);
    ApptStats a2 = (y2 != null) ? computeAppointmentStats(y2, m2) : null;

    String period = buildPeriodLine(y1, m1, y2, m2);

    StringBuilder sb = new StringBuilder(standardHtmlHead());
    sb.append("<div class='sheet'><div class='hdr'><div class='brand'>APU Medical Centre</div></div>");
    sb.append("<div class='title'>Appointment Statistics Report</div>");
    sb.append("<div class='period'>").append(escape(period)).append("</div>");

    // status table
    sb.append("<h4>Main Appointment Status</h4>");
    sb.append("<table class='tbl'><tr><th>Status</th><th>").append(sideLabel(y1, m1)).append("</th>");
    if (a2 != null) sb.append("<th>").append(sideLabel(y2, m2)).append("</th><th>Change</th>");
    sb.append("</tr>");

    sb.append(row("Completed", String.valueOf(a1.completed),
            a2 != null ? String.valueOf(a2.completed) : null,
            a2 != null ? diffNum(a1.completed, a2.completed) : null));
    sb.append(row("Cancelled", String.valueOf(a1.cancelled),
            a2 != null ? String.valueOf(a2.cancelled) : null,
            a2 != null ? diffNum(a1.cancelled, a2.cancelled) : null));
    sb.append(row("Upcoming", String.valueOf(a1.upcoming),
            a2 != null ? String.valueOf(a2.upcoming) : null,
            a2 != null ? diffNum(a1.upcoming, a2.upcoming) : null));
    sb.append(row("Total", String.valueOf(a1.total),
            a2 != null ? String.valueOf(a2.total) : null,
            a2 != null ? diffNum(a1.total, a2.total) : null));

    sb.append("</table>");

    // speciality
    sb.append("<h4>Appointments by Specialty</h4>");
    sb.append("<table class='tbl'><tr><th>Specialty</th><th>Appointments</th></tr>");
    for (Map.Entry<String, Integer> e : a1.bySpecialty.entrySet()) {
        sb.append("<tr><td>").append(escape(e.getKey())).append("</td><td>")
          .append(e.getValue()).append("</td></tr>");
    }
    sb.append("</table>");

    //common speciality
    String mostCommonSpecialty = mostCommon(a1.bySpecialty);
    sb.append("<h4>Most Common Specialty</h4>");
    sb.append("<p>").append(escape(mostCommonSpecialty)).append("</p>");

    // summary
    sb.append("<p class='summary'>Summary: ");
    if (a1.bySpecialty.isEmpty()) {
        sb.append("No appointments were recorded for this period.");
    } else {
        String topSpec = mostCommonSpecialty; // Using the mostCommon method
        int topCount = a1.bySpecialty.getOrDefault(topSpec, 0);

        if (a2 == null) {
            sb.append("For ").append(escape(sideLabel(y1, m1))).append(", total appointments were ")
              .append(a1.total).append(". The busiest specialty was ")
              .append(topSpec).append(" with ").append(topCount).append(" appointments.");
        } else {
            String mostCommonSpecialty2 = (a2 != null) ? mostCommon(a2.bySpecialty) : "-";
            sb.append("From ").append(escape(sideLabel(y1, m1))).append(" to ")
              .append(escape(sideLabel(y2, m2))).append(", total appointments changed ")
              .append(diffNum(a1.total, a2.total)).append(". In ")
              .append(sideLabel(y1, m1)).append(", busiest specialty was ")
              .append(topSpec).append(" with ").append(topCount).append(" appointments.")
              .append(" In ").append(sideLabel(y2, m2)).append(", busiest specialty was ")
              .append(mostCommonSpecialty2).append(".");
        }
    }
    sb.append("</p></div>");
    return sb.toString();
}
    

    private String buildPerformanceReportHtml(Integer y1, Integer m1, Integer y2, Integer m2) {
        PerformanceStats p1 = computePerformance(y1, m1);
        PerformanceStats p2 = (y2 != null) ? computePerformance(y2, m2) : null;

        String period = buildPeriodLine(y1, m1, y2, m2);

        StringBuilder sb = new StringBuilder(standardHtmlHead());
        sb.append("<div class='sheet'><div class='hdr'><div class='brand'>APU Medical Centre</div></div>");
        sb.append("<div class='title'>Performance Report</div>");
        sb.append("<div class='period'>").append(escape(period)).append("</div>");
        sb.append("<table class='tbl'><tr><th>Metric</th><th>").append(sideLabel(y1, m1)).append("</th>");
        if (p2 != null) sb.append("<th>").append(sideLabel(y2, m2)).append("</th><th>Change</th>");
        sb.append("</tr>");

        // table
        sb.append(row("Average Doctor Rating", fmtRating(p1.avgDoctorRating),
                p2 != null ? fmtRating(p2.avgDoctorRating) : null,
                p2 != null ? diffPct(p1.avgDoctorRating, p2.avgDoctorRating) : null));
        sb.append(row("Average Staff Rating", fmtRating(p1.avgStaffRating),
                p2 != null ? fmtRating(p2.avgStaffRating) : null,
                p2 != null ? diffPct(p1.avgStaffRating, p2.avgStaffRating) : null));
        sb.append(row("Doctor Feedback Count", String.valueOf(p1.docCnt),
                p2 != null ? String.valueOf(p2.docCnt) : null,
                p2 != null ? diffNum(p1.docCnt, p2.docCnt) : null));
        sb.append(row("Staff Feedback Count", String.valueOf(p1.staffCnt),
                p2 != null ? String.valueOf(p2.staffCnt) : null,
                p2 != null ? diffNum(p1.staffCnt, p2.staffCnt) : null));
        sb.append(row("Total Feedback Count", String.valueOf(p1.count),
                p2 != null ? String.valueOf(p2.count) : null,
                p2 != null ? diffNum(p1.count, p2.count) : null));

        sb.append("</table><h4>Average Doctor Ratings by Specialty</h4>");
        sb.append("<table class='tbl'><tr><th>Specialty</th><th>Avg Rating</th></tr>");
        for (Map.Entry<String, Double> e : p1.avgBySpecialty.entrySet()) {
            sb.append("<tr><td>").append(escape(e.getKey())).append("</td><td>")
              .append(fmtRating(e.getValue())).append("</td></tr>");
        }
        sb.append("</table>");

        sb.append("<p class='summary'>Summary: ");
        if (p2 == null) {
            sb.append("For ").append(escape(sideLabel(y1, m1)))
              .append(", doctor rating was ").append(fmtRating(p1.avgDoctorRating))
              .append(" (highest ").append(fmtRating(p1.highestDoctor))
              .append(", lowest ").append(fmtRating(p1.lowestDoctor)).append("), ")
              .append("and staff rating was ").append(fmtRating(p1.avgStaffRating))
              .append(" (highest ").append(fmtRating(p1.highestStaff))
              .append(", lowest ").append(fmtRating(p1.lowestStaff)).append("). ");
        } else {
            sb.append("From ").append(escape(sideLabel(y1, m1))).append(" to ")
              .append(escape(sideLabel(y2, m2))).append(", doctor ratings ")
              .append(diffPct(p1.avgDoctorRating, p2.avgDoctorRating))
              .append(", staff ratings ").append(diffPct(p1.avgStaffRating, p2.avgStaffRating))
              .append(". Top specialty in ").append(sideLabel(y1, m1)).append(" was ")
              .append(p1.avgBySpecialty.entrySet().stream()
                      .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("-"))
              .append(".");
        }
        sb.append("</p>");

        return sb.toString();
    }

    
    private FinancialStats computeFinancial(int year, Integer month) {
        List<model.Payment> allPayments = FileStorage.readPayments();
        List<Appointment> allAppointments = FileStorage.readAppointments(); //get appointment status
        Map<String, Appointment> apptMap = new HashMap<>();
        for (Appointment a : allAppointments) {
            apptMap.put(a.getAppointmentId(), a);
        }

        List<Doctor> doctors = FileStorage.getAllDoctors();
        Map<String, String> doctorTypeMap = new HashMap<>();
        for (Doctor d : doctors) {
            doctorTypeMap.put(d.getId(), d.getDoctorType());
        }

        double revenue = 0;
        int count = 0;
        double clinicalRevenue = 0;
        double oncallRevenue = 0;

        Map<String, Double> revenueByDoctor = new HashMap<>();

        for (model.Payment p : allPayments) {
            LocalDate dt = parseFlexibleDate(p.getDate());
            if (dt == null) continue;
            if (dt.getYear() != year) continue;
            if (month != null && dt.getMonthValue() != month) continue;

            Appointment appt = apptMap.get(p.getAppointmentId());
            if (appt == null || !"Paid".equalsIgnoreCase(appt.getpaymentStatus())) {
                continue; // skip unpaid / null
            }
        
            double amt = p.getTotal();
            revenue += amt;
            count++;

            // doctor breakdown
            String doctorId = p.getDoctorId();
            revenueByDoctor.put(doctorId, revenueByDoctor.getOrDefault(doctorId, 0.0) + amt);

            // doctor type breakdown 
            String docType = doctorTypeMap.getOrDefault(doctorId, "Clinical");
            if (docType.toLowerCase().contains("on-call")) {
                oncallRevenue += amt;
            } else {
                clinicalRevenue += amt;
            }
        }

        // find top doctor
        String topDoc = "-";
        double topRevenue = 0;
        for (Map.Entry<String, Double> e : revenueByDoctor.entrySet()) {
            if (e.getValue() > topRevenue) {
                topRevenue = e.getValue();
                topDoc = e.getKey();
            }
        }

        FinancialStats s = new FinancialStats();
        s.totalRevenue = round2(revenue);
        s.completedCount = count;
        s.avgRevenuePerAppt = (count == 0) ? 0 : round2(revenue / count);
        s.clinicalRevenue = round2(clinicalRevenue);
        s.oncallRevenue = round2(oncallRevenue);
        s.topDoctorName = topDoc;
        s.topDoctorRevenue = round2(topRevenue);

        return s;
    }

    private ApptStats computeAppointmentStats(int year, Integer month) {
        List<Appointment> list = filterAppointmentsByYearMonth(allAppointments, year, month);

        // Build a map of doctorId until specialty
        Map<String, String> doctorSpecMap = new HashMap<>();
        for (Doctor d : FileStorage.getAllDoctors()) {
            doctorSpecMap.put(d.getId(), d.getSpecialty());
        }

        ApptStats s = new ApptStats();

        for (Appointment a : list) {
            String st = safe(a.getStatus()).toLowerCase();
            String pay = safe(a.getpaymentStatus()).toLowerCase();
            
            switch (st) {
                case "completed" -> {
                if ("paid".equals(pay)) {
                    s.completed++; //only count completed if paid
                }
            }
                case "cancelled" -> s.cancelled++;
                case "upcoming" -> s.upcoming++;
                default -> {
                }
            }

            // Lookup doctor specialty
            String spec = doctorSpecMap.getOrDefault(a.getDoctorId(), "Unknown");
            s.bySpecialty.put(spec, s.bySpecialty.getOrDefault(spec, 0) + 1);
        }

        s.total = list.size();
        return s;
    }

    private PerformanceStats computePerformance(int year, Integer month) {
        // Build doctor map once
        Map<String, Doctor> doctorMap = new HashMap<>();
        for (Doctor d : FileStorage.getAllDoctors()) {
            doctorMap.put(d.getId(), d);
        }

        Map<String, LocalDate> apptDates = new HashMap<>();
        Map<String, String> apptDoctor = new HashMap<>();

        for (Appointment a : allAppointments) {
            LocalDate dt = parseFlexibleDate(a.getDate());
            if (dt != null) {
                apptDates.put(a.getAppointmentId(), dt);
                apptDoctor.put(a.getAppointmentId(), a.getDoctorId());
            }
        }

        double docSum = 0, staffSum = 0;
        int docCnt = 0, staffCnt = 0, fbCnt = 0;
        double highestDoc = 0, lowestDoc = 6;
        double highestStaff = 0, lowestStaff = 6;

        Map<String, Double> specSum = new HashMap<>();
        Map<String, Integer> specCnt = new HashMap<>();

        for (Feedback f : allFeedback) {
            LocalDate dt = apptDates.get(f.getAppointmentId());
            if (dt == null || dt.getYear() != year) continue;
            if (month != null && dt.getMonthValue() != month) continue;

            fbCnt++;
            if (f.getDoctorRating() > 0 && f.getDoctorRating() <= 5) {
                docSum += f.getDoctorRating();
                docCnt++;
                highestDoc = Math.max(highestDoc, f.getDoctorRating());
                lowestDoc = Math.min(lowestDoc, f.getDoctorRating());

                //Lookup doctor specialty thru doctorId
                String docId = apptDoctor.get(f.getAppointmentId());
                Doctor d = doctorMap.get(docId);
                if (d != null && d.getSpecialty() != null) {
                    String sp = d.getSpecialty();
                    specSum.put(sp, specSum.getOrDefault(sp, 0.0) + f.getDoctorRating());
                    specCnt.put(sp, specCnt.getOrDefault(sp, 0) + 1);
                }
            }

            if (f.getStaffRating() > 0 && f.getStaffRating() <= 5) {
                staffSum += f.getStaffRating();
                staffCnt++;
                highestStaff = Math.max(highestStaff, f.getStaffRating());
                lowestStaff = Math.min(lowestStaff, f.getStaffRating());
            }
        }

        PerformanceStats p = new PerformanceStats();
        p.avgDoctorRating = docCnt == 0 ? 0 : round2(docSum / docCnt);
        p.avgStaffRating  = staffCnt == 0 ? 0 : round2(staffSum / staffCnt);
        p.count = fbCnt;
        p.docCnt = docCnt;
        p.staffCnt = staffCnt;
        p.highestDoctor = docCnt == 0 ? 0 : highestDoc;
        p.lowestDoctor = docCnt == 0 ? 0 : lowestDoc;
        p.highestStaff = staffCnt == 0 ? 0 : highestStaff;
        p.lowestStaff = staffCnt == 0 ? 0 : lowestStaff;

        for (String sp : specSum.keySet()) {
            p.avgBySpecialty.put(sp, round2(specSum.get(sp) / specCnt.get(sp)));
        }

        return p;
    }

    private List<Appointment> filterAppointmentsByYearMonth(List<Appointment> in, int year, Integer month) {
        List<Appointment> out = new ArrayList<>();
        for (Appointment a : in) {
            LocalDate dt = parseFlexibleDate(a.getDate());
            if (dt==null) continue;
            if (dt.getYear()!=year) continue;
            if (month!=null && dt.getMonthValue()!=month) continue;
            out.add(a);
        }
        return out;
    }

    // formatting

    private static String standardHtmlHead() {
        return """
        <style>
          body { font-family: 'Segoe UI', Arial; margin:32px; color:#111; }
          .sheet { max-width:820px; margin:auto; padding:28px; border:1px solid #C9CED6; border-radius:12px; background:#fff; }
          .hdr { text-align:center; }
          .brand { font-size:24px; font-weight:800; }
          .title { text-align:center; font-size:18px; font-weight:700; margin:10px 0 4px; }
          .period { text-align:center; font-size:13px; color:#666; margin-bottom:14px; }
          table.tbl { width:100%; border-collapse:collapse; font-size:14px; }
          table.tbl th, table.tbl td { border:1px solid #E0E5EC; padding:8px 10px; text-align:left; }
          table.tbl th { background:#F5F7FA; font-weight:700; }
          .summary { margin-top:15px; font-style:italic; }
        </style>
        """;
    }

    private String row(String metric, String v1, String v2, String change) {
        StringBuilder r = new StringBuilder("<tr><td>").append(escape(metric)).append("</td><td>").append(escape(v1)).append("</td>");
        if (v2 != null) r.append("<td>").append(escape(v2)).append("</td><td>").append(escape(change)).append("</td>");
        r.append("</tr>");
        return r.toString();
    }

    private static String fmtMoney(double v) { return String.format("RM %.2f", v); }
    private static String fmtRating(double v) { return String.format("%.2f / 5", v); }
    private static double round2(double v) { return Math.round(v*100.0)/100.0; }

    private static String diffPct(double a, double b) {
        if (a == 0 && b == 0) return "0.0%";
        if (a == 0) return "+∞%";
        double pct = ((b - a) / Math.abs(a)) * 100.0;
        return String.format("%+.1f%%", pct);
    }

    private static String diffNum(int a, int b) { return String.format("%+d", b - a); }

    private Integer getSelectedMonthOrNull(JComboBox<String> combo) {
        int idx = combo.getSelectedIndex();
        return (idx <= 0) ? null : idx; 
    }

    private List<String> combineYears(List<Integer> years) {
    List<String> out = new ArrayList<>();
    out.add("Select Year"); 
    for (Integer year : years) {
        out.add(year.toString());
    }
    return out;
}

private List<Integer> inferYearsFromAppointments() {
    Set<Integer> yrs = new TreeSet<>();
    for (Appointment a : allAppointments) {
        LocalDate dt = parseFlexibleDate(a.getDate());
        if (dt != null) yrs.add(dt.getYear());
    }
    if (yrs.isEmpty()) {
        int y = LocalDate.now().getYear();
        return Arrays.asList(y - 1, y, y + 1);
    }
    return new ArrayList<>(yrs);
}
    

    private static LocalDate parseFlexibleDate(String s) {
        if (s == null) return null;
        String[] fmts = {"dd/MM/yyyy", "dd-MM-yyyy", "yyyy-MM-dd", "yyyy/MM/dd"};
        for (String f : fmts) {
            try { return LocalDate.parse(s.trim(), DateTimeFormatter.ofPattern(f)); }
            catch (Exception ignored) {}
        }
        try {
            if (s.contains("/")) {
                String[] parts = Arrays.stream(s.split("/")).map(String::trim).toArray(String[]::new);
                if (parts.length == 3) {
                    return LocalDate.parse(String.join("/", parts), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String monthLabel(Integer month) {
        if (month == null) return "";
        String[] m = {"", "Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        if (month >= 1 && month <= 12) return m[month];
        return "";
    }

    private String sideLabel(Integer y, Integer m) {
        if (y == null || y == 0) return "";
        String ml = monthLabel(m);
        return ml.isEmpty() ? ("" + y) : (ml + " " + y);
    }

    private String buildPeriodLine(Integer y1, Integer m1, Integer y2, Integer m2) {
        if (y1 != null && y1 != 0 && y2 != null && y2 != 0) {
            return sideLabel(y1, m1) + " vs " + sideLabel(y2, m2);
        } else if (y1 != null && y1 != 0) {
            return sideLabel(y1, m1) + " Only";
        }
        return "No period selected";
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
                .replace("\"","&quot;").replace("'","&#39;");
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }

    private static class FinancialStats {
        double totalRevenue;
        int completedCount;
        double avgRevenuePerAppt;
        double clinicalRevenue;
        double oncallRevenue;
        String topDoctorName;
        double topDoctorRevenue;
    }

    private static class ApptStats {
        int completed;
        int cancelled;
        int upcoming;
        int total;
        Map<String, Integer> bySpecialty = new LinkedHashMap<>();
    }

    private static class PerformanceStats {
        double avgDoctorRating;
        double avgStaffRating;
        int count;        // total feedback count
        int docCnt;       // number of doctor ratings given
        int staffCnt;     // number of staff ratings given
        double highestDoctor;
        double lowestDoctor;
        double highestStaff;
        double lowestStaff;
        Map<String, Double> avgBySpecialty = new HashMap<>();
    }
    
    private String mostCommon(Map<String, Integer> map) {
        if (map.isEmpty()) return "-";
        return map.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("-");
    }
    
    private void exportAppointmentsRaw(PrintWriter w) {
        w.println("AppointmentID,CustomerID,CustomerName,DoctorID,DoctorName,Date,Time,Status,Payment Status");
        for (Appointment a : FileStorage.readAppointments()) {
            w.println(String.join(",",
                    a.getAppointmentId(),
                    a.getCustomerId(),
                    a.getCustomerName(),
                    a.getDoctorId(),
                    a.getDoctorName(),
                    a.getDate(),
                    a.getTime(),
                    a.getStatus(),
                    a.getpaymentStatus()
            ));
        }
    }
    
    private void exportPaymentsRaw(PrintWriter w) {
        w.println("ReceiptID,AppointmentID,CustomerID,CustomerName,DoctorID,DoctorName,Date,Time,Diagnosis,Charges,Tax,Total,Method,Paid,Change,StaffID,StaffName,Timestamp");
        for (model.Payment p : FileStorage.readPayments()) {
            w.println(String.join(",",
                    p.getReceiptId(),
                    p.getAppointmentId(),
                    p.getCustomerId(),
                    p.getCustomerName(),
                    p.getDoctorId(),
                    p.getDoctorName(),
                    p.getDate(),
                    p.getTime(),
                    p.getDiagnosis(),
                    String.format("%.2f", p.getCharges()),
                    String.format("%.2f", p.getTax()),
                    String.format("%.2f", p.getTotal()),
                    p.getPaymentMethod(),
                    String.format("%.2f", p.getPaidAmount()),
                    String.format("%.2f", p.getChangeAmount()),
                    p.getStaffId(),
                    p.getStaffName(),
                    p.getTimestamp()
            ));
        }
    }
    
    private void exportFeedbackRaw(PrintWriter w) {
        w.println("AppointmentID,CustomerID,DoctorFeedback,DoctorRating,StaffFeedback,StaffRating");
        for (Feedback f : FileStorage.getAllFeedback()) {
            String docFeedback = safeCsv(f.getDoctorFeedback());
            String staffFeedback = safeCsv(f.getStaffFeedback());

            String docRating = (f.getDoctorRating() < 0) ? "" : String.valueOf(f.getDoctorRating());
            String staffRating = (f.getStaffRating() < 0) ? "" : String.valueOf(f.getStaffRating());

            w.println(String.join(",",
                    safeCsv(f.getAppointmentId()),
                    safeCsv(f.getCustomerId()),
                    docFeedback,
                    docRating,
                    staffFeedback,
                    staffRating
            ));
        }
    }

    private String safeCsv(String input) {
        if (input == null) return "";
        return input.replace(",", ";");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        label1 = new java.awt.Label();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        reportTypeCombo = new javax.swing.JComboBox<>();
        year1Combo = new javax.swing.JComboBox<>();
        year2Combo = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        month1Combo = new javax.swing.JComboBox<>();
        month2Combo = new javax.swing.JComboBox<>();
        previewBtn = new javax.swing.JButton();
        resetBtn = new javax.swing.JButton();
        backBtn = new javax.swing.JButton();

        label1.setText("label1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Generate Analysed Report");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(281, 281, 281)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(46, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(41, 41, 41))
        );

        jLabel2.setText("Report Type:");

        jLabel3.setText("Year 1:");

        jLabel4.setText("Year 2 (optional) :");

        reportTypeCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        reportTypeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reportTypeComboActionPerformed(evt);
            }
        });

        year1Combo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        year1Combo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                year1ComboActionPerformed(evt);
            }
        });

        year2Combo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        year2Combo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                year2ComboActionPerformed(evt);
            }
        });

        jLabel5.setText("Month 1:");

        jLabel6.setText("Month 2 (optional) :");

        month1Combo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        month1Combo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                month1ComboActionPerformed(evt);
            }
        });

        month2Combo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        month2Combo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                month2ComboActionPerformed(evt);
            }
        });

        previewBtn.setText("Preview Report");
        previewBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previewBtnActionPerformed(evt);
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(242, 242, 242)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(reportTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(74, 74, 74)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(42, 42, 42)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(year1Combo, 0, 154, Short.MAX_VALUE)
                            .addComponent(year2Combo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(85, 85, 85)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(month2Combo, 0, 154, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(month1Combo, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(58, 58, 58))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(previewBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(292, 292, 292))
            .addGroup(layout.createSequentialGroup()
                .addGap(197, 197, 197)
                .addComponent(resetBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(137, 137, 137)
                .addComponent(backBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(reportTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(34, 34, 34)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(year1Combo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(month1Combo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(68, 68, 68)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(year2Combo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(month2Combo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(63, 63, 63)
                .addComponent(previewBtn)
                .addGap(32, 32, 32)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(resetBtn)
                    .addComponent(backBtn))
                .addContainerGap(106, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void year1ComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_year1ComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_year1ComboActionPerformed

    private void year2ComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_year2ComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_year2ComboActionPerformed

    private void reportTypeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reportTypeComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_reportTypeComboActionPerformed

    private void month1ComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_month1ComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_month1ComboActionPerformed

    private void month2ComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_month2ComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_month2ComboActionPerformed

    private void previewBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previewBtnActionPerformed
        String reportType = (String) reportTypeCombo.getSelectedItem();
    String y1Str = (String) year1Combo.getSelectedItem();
    String y2Str = (String) year2Combo.getSelectedItem();
    
    // Convert string selections to integers (handle "Select Year" case)
    Integer y1 = null;
    Integer y2 = null;
    
    if (y1Str != null && !y1Str.equals("Select Year")) {
        try {
            y1 = Integer.valueOf(y1Str);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid year format.");
            return;
        }
    }
    
    if (y2Str != null && !y2Str.equals("Select Year")) {
        try {
            y2 = Integer.valueOf(y2Str);
        } catch (NumberFormatException e) {
            y2 = null;
        }
    }
    
    Integer m1 = getSelectedMonthOrNull(month1Combo);
    Integer m2 = getSelectedMonthOrNull(month2Combo);

    if (reportType == null || reportType.startsWith("Select")) {
        JOptionPane.showMessageDialog(this, "Please select a report type.");
        return;
    }
    if (y1 == null) {
        JOptionPane.showMessageDialog(this, "Please select Year 1.");
        return;
    }

    String html;
    switch (reportType) {
        case "Financial Report":
            html = buildFinancialReportHtml(y1, m1, y2, m2);
            break;
        case "Appointment Statistics Report":
            html = buildAppointmentStatsReportHtml(y1, m1, y2, m2);
            break;
        case "Performance Report":
            html = buildPerformanceReportHtml(y1, m1, y2, m2);
            break;
        default:
            html = "<html><body><h3>Unsupported report.</h3></body></html>";
    }

    showPreviewDialog(reportType, html);
    }//GEN-LAST:event_previewBtnActionPerformed

    private void resetBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetBtnActionPerformed
        reportTypeCombo.setSelectedIndex(0);
        year1Combo.setSelectedIndex(0);  
        year2Combo.setSelectedIndex(0);
        month1Combo.setSelectedIndex(0); 
        month2Combo.setSelectedIndex(0);
    }//GEN-LAST:event_resetBtnActionPerformed

    private void backBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backBtnActionPerformed
        dispose();
        new ManagerDashboard(loggedInManager).setVisible(true);
    }//GEN-LAST:event_backBtnActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private java.awt.Label label1;
    private javax.swing.JComboBox<String> month1Combo;
    private javax.swing.JComboBox<String> month2Combo;
    private javax.swing.JButton previewBtn;
    private javax.swing.JComboBox<String> reportTypeCombo;
    private javax.swing.JButton resetBtn;
    private javax.swing.JComboBox<String> year1Combo;
    private javax.swing.JComboBox<String> year2Combo;
    // End of variables declaration//GEN-END:variables
}
