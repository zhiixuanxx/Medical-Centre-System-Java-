package GUI;

import model.Doctor;
import model.Appointment;
import model.Customer;
import model.AppointmentDetails;
import util.FileStorage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import static util.FileStorage.getCustomerById;
import view.EnterChargesDiagnosis;

public class DoctorViewAppointments1 extends JFrame {
    private static final Logger logger = Logger.getLogger(DoctorViewAppointments1.class.getName());

    private Doctor doctor;
    private DoctorDashboard1 parentDashboard;

    public DoctorViewAppointments1() {
        initComponents();
        setLocationRelativeTo(null);
    }

    // Constructor with doctor only
    public DoctorViewAppointments1(Doctor doctor) {
        this(); // calls default constructor → initComponents()
        if (doctor == null) throw new IllegalArgumentException("Doctor cannot be null");

        this.doctor = doctor;
        setTitle("Appointments - " + doctor.getName());
        setLocationRelativeTo(null);

        loadUpcomingAppointments();
    }

    // Constructor with doctor + parent dashboard
    public DoctorViewAppointments1(Doctor doctor, DoctorDashboard1 parentDashboard) {
        this(doctor); // reuse doctor constructor → ensures initComponents() is called
        this.parentDashboard = parentDashboard;

        setLocationRelativeTo(parentDashboard); // center on parent
    }


    private void loadUpcomingAppointments() {
        try {
            List<Appointment> allAppointments = FileStorage.readAppointments();
            if (allAppointments == null) allAppointments = new ArrayList<>();

            List<Appointment> doctorUpcomingAppointments = new ArrayList<>();
            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();

            for (Appointment appointment : allAppointments) {
                if (appointment == null) continue;
                if (!doctor.getId().equals(appointment.getDoctorId())) continue;

                String status = appointment.getStatus();
                boolean notPaid = !"Paid".equalsIgnoreCase(appointment.getpaymentStatus());

                try {
                    LocalDate appointmentDate = parseAppointmentDate(appointment.getDate());
                    LocalTime appointmentTime = parseAppointmentTime(appointment.getTime());
                    LocalDateTime apptDateTime = LocalDateTime.of(appointmentDate, appointmentTime);

                    // Logic for regular doctors: show future/current upcoming
                    if (!doctor.isOnCall() && "Upcoming".equalsIgnoreCase(status) && notPaid) {
                        if (!apptDateTime.isBefore(LocalDateTime.now())) { // still requires future for regular
                            doctorUpcomingAppointments.add(appointment);
                        }
                    } 
                    // Logic for on-call doctors: show emergency appointments for today
                    else if (doctor.isOnCall() && status.toLowerCase().contains("emergency") && notPaid) {
                        // For on-call emergencies, we want to see all today's emergencies, 
                        // even if their time slot has passed.
                        if (appointmentDate.equals(today)) { 
                            doctorUpcomingAppointments.add(appointment);
                        }
                    }

                } catch (Exception e) {
                    logger.log(Level.WARNING,
                        "Skipping appointment due to date/time parse error: " + appointment.getAppointmentId(), e);
                }
            }

            // Sort by soonest date/time
            sortAppointmentsByDateTime(doctorUpcomingAppointments);

            updateTable(doctorUpcomingAppointments);

            if (doctorUpcomingAppointments.isEmpty()) {
                statusLabel.setText("No upcoming appointments found.");
            } else {
                statusLabel.setText("Showing all upcoming appointments, sorted by nearest.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Unable to load appointments", "Error", JOptionPane.ERROR_MESSAGE);
            updateTable(new ArrayList<>());
        }
    }

    private void sortAppointmentsByDateTime(List<Appointment> appointments) {
        appointments.sort((a1, a2) -> {
            try {
                LocalDateTime dt1 = LocalDateTime.of(
                    parseAppointmentDate(a1.getDate()), parseAppointmentTime(a1.getTime())
                );
                LocalDateTime dt2 = LocalDateTime.of(
                    parseAppointmentDate(a2.getDate()), parseAppointmentTime(a2.getTime())
                );
                return dt1.compareTo(dt2);
            } catch (Exception e) {
                return a1.getAppointmentId().compareTo(a2.getAppointmentId());
            }
        });
    }

    private void updateTable(List<Appointment> appointments) {
        String[] columns = {
            "Appointment ID", "Patient Name", "Date", "Time",
            "Status", "Patient Contact", "Patient DOB"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        for (Appointment appointment : appointments) {
            Customer customer = getCustomerById(appointment.getCustomerId());
            String patientName = customer != null ? customer.getName() : "Unknown";
            String patientContact = customer != null ? customer.getPhone() : "N/A";
            String patientDOB = customer != null ? customer.getDob() : "N/A";
            String displayDate = formatDateForDisplay(appointment.getDate());

            model.addRow(new Object[]{
                appointment.getAppointmentId(),
                patientName,
                displayDate,
                appointment.getTime(),
                appointment.getStatus(),
                patientContact,
                patientDOB
            });
        }

        appointmentTable.setModel(model);
    }

    // ============ Event Handlers ============

    private void viewAppointmentDetails() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow == -1) {
            showInfoDialog("Please select an appointment to view details.");
            return;
        }

        try {
            String appointmentId = safeGetTableValue(selectedRow, 0, "Unknown");
            if ("ERROR".equals(appointmentId)) {
                showErrorDialog("Cannot view details for this appointment due to data errors.");
                return;
            }

            Appointment appointment = FileStorage.getAppointmentById(appointmentId);
            if (appointment == null) {
                showErrorDialog("Appointment not found in system.");
                return;
            }

            Customer customer = getCustomerById(appointment.getCustomerId());
            List<Appointment> allAppointments = FileStorage.readAppointments();
            String prevAppointmentId = null;

            for (Appointment ap : allAppointments) {
                if (ap.getCustomerId().equals(appointment.getCustomerId())
                        && ap.getStatus().equalsIgnoreCase("Completed")
                        && !ap.getAppointmentId().equals(appointmentId)) {
                    prevAppointmentId = ap.getAppointmentId();
                }
            }

            AppointmentDetails details = null;
            if (prevAppointmentId != null) {
                details = FileStorage.getAppointmentDetailsById(prevAppointmentId.trim());
            }

            StringBuilder detailsText = new StringBuilder();
            detailsText.append("APPOINTMENT DETAILS\n");
            detailsText.append("═══════════════════════════════════\n\n");
            detailsText.append("Appointment ID: ").append(appointment.getAppointmentId()).append("\n");
            detailsText.append("Date: ").append(formatDateForDisplay(appointment.getDate())).append("\n");
            detailsText.append("Time: ").append(appointment.getTime()).append("\n");
            detailsText.append("Status: ").append(appointment.getStatus()).append("\n\n");

            detailsText.append("PATIENT INFORMATION\n");
            detailsText.append("───────────────────────────\n");
            if (customer != null) {
                detailsText.append("Name: ").append(customer.getName()).append("\n");
                detailsText.append("Gender: ").append(customer.getGender()).append("\n");
                detailsText.append("Date of Birth: ").append(customer.getDob()).append("\n");
                detailsText.append("Email: ").append(customer.getEmail()).append("\n");
                detailsText.append("Phone: ").append(customer.getPhone()).append("\n\n");
            } else {
                detailsText.append("Patient information not available\n\n");
            }

            detailsText.append("MEDICAL INFORMATION\n");
            detailsText.append("───────────────────────────\n");
            if (details != null) {
                detailsText.append("Previous Diagnosis: ")
                        .append(details.getDiagnosis() != null ? details.getDiagnosis() : "None").append("\n");
                detailsText.append("Previous Charges: RM ")
                        .append(String.format("%.2f", details.getCharges())).append("\n");
                try {
                    String notes = details.getNotes();
                    detailsText.append("Previous Notes: ").append(
                            (notes != null && !notes.isEmpty()) ? notes : "None"
                    ).append("\n");
                } catch (Exception e) {
                    detailsText.append("Previous Notes: None\n");
                }
            } else {
                detailsText.append("No previous medical information available\n");
            }

            JTextArea textArea = new JTextArea(detailsText.toString());
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            textArea.setEditable(false);
            textArea.setCaretPosition(0);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 400));

