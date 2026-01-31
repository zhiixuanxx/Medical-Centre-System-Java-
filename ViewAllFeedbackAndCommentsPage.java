package view;

import model.Feedback;
import model.Manager;
import model.Appointment;
import util.FileStorage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class ViewAllFeedbackAndCommentsPage extends JFrame {
    private JTable feedbackTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> ratingFilter;
    private JButton searchBtn, resetBtn, backBtn, viewDetailBtn;
    private final Manager manager;

    public ViewAllFeedbackAndCommentsPage(Manager manager) {
        this.manager = manager;

        setTitle("View All Feedback & Comments");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== Top Panel: Search & Filter =====
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Search (Appt ID / Cust ID / Doctor Name):"));
        searchField = new JTextField(20);
        searchBtn = new JButton("Search");
        topPanel.add(searchField);
        topPanel.add(searchBtn);

        topPanel.add(new JLabel("Filter by Rating:"));
        ratingFilter = new JComboBox<>(new String[]{"All", "1", "2", "3", "4", "5"});
        topPanel.add(ratingFilter);

        add(topPanel, BorderLayout.NORTH);

        // ===== Table =====
        String[] columns = {
            "Appointment ID", "Customer ID",
            "Doctor Feedback", "Doctor Rating",
            "Staff Feedback", "Staff Rating"
        };
        tableModel = new DefaultTableModel(columns, 0);
        feedbackTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(feedbackTable);
        add(scrollPane, BorderLayout.CENTER);

        // ===== Bottom Buttons =====
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        viewDetailBtn = new JButton("View Detail");
        resetBtn = new JButton("Reset");
        backBtn = new JButton("Back");
        bottomPanel.add(viewDetailBtn);
        bottomPanel.add(resetBtn);
        bottomPanel.add(backBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // ===== Load Data =====
        loadFeedback(FileStorage.getAllFeedback());

        // ===== Events =====
        searchBtn.addActionListener(e -> searchFeedback());
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            ratingFilter.setSelectedIndex(0);
            loadFeedback(FileStorage.getAllFeedback());
        });
        backBtn.addActionListener(e -> {
            new ManagerDashboard(manager).setVisible(true);
            dispose();
        });
        viewDetailBtn.addActionListener(e -> showDetail());

        setVisible(true);
    }

    private void loadFeedback(List<Feedback> feedbackList) {
        tableModel.setRowCount(0);
        for (Feedback f : feedbackList) {
            tableModel.addRow(new Object[]{
                f.getAppointmentId(),
                f.getCustomerId(),
                f.getDoctorFeedback(),
                f.getDoctorRating() == -1 ? "N/A" : f.getDoctorRating(),
                f.getStaffFeedback(),
                f.getStaffRating() == -1 ? "N/A" : f.getStaffRating()
            });
        }
    }

    private void searchFeedback() {
        String keyword = searchField.getText().trim().toLowerCase();
        String selectedRating = ratingFilter.getSelectedItem().toString();

        List<Feedback> allFeedback = FileStorage.getAllFeedback();
        List<Feedback> filtered = allFeedback.stream()
                .filter(f ->
                        keyword.isEmpty()
                                || f.getAppointmentId().toLowerCase().contains(keyword)
                                || f.getCustomerId().toLowerCase().contains(keyword)
                                || FileStorage.getAppointmentById(f.getAppointmentId())
                                   .getDoctorName().toLowerCase().contains(keyword))
                .filter(f -> {
                    if (selectedRating.equals("All")) return true;
                    int r = Integer.parseInt(selectedRating);
                    return f.getDoctorRating() == r || f.getStaffRating() == r;
                })
                .collect(Collectors.toList());

        loadFeedback(filtered);
    }

    private void showDetail() {
        int selectedRow = feedbackTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row first.");
            return;
        }

        String apptId = tableModel.getValueAt(selectedRow, 0).toString();
        String custId = tableModel.getValueAt(selectedRow, 1).toString();
        String doctorFeedback = tableModel.getValueAt(selectedRow, 2).toString();
        String doctorRating = tableModel.getValueAt(selectedRow, 3).toString();
        String staffFeedback = tableModel.getValueAt(selectedRow, 4).toString();
        String staffRating = tableModel.getValueAt(selectedRow, 5).toString();

        Appointment appt = FileStorage.getAppointmentById(apptId);
        String doctorName = appt != null ? appt.getDoctorName() : "N/A";
        String customerName = appt != null ? appt.getCustomerName() : "N/A";
        String date = appt != null ? appt.getDate() : "N/A";
        String time = appt != null ? appt.getTime() : "N/A";

        double charges = 0.0;
        if (FileStorage.getAppointmentDetailsById(apptId) != null) {
            charges = FileStorage.getAppointmentDetailsById(apptId).getCharges();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Appointment ID: ").append(apptId).append("\n");
        sb.append("Customer ID: ").append(custId).append("\n");
        sb.append("Customer Name: ").append(customerName).append("\n");
        sb.append("Doctor Name: ").append(doctorName).append("\n");
        sb.append("Date & Time: ").append(date).append(" ").append(time).append("\n");
        sb.append("Charges: RM").append(String.format("%.2f", charges)).append("\n\n");

        sb.append("Doctor Feedback: ").append(doctorFeedback).append("\n");
        sb.append("Doctor Rating: ").append(doctorRating).append("\n\n");
        sb.append("Staff Feedback: ").append(staffFeedback).append("\n");
        sb.append("Staff Rating: ").append(staffRating).append("\n");

        JOptionPane.showMessageDialog(this, sb.toString(),
                "Feedback Detail", JOptionPane.INFORMATION_MESSAGE);
    }
}
