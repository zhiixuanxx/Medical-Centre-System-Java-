package view;

import model.Doctor;
import model.Appointment;
import model.AppointmentDetails;
import util.FileStorage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ArrayList;

public class EnterChargesDiagnosis extends JDialog {
    private final Doctor doctor;

    // Appointment components
    private JTable appointmentTable;
    private DefaultTableModel appointmentModel;
    private JTextField searchField;
    private JButton searchBtn;

    // Form components
    private JTextField appointmentIdField;
    private JTextField patientNameField;
    private JTextField diagnosisField;
    private JTextField chargeAmountField;
    private JTextArea notesArea;
    private JCheckBox followUpCheckBox;   // ✅ new
    private JButton submitBtn;
    private JButton cancelBtn;

    // Data storage
    private final List<Appointment> doctorAppointments;

    public EnterChargesDiagnosis(Doctor doctor, JFrame parent) {
        super(parent, "Enter Charges, Diagnosis & Notes", true);
        this.doctor = doctor;
        this.doctorAppointments = new ArrayList<>();

        initializeComponents();
        loadDoctorAppointments();

        setMinimumSize(new Dimension(800, 600));
        setSize(900, 700);
        setLocationRelativeTo(parent);
        setResizable(true);
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));

        // ===== Top panel - Appointments =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Appointments - " + doctor.getName()));
        topPanel.setPreferredSize(new Dimension(780, 300));

        // Search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(18);
        searchBtn = new JButton("Search");
        searchPanel.add(new JLabel("Search (Name / ID):"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);

        // Table
        String[] columns = {"Appointment ID", "Patient Name", "Date", "Time", "Status"};
        appointmentModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        appointmentTable = new JTable(appointmentModel);
        appointmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appointmentTable.setPreferredScrollableViewportSize(new Dimension(750, 220));
        JScrollPane tableScrollPane = new JScrollPane(appointmentTable);
        tableScrollPane.setPreferredSize(new Dimension(780, 250));

        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(tableScrollPane, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // ===== Bottom panel - Diagnosis & Charges Form =====
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Diagnosis, Charges & Notes"));
        formPanel.setPreferredSize(new Dimension(780, 350));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        // Appointment ID
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Appointment ID:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        appointmentIdField = new JTextField(24);
        appointmentIdField.setEditable(false);
        appointmentIdField.setBackground(new Color(240,240,240));
        formPanel.add(appointmentIdField, gbc);

        // Patient Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Patient:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        patientNameField = new JTextField(24);
        patientNameField.setEditable(false);
        patientNameField.setBackground(new Color(240,240,240));
        formPanel.add(patientNameField, gbc);

        // Diagnosis
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Diagnosis:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        diagnosisField = new JTextField(24);
        formPanel.add(diagnosisField, gbc);

        // Charge Amount
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Charge (RM):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        chargeAmountField = new JTextField(24);
        formPanel.add(chargeAmountField, gbc);

        // Notes
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        notesArea = new JTextArea(4, 24);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(notesArea), gbc);

        // Follow-up
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        followUpCheckBox = new JCheckBox("Follow-up required");
        formPanel.add(followUpCheckBox, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER; gbc.weightx = 0; gbc.weighty = 0;
        JPanel buttonPanel = new JPanel();
        submitBtn = new JButton("Submit");
        cancelBtn = new JButton("Cancel");
        submitBtn.setEnabled(false);
        buttonPanel.add(submitBtn);
        buttonPanel.add(cancelBtn);
        formPanel.add(buttonPanel, gbc);

        add(formPanel, BorderLayout.CENTER);

        // ===== Events =====
        appointmentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onSelectRow();
        });
        searchBtn.addActionListener(e -> searchPatient());
        searchField.addActionListener(e -> searchPatient());
        submitBtn.addActionListener(e -> submitData());
        cancelBtn.addActionListener(e -> dispose());

        // ESC to close
        KeyStroke esc = KeyStroke.getKeyStroke("ESCAPE");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(esc, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { dispose(); }
        });
    }

    private void loadDoctorAppointments() {
        try {
            List<Appointment> all = FileStorage.readAppointments();

            appointmentModel.setRowCount(0);
            doctorAppointments.clear();

            for (Appointment a : all) {
                if (a.getDoctorId().equals(doctor.getId())) {
                    doctorAppointments.add(a);
                    appointmentModel.addRow(new Object[]{
                            a.getAppointmentId(),
                            a.getCustomerName(),
                            a.getDate(),
                            a.getTime(),
                            a.getStatus()
                    });
                }
            }

            if (doctorAppointments.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No appointments found for Dr. " + doctor.getName(),
                        "No Appointments", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading appointments: " + ex.getMessage(),
                    "Load Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void onSelectRow() {
        int row = appointmentTable.getSelectedRow();
        if (row < 0) return;

        String apptId = String.valueOf(appointmentModel.getValueAt(row, 0));
        String patient = String.valueOf(appointmentModel.getValueAt(row, 1));
        String status = String.valueOf(appointmentModel.getValueAt(row, 4));

        if (!"Upcoming".equalsIgnoreCase(status) && !"Completed".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this,
                    "Only 'Upcoming' or 'Completed' appointments can be edited.",
                    "Invalid Selection", JOptionPane.WARNING_MESSAGE);
            appointmentTable.clearSelection();
            return;
        }

        appointmentIdField.setText(apptId);
        patientNameField.setText(patient);
        submitBtn.setEnabled(true);

        loadExistingDetails(apptId);
    }

    private void loadExistingDetails(String appointmentId) {
        try {
            AppointmentDetails det = FileStorage.getAppointmentDetailsById(appointmentId);
            if (det != null) {
                chargeAmountField.setText(det.getCharges() == 0 ? "" : String.format("%.2f", det.getCharges()));
                diagnosisField.setText(det.getDiagnosis() == null ? "" : det.getDiagnosis());
                notesArea.setText(det.getNotes() == null ? "" : det.getNotes());
                followUpCheckBox.setSelected(det.isFollowUpNeeded()); // ✅ restore flag
            } else {
                diagnosisField.setText("");
                chargeAmountField.setText("");
                notesArea.setText("");
                followUpCheckBox.setSelected(false);
            }
        } catch (Exception ex) {
            diagnosisField.setText("");
            chargeAmountField.setText("");
            notesArea.setText("");
            followUpCheckBox.setSelected(false);
        }
    }

    private void searchPatient() {
        String q = searchField.getText().trim().toLowerCase();
        if (q.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a name or appointment ID to search.",
                    "Search Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        appointmentTable.clearSelection();
        for (int i = 0; i < appointmentModel.getRowCount(); i++) {
            String name = String.valueOf(appointmentModel.getValueAt(i, 1)).toLowerCase();
            String id = String.valueOf(appointmentModel.getValueAt(i, 0)).toLowerCase();
            if (name.contains(q) || id.contains(q)) {
                appointmentTable.setRowSelectionInterval(i, i);
                appointmentTable.scrollRectToVisible(appointmentTable.getCellRect(i, 0, true));
                searchField.setText("");
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "No matches found for: " + q,
                "Search Result", JOptionPane.INFORMATION_MESSAGE);
    }

    private void submitData() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment first.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String appointmentId = appointmentIdField.getText().trim();
        String patientName = patientNameField.getText().trim();
        String diagnosis = diagnosisField.getText().trim();
        String chargeAmount = chargeAmountField.getText().trim();
        String notes = notesArea.getText().trim();
        boolean followUpNeeded = followUpCheckBox.isSelected();

        if (diagnosis.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a diagnosis.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            diagnosisField.requestFocus();
            return;
        }
        if (chargeAmount.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter charge amount.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            chargeAmountField.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(chargeAmount);
            if (amount < 0) throw new NumberFormatException("negative");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid non-negative number for charges.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            chargeAmountField.requestFocus();
            return;
        }

        try {
            AppointmentDetails det = new AppointmentDetails(
                    appointmentId, amount, diagnosis, notes, followUpNeeded);
            FileStorage.saveOrUpdateAppointmentDetails(det);
            FileStorage.updateAppointmentStatus(appointmentId, "Completed");

            JOptionPane.showMessageDialog(this, 
                "Saved. Follow-up needed: " + (followUpNeeded ? "YES" : "NO"),
                "Success", JOptionPane.INFORMATION_MESSAGE);

            appointmentModel.setValueAt("Completed", selectedRow, 4);
            clearForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving data: " + ex.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void clearForm() {
        appointmentTable.clearSelection();
        appointmentIdField.setText("");
        patientNameField.setText("");
        diagnosisField.setText("");
        chargeAmountField.setText("");
        notesArea.setText("");
        followUpCheckBox.setSelected(false);
        searchField.setText("");
        submitBtn.setEnabled(false);
    }
}
