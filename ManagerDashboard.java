package view;

import model.Manager;

import javax.swing.*;
import java.awt.*;

public class ManagerDashboard extends JFrame {
    private Manager manager;
    private JLabel welcomeLabel;
    private JButton editProfileBtn;

    public ManagerDashboard(Manager manager) {
        this.manager = manager;

        setTitle("Manager Dashboard");
        setSize(500, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ===== Header =====
        JLabel appTitle = new JLabel("APU Medical Centre", SwingConstants.CENTER);
        appTitle.setFont(new Font("Serif", Font.BOLD, 22));
        appTitle.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));

        welcomeLabel = new JLabel("Welcome, " + manager.getName(), SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.add(appTitle);
        headerPanel.add(welcomeLabel);

        // ===== Buttons =====
        JButton crudManagerBtn = new JButton("Manage Managers");
        JButton crudStaffBtn = new JButton("Manage Staff");
        JButton crudDoctorBtn = new JButton("Manage Doctors");
        JButton appointmentBtn = new JButton("View All Appointments");
        JButton feedbackBtn = new JButton("View Feedback & Comments");
        JButton doctorScheduleBtn = new JButton("Doctor Schedule Management");
        JButton reportBtn = new JButton("Generate Analysed Reports");
        JButton editProfileBtn = new JButton("Edit Profile");
        JButton logoutBtn = new JButton("Logout");

        // ===== Action listeners =====
        crudManagerBtn.addActionListener(e -> {
            dispose();
            new CrudManagerPage(manager);
        });

        crudStaffBtn.addActionListener(e -> {
            dispose();
            new CrudStaffPage(manager);
        });

        crudDoctorBtn.addActionListener(e -> {
            dispose();
            new CrudDoctorPage(manager);
        });

        appointmentBtn.addActionListener(e -> {
            dispose();
            new ViewAllAppointments(manager);
        });

        feedbackBtn.addActionListener(e -> {
            dispose();
            new ViewAllFeedbackAndCommentsPage(manager).setVisible(true);
        });

        doctorScheduleBtn.addActionListener(e -> {
            dispose();
            new DoctorScheduleManagement(manager).setVisible(true);
        });

        reportBtn.addActionListener(e -> {
            dispose();
            new AnalyzedReportPage(manager).setVisible(true);
        });

        editProfileBtn.addActionListener(e -> {
            dispose();
            new GenericEditProfileFrame(manager, this).setVisible(true);
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
                        "Logout successful. Thank you, " + manager.getName() + "!",
                        "Logout",
                        JOptionPane.INFORMATION_MESSAGE
                );
                new Login().setVisible(true);
            }
        });

        // ===== Layout =====
        JPanel buttonPanel = new JPanel(new GridLayout(9, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));
        buttonPanel.add(crudManagerBtn);
        buttonPanel.add(crudStaffBtn);
        buttonPanel.add(crudDoctorBtn);
        buttonPanel.add(appointmentBtn);
        buttonPanel.add(feedbackBtn);
        buttonPanel.add(doctorScheduleBtn);
        buttonPanel.add(reportBtn);
        buttonPanel.add(editProfileBtn);
        buttonPanel.add(logoutBtn);

        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    public void refreshWelcomeLabel() {
        welcomeLabel.setText("Welcome, " + manager.getName());
        setTitle("Manager Dashboard - " + manager.getName());
    }
}
