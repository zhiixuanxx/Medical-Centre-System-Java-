package view;

import model.*;
import util.FileStorage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ViewAllAppointments extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JTextField fromDateField, toDateField;
    private JButton searchButton, resetButton, backButton, viewDetailButton;
    private Manager loggedInManager; 

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ViewAllAppointments(Manager manager) {
        this.loggedInManager = manager;
        setTitle("All Appointments");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ===== Table =====
        tableModel = new DefaultTableModel(
                new Object[]{"Appt ID", "Patient ID", "Patient Name", "Doctor ID", "Doctor Name", "Date", "Time", "Status"}, 0
        );
        table = new JTable(tableModel);
        loadAppointments(FileStorage.readAppointments());

        JScrollPane scrollPane = new JScrollPane(table);

        // ===== Search Panel =====
        JPanel searchPanel = new JPanel(new GridLayout(2, 5, 10, 10));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search & Filter"));

        searchField = new JTextField();
        statusFilter = new JComboBox<>(new String[]{"All", "Upcoming", "Completed", "Cancelled"});
        fromDateField = new JTextField(); // dd/MM/yyyy
        toDateField = new JTextField();

        searchButton = new JButton("Search");
        resetButton = new JButton("Reset");
        backButton = new JButton("Back");
        viewDetailButton = new JButton("View Detail");

        searchPanel.add(new JLabel("Search (Appt ID / Cust Name / Doctor Name):"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("Status:"));
        searchPanel.add(statusFilter);
        searchPanel.add(new JLabel());

        searchPanel.add(new JLabel("From Date (dd/MM/yyyy):"));
        searchPanel.add(fromDateField);
        searchPanel.add(new JLabel("To Date (dd/MM/yyyy):"));
        searchPanel.add(toDateField);
        searchPanel.add(searchButton);

        // ===== Buttons Panel =====
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(viewDetailButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(backButton);

        // ===== Layout =====
        setLayout(new BorderLayout());
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // ===== Events =====
        searchButton.addActionListener(e -> doSearch());
        resetButton.addActionListener(e -> {
            searchField.setText("");
            statusFilter.setSelectedIndex(0);
            fromDateField.setText("");
            toDateField.setText("");
            loadAppointments(FileStorage.readAppointments());
        });
        backButton.addActionListener(e -> {
            dispose();
            new ManagerDashboard(loggedInManager);
        });

        viewDetailButton.addActionListener(e -> viewDetail());

        setVisible(true);
    }

    private void loadAppointments(List<Appointment> appointments) {
        tableModel.setRowCount(0);
        for (Appointment a : appointments) {
            tableModel.addRow(new Object[]{
                    a.getAppointmentId(),
                    a.getCustomerId(),
                    a.getCustomerName(),
                    a.getDoctorId(),
                    a.getDoctorName(),
                    a.getDate(),
                    a.getTime(),
                    a.getStatus()
            });
        }
    }

    private void doSearch() {
        String keyword = searchField.getText().trim().toLowerCase();
        String selectedStatus = statusFilter.getSelectedItem().toString();
        String fromDateStr = fromDateField.getText().trim();
        String toDateStr = toDateField.getText().trim();

        List<Appointment> all = FileStorage.readAppointments();
        List<Appointment> filtered = all.stream()
                .filter(a ->
                        keyword.isEmpty() ||
                        a.getAppointmentId().toLowerCase().contains(keyword) ||
                        a.getCustomerName().toLowerCase().contains(keyword) ||
                        a.getDoctorName().toLowerCase().contains(keyword))
                .filter(a -> selectedStatus.equals("All") || a.getStatus().equalsIgnoreCase(selectedStatus))
                .filter(a -> {
                    if (fromDateStr.isEmpty() && toDateStr.isEmpty()) return true;
                    try {
                        LocalDate date = LocalDate.parse(a.getDate(), DATE_FORMAT);
                        LocalDate from = fromDateStr.isEmpty() ? LocalDate.MIN : LocalDate.parse(fromDateStr, DATE_FORMAT);
                        LocalDate to = toDateStr.isEmpty() ? LocalDate.MAX : LocalDate.parse(toDateStr, DATE_FORMAT);
                        return !date.isBefore(from) && !date.isAfter(to);
                    } catch (Exception e) {
                        return true; // ignore invalid dates
                    }
                })
                .collect(Collectors.toList());

        loadAppointments(filtered);
    }

    private void viewDetail() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment first.");
            return;
        }

        String detail = "Appointment Details:\n\n"
                + "Appointment ID: " + tableModel.getValueAt(selectedRow, 0) + "\n"
                + "Customer ID: " + tableModel.getValueAt(selectedRow, 1) + "\n"
                + "Customer Name: " + tableModel.getValueAt(selectedRow, 2) + "\n"
                + "Doctor ID: " + tableModel.getValueAt(selectedRow, 3) + "\n"
                + "Doctor Name: " + tableModel.getValueAt(selectedRow, 4) + "\n"
                + "Date: " + tableModel.getValueAt(selectedRow, 5) + "\n"
                + "Time: " + tableModel.getValueAt(selectedRow, 6) + "\n"
                + "Status: " + tableModel.getValueAt(selectedRow, 7);

        JOptionPane.showMessageDialog(this, detail, "Appointment Detail", JOptionPane.INFORMATION_MESSAGE);
    }
}
