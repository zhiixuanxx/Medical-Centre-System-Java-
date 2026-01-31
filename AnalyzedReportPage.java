package view;

import model.Appointment;
import model.AppointmentDetails;
import model.Feedback;
import model.Manager;
import util.FileStorage;

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
import util.PDFReportWriter;

public class AnalyzedReportPage extends JFrame {

    private final Manager loggedInManager;

    private JComboBox<String> reportTypeCombo;
    private JComboBox<Integer> year1Combo, year2Combo;
    private JComboBox<String> month1Combo, month2Combo; // separate months
    private JButton previewBtn, resetBtn, backBtn;

    private List<Appointment> allAppointments;
    private Map<String, AppointmentDetails> detailsMap;
    private List<Feedback> allFeedback;

    public AnalyzedReportPage(Manager manager) {
        this.loggedInManager = manager;

        setTitle("Analyzed Reports");
        setSize(980, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));

        allAppointments = FileStorage.readAppointments();
        detailsMap = FileStorage.getAllAppointmentDetailsMap();
        try {
            allFeedback = FileStorage.getAllFeedback();
        } catch (Throwable t) {
            allFeedback = new ArrayList<>();
        }

        add(buildControlsPanel(), BorderLayout.NORTH);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel buildControlsPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new TitledBorder("Filters & Options"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        reportTypeCombo = new JComboBox<>(new String[]{
                "Select Report Type", "Financial Report", "Appointment Statistics Report", "Performance Report"
        });

        // Years from data, prepended with "0" sentinel = Select Year
        List<Integer> years = inferYearsFromAppointments();
        List<Integer> withSelect = combineYears(years);
        year1Combo = new JComboBox<>(withSelect.toArray(new Integer[0]));
        year2Combo = new JComboBox<>(withSelect.toArray(new Integer[0]));
        applyYearRenderer(year1Combo);
        applyYearRenderer(year2Combo);
        year1Combo.setSelectedIndex(0);
        year2Combo.setSelectedIndex(0);

        // Separate months for each side
        String[] months = {
                "Select Month", "01 - January", "02 - February", "03 - March", "04 - April",
                "05 - May", "06 - June", "07 - July", "08 - August", "09 - September",
                "10 - October", "11 - November", "12 - December"
        };
        month1Combo = new JComboBox<>(months);
        month2Combo = new JComboBox<>(months);

        previewBtn = new JButton("Preview Report");

        // Row 1
        c.gridx = 0; c.gridy = 0; p.add(new JLabel("Report Type:"), c);
        c.gridx = 1; p.add(reportTypeCombo, c);

        // Row 2 (Year 1 + Month 1)
        c.gridy = 1; c.gridx = 0; p.add(new JLabel("Year 1:"), c);
        c.gridx = 1; p.add(year1Combo, c);
        c.gridx = 2; p.add(new JLabel("Month 1:"), c);
        c.gridx = 3; p.add(month1Combo, c);

        // Row 3 (Year 2 + Month 2)
        c.gridy = 2; c.gridx = 0; p.add(new JLabel("Year 2 (optional):"), c);
        c.gridx = 1; p.add(year2Combo, c);
        c.gridx = 2; p.add(new JLabel("Month 2 (optional):"), c);
        c.gridx = 3; p.add(month2Combo, c);

        // Row 4 (button)
        c.gridy = 3; c.gridx = 0; p.add(previewBtn, c);

        previewBtn.addActionListener(e -> onPreview());

        return p;
    }

    private JPanel buildBottomPanel() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        resetBtn = new JButton("Reset");
        backBtn = new JButton("Back");

        resetBtn.addActionListener(e -> {
            reportTypeCombo.setSelectedIndex(0);
            year1Combo.setSelectedIndex(0);  // back to "Select Year"
            year2Combo.setSelectedIndex(0);
            month1Combo.setSelectedIndex(0); // "Select Month"
            month2Combo.setSelectedIndex(0);
        });

        backBtn.addActionListener(e -> {
            dispose();
            new ManagerDashboard(loggedInManager).setVisible(true);
        });

