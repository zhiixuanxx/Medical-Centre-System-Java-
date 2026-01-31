package view;

import model.Doctor;
import model.Appointment;
import model.AppointmentDetails;
import model.Customer;
import util.FileStorage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class ViewPatientHistory extends JDialog {
    private final Doctor doctor;

    // Search components
    private JTextField searchField;
    private JButton searchBtn;
    private JButton viewHistoryBtn;

    // Patient selection
    private JTable patientTable;
    private DefaultTableModel patientTableModel;

    // History display components
    private JLabel selectedPatientLabel;
    private JTable appointmentHistoryTable;
    private DefaultTableModel appointmentHistoryModel;
    private JTextArea feedbackArea;

    // Data storage
    private List<Customer> allCustomers;
    private Map<String, AppointmentDetails> appointmentDetailsMap;

    public ViewPatientHistory(Doctor doctor, JFrame parent) {
        super(parent, "Patient History - " + doctor.getName(), true);
        this.doctor = doctor;
        this.allCustomers = new ArrayList<>();
        this.appointmentDetailsMap = new HashMap<>();

        initializeComponents();
        loadCustomerData();
        loadAppointmentDetails();

        setSize(1200, 800);
        setLocationRelativeTo(parent);
        setResizable(true);
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));

        // Top: patient search & selection
        JPanel topPanel = createPatientSelectionPanel();
        add(topPanel, BorderLayout.NORTH);

        // Center: history + feedback
        JPanel centerPanel = createHistoryDisplayPanel();
        add(centerPanel, BorderLayout.CENTER);

        // Bottom: close
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createPatientSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Patient Selection"));
        panel.setPreferredSize(new Dimension(1180, 200));

        // Search row
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        searchBtn = new JButton("Search Patient");
        viewHistoryBtn = new JButton("View History");
        viewHistoryBtn.setEnabled(false);

        searchPanel.add(new JLabel("Search (Name/ID):"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(viewHistoryBtn);

        // Patient table
        String[] patientColumns = {"Customer ID", "Name", "Email", "Phone", "Total Appointments"};
        patientTableModel = new DefaultTableModel(patientColumns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        patientTable = new JTable(patientTableModel);
        patientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane patientScrollPane = new JScrollPane(patientTable);
        patientScrollPane.setPreferredSize(new Dimension(1160, 120));

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(patientScrollPane, BorderLayout.CENTER);

        setupPatientSelectionEvents();
        return panel;
    }

    private JPanel createHistoryDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        selectedPatientLabel = new JLabel("Select a patient to view history");
        selectedPatientLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        selectedPatientLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(selectedPatientLabel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(700);

        // Left: appointment history
        JPanel appointmentPanel = createAppointmentHistoryPanel();
        splitPane.setLeftComponent(appointmentPanel);

        // Right: feedback
        JPanel feedbackPanel = createFeedbackPanel();
        splitPane.setRightComponent(feedbackPanel);

        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAppointmentHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Appointment History & Charges"));

        String[] historyColumns = {"Date", "Time", "Status", "Diagnosis", "Charges (RM)", "Notes"};
        appointmentHistoryModel = new DefaultTableModel(historyColumns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        appointmentHistoryTable = new JTable(appointmentHistoryModel);
        appointmentHistoryTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Column widths
        appointmentHistoryTable.getColumnModel().getColumn(0).setPreferredWidth(90);  // Date
        appointmentHistoryTable.getColumnModel().getColumn(1).setPreferredWidth(70);  // Time
        appointmentHistoryTable.getColumnModel().getColumn(2).setPreferredWidth(90);  // Status
        appointmentHistoryTable.getColumnModel().getColumn(3).setPreferredWidth(180); // Diagnosis
        appointmentHistoryTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Charges
        appointmentHistoryTable.getColumnModel().getColumn(5).setPreferredWidth(250); // Notes

        JScrollPane historyScrollPane = new JScrollPane(appointmentHistoryTable);
        historyScrollPane.setPreferredSize(new Dimension(680, 400));
        panel.add(historyScrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFeedbackPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Patient Feedback"));

        feedbackArea = new JTextArea();
        feedbackArea.setEditable(false);
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        feedbackArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane feedbackScrollPane = new JScrollPane(feedbackArea);
        feedbackScrollPane.setPreferredSize(new Dimension(450, 400));
        panel.add(feedbackScrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setPreferredSize(new Dimension(1180, 50));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        panel.add(closeBtn);

        return panel;
    }

    private void setupPatientSelectionEvents() {
        searchBtn.addActionListener(e -> searchPatients());
        searchField.addActionListener(e -> searchPatients());

        patientTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                viewHistoryBtn.setEnabled(patientTable.getSelectedRow() >= 0);
            }
        });

        viewHistoryBtn.addActionListener(e -> viewSelectedPatientHistory());

        patientTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) viewSelectedPatientHistory();
            }
        });
    }

    private void loadCustomerData() {
        try {
            allCustomers = FileStorage.getAllCustomers();
            loadPatientsWithAppointments();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading customer data: " + e.getMessage(),
                    "Load Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void loadPatientsWithAppointments() {
        patientTableModel.setRowCount(0);

        List<Appointment> allAppointments = FileStorage.readAppointments();
        Map<String, Integer> patientAppointmentCount = new HashMap<>();

        for (Appointment appointment : allAppointments) {
            if (appointment.getDoctorId().equals(doctor.getId())) {
                String customerId = appointment.getCustomerId();
                patientAppointmentCount.put(
                        customerId,
                        patientAppointmentCount.getOrDefault(customerId, 0) + 1
                );
            }
        }

        for (Customer customer : allCustomers) {
            if (patientAppointmentCount.containsKey(customer.getId())) {
                patientTableModel.addRow(new Object[]{
                        customer.getId(),
                        customer.getName(),
                        customer.getEmail(),
                        customer.getPhone(),
                        patientAppointmentCount.get(customer.getId())
                });
            }
        }
    }

    // Use FileStorage helper (reads ID, charges, diagnosis, notes; backward-compatible)
    private void loadAppointmentDetails() {
        this.appointmentDetailsMap = FileStorage.getAllAppointmentDetailsMap();
    }

    private void searchPatients() {
        String searchText = searchField.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            loadPatientsWithAppointments();
            return;
        }

        patientTableModel.setRowCount(0);

        List<Appointment> allAppointments = FileStorage.readAppointments();
        Map<String, Integer> patientAppointmentCount = new HashMap<>();
        for (Appointment appointment : allAppointments) {
            if (appointment.getDoctorId().equals(doctor.getId())) {
                patientAppointmentCount.put(
                        appointment.getCustomerId(),
                        patientAppointmentCount.getOrDefault(appointment.getCustomerId(), 0) + 1
                );
            }
        }

        for (Customer customer : allCustomers) {
            boolean matches = customer.getName().toLowerCase().contains(searchText)
                    || customer.getId().toLowerCase().contains(searchText)
                    || customer.getEmail().toLowerCase().contains(searchText);

            if (patientAppointmentCount.containsKey(customer.getId()) && matches) {
                patientTableModel.addRow(new Object[]{
                        customer.getId(),
                        customer.getName(),
                        customer.getEmail(),
                        customer.getPhone(),
                        patientAppointmentCount.get(customer.getId())
                });
            }
        }
    }

    private void viewSelectedPatientHistory() {
        int selectedRow = patientTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a patient first");
            return;
        }

        String customerId = (String) patientTableModel.getValueAt(selectedRow, 0);
        String customerName = (String) patientTableModel.getValueAt(selectedRow, 1);

        selectedPatientLabel.setText("Patient History: " + customerName + " (ID: " + customerId + ")");

        loadAppointmentHistory(customerId);
        loadPatientFeedback(customerId);
    }

    private void loadAppointmentHistory(String customerId) {
        appointmentHistoryModel.setRowCount(0);

        List<Appointment> allAppointments = FileStorage.readAppointments();

        for (Appointment appointment : allAppointments) {
            if (appointment.getCustomerId().equals(customerId)
                    && appointment.getDoctorId().equals(doctor.getId())) {

                String appointmentId = appointment.getAppointmentId();
                AppointmentDetails details = appointmentDetailsMap.get(appointmentId);

                String diagnosis = (details != null) ? details.getDiagnosis() : "Not recorded";
                String charges   = (details != null) ? String.format("%.2f", details.getCharges()) : "0.00";
                String notes     = (details != null) ? details.getNotes() : ""; // <-- show notes

                appointmentHistoryModel.addRow(new Object[]{
                        appointment.getDate(),
                        appointment.getTime(),
                        appointment.getStatus(),
                        diagnosis,
                        charges,
                        notes
                });
            }
        }
    }

    private void loadPatientFeedback(String customerId) {
        StringBuilder feedbackText = new StringBuilder();
        feedbackText.append("PATIENT FEEDBACK HISTORY\n");
        feedbackText.append("==================================================\n\n");

        try (BufferedReader reader = new BufferedReader(new FileReader("feedback.txt"))) {
            String line;
            boolean hasFeedback = false;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String appointmentId = parts[0].trim();
                    String feedbackCustomerId = parts[1].trim();
                    String feedback = parts[2].trim();

                    if (feedbackCustomerId.equals(customerId)) {
                        String appointmentDate = getAppointmentDate(appointmentId);

                        feedbackText.append("Date: ").append(appointmentDate).append("\n");
                        feedbackText.append("Appointment ID: ").append(appointmentId).append("\n");
                        feedbackText.append("Feedback: ").append(feedback).append("\n");
                        feedbackText.append("----------------------------------------------\n\n");
                        hasFeedback = true;
                    }
                }
            }

            if (!hasFeedback) {
                feedbackText.append("No feedback records found for this patient.");
            }

        } catch (FileNotFoundException e) {
            feedbackText.append("Feedback file not found.");
        } catch (IOException e) {
            feedbackText.append("Error loading feedback: ").append(e.getMessage());
        }

        feedbackArea.setText(feedbackText.toString());
        feedbackArea.setCaretPosition(0);
    }

    private String getAppointmentDate(String appointmentId) {
        List<Appointment> appointments = FileStorage.readAppointments();
        for (Appointment appointment : appointments) {
            if (appointment.getAppointmentId().equals(appointmentId)) {
                return appointment.getDate();
            }
        }
        return "Unknown";
    }

    // Public refresh hook
    public void refreshData() {
        loadCustomerData();
        loadAppointmentDetails();

        appointmentHistoryModel.setRowCount(0);
        feedbackArea.setText("");
        selectedPatientLabel.setText("Select a patient to view history");
        viewHistoryBtn.setEnabled(false);
    }
}
