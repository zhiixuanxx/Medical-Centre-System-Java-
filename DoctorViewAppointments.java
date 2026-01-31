package view;

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
import javax.swing.table.DefaultTableCellRenderer;

public class DoctorViewAppointments extends JFrame {
    private static final Logger logger = Logger.getLogger(DoctorViewAppointments.class.getName());

    private final Doctor doctor;
    private final DoctorDashboard parentDashboard;

    private JTable appointmentTable;
    private JButton viewDetailsButton, completeAppointmentButton, refreshButton, backButton;
    private JLabel statusLabel; // <-- field instead of looking up by index

    public DoctorViewAppointments(Doctor doctor, DoctorDashboard parentDashboard) {
        if (doctor == null) throw new IllegalArgumentException("Doctor cannot be null");
        this.doctor = doctor;
        this.parentDashboard = parentDashboard;

        initializeComponents();
        setupLayout();
        loadUpcomingAppointments();
        setVisible(true);
    }

    private void initializeComponents() {
        setTitle("Upcoming Appointments - Dr. " + doctor.getName());
        setSize(1100, 650);
        if (parentDashboard != null) {
            setLocationRelativeTo(parentDashboard);
        } else {
            setLocationRelativeTo(null);
        }
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String[] columns = {
                "Appointment ID", "Patient Name", "Date", "Time",
                "Status", "Patient Contact", "Patient DOB"
        };

        appointmentTable = new JTable(new DefaultTableModel(new Object[0][0], columns) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        });
        appointmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appointmentTable.setRowHeight(25);
        appointmentTable.getTableHeader().setReorderingAllowed(false);

        // Buttons
        viewDetailsButton = createStyledButton("View Details", new Color(52, 152, 219));
        completeAppointmentButton = createStyledButton("Mark as Completed", new Color(46, 204, 113));
        refreshButton = createStyledButton("Refresh", new Color(52, 73, 94));
        backButton = createStyledButton("Back to Dashboard", new Color(149, 165, 166));

        // Actions
        viewDetailsButton.addActionListener(e -> safeExecute(this::viewAppointmentDetails, "viewing appointment details"));
        completeAppointmentButton.addActionListener(e -> safeExecute(this::markAppointmentCompleted, "completing appointment"));
        refreshButton.addActionListener(e -> safeExecute(this::loadUpcomingAppointments, "refreshing appointments"));
        backButton.addActionListener(e -> {
            dispose();
            if (parentDashboard != null) parentDashboard.setVisible(true);
        });