        bottom.add(resetBtn);
        bottom.add(backBtn);
        return bottom;
    }

    private void onPreview() {
        String reportType = (String) reportTypeCombo.getSelectedItem();
        Integer y1 = (Integer) year1Combo.getSelectedItem();
        Integer y2 = (Integer) year2Combo.getSelectedItem();
        Integer m1 = getSelectedMonthOrNull(month1Combo);
        Integer m2 = getSelectedMonthOrNull(month2Combo);

        if (reportType == null || reportType.startsWith("Select")) {
            JOptionPane.showMessageDialog(this, "Please select a report type.");
            return;
        }
        if (y1 == null || y1 == 0) {
            JOptionPane.showMessageDialog(this, "Please select Year 1.");
            return;
        }
        if (y2 != null && y2 == 0) y2 = null; // optional
        if (y2 == null && m2 != null) {
            // If no Year 2 but Month 2 selected, ignore Month 2
            m2 = null;
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
        JOptionPane.showMessageDialog(
                this,
                "Please connect to a printer to print the report.",
                "Print Report",
                JOptionPane.INFORMATION_MESSAGE
        );
    }


    private void exportCsv(String reportType) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(reportType.replace(" ", "_") + "_RawData.csv"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter w = new PrintWriter(new FileWriter(chooser.getSelectedFile()))) {

                switch (reportType) {
                    case "Financial Report":
                        exportPaymentsRaw(w);
                        break;
                    case "Appointment Statistics Report":
                        exportAppointmentsRaw(w);
                        break;
                    case "Performance Report":
                        exportFeedbackRaw(w);
                        break;
                    default:
                        JOptionPane.showMessageDialog(this, "Unsupported report type for raw export.");
                }

                JOptionPane.showMessageDialog(this, "CSV exported successfully.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage());
            }
        }
    }


    private void exportPdf(String reportType, String html, String managerName) {
        JFileChooser chooser = new JFileChooser();

        // 🔹 Generate unique Report ID for filename
        String reportId = UUID.randomUUID().toString().substring(0, 8);
        String defaultFileName = reportType.replace(" ", "_") + "_" + reportId + ".pdf";
        chooser.setSelectedFile(new File(defaultFileName));

        int res = chooser.showSaveDialog(this);

        if (res == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null) {
                try {
                    util.PDFReportWriter.writeReportPdf(
                            file.getAbsolutePath(),
                            reportType,
                            html,
                            managerName   // ✅ Manager name
                    );
                    JOptionPane.showMessageDialog(
                            this,
                            "PDF exported successfully.\nFile: " + file.getName()
                    );
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Export failed: " + ex.getMessage()
                    );
                }
            }
        } else {
            // User cancelled
            System.out.println("Export cancelled by user.");
        }
    }

    // ===== Report Builders (with separate month per side) =====

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

        // Existing
        sb.append(row("Total Revenue (RM)", fmtMoney(s1.totalRevenue),
                s2 != null ? fmtMoney(s2.totalRevenue) : null,
                s2 != null ? diffPct(s1.totalRevenue, s2.totalRevenue) : null));

        sb.append(row("Completed Appointments", String.valueOf(s1.completedCount),
                s2 != null ? String.valueOf(s2.completedCount) : null,
                s2 != null ? diffNum(s1.completedCount, s2.completedCount) : null));

        sb.append(row("Average Revenue / Appt (RM)", fmtMoney(s1.avgRevenuePerAppt),
                s2 != null ? fmtMoney(s2.avgRevenuePerAppt) : null,
                s2 != null ? diffPct(s1.avgRevenuePerAppt, s2.avgRevenuePerAppt) : null));

        // 🔹 New Metrics
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

        // ===== Main Status Table =====
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

        // ===== Specialty Breakdown =====
        sb.append("<h4>Appointments by Specialty</h4>");
        sb.append("<table class='tbl'><tr><th>Specialty</th><th>Appointments</th></tr>");
        for (Map.Entry<String, Integer> e : a1.bySpecialty.entrySet()) {
            sb.append("<tr><td>").append(escape(e.getKey())).append("</td><td>")
              .append(e.getValue()).append("</td></tr>");
        }
        sb.append("</table>");

        // ===== Summary Narrative =====
        sb.append("<p class='summary'>Summary: ");
        if (a1.bySpecialty.isEmpty()) {
            sb.append("No appointments were recorded for this period.");
        } else {
            String topSpec = a1.bySpecialty.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey).orElse("-");
            int topCount = a1.bySpecialty.getOrDefault(topSpec, 0);

            if (a2 == null) {
                sb.append("For ").append(escape(sideLabel(y1, m1))).append(", total appointments were ")
                  .append(a1.total).append(". The busiest specialty was ")
                  .append(topSpec).append(" with ").append(topCount).append(" appointments.");
            } else {
                sb.append("From ").append(escape(sideLabel(y1, m1))).append(" to ")
                  .append(escape(sideLabel(y2, m2))).append(", total appointments changed ")
                  .append(diffNum(a1.total, a2.total)).append(". In ")
                  .append(sideLabel(y1, m1)).append(", busiest specialty was ")
                  .append(topSpec).append(" with ").append(topCount).append(" appointments.");
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

        // ===== Main Table =====
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

    // ===== Computation Helpers =====
    private FinancialStats computeFinancial(int year, Integer month) {
        List<model.Payment> allPayments = FileStorage.readPayments();
        List<Doctor> doctors = FileStorage.getAllDoctors();  // ✅ load doctors
        Map<String, String> doctorTypeMap = new HashMap<>();
        for (Doctor d : doctors) {
            doctorTypeMap.put(d.getId(), d.getDoctorType()); // D001 -> Clinical
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

            double amt = p.getTotal();
            revenue += amt;
            count++;

            // doctor breakdown
            String doctorId = p.getDoctorId();
            revenueByDoctor.put(doctorId, revenueByDoctor.getOrDefault(doctorId, 0.0) + amt);

            // type breakdown from doctor.txt
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

        // Build a map of doctorId -> specialty
        Map<String, String> doctorSpecMap = new HashMap<>();
        for (Doctor d : FileStorage.getAllDoctors()) {
            doctorSpecMap.put(d.getId(), d.getSpecialty());
        }

        ApptStats s = new ApptStats();

        for (Appointment a : list) {
            String st = safe(a.getStatus()).toLowerCase();
            if (st.equals("completed")) s.completed++;
            else if (st.equals("cancelled")) s.cancelled++;
            else if (st.equals("upcoming")) s.upcoming++;

            // Lookup doctor specialty
            String spec = doctorSpecMap.getOrDefault(a.getDoctorId(), "Unknown");
            s.bySpecialty.put(spec, s.bySpecialty.getOrDefault(spec, 0) + 1);
        }

        s.total = list.size();
        return s;
    }

    private PerformanceStats computePerformance(int year, Integer month) {
        // 🔑 Build doctor map once
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

                //Lookup doctor's specialty via doctorId
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

    // ===== UI Helpers & Formatting =====

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
        return (idx <= 0) ? null : idx; // 1..12
    }

    private List<Integer> combineYears(List<Integer> years) {
        List<Integer> out = new ArrayList<>();
        out.add(0); // sentinel for "Select Year"
        out.addAll(years);
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
        // try with stray spaces around slashes
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

    private void applyYearRenderer(JComboBox<Integer> combo) {
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                String text;
                if (value == null || (value instanceof Integer && ((Integer) value) == 0)) {
                    text = "Select Year";
                } else {
                    text = String.valueOf(value);
                }
                return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
            }
        });
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

    // ===== POJOs =====
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
        w.println("AppointmentID,CustomerID,CustomerName,DoctorID,DoctorName,Date,Time,Status");
        for (Appointment a : FileStorage.readAppointments()) {
            w.println(String.join(",",
                    a.getAppointmentId(),
                    a.getCustomerId(),
                    a.getCustomerName(),
                    a.getDoctorId(),
                    a.getDoctorName(),
                    a.getDate(),
                    a.getTime(),
                    a.getStatus()
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
        // replace commas with semicolons so Excel won’t split columns
        return input.replace(",", ";");
    }



}
