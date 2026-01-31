package view;

import model.Customer;
import model.Appointment;
import model.Feedback;
import util.FileStorage;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;

public class ProvideFeedbackFrame extends JFrame {
    private final Customer customer;
    private JTable appointmentTable;
    private DefaultTableModel tableModel;
    private JTextArea doctorCommentsArea;
    private JComboBox<Integer> doctorRatingBox;
    private JTextArea staffCommentsArea;
    private JComboBox<Integer> staffRatingBox;
    private JButton submitBtn, backBtn;
    private Appointment selectedAppointment;

    public ProvideFeedbackFrame(Customer customer) {
        this.customer = customer;
        setTitle("Provide Feedback");
        setSize(800, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initUI();
        loadCompletedAppointments();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Top panel with appointment table
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Select Completed Appointment"));
        
        // Create table
        String[] columnNames = {"Appointment ID", "Doctor", "Date", "Time", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        appointmentTable = new JTable(tableModel);
        appointmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appointmentTable.getTableHeader().setReorderingAllowed(false);
        
        // Add mouse click listener for row selection
        appointmentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectAppointment();
            }
        });
        
        // Add keyboard listener for row selection
        appointmentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectAppointment();
            }
        });
        
        JScrollPane tableScrollPane = new JScrollPane(appointmentTable);
        tableScrollPane.setPreferredSize(new Dimension(750, 200));
        topPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Instructions label
        JLabel instructionLabel = new JLabel("Click on an appointment to provide feedback");
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        instructionLabel.setFont(instructionLabel.getFont().deriveFont(Font.ITALIC));
        topPanel.add(instructionLabel, BorderLayout.SOUTH);

        // Bottom panel with feedback form
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Provide Feedback"));
        
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Selected appointment info
        JLabel selectedLabel = new JLabel("Selected Appointment:");
        JLabel selectedInfoLabel = new JLabel("None selected");
        selectedInfoLabel.setName("selectedInfo"); // For easy reference
        formPanel.add(selectedLabel);
        formPanel.add(selectedInfoLabel);

        // Doctor Feedback
        formPanel.add(new JLabel("Doctor Comments:"));
        doctorCommentsArea = new JTextArea(2, 20);
        doctorCommentsArea.setEnabled(false);
        formPanel.add(new JScrollPane(doctorCommentsArea));

        formPanel.add(new JLabel("Doctor Rating (1-5):"));
        doctorRatingBox = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        doctorRatingBox.setEnabled(false);
        formPanel.add(doctorRatingBox);

        // Staff Feedback
        formPanel.add(new JLabel("Staff Comments:"));
        staffCommentsArea = new JTextArea(2, 20);
        staffCommentsArea.setEnabled(false);
        formPanel.add(new JScrollPane(staffCommentsArea));

        formPanel.add(new JLabel("Staff Rating (1-5):"));
        staffRatingBox = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        staffRatingBox.setEnabled(false);
        formPanel.add(staffRatingBox);

        bottomPanel.add(formPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        submitBtn = new JButton("Submit Feedback");
        submitBtn.setEnabled(false);
        submitBtn.addActionListener(e -> submitFeedback());

        backBtn = new JButton("Back");
        backBtn.addActionListener(e -> {
            dispose();
            new CustomerDashboard(customer).setVisible(true);
        });

        buttonPanel.add(backBtn);
        buttonPanel.add(submitBtn);

        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add panels to frame
        add(topPanel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.CENTER);
    }

    private void loadCompletedAppointments() {
        List<Appointment> allAppointments = FileStorage.getAppointmentsByCustomerId(customer.getId());
        List<Appointment> completedAppointments = new ArrayList<>();
        
        for (Appointment apt : allAppointments) {
            if ("Completed".equalsIgnoreCase(apt.getStatus())) {
                Feedback existingFeedback = FileStorage.getFeedbackByAppointmentId(apt.getAppointmentId());
                if (existingFeedback == null) {
                    completedAppointments.add(apt);
                }
            }
        }

        tableModel.setRowCount(0);

        for (Appointment apt : completedAppointments) {
            Object[] rowData = {
                apt.getAppointmentId(),
                apt.getDoctorName(),
                apt.getDate(),
                apt.getTime(),
                apt.getStatus()
            };
            tableModel.addRow(rowData);
        }

        if (completedAppointments.isEmpty()) {
            JLabel noDataLabel = new JLabel("No completed appointments available for feedback");
            noDataLabel.setHorizontalAlignment(SwingConstants.CENTER);
            noDataLabel.setFont(noDataLabel.getFont().deriveFont(Font.ITALIC));
            
            Container parent = appointmentTable.getParent().getParent();
            parent.removeAll();
            parent.add(noDataLabel);
            parent.revalidate();
            parent.repaint();
        }
    }

    private void selectAppointment() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow >= 0) {
            String appointmentId = (String) tableModel.getValueAt(selectedRow, 0);
            selectedAppointment = FileStorage.getAppointmentById(appointmentId);
            
            if (selectedAppointment != null) {
                Component[] components = ((JPanel) ((JPanel) getContentPane().getComponent(1)).getComponent(0)).getComponents();
                for (Component comp : components) {
                    if (comp instanceof JLabel && "selectedInfo".equals(comp.getName())) {
                        ((JLabel) comp).setText(selectedAppointment.getAppointmentId() + " - " + 
                                              selectedAppointment.getDoctorName() + " (" + 
                                              selectedAppointment.getDate() + " " + 
                                              selectedAppointment.getTime() + ")");
                        break;
                    }
                }
                
                doctorCommentsArea.setEnabled(true);
                doctorRatingBox.setEnabled(true);
                staffCommentsArea.setEnabled(true);
                staffRatingBox.setEnabled(true);
                submitBtn.setEnabled(true);
                
                doctorCommentsArea.setText("");
                staffCommentsArea.setText("");
                doctorRatingBox.setSelectedIndex(0);
                staffRatingBox.setSelectedIndex(0);
            }
        }
    }

    private void submitFeedback() {
        if (selectedAppointment == null) {
            JOptionPane.showMessageDialog(this, "Please select an appointment.");
            return;
        }

        String doctorComments = doctorCommentsArea.getText().trim();
        int doctorRating = (int) doctorRatingBox.getSelectedItem();
        String staffComments = staffCommentsArea.getText().trim();
        int staffRating = (int) staffRatingBox.getSelectedItem();

        if (doctorComments.isEmpty() && staffComments.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter at least one comment.");
            return;
        }

        String customerFeedback = String.format("Doctor: %s (Rating: %d/5) | Staff: %s (Rating: %d/5)", 
                                               doctorComments.isEmpty() ? "No comments" : doctorComments, 
                                               doctorRating,
                                               staffComments.isEmpty() ? "No comments" : staffComments, 
                                               staffRating);

        Feedback feedback = new Feedback(
                selectedAppointment.getAppointmentId(),
                customer.getId(),
                customerFeedback,
                "" 
        );

        FileStorage.saveFeedback(feedback);
        JOptionPane.showMessageDialog(this, "Thank you! Your feedback has been recorded.");
        
        loadCompletedAppointments();
        
        selectedAppointment = null;
        doctorCommentsArea.setEnabled(false);
        doctorRatingBox.setEnabled(false);
        staffCommentsArea.setEnabled(false);
        staffRatingBox.setEnabled(false);
        submitBtn.setEnabled(false);
        doctorCommentsArea.setText("");
        staffCommentsArea.setText("");
        
        Component[] components = ((JPanel) ((JPanel) getContentPane().getComponent(1)).getComponent(0)).getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel && "selectedInfo".equals(comp.getName())) {
                ((JLabel) comp).setText("None selected");
                break;
            }
        }
    }
}
