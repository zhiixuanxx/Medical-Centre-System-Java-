package view;

import model.Customer;

import javax.swing.*;
import java.awt.*;

public class CustomerDashboard extends JFrame {
    private Customer customer;
    private JLabel welcomeLabel;

    public CustomerDashboard(Customer customer) {
        this.customer = customer;

        setTitle("Customer Dashboard");
        setSize(400, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ===== Header =====
        JLabel appTitle = new JLabel("APU Medical Centre", SwingConstants.CENTER);
        appTitle.setFont(new Font("Serif", Font.BOLD, 20));
        appTitle.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));

        welcomeLabel = new JLabel("Welcome, " + customer.getName(), SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.add(appTitle);
        headerPanel.add(welcomeLabel);

        // ===== Emergency note (if walk-in) =====
        JLabel emergencyNote = null;
        if (isWalkInCustomer(customer)) {
            emergencyNote = new JLabel("⚠ Please update your information at the APU counter.");
            emergencyNote.setFont(new Font("SansSerif", Font.ITALIC, 12));
            emergencyNote.setForeground(Color.RED);
            emergencyNote.setHorizontalAlignment(SwingConstants.CENTER);
        }

        // ===== Buttons =====
        JButton makeAppointmentBtn = new JButton("Make Appointment");
        JButton manageAppointmentBtn = new JButton("Appointment Management");
        JButton viewHistoryBtn = new JButton("View Appointment History");
        JButton editProfileBtn = new JButton("Edit Profile");
        JButton provideFeedbackBtn = new JButton("Provide Feedback");
        JButton logoutBtn = new JButton("Logout");

        // Action listeners
        makeAppointmentBtn.addActionListener(e -> {
            new AppointmentForm(customer).setVisible(true);
            dispose();
        });

        manageAppointmentBtn.addActionListener(e -> {
            new AppointmentManagement(customer).setVisible(true);
            dispose();
        });

        viewHistoryBtn.addActionListener(e -> {
            new History(customer).setVisible(true);
            dispose();
        });

        editProfileBtn.addActionListener(e -> {
            new GenericEditProfileFrame(customer, this).setVisible(true);
        });

        provideFeedbackBtn.addActionListener(e -> {
            new ProvideFeedbackFrame(customer).setVisible(true);
        });

        logoutBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to logout?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                dispose();
                new Login().setVisible(true);
            }
        });

        // ===== Layout =====
        JPanel buttonPanel = new JPanel(new GridLayout(6, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));
        buttonPanel.add(makeAppointmentBtn);
        buttonPanel.add(manageAppointmentBtn);
        buttonPanel.add(viewHistoryBtn);
        buttonPanel.add(editProfileBtn);
        buttonPanel.add(provideFeedbackBtn);
        buttonPanel.add(logoutBtn);

        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        if (emergencyNote != null) add(emergencyNote, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private boolean isWalkInCustomer(Customer c) {
        return (c.getEmail() != null && c.getEmail().toLowerCase().contains("walkin"))
            || (c.getPhone() != null && c.getPhone().equals("0000000000"))
            || (c.getDob() != null && c.getDob().equals("01/01/1970"));
    }

    public void refreshWelcomeLabel() {
        welcomeLabel.setText("Welcome, " + customer.getName());
        setTitle("Customer Dashboard - " + customer.getName());
    }
}
