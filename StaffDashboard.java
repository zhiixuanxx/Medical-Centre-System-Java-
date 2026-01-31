package view;

import model.Staff;
import javax.swing.*;
import java.awt.*;

public class StaffDashboard extends JFrame {
    private Staff staff;
    private JLabel welcomeLabel;
    private JButton editProfileBtn;

    public StaffDashboard(Staff staff) {
        this.staff = staff;

        setTitle("Staff Dashboard");
        setSize(500, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ===== Header =====
        JLabel appTitle = new JLabel("APU Medical Centre", SwingConstants.CENTER);
        appTitle.setFont(new Font("Serif", Font.BOLD, 22));
        appTitle.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));

        welcomeLabel = new JLabel("Welcome, " + staff.getName(), SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.add(appTitle);
        headerPanel.add(welcomeLabel);

        // ===== Buttons =====
        JButton crudCustomerBtn = new JButton("Manage Customers");
        JButton bookingBtn = new JButton("Assist Customer Booking");
        JButton emergencyBtn = new JButton("Emergency Appointment");
        JButton assignApptBtn = new JButton("Assign Appointment to Doctor");
        JButton paymentBtn = new JButton("Collect Payment & Generate Receipt");
        JButton editProfileBtn = new JButton("Edit Profile");
        JButton logoutBtn = new JButton("Logout");

        // ===== Action Listeners =====
        crudCustomerBtn.addActionListener(e -> {
            dispose();
            new CrudCustomerPage(staff);
        });

        bookingBtn.addActionListener(e -> {
            dispose();
            new AppointmentFormForStaff(staff);
        });

        emergencyBtn.addActionListener(e -> {
            dispose();
            new EmergencyAppointmentForm(staff);
        });

        assignApptBtn.addActionListener(e -> {
            dispose();
            new AssignAppointmentPage(staff);
        });

        paymentBtn.addActionListener(e -> {
            dispose();
            new CollectPaymentPage(staff).setVisible(true);
        });

        editProfileBtn.addActionListener(e -> {
            dispose();
            new GenericEditProfileFrame(staff, this).setVisible(true);
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
                JOptionPane.showMessageDialog(
                        null,
                        "Logout successful. Thank you, " + staff.getName() + "!",
                        "Logout",
                        JOptionPane.INFORMATION_MESSAGE
                );
                new Login().setVisible(true);
            }
        });

        // ===== Layout =====
        JPanel buttonPanel = new JPanel(new GridLayout(6, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));
        buttonPanel.add(crudCustomerBtn);
        buttonPanel.add(bookingBtn);
        buttonPanel.add(emergencyBtn);
        buttonPanel.add(assignApptBtn);
        buttonPanel.add(paymentBtn);
        buttonPanel.add(editProfileBtn);
        buttonPanel.add(logoutBtn);

        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);

        setVisible(true);
    }
}
