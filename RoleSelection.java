package view;

import javax.swing.*;
import java.awt.*;

public class RoleSelection extends JFrame {
    public RoleSelection() {
        setTitle("Select Role to Register");
        setSize(300, 150);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        JLabel label = new JLabel("Register As:", JLabel.CENTER);

        JButton managerBtn = new JButton("Manager");
        JButton customerBtn = new JButton("Customer");

        managerBtn.addActionListener(e -> {
            dispose();
            new RegisterManager();
        });

        customerBtn.addActionListener(e -> {
            dispose();
            new RegisterCustomer();
        });

        panel.add(label);
        panel.add(managerBtn);
        panel.add(customerBtn);

        add(panel);
        setVisible(true);
    }
}
