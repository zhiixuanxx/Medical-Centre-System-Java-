package model;

import GUI.*;

public class Customer extends User {
    
    public Customer(String id, String name, String gender, String email, String phone, String password, String dob) {
        super(id, name, gender, email, phone, password, dob);
    }
    
    @Override
    public void displayDashboard() {
        new GUI.CustomerDashboard1(this).setVisible(true);
        System.out.println("Customer dashboard displayed.");
    }
    

}