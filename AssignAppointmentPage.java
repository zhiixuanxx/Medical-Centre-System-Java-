package view;

import model.Appointment;
import model.AppointmentDetails;
import model.Customer;
import model.Doctor;
import model.Staff;
import util.FileStorage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AssignAppointmentPage extends JFrame {

    private final Staff loggedInStaff;

    private JTextField searchField;
    private JButton searchButton;
    private JTable apptTable;
    private DefaultTableModel apptModel;

    private JComboBox<String> specialtyComboBox;
    private JComboBox<Doctor> doctorComboBox;
    private JComboBox<String> slotComboBox;
    private JButton assignButton;
    private JLabel statusLabel;

    private Appointment selectedAppointment;

    public AssignAppointmentPage(Staff staff) {
        this.loggedInStaff = staff;

        setTitle("Assign Follow-up Appointment");
        setSize(950, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadFollowUpAppointments();

        setVisible(true);
    }

    private void initializeComponents() {
        searchField = new JTextField(20);
        searchButton = new JButton("Search");

        apptModel = new DefaultTableModel(
                new Object[]{"Appt ID", "Customer", "Doctor", "Date", "Diagnosis"}, 0);
        apptTable = new JTable(apptModel);
        apptTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        specialtyComboBox = new JComboBox<>();
        doctorComboBox = new JComboBox<>();
        slotComboBox = new JComboBox<>();

        assignButton = new JButton("Assign Follow-up");
        assignButton.setEnabled(false);

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void setupLayout() {
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Appointment table (left side)
        JScrollPane apptScroll = new JScrollPane(apptTable);
        apptScroll.setBorder(BorderFactory.createTitledBorder("Follow-up Appointments"));

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(searchPanel, BorderLayout.NORTH);
        leftPanel.add(apptScroll, BorderLayout.CENTER);

        // Right panel (form)
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Assign Next Appointment"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; rightPanel.add(new JLabel("Specialty:"), gbc);
        gbc.gridx = 1; rightPanel.add(specialtyComboBox, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; rightPanel.add(new JLabel("Doctor:"), gbc);
        gbc.gridx = 1; rightPanel.add(doctorComboBox, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; rightPanel.add(new JLabel("Slot:"), gbc);
        gbc.gridx = 1; rightPanel.add(slotComboBox, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; rightPanel.add(statusLabel, gbc);

        // Split
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.6);

        // Bottom buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            dispose();
            new StaffDashboard(loggedInStaff);
        });
        buttonPanel.add(backButton);
        buttonPanel.add(assignButton);

        add(splitPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        searchButton.addActionListener(e -> searchAppointments());

        apptTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && apptTable.getSelectedRow() != -1) {
                int row = apptTable.getSelectedRow();
                String apptId = apptModel.getValueAt(row, 0).toString();

                selectedAppointment = FileStorage.readAppointments().stream()
                        .filter(a -> a.getAppointmentId().equals(apptId))
                        .findFirst().orElse(null);

                if (selectedAppointment != null) {
                    statusLabel.setText("Selected follow-up for " + selectedAppointment.getCustomerName());
                    loadSpecialties();
                }
            }
        });

        specialtyComboBox.addActionListener(e -> handleSpecialtySelection());
        doctorComboBox.addActionListener(e -> handleDoctorSelection());
        slotComboBox.addActionListener(e -> validateForm());
        assignButton.addActionListener(e -> handleAssign());
    }

    private void loadFollowUpAppointments() {
        apptModel.setRowCount(0);
        List<Appointment> allAppointments = FileStorage.readAppointments();
        List<AppointmentDetails> details = FileStorage.readAppointmentDetails();

        for (AppointmentDetails det : details) {
            if (det.isFollowUpNeeded()) {
                Appointment a = allAppointments.stream()
                        .filter(ap -> ap.getAppointmentId().equals(det.getAppointmentId()))
                        .findFirst().orElse(null);

                if (a != null) {
                    apptModel.addRow(new Object[]{
                            a.getAppointmentId(),
                            a.getCustomerName(),
                            a.getDoctorName(),
                            a.getDate(),
                            det.getDiagnosis()
                    });
                }
            }
        }
    }

    private void searchAppointments() {
        String keyword = searchField.getText().trim().toLowerCase();
        for (int i = apptModel.getRowCount() - 1; i >= 0; i--) {
            String apptId = apptModel.getValueAt(i, 0).toString().toLowerCase();
            String cust = apptModel.getValueAt(i, 1).toString().toLowerCase();
            String doc = apptModel.getValueAt(i, 2).toString().toLowerCase();
            if (!(apptId.contains(keyword) || cust.contains(keyword) || doc.contains(keyword))) {
                apptModel.removeRow(i);
            }
        }
    }

    private void loadSpecialties() {
        specialtyComboBox.removeAllItems();
        specialtyComboBox.addItem("-- Select Specialty --");
        for (String s : FileStorage.getAllSpecialties()) {
            if (!s.equalsIgnoreCase("Emergency")) {
                specialtyComboBox.addItem(s);
            }
        }
    }

    private void handleSpecialtySelection() {
        doctorComboBox.removeAllItems();
        slotComboBox.removeAllItems();
        String specialty = (String) specialtyComboBox.getSelectedItem();
        if (specialty != null && !specialty.startsWith("--")) {
            for (Doctor d : FileStorage.getDoctorsBySpecialty(specialty)) {
                if ("Clinical Doctor".equalsIgnoreCase(d.getDoctorType())) {
                    doctorComboBox.addItem(d);
                }
            }
        }
        validateForm();
    }

    private void handleDoctorSelection() {
         slotComboBox.removeAllItems();
        Doctor doctor = (Doctor) doctorComboBox.getSelectedItem();
        if (doctor != null) {
            if (!"Clinical Doctor".equalsIgnoreCase(doctor.getDoctorType())) {
                return;
            }

            List<String> rawSlots = FileStorage.getAvailableSlotsByDoctorId(doctor.getId());
            for (String raw : rawSlots) {
                String[] parts = raw.split(" ");
                if (parts.length == 2) {
                    LocalDate date = LocalDate.parse(parts[0]); // yyyy-MM-dd
                    String displayDate = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    slotComboBox.addItem(displayDate + " at " + parts[1]);
                }
            }
        }
        validateForm();
    }

    private void validateForm() {
        boolean isValid = selectedAppointment != null &&
                specialtyComboBox.getSelectedItem() != null &&
                doctorComboBox.getSelectedItem() != null &&
                slotComboBox.getSelectedItem() != null;
        assignButton.setEnabled(isValid);
    }

    private void handleAssign() {
        if (selectedAppointment == null) {
            statusLabel.setText("Please select an appointment first.");
            return;
        }

        Doctor selectedDoctor = (Doctor) doctorComboBox.getSelectedItem();
        String selectedSlot = (String) slotComboBox.getSelectedItem();
        if (selectedDoctor == null || selectedSlot == null) {
            statusLabel.setText("Please complete all fields.");
            return;
        }

        String[] parts = selectedSlot.split(" at ");
        String displayDate = parts[0];
        String time = parts[1];

        String[] dateParts = displayDate.split("/");
        String scheduleDate = String.format("%s-%02d-%02d",
                dateParts[2], Integer.parseInt(dateParts[1]), Integer.parseInt(dateParts[0]));

        String appointmentId = FileStorage.generateAppointmentID();
        Appointment newAppt = new Appointment(
                appointmentId,
                selectedAppointment.getCustomerId(),
                selectedAppointment.getCustomerName(),
                selectedDoctor.getId(),
                selectedDoctor.getName(),
                displayDate,
                time,
                "Upcoming",
                "Unpaid"
        );

        FileStorage.writeAppointment(newAppt);
        FileStorage.removeSlot(selectedDoctor.getId(), scheduleDate, time);

        JOptionPane.showMessageDialog(this,
                "Follow-up booked for " + selectedAppointment.getCustomerName() +
                        "\nDoctor: " + selectedDoctor.getName() +
                        "\nDate: " + displayDate +
                        " at " + time,
                "Success", JOptionPane.INFORMATION_MESSAGE);

        dispose();
        new StaffDashboard(loggedInStaff);
    }
}
