package view;

import model.Customer;
import model.Appointment;
import model.Doctor;
import model.AppointmentDetails;
import util.FileStorage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class History extends JFrame {
    private final Customer customer;
    private JTable appointmentTable;
    private JButton viewButton, refreshButton, backButton;

    public History(Customer customer) {
        this.customer = customer;
        initializeComponents();
        setupLayout();
        loadAppointments();
        setVisible(true);
    }

    private void initializeComponents() {
        setTitle("Appointment History - " + customer.getName());
        setSize(1000, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String[] columns = {
                "ID", "Doctor", "Specialty", "Date", "Time", "Status",
                "Diagnosis", "Charges (RM)", "Notes"
        };
        appointmentTable = new JTable(new DefaultTableModel(new Object[0][0], columns) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        });
        appointmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appointmentTable.getTableHeader().setReorderingAllowed(false);

        viewButton = new JButton("View Details");
        viewButton.setBackground(new Color(0, 120, 215));
        viewButton.setForeground(Color.WHITE);

        refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(50, 150, 50));
        refreshButton.setForeground(Color.WHITE);

        backButton = new JButton("Back");
        backButton.setBackground(new Color(102, 102, 255));
        backButton.setForeground(Color.WHITE);

        viewButton.addActionListener(e -> viewAppointmentDetails());
        refreshButton.addActionListener(e -> loadAppointments());
        backButton.addActionListener(e -> {
            dispose();
            new CustomerDashboard(customer);
        });
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("My Past Appointments", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titlePanel.add(titleLabel);

        JScrollPane scrollPane = new JScrollPane(appointmentTable);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(viewButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(backButton);

        add(titlePanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
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
                            LocalDate ad;
                            if (s.contains("/")) ad = LocalDate.parse(s, dmy);
                            else if (s.contains("-")) ad = LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            else return false;

                            return ad.isBefore(today)
                                    || "Completed".equalsIgnoreCase(apt.getStatus())
                                    || "Cancelled".equalsIgnoreCase(apt.getStatus());
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
                "ID", "Doctor", "Specialty", "Date", "Time", "Status",
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

            model.addRow(new Object[]{
                    apt.getAppointmentId(),
                    doctor != null ? doctor.getName() : "Unknown",
                    doctor != null ? doctor.getSpecialty() : "Unknown",
                    displayDate(apt.getDate()),
                    apt.getTime(),
                    apt.getStatus(),
                    diagnosis,
                    charges == 0.0 ? "" : String.format("%.2f", charges),
                    notes
            });
        }

        appointmentTable.setModel(model);
        appointmentTable.getColumnModel().getColumn(6).setPreferredWidth(180); // Diagnosis
        appointmentTable.getColumnModel().getColumn(8).setPreferredWidth(220); // Notes
    }

    private String displayDate(String raw) {
        String s = raw == null ? "" : raw.trim();
        try {
            if (s.contains("-")) {
                // yyyy-MM-dd -> dd/MM/yyyy
                return LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } else {
                // assume dd/MM/yyyy
                LocalDate.parse(s, DateTimeFormatter.ofPattern("dd/MM/yyyy")); // validate
                return s;
            }
        } catch (Exception e) { return s; }
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
            AppointmentDetails details = FileStorage.getAppointmentDetailsById(appointmentId);

            String diagnosis = details != null ? safe(details.getDiagnosis()) : "No diagnosis available";
            String notes;
            try { notes = details != null ? safe(details.getNotes()) : ""; } catch (Throwable ignore) { notes = ""; }
            double charges = 0.0;
            try { if (details != null) charges = details.getCharges(); } catch (Exception ignore) {}

            String detailsText = String.format(
                    "Appointment Details:\n\n" +
                            "ID: %s\n" +
                            "Doctor: %s\n" +
                            "Specialty: %s\n" +
                            "Date: %s\n" +
                            "Time: %s\n" +
                            "Status: %s\n" +
                            "Patient: %s\n\n" +
                            "Diagnosis: %s\n" +
                            "Charges: RM %s\n" +
                            "Notes: %s",
                    apt.getAppointmentId(),
                    doctor != null ? doctor.getName() : "Unknown",
                    doctor != null ? doctor.getSpecialty() : "Unknown",
                    displayDate(apt.getDate()),
                    apt.getTime(),
                    apt.getStatus(),
                    customer.getName(),
                    diagnosis,
                    (charges == 0.0 ? "-" : String.format("%.2f", charges)),
                    (notes.isEmpty() ? "-" : notes)
            );

            JOptionPane.showMessageDialog(this, detailsText, "Appointment Details", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error viewing appointment details: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