        statusLabel = new JLabel("Select an appointment to view details or mark as completed");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        statusLabel.setForeground(Color.GRAY);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        // Title
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(248, 249, 250));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("My Upcoming Appointments", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(25, 25, 112));

        JLabel doctorLabel = new JLabel("Dr. " + doctor.getName() + " - " + doctor.getSpecialty(), SwingConstants.CENTER);
        doctorLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        doctorLabel.setForeground(Color.GRAY);

        titlePanel.setLayout(new BorderLayout());
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(doctorLabel, BorderLayout.SOUTH);

        // Table
        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Appointment List"));

        // Buttons row
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(new Color(248, 249, 250));
        buttonPanel.add(viewDetailsButton);
        buttonPanel.add(completeAppointmentButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(backButton);

        // Footer (status + buttons)
        JPanel footer = new JPanel(new BorderLayout());
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(new Color(248, 249, 250));
        statusPanel.add(statusLabel);

        footer.add(statusPanel, BorderLayout.NORTH);
        footer.add(buttonPanel, BorderLayout.SOUTH);

        add(titlePanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH); // single SOUTH container (no PAGE_END)
    }

    private void loadUpcomingAppointments() {
        try {
            List<Appointment> allAppointments = FileStorage.readAppointments();
            if (allAppointments == null) {
                allAppointments = new ArrayList<>();
                logger.warning("readAppointments returned null");
            }

            List<Appointment> doctorUpcomingAppointments = new ArrayList<>();
            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();

            logger.info("Processing appointments for doctor: " + doctor.getId());
            logger.info("Today's date: " + today + ", Current time: " + now);

            for (Appointment appointment : allAppointments) {
                if (appointment == null) continue;
                if (!doctor.getId().equals(appointment.getDoctorId())) continue;
                if (!("Upcoming".equalsIgnoreCase(appointment.getStatus())
                   || "Emergency".equalsIgnoreCase(appointment.getStatus()))) continue;

                try {
                    LocalDate appointmentDate = parseAppointmentDate(appointment.getDate());
                    LocalTime appointmentTime = parseAppointmentTime(appointment.getTime());

                    //Emergency: always show, regardless of time
                    if ("Emergency".equalsIgnoreCase(appointment.getStatus())) {
                        doctorUpcomingAppointments.add(appointment);
                        continue;
                    }

                    // Normal rule for upcoming
                    if (appointmentDate.isAfter(today) ||
                        (appointmentDate.equals(today) && appointmentTime.isAfter(now))) {
                        doctorUpcomingAppointments.add(appointment);
                    }

                } catch (Exception e) {
                    logger.log(Level.WARNING,
                        "Error parsing date/time for appointment " + appointment.getAppointmentId(), e);
                    doctorUpcomingAppointments.add(appointment); // fallback
                }
            }

            logger.info("Found " + doctorUpcomingAppointments.size() + " upcoming appointments before sorting");
            sortAppointmentsByDateTime(doctorUpcomingAppointments);
            logger.info("Appointments sorted successfully");

            updateTable(doctorUpcomingAppointments);
            statusLabel.setText("Found " + doctorUpcomingAppointments.size() + " upcoming appointments (sorted by date & time)");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading upcoming appointments for doctor: " + doctor.getId(), e);
            showErrorDialog("Unable to load appointments. Please try again.");
            updateTable(new ArrayList<>());
            statusLabel.setText("Unable to load appointments.");
        }
    }

    private void sortAppointmentsByDateTime(List<Appointment> appointments) {
        appointments.sort((a1, a2) -> {
            try {
                if ("Emergency".equalsIgnoreCase(a1.getStatus()) && !"Emergency".equalsIgnoreCase(a2.getStatus())) return -1;
                if ("Emergency".equalsIgnoreCase(a2.getStatus()) && !"Emergency".equalsIgnoreCase(a1.getStatus())) return 1;
                
                LocalDate date1 = parseAppointmentDate(a1.getDate());
                LocalTime time1 = parseAppointmentTime(a1.getTime());
                LocalDateTime dt1 = LocalDateTime.of(date1, time1);

                LocalDate date2 = parseAppointmentDate(a2.getDate());
                LocalTime time2 = parseAppointmentTime(a2.getTime());
                LocalDateTime dt2 = LocalDateTime.of(date2, time2);

                return dt1.compareTo(dt2);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error parsing date/time during sorting", e);
                String id1 = a1.getAppointmentId();
                String id2 = a2.getAppointmentId();
                if (id1 != null && id2 != null) return id1.compareTo(id2);
                return 0;
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
            try {
                if (appointment == null) continue;

                Customer customer = getCustomerById(appointment.getCustomerId());
                String patientName = customer != null ? customer.getName() : "Unknown Patient";
                String patientContact = customer != null ? customer.getPhone() : "N/A";
                String patientDOB = customer != null ? customer.getDob() : "N/A";
                String displayDate = formatDateForDisplay(appointment.getDate());

                model.addRow(new Object[]{
                        nonNull(appointment.getAppointmentId(), "N/A"),
                        patientName,
                        nonNull(displayDate, "N/A"),
                        nonNull(appointment.getTime(), "N/A"),
                        nonNull(appointment.getStatus(), "Unknown"),
                        patientContact,
                        patientDOB
                });
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error adding appointment to table", e);
                model.addRow(new Object[]{"ERROR", "Error loading", "Error", "Error", "Error", "Error", "Error"});
            }
        }

        appointmentTable.setModel(model);
        applyColumnWidths();
        //Custom renderer for Emergency highlighting
        appointmentTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = (String) table.getValueAt(row, 4); // Status column

                if ("Emergency".equalsIgnoreCase(status)) {
                    c.setForeground(Color.RED);
                    setText(value.toString());
                } else {
                    c.setForeground(isSelected ? table.getSelectionForeground() : Color.BLACK);
                }
                return c;
            }
        });
    }

    private void applyColumnWidths() {
        try {
            TableColumnModel cols = appointmentTable.getColumnModel();
            cols.getColumn(0).setPreferredWidth(120); // Appointment ID
            cols.getColumn(1).setPreferredWidth(150); // Patient Name
            cols.getColumn(2).setPreferredWidth(100); // Date
            cols.getColumn(3).setPreferredWidth(80);  // Time
            cols.getColumn(4).setPreferredWidth(100); // Status
            cols.getColumn(5).setPreferredWidth(120); // Contact
            cols.getColumn(6).setPreferredWidth(100); // DOB
        } catch (Exception ignored) {}
    }

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
            AppointmentDetails details = FileStorage.getAppointmentDetailsById(appointmentId);

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

    private void markAppointmentCompleted() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow == -1) {
            showInfoDialog("Please select an appointment to mark as completed.");
            return;
        }

        try {
            String appointmentId = safeGetTableValue(selectedRow, 0, "Unknown");
            String patientName   = safeGetTableValue(selectedRow, 1, "Unknown Patient");
            String date          = safeGetTableValue(selectedRow, 2, "Unknown Date");
            String time          = safeGetTableValue(selectedRow, 3, "Unknown Time");

            if ("ERROR".equals(appointmentId)) {
                showErrorDialog("Cannot complete this appointment due to data errors.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Mark this appointment as completed?\n\n" +
                            "Patient: " + patientName + "\n" +
                            "Date: " + date + "\n" +
                            "Time: " + time,
                    "Confirm Completion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = FileStorage.updateAppointmentStatus(appointmentId, "Completed");

                if (success) {
                    showSuccessDialog("Appointment marked as completed successfully!");
                    loadUpcomingAppointments(); // Refresh

                    int enterDetails = JOptionPane.showConfirmDialog(this,
                            "Would you like to enter charges and diagnosis for this appointment?",
                            "Enter Medical Details",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);

                    if (enterDetails == JOptionPane.YES_OPTION) {
                        // If your EnterChargesDiagnosis dialog can take an appointmentId, prefer passing it.
                        // new EnterChargesDiagnosis(doctor, parentDashboard, appointmentId).setVisible(true);
                        EnterChargesDiagnosis dialog = new EnterChargesDiagnosis(doctor, parentDashboard);
                        dialog.setVisible(true);
                    }
                } else {
                    showErrorDialog("Failed to update appointment status. Please try again.");
                }
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error marking appointment as completed", e);
            showErrorDialog("Unable to complete appointment: " + e.getMessage());
        }
    }

    // ---------- Helpers ----------

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

    private LocalDate parseAppointmentDate(String dateStr) throws DateTimeParseException {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new DateTimeParseException("Date is null or empty", dateStr, 0);
        }
        String cleanDate = dateStr.trim();

        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("d/MM/yyyy"),
                DateTimeFormatter.ofPattern("dd/M/yyyy"),
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                DateTimeFormatter.ofPattern("yyyy/M/dd"),
                DateTimeFormatter.ofPattern("yyyy/MM/d"),
                DateTimeFormatter.ofPattern("yyyy/M/d"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                DateTimeFormatter.ofPattern("M/dd/yyyy"),
                DateTimeFormatter.ofPattern("MM/d/yyyy"),
                DateTimeFormatter.ofPattern("M/d/yyyy")
        };

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(cleanDate, formatter);
            } catch (DateTimeParseException ignored) {}
        }
        logger.log(Level.WARNING, "Unable to parse date: '" + cleanDate + "' with any known format");
        throw new DateTimeParseException("Unable to parse date with any known format", cleanDate, 0);
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

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(160, 35));

        Color hoverColor = backgroundColor.darker();
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent evt) { button.setBackground(hoverColor); }
            @Override public void mouseExited (java.awt.event.MouseEvent evt) { button.setBackground(backgroundColor); }
        });
        return button;
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
}
