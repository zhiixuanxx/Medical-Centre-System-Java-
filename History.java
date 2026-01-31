package GUI;

import java.awt.*;  
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.Appointment;
import model.AppointmentDetails;
import model.Customer;
import model.Doctor;
import model.Feedback;
import model.MedicalCertificate;
import util.FileStorage;
import util.ReceiptGenerator;
import model.Payment;
import model.Staff;

public class History extends JFrame {
    
    private final Customer customer;
    

    public History(Customer customer) {
        
        this.customer = customer;
        initComponents();
        loadAppointments();
        setVisible(true);
        setLocationRelativeTo(null);
        
        // Disable MC button by default
        viewMcButton.setEnabled(false);

        // Add listener to table AFTER initComponents
        appointmentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = appointmentTable.getSelectedRow();
                if (row >= 0) {
                    String mcFlag = String.valueOf(appointmentTable.getValueAt(row, 6)); // MC column
                    viewMcButton.setEnabled("Yes".equals(mcFlag));

                    String apptId = String.valueOf(appointmentTable.getValueAt(row, 0));
                    model.Payment pay = util.FileStorage.getPaymentByAppt(apptId);
                    viewReceiptButton.setEnabled(pay != null);
                } else {
                    viewMcButton.setEnabled(false);
                    viewReceiptButton.setEnabled(false);
                }
            }
        });

    }
    
    private void loadAppointments() {
        try {
            List<Appointment> allAppointments = FileStorage.getAppointmentsByCustomer(customer.getId());

            // Past = date before today OR status Completed/Cancelled
            LocalDate today = LocalDate.now();
            DateTimeFormatter dmy = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            List<Appointment> historyAppointments = allAppointments.stream()
                    .filter(apt -> {
                        try {
                            String s = apt.getDate().trim();
                            LocalDate ad = LocalDate.parse(s, dmy);

                            return ad.isBefore(today)
                                    || "Completed".equalsIgnoreCase(apt.getStatus())
                                    || "Cancelled".equalsIgnoreCase(apt.getStatus())
                                    || "Paid".equalsIgnoreCase(apt.getpaymentStatus());
                        } catch (Exception ex) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());

            updateTable(historyAppointments);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading appointment history: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable(List<Appointment> appointments) {
        String[] columns = {
                "ID", "Doctor", "Specialty", "Date", "Time", "Status", "MC",
                "Diagnosis", "Charges (RM)", "Notes"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        // Build details map once for fast lookup (supports 3 or 4 columns in file)
        Map<String, AppointmentDetails> detailsMap = FileStorage.getAllAppointmentDetailsMap();

        for (Appointment apt : appointments) {
            Doctor doctor = FileStorage.getDoctorById(apt.getDoctorId());
            AppointmentDetails det = detailsMap.get(apt.getAppointmentId());

            String diagnosis = det != null ? safe(det.getDiagnosis()) : "";
            String notes     = "";
            double charges   = 0.0;

            if (det != null) {
                try { charges = det.getCharges(); } catch (Exception ignore) {}
                // getNotes() exists in the updated model; if still on old model it will just be empty.
                try { notes = safe(det.getNotes()); } catch (Throwable ignore) { notes = ""; }
            }
            
            MedicalCertificate mc = FileStorage.getMedicalCertificateByAppt(apt.getAppointmentId());
            String mcFlag = (mc != null) ? "Yes" : "—";

            model.addRow(new Object[]{
                    apt.getAppointmentId(),
                    doctor != null ? doctor.getName() : "Unknown",
                    doctor != null ? doctor.getSpecialty() : "Unknown",
                    displayDate(apt.getDate()),
                    apt.getTime(),
                    apt.getStatus(),
                    mcFlag,
                    diagnosis,
                    charges == 0.0 ? "" : String.format("%.2f", charges),
                    notes
            });
        }

        appointmentTable.setModel(model);
        appointmentTable.getColumnModel().getColumn(6).setPreferredWidth(50);   // MC
        appointmentTable.getColumnModel().getColumn(7).setPreferredWidth(180);  // Diagnosis
        appointmentTable.getColumnModel().getColumn(9).setPreferredWidth(220);  // Notes
    }

    private String displayDate(String raw) {
        String s = raw == null ? "" : raw.trim();
        try {
            LocalDate.parse(s, DateTimeFormatter.ofPattern("dd/MM/yyyy")); // validate
            return s;
        } catch (Exception e) {
            return s; // fallback if parsing fails
        }
    }

    private String safe(String t) { return t == null ? "" : t; }

    private void viewAppointmentDetails() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to view.");
            return;
        }

        try {
            String appointmentId = appointmentTable.getValueAt(selectedRow, 0).toString();
            Appointment apt = FileStorage.getAppointmentById(appointmentId);
            if (apt == null) {
                JOptionPane.showMessageDialog(this, "Appointment not found.");
                return;
            }

            Doctor doctor = FileStorage.getDoctorById(apt.getDoctorId());
            AppointmentDetails det = FileStorage.getAppointmentDetailsById(appointmentId);

            String diagnosis = (det != null && det.getDiagnosis() != null && !det.getDiagnosis().isEmpty())
                    ? det.getDiagnosis() : "-";
            String notes = (det != null && det.getNotes() != null && !det.getNotes().isEmpty())
                    ? det.getNotes() : "-";
            String charges = (det != null && det.getCharges() > 0)
                    ? String.format("RM %.2f", det.getCharges())
                    : "-";

            // === Feedback ===
            String doctorFeedback = "-";
            int doctorRating = -1;
            String staffFeedback = "-";
            int staffRating = -1;

            Feedback fb = FileStorage.getFeedbackByAppointmentId(appointmentId);
            if (fb != null) {
                if (fb.getDoctorFeedback() != null && !fb.getDoctorFeedback().isEmpty()) {
                    doctorFeedback = fb.getDoctorFeedback();
                }
                doctorRating = fb.getDoctorRating();

                if (fb.getStaffFeedback() != null && !fb.getStaffFeedback().isEmpty()) {
                    staffFeedback = fb.getStaffFeedback();
                }
                staffRating = fb.getStaffRating();
            }

            MedicalCertificate mc = FileStorage.getMedicalCertificateByAppt(appointmentId);

            // show dialog with all details
            showDetailsDialog(
                    apt.getAppointmentId(),
                    doctor != null ? doctor.getName() : "Unknown",
                    doctor != null ? doctor.getSpecialty() : "Unknown",
                    displayDate(apt.getDate()),
                    apt.getTime(),
                    apt.getStatus(),
                    customer.getName(),
                    diagnosis, charges, notes,
                    doctorFeedback, staffFeedback,
                    doctorRating, staffRating,
                    mc
            );

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error viewing appointment details: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showDetailsDialog(
            String id, String doctorName, String specialty, String date, String time, String status, String patientName,
            String diagnosis, String charges, String notes,
            String doctorFeedback, String staffFeedback, int doctorRating, int staffRating,
            MedicalCertificate mc
    ) {
        JDialog dialog = new JDialog(this, "Appointment Details", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(600, 560);
        dialog.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        dialog.setContentPane(root);

        JLabel header = new JLabel("Appointment Details");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));
        root.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridBagLayout());
        root.add(new JScrollPane(content), BorderLayout.CENTER);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0;
        gc.weightx = 1; gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(2, 2, 2, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;

        // Appointment info
        content.add(sectionLabel("Appointment"), gc); gc.gridy++;
        content.add(row("ID", id), gc); gc.gridy++;
        content.add(row("Date", date), gc); gc.gridy++;
        content.add(row("Time", time), gc); gc.gridy++;
        content.add(row("Status", status), gc); gc.gridy++;
        content.add(separator(), gc); gc.gridy++;

        // Doctor info
        content.add(sectionLabel("Doctor"), gc); gc.gridy++;
        content.add(row("Name", doctorName), gc); gc.gridy++;
        content.add(row("Specialty", specialty), gc); gc.gridy++;
        content.add(separator(), gc); gc.gridy++;

        // Patient info
        content.add(sectionLabel("Patient"), gc); gc.gridy++;
        content.add(row("Name", patientName), gc); gc.gridy++;
        content.add(separator(), gc); gc.gridy++;

        // Medical notes
        content.add(sectionLabel("Medical Notes"), gc); gc.gridy++;
        content.add(row("Diagnosis", diagnosis), gc); gc.gridy++;
        content.add(row("Charges", charges), gc); gc.gridy++;
        content.add(multiRow("Notes", notes), gc); gc.gridy++;
        content.add(separator(), gc); gc.gridy++;

        // Feedback
        content.add(sectionLabel("Feedback"), gc); gc.gridy++;
        if (doctorRating >= 0) { content.add(row("Doctor Rating", stars(doctorRating)), gc); gc.gridy++; }
        content.add(multiRow("Doctor Feedback", doctorFeedback), gc); gc.gridy++;
        if (staffRating >= 0) { content.add(row("Staff Rating", stars(staffRating)), gc); gc.gridy++; }
        content.add(multiRow("Staff Feedback", staffFeedback), gc); gc.gridy++;

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton ok = new JButton("OK");
        ok.addActionListener(e -> dialog.dispose());
        south.add(ok);

        if (mc != null) {
            JButton mcBtn = new JButton("View MC");
            mcBtn.addActionListener(e -> showMedicalCertificate(mc, doctorName, patientName));
            south.add(mcBtn);
        }

        root.add(south, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }


    
    private void openSelectedMc() {
        int row = appointmentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment first.");
            return;
        }
        String apptId = String.valueOf(appointmentTable.getValueAt(row, 0));
        MedicalCertificate mc = FileStorage.getMedicalCertificateByAppt(apptId);
        if (mc == null) {
            JOptionPane.showMessageDialog(this, "No Medical Certificate has been issued for this appointment.");
            return;
        }
        Appointment apt = FileStorage.getAppointmentById(apptId);
        Doctor doctor = (apt != null) ? FileStorage.getDoctorById(apt.getDoctorId()) : null;
        String doctorName = (doctor != null) ? doctor.getName() : "Unknown Doctor";
        showMedicalCertificate(mc, doctorName, customer.getName());
    }
    
    private void downloadSelectedMc() {
        int row = appointmentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment first.");
            return;
        }
        String apptId = String.valueOf(appointmentTable.getValueAt(row, 0));
        MedicalCertificate mc = FileStorage.getMedicalCertificateByAppt(apptId);
        Appointment apt = FileStorage.getAppointmentById(apptId);
        Doctor doctor = (apt != null) ? FileStorage.getDoctorById(apt.getDoctorId()) : null;

        if (mc == null) {
            JOptionPane.showMessageDialog(this, "No Medical Certificate available for this appointment.");
            return;
        }

        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        chooser.setSelectedFile(new java.io.File("MedicalCertificate_" + apptId + ".pdf"));
        int option = chooser.showSaveDialog(this);
        if (option == javax.swing.JFileChooser.APPROVE_OPTION) {
            try {
                util.PDFMCWriter.writeMCPdf(
                    chooser.getSelectedFile().getAbsolutePath(),
                    apt, doctor, mc
                );
                JOptionPane.showMessageDialog(this, "MC saved successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving MC: " + ex.getMessage());
            }
        }
    }


     private String formatDoctorDisplayName(String name) {
        if (name == null) return "Dr. Unknown";
        String clean = name.replaceAll("(?i)^\\s*dr\\.?\\s*", "").trim();
        return "Dr. " + (clean.isEmpty() ? "Unknown" : clean);
    }

    // Cursive-like Unicode signature from clean name (no "Dr.")
    private String generateSignature(String doctorName) {
        if (doctorName == null) doctorName = "";
        String clean = doctorName.replaceAll("(?i)^\\s*dr\\.?\\s*", "").trim();
        if (clean.isEmpty()) clean = "Doctor";

        StringBuilder sig = new StringBuilder();
        for (int i = 0; i < clean.length(); i++) {
            char c = clean.charAt(i);
            if (Character.isLetter(c)) {
                switch (Character.toLowerCase(c)) {
                    case 'a': sig.append("𝒶"); break; case 'b': sig.append("𝒷"); break;
                    case 'c': sig.append("𝒸"); break; case 'd': sig.append("𝒹"); break;
                    case 'e': sig.append("ℯ"); break; case 'f': sig.append("𝒻"); break;
                    case 'g': sig.append("ℊ"); break; case 'h': sig.append("𝒽"); break;
                    case 'i': sig.append("𝒾"); break; case 'j': sig.append("𝒿"); break;
                    case 'k': sig.append("𝓀"); break; case 'l': sig.append("𝓁"); break;
                    case 'm': sig.append("𝓂"); break; case 'n': sig.append("𝓃"); break;
                    case 'o': sig.append("ℴ"); break; case 'p': sig.append("𝓅"); break;
                    case 'q': sig.append("𝓆"); break; case 'r': sig.append("𝓇"); break;
                    case 's': sig.append("𝓈"); break; case 't': sig.append("𝓉"); break;
                    case 'u': sig.append("𝓊"); break; case 'v': sig.append("𝓋"); break;
                    case 'w': sig.append("𝓌"); break; case 'x': sig.append("𝓍"); break;
                    case 'y': sig.append("𝓎"); break; case 'z': sig.append("𝓏"); break;
                    default:  sig.append(c);
                }
            } else {
                sig.append(c);
            }
        }
        sig.append("\n");
        return sig.toString();
    }
    
    private void showMedicalCertificate(MedicalCertificate mc, String doctorName, String patientName) {
        String remarks = (mc.getRemarks() == null || mc.getRemarks().trim().isEmpty()) ? "-" : mc.getRemarks().trim();

        String doctorLabelName = formatDoctorDisplayName(doctorName);

        String signatureEscaped = generateSignature(doctorName)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");

        String html = "<html><body style='font-family:Arial;'>"
                + "<h2 style='text-align:center;'>Medical Certificate</h2>"
                + "<p><b>Patient:</b> " + patientName + " (ID: " + mc.getCustomerId() + ")</p>"
                + "<p><b>Doctor:</b> " + doctorLabelName + " (ID: " + mc.getDoctorId() + ")</p>"
                + "<p><b>Issue Date:</b> " + mc.getIssueDate() + "</p>"
                + "<p><b>Rest From:</b> " + mc.getRestFrom() + " <b>To:</b> " + mc.getRestTo() + "</p>"
                + "<p><b>Remarks:</b> " + remarks + "</p>"
                + "<br><br>"
                + "<div style='font-family:cursive; font-size:18px; color:#000080; margin-top:40px;'>"
                +   signatureEscaped
                + "</div>"
                + "<div style='border-top:1px solid #000; width:200px; margin:4px auto;'></div>"
                + "<div style='margin-top:4px; font-size:12px; color:#777;'>Doctor's Signature</div>"
                + "</body></html>";

        // --- Custom dialog instead of JOptionPane ---
        javax.swing.JDialog dialog = new javax.swing.JDialog(this, "Official Medical Certificate", true);

        javax.swing.JEditorPane pane = new javax.swing.JEditorPane("text/html", html);
        pane.setEditable(false);
        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(pane);
        scroll.setPreferredSize(new Dimension(520, 420));

        // Buttons
        javax.swing.JButton okButton = new javax.swing.JButton("OK");
        javax.swing.JButton downloadButton = new javax.swing.JButton("Download (PDF)");

        okButton.addActionListener(e -> dialog.dispose());

        downloadButton.addActionListener(e -> {
            try {
                javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
                chooser.setSelectedFile(new java.io.File("MedicalCertificate_" + mc.getAppointmentId() + ".pdf"));
                if (chooser.showSaveDialog(dialog) == javax.swing.JFileChooser.APPROVE_OPTION) {
                    util.PDFMCWriter.writeMCPdf(
                            chooser.getSelectedFile().getAbsolutePath(),
                            FileStorage.getAppointmentById(mc.getAppointmentId()),
                            FileStorage.getDoctorById(mc.getDoctorId()),
                            mc
                    );
                    JOptionPane.showMessageDialog(dialog, "MC saved successfully!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error saving MC: " + ex.getMessage());
            }
        });

        javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(downloadButton);

        dialog.getContentPane().add(scroll, java.awt.BorderLayout.CENTER);
        dialog.getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    // Bold section header label
    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 14f));
        return lbl;
    }

    // Simple row with "Label: value"
    private JPanel row(String label, String value) {
        JPanel p = new JPanel(new BorderLayout());
        JLabel l = new JLabel(label + ": ");
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        p.add(l, BorderLayout.WEST);
        p.add(new JLabel(value), BorderLayout.CENTER);
        return p;
    }

    // Multi-line row for long text (like notes/feedback)
    private JPanel multiRow(String label, String value) {
        JPanel p = new JPanel(new BorderLayout());
        JLabel l = new JLabel(label + ": ");
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        JTextArea area = new JTextArea(value);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setOpaque(false);
        p.add(l, BorderLayout.NORTH);
        p.add(area, BorderLayout.CENTER);
        return p;
    }

    // Horizontal separator line
    private JSeparator separator() {
        return new JSeparator(SwingConstants.HORIZONTAL);
    }

    // Render rating as stars (★)
    private String stars(int rating) {
        if (rating < 0) return "-";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(i < rating ? "★" : "☆");
        }
        return sb.toString();
    }

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        appointmentTable = new javax.swing.JTable();
        viewMcButton = new javax.swing.JButton();
        viewButton = new javax.swing.JButton();
        refreshButton = new javax.swing.JButton();
        backButton = new javax.swing.JButton();
        viewReceiptButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(153, 204, 255));

        jLabel1.setFont(new java.awt.Font("Perpetua", 1, 28)); // NOI18N
        jLabel1.setText("APPOINTMENT HISTORY ");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(jLabel1)
                .addContainerGap(29, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(12, Short.MAX_VALUE))
        );

        appointmentTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Doctor", "Specialty", "Date ", "Time", "Status", "Diagnosis", "Charges (RM)", "Notes"
            }

        ));
        jScrollPane1.setViewportView(appointmentTable);

        viewMcButton.setBackground(new java.awt.Color(0, 153, 153));
        viewMcButton.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        viewMcButton.setForeground(new java.awt.Color(255, 255, 255));
        viewMcButton.setText("View MC");
        viewMcButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewMcButtonActionPerformed(evt);
            }
        });

        viewButton.setBackground(new java.awt.Color(0, 120, 215));
        viewButton.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        viewButton.setForeground(new java.awt.Color(255, 255, 255));
        viewButton.setText("View Details");
        viewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewButtonActionPerformed(evt);
            }
        });

        refreshButton.setBackground(new java.awt.Color(50, 150, 50));
        refreshButton.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        refreshButton.setForeground(new java.awt.Color(255, 255, 255));
        refreshButton.setText("Refresh");
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        backButton.setBackground(new java.awt.Color(102, 102, 255));
        backButton.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        backButton.setForeground(new java.awt.Color(255, 255, 255));
        backButton.setText("Back");
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });

        viewReceiptButton.setBackground(new java.awt.Color(94, 175, 145));
        viewReceiptButton.setFont(new java.awt.Font("Perpetua", 1, 14)); // NOI18N
        viewReceiptButton.setForeground(new java.awt.Color(255, 255, 255));
        viewReceiptButton.setText("View Receipt");
        viewReceiptButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewReceiptButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(181, 181, 181)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 698, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 25, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(77, 77, 77)
                .addComponent(viewButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(viewMcButton)
                .addGap(18, 18, 18)
                .addComponent(viewReceiptButton)
                .addGap(21, 21, 21)
                .addComponent(refreshButton)
                .addGap(18, 18, 18)
                .addComponent(backButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(38, 38, 38)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 293, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(58, 58, 58)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(viewButton)
                    .addComponent(viewMcButton)
                    .addComponent(refreshButton)
                    .addComponent(backButton)
                    .addComponent(viewReceiptButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void viewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewButtonActionPerformed
        viewAppointmentDetails();
    }//GEN-LAST:event_viewButtonActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
       loadAppointments();
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
        dispose();
        CustomerDashboard1 customerDashboard1 = new CustomerDashboard1(customer);
        customerDashboard1.setVisible(true);
    }//GEN-LAST:event_backButtonActionPerformed

    private void viewMcButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewMcButtonActionPerformed
        openSelectedMc();
    }//GEN-LAST:event_viewMcButtonActionPerformed

    private void viewReceiptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewReceiptButtonActionPerformed
        int row = appointmentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment first.");
            return;
        }

        // Appointment ID from table
        String apptId = String.valueOf(appointmentTable.getValueAt(row, 0));

        // Load payment
        Payment pay = FileStorage.getPaymentByAppt(apptId);
        if (pay == null) {
            JOptionPane.showMessageDialog(this, "No receipt found for this appointment.");
            return;
        }

        Appointment appt = FileStorage.getAppointmentById(apptId);
        AppointmentDetails details = FileStorage.getAppointmentDetailsById(apptId);
        Staff staff = FileStorage.getStaffById(pay.getStaffId());

        // Show receipt
        ReceiptGenerator receiptDialog = new ReceiptGenerator(
                this,
                appt,
                details,
                staff,
                pay.getReceiptId(),
                pay.getPaymentMethod(),
                pay.getPaidAmount(),
                pay.getChangeAmount()
        );
        receiptDialog.setVisible(true);
    }//GEN-LAST:event_viewReceiptButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable appointmentTable;
    private javax.swing.JButton backButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton viewButton;
    private javax.swing.JButton viewMcButton;
    private javax.swing.JButton viewReceiptButton;
    // End of variables declaration//GEN-END:variables
}