            JOptionPane.showMessageDialog(this, scrollPane, "Appointment Details", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error viewing appointment details", e);
            showErrorDialog("Unable to load appointment details: " + e.getMessage());
        }
    }
    
    private LocalDate parseAppointmentDate(String dateStr) throws DateTimeParseException {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new DateTimeParseException("Date is null or empty", dateStr, 0);
        }
        String cleanDate = dateStr.trim();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try {
            return LocalDate.parse(cleanDate, formatter);
        } catch (DateTimeParseException e) {
            logger.log(Level.WARNING, "Unable to parse date: '" + cleanDate + "' with format dd/MM/yyyy", e);
            throw new DateTimeParseException("Invalid date format, expected dd/MM/yyyy", cleanDate, 0);
        }
    }

    private LocalTime parseAppointmentTime(String timeStr) throws DateTimeParseException {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            throw new DateTimeParseException("Time is null or empty", timeStr, 0);
        }
        String cleanTime = timeStr.trim();

        DateTimeFormatter[] timeFormatters = {
                DateTimeFormatter.ofPattern("HH:mm"),
                DateTimeFormatter.ofPattern("H:mm"),
                DateTimeFormatter.ofPattern("hh:mm a"),
                DateTimeFormatter.ofPattern("h:mm a"),
                DateTimeFormatter.ofPattern("hh:mm"),
                DateTimeFormatter.ofPattern("h:mm")
        };

        for (DateTimeFormatter formatter : timeFormatters) {
            try {
                return LocalTime.parse(cleanTime, formatter);
            } catch (DateTimeParseException ignored) {}
        }
        logger.log(Level.WARNING, "Unable to parse time: '" + cleanTime + "' with any known format");
        throw new DateTimeParseException("Unable to parse time with any known format", cleanTime, 0);
    }

    private String formatDateForDisplay(String dateStr) {
        try {
            LocalDate date = parseAppointmentDate(dateStr);
            return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error formatting date for display: " + dateStr, e);
            return dateStr != null ? dateStr : "Invalid Date";
        }
    }
    
    private Customer getCustomerById(String customerId) {
        try {
            List<Customer> customers = FileStorage.getAllCustomers();
            if (customers != null) {
                for (Customer customer : customers) {
                    if (customer != null && customerId != null && customerId.equals(customer.getId())) {
                        return customer;
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error getting customer by ID: " + customerId, e);
        }
        return null;
    }
    
    private void safeExecute(Runnable action, String actionName) {
        try {
            action.run();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error " + actionName, e);
            showErrorDialog("An error occurred while " + actionName + ". Please try again.");
        }
    }

    private String safeGetTableValue(int row, int column, String defaultValue) {
        try {
            Object value = appointmentTable.getValueAt(row, column);
            return value != null ? value.toString() : defaultValue;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error getting table value at row " + row + ", column " + column, e);
            return defaultValue;
        }
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    private void showInfoDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
    private void showSuccessDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private String nonNull(String s, String def) { return (s == null || s.isEmpty()) ? def : s; }
    // </editor-fold>
    
    @SuppressWarnings("unchecked")

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jButton2 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        appointmentTable = new javax.swing.JTable();
        titlePanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        doctorLabel = new javax.swing.JLabel();
        buttonPanel = new javax.swing.JPanel();
        viewDetailsButton = new javax.swing.JButton();
        refreshButton = new javax.swing.JButton();
        backButton = new javax.swing.JButton();
        statusPanel = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();

        jButton2.setText("jButton2");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(248, 249, 250));
        setSize(new java.awt.Dimension(1000, 650));

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Appointment List", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Perpetua", 0, 18))); // NOI18N

        appointmentTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Appointment ID", "Patient Name", "Date", "Time", "Status", "Patient Contact", "Patient DOB"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        jScrollPane1.setViewportView(appointmentTable);

        titlePanel.setBackground(new java.awt.Color(230, 245, 250));
        titlePanel.setLayout(new java.awt.GridBagLayout());

        titleLabel.setFont(new java.awt.Font("Perpetua", 1, 28)); // NOI18N
        titleLabel.setForeground(new java.awt.Color(0, 51, 102));
        titleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        titleLabel.setText("My Upcoming Appointments");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(38, 259, 0, 259);
        titlePanel.add(titleLabel, gridBagConstraints);

        doctorLabel.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 323;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 262, 18, 0);
        titlePanel.add(doctorLabel, gridBagConstraints);

        buttonPanel.setBackground(new java.awt.Color(248, 249, 250));
        buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        viewDetailsButton.setBackground(new java.awt.Color(102, 153, 255));
        viewDetailsButton.setFont(new java.awt.Font("Perpetua", 0, 14)); // NOI18N
        viewDetailsButton.setForeground(new java.awt.Color(255, 255, 255));
        viewDetailsButton.setText("View Details");
        viewDetailsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewDetailsButtonActionPerformed(evt);
            }
        });

        refreshButton.setBackground(new java.awt.Color(52, 73, 94));
        refreshButton.setFont(new java.awt.Font("Perpetua", 0, 14)); // NOI18N
        refreshButton.setForeground(new java.awt.Color(255, 255, 255));
        refreshButton.setText("Refresh");
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        backButton.setBackground(new java.awt.Color(149, 165, 166));
        backButton.setFont(new java.awt.Font("Perpetua", 0, 14)); // NOI18N
        backButton.setForeground(new java.awt.Color(255, 255, 255));
        backButton.setText("Back To Dashboard");
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout buttonPanelLayout = new javax.swing.GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, buttonPanelLayout.createSequentialGroup()
                .addGap(129, 129, 129)
                .addComponent(viewDetailsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(98, 98, 98)
                .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(92, 92, 92)
                .addComponent(backButton, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, buttonPanelLayout.createSequentialGroup()
                .addContainerGap(40, Short.MAX_VALUE)
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(viewDetailsButton)
                    .addComponent(refreshButton)
                    .addComponent(backButton))
                .addGap(24, 24, 24))
        );

        statusLabel.setFont(new java.awt.Font("Perpetua", 0, 14)); // NOI18N
        statusLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        statusLabel.setText(" ");

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 429, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(titlePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 1034, Short.MAX_VALUE)
            .addComponent(buttonPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(statusPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(titlePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 293, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
        dispose();
        if (parentDashboard != null) parentDashboard.setVisible(true);
    }//GEN-LAST:event_backButtonActionPerformed

    private void viewDetailsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewDetailsButtonActionPerformed
        safeExecute(this::viewAppointmentDetails, "viewing appointment details");
    }//GEN-LAST:event_viewDetailsButtonActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        safeExecute(this::loadUpcomingAppointments, "refreshing appointments");
    }//GEN-LAST:event_refreshButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable appointmentTable;
    private javax.swing.JButton backButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JLabel doctorLabel;
    private javax.swing.JButton jButton2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton refreshButton;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JPanel titlePanel;
    private javax.swing.JButton viewDetailsButton;
    // End of variables declaration//GEN-END:variables




}