package view;

import model.Doctor;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class DoctorDashboard extends JFrame {
    private Doctor doctor;
    private JLabel welcomeLabel; // instance variable for refreshing

    public DoctorDashboard(Doctor doctor) {
        this.doctor = doctor;
        initializeUI();
        setupEventHandlers();
    }

    private void initializeUI() {
        setTitle("Doctor Dashboard - " + doctor.getName());
        setSize(450, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // ===== Main Panel =====
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(248, 249, 250));

        // ===== Header =====
        welcomeLabel = new JLabel("Welcome, Dr. " + doctor.getName(), SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setForeground(new Color(25, 25, 112));

        JLabel specialtyLabel = new JLabel("Specialty: " + doctor.getSpecialty(), SwingConstants.CENTER);
        specialtyLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        specialtyLabel.setForeground(Color.GRAY);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(welcomeLabel, BorderLayout.CENTER);
        headerPanel.add(specialtyLabel, BorderLayout.SOUTH);

        panel.add(headerPanel, BorderLayout.NORTH);

        // ===== Buttons =====
        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 15, 15));
        buttonPanel.setOpaque(false);

        JButton viewAppointmentsBtn = createStyledButton("View Appointments", new Color(52, 152, 219));
        JButton enterChargesBtn     = createStyledButton("Enter Charges & Diagnosis", new Color(46, 204, 113));
        JButton viewHistoryBtn      = createStyledButton("View Patient History", new Color(155, 89, 182));
        JButton editProfileBtn      = createStyledButton("Edit Profile", new Color(230, 126, 34));
        JButton logoutBtn           = createStyledButton("Logout", new Color(231, 76, 60));

        buttonPanel.add(viewAppointmentsBtn);
        buttonPanel.add(enterChargesBtn);
        buttonPanel.add(viewHistoryBtn);
        buttonPanel.add(editProfileBtn);
        buttonPanel.add(logoutBtn);

        panel.add(buttonPanel, BorderLayout.CENTER);

        // ===== Footer =====
        JLabel footerLabel = new JLabel("Doctor ID: " + doctor.getId(), SwingConstants.CENTER);
        footerLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        footerLabel.setForeground(Color.GRAY);
        panel.add(footerLabel, BorderLayout.SOUTH);

        add(panel);
        setVisible(true);
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(300, 45));

        Color originalColor = backgroundColor;
        Color hoverColor = backgroundColor.darker();

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent evt) { button.setBackground(hoverColor); }
            @Override public void mouseExited (java.awt.event.MouseEvent evt) { button.setBackground(originalColor); }
        });

        return button;
    }

    private void setupEventHandlers() {
        JPanel root = (JPanel) getContentPane().getComponent(0);
        JPanel buttonPanel = (JPanel) root.getComponent(1);

        JButton viewAppointmentsBtn = (JButton) buttonPanel.getComponent(0);
        JButton enterChargesBtn     = (JButton) buttonPanel.getComponent(1);
        JButton viewHistoryBtn      = (JButton) buttonPanel.getComponent(2);
        JButton editProfileBtn      = (JButton) buttonPanel.getComponent(3);
        JButton logoutBtn           = (JButton) buttonPanel.getComponent(4);

        // View Appointments (placeholder)
        viewAppointmentsBtn.addActionListener(e ->{
            DoctorViewAppointments dialog = new DoctorViewAppointments(doctor, this);
            dialog.setVisible(true);
        });

        // Enter Charges & Diagnosis
        enterChargesBtn.addActionListener((ActionEvent e) -> {
            EnterChargesDiagnosis dialog = new EnterChargesDiagnosis(doctor, this);
            dialog.setVisible(true);
        });

        // View Patient History — hook up your dialog here
        viewHistoryBtn.addActionListener((ActionEvent e) -> {
            ViewPatientHistory dialog = new ViewPatientHistory(doctor, this);
            dialog.setVisible(true);
        });

        // Edit Profile
        editProfileBtn.addActionListener((ActionEvent e) -> openEditProfileDialog());

        // Logout
        logoutBtn.addActionListener((ActionEvent e) -> handleLogout());
    }

    private void openEditProfileDialog() {
        try {
            GenericEditProfileFrame editFrame = new GenericEditProfileFrame(doctor, this);
            editFrame.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Error opening profile editor: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    // Refresh header after profile updates
    public void refreshWelcomeLabel() {
        SwingUtilities.invokeLater(() -> {
            welcomeLabel.setText("Welcome, Dr. " + doctor.getName());
            setTitle("Doctor Dashboard - " + doctor.getName());

            JPanel headerPanel = (JPanel) ((JPanel) getContentPane().getComponent(0)).getComponent(0);
            JLabel specialtyLabel = (JLabel) headerPanel.getComponent(1);
            specialtyLabel.setText("Specialty: " + doctor.getSpecialty());

            repaint();
        });
    }

    private void handleLogout() {
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
                "Logout successful. Thank you, Dr. " + doctor.getName() + "!",
                "Logout",
                JOptionPane.INFORMATION_MESSAGE
            );
            new Login().setVisible(true); 
        }
    }

    public Doctor getDoctor() { return doctor; }

    public void updateDoctor(Doctor updatedDoctor) {
        this.doctor = updatedDoctor;
        refreshWelcomeLabel();
    }
}
