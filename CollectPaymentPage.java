package view;

import model.Appointment;
import model.AppointmentDetails;
import model.Customer;
import model.Staff;
import util.FileStorage;
import util.ReceiptGenerator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class CollectPaymentPage extends JFrame {

    private final Staff loggedInStaff;

    private JTextField searchField;
    private JButton searchButton;
    private JTable apptTable;
    private DefaultTableModel apptModel;

    private JComboBox<String> paymentMethodCombo;
    private JTextField amountPaidField;
    private JLabel statusLabel;

    private JButton processBtn;
    private JButton backBtn;

    private Appointment selectedAppt;
    private AppointmentDetails selectedDetails;

    public CollectPaymentPage(Staff staff) {
        this.loggedInStaff = staff;

        setTitle("Collect Payment & Generate Receipt");
        setSize(950, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10,10));

        initComponents();
        setupLayout();
        setupEvents();
        loadCompletedAppointments();

        setVisible(true);
    }

    private void initComponents() {
        searchField = new JTextField(20);
        searchButton = new JButton("Search");

        apptModel = new DefaultTableModel(
                new Object[]{"Appt ID", "Customer", "Doctor", "Date", "Diagnosis", "Charges"}, 0
        );
        apptTable = new JTable(apptModel);
        apptTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        paymentMethodCombo = new JComboBox<>(new String[]{
                "-- Select Payment Method --", "Cash", "Debit/Credit Card", "TNG eWallet"
        });
        amountPaidField = new JTextField();

        processBtn = new JButton("Process Payment & Generate Receipt");
        processBtn.setEnabled(false);

        backBtn = new JButton("Back");

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void setupLayout() {
        // Left side
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        JScrollPane apptScroll = new JScrollPane(apptTable);
        apptScroll.setBorder(BorderFactory.createTitledBorder("Completed Appointments (Unpaid)"));

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(searchPanel, BorderLayout.NORTH);
        leftPanel.add(apptScroll, BorderLayout.CENTER);

        // Right side (Payment details)
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Payment Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;
        gbc.gridx=0; gbc.gridy=row; rightPanel.add(new JLabel("Payment Method:"), gbc);
        gbc.gridx=1; rightPanel.add(paymentMethodCombo, gbc);

        row++;
        gbc.gridx=0; gbc.gridy=row; rightPanel.add(new JLabel("Amount Paid:"), gbc);
        gbc.gridx=1; rightPanel.add(amountPaidField, gbc);

        row++;
        gbc.gridx=0; gbc.gridy=row; gbc.gridwidth=2;
        rightPanel.add(statusLabel, gbc);

        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.6);

        // Bottom buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(backBtn);
        btnPanel.add(processBtn);

        add(splitPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void setupEvents() {
        searchButton.addActionListener(e -> searchAppointments());

        apptTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && apptTable.getSelectedRow() != -1) {
                int row = apptTable.getSelectedRow();
                String apptId = apptModel.getValueAt(row, 0).toString();

                selectedAppt = FileStorage.readAppointments().stream()
                        .filter(a -> a.getAppointmentId().equals(apptId))
                        .findFirst().orElse(null);

                selectedDetails = FileStorage.readAppointmentDetails().stream()
                        .filter(d -> d.getAppointmentId().equals(apptId))
                        .findFirst().orElse(null);

                if (selectedAppt != null && selectedDetails != null) {
                    statusLabel.setText("Selected appointment " + apptId + " for " + selectedAppt.getCustomerName());
                    processBtn.setEnabled(true);
                }
            }
        });
        
        paymentMethodCombo.addActionListener(e -> {
        if (selectedDetails == null) return;
        double charges = selectedDetails.getCharges();
        double tax = charges * 0.06;
        double totalWithTax = charges + tax;

        String method = (String) paymentMethodCombo.getSelectedItem();
        if ("Cash".equalsIgnoreCase(method)) {
            amountPaidField.setText("");
            amountPaidField.setEditable(true);
        } else if (!method.startsWith("--")) {
            amountPaidField.setText(String.format("%.2f", totalWithTax));
            amountPaidField.setEditable(false);
        }
    });

        processBtn.addActionListener(e -> handleProcessPayment());

        backBtn.addActionListener(e -> {
            dispose();
            new StaffDashboard(loggedInStaff);
        });
    }

    private void loadCompletedAppointments() {
        apptModel.setRowCount(0);
        List<Appointment> all = FileStorage.readAppointments();
        List<AppointmentDetails> details = FileStorage.readAppointmentDetails();

        for (Appointment a : all) {
            if ("Completed".equalsIgnoreCase(a.getStatus())) {
                AppointmentDetails det = details.stream()
                        .filter(d -> d.getAppointmentId().equals(a.getAppointmentId()))
                        .findFirst().orElse(null);

                if (det != null) {
                    apptModel.addRow(new Object[]{
                            a.getAppointmentId(),
                            a.getCustomerName(),
                            a.getDoctorName(),
                            a.getDate(),
                            det.getDiagnosis(),
                            String.format("RM %.2f", det.getCharges())
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
            if (!(apptId.contains(keyword) || cust.contains(keyword))) {
                apptModel.removeRow(i);
            }
        }
    }

    private void handleProcessPayment() {
        if (selectedAppt == null || selectedDetails == null) {
            statusLabel.setText("Select an appointment first.");
            return;
        }

        String method = (String) paymentMethodCombo.getSelectedItem();
        if (method == null || method.startsWith("--")) {
            statusLabel.setText("Please select a payment method.");
            return;
        }

        double charges = selectedDetails.getCharges();
        double tax = charges * 0.06;
        double totalWithTax = charges + tax;

        double paidAmount;
        double change = 0.0;

        if ("Cash".equalsIgnoreCase(method)) {
            try {
                paidAmount = Double.parseDouble(amountPaidField.getText().trim());
            } catch (NumberFormatException e) {
                statusLabel.setText("Invalid paid amount.");
                return;
            }

            if (paidAmount < totalWithTax) {
                statusLabel.setText("Amount paid is less than total (RM " 
                                    + String.format("%.2f", totalWithTax) + ")!");
                return;
            }
            change = paidAmount - totalWithTax;

        } else {
            // Non-cash → auto full payment
            paidAmount = totalWithTax;
            change = 0.0;
        }

        String receiptId = "R" + System.currentTimeMillis();
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Save payment record
        FileStorage.writePayment(
                receiptId,
                selectedAppt,
                selectedDetails,
                loggedInStaff,
                method,
                paidAmount,
                change,
                timestamp
        );

        // Update appointment status
        FileStorage.updateAppointmentStatus(selectedAppt.getAppointmentId(), "Paid");

        // Generate receipt dialog
        ReceiptGenerator receiptDialog = new ReceiptGenerator(
                this,
                selectedAppt,
                selectedDetails,
                loggedInStaff,
                receiptId,
                method,
                paidAmount,
                change
        );
        receiptDialog.setVisible(true);

        JOptionPane.showMessageDialog(this,
                "Payment successful. Receipt generated.\nChange: RM " + String.format("%.2f", change),
                "Success", JOptionPane.INFORMATION_MESSAGE);

        // Reset fields
        paymentMethodCombo.setSelectedIndex(0);
        amountPaidField.setText("");
        amountPaidField.setEditable(true);
        statusLabel.setText(" ");
        processBtn.setEnabled(false);
        apptTable.clearSelection();

        loadCompletedAppointments();
    }
}
