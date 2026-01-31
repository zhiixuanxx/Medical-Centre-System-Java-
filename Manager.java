package model;

import GUI.*;

public class Manager extends User implements ProfileEditable {

    public Manager(String id, String name, String gender, String email, String phone, String password, String dob) {
        super(id, name, gender, email, phone, password, dob);
    }

    public String getRole() {
        return "manager";
    }

    @Override
    public void displayDashboard() {
        new GUI.ManagerDashboard(this).setVisible(true);
        System.out.println("Manager dashboard displayed.");
    }

    //ProfileEditable Implementation
    @Override
    public String[] getEditableFields() {
        // manager can edit only email + password
        return new String[]{"email", "password"};
    }

    @Override
    public String[] getNonEditableFields() {
        return new String[]{"id", "name", "gender", "phone", "dob"};
    }

    @Override
    public boolean isFieldEditable(String fieldName) {
        for (String f : getEditableFields()) {
            if (f.equalsIgnoreCase(fieldName)) return true;
        }
        return false;
    }

    @Override
    public void updateEditableField(String fieldName, String value) {
        switch (fieldName.toLowerCase()) {
            case "email": setEmail(value); break;
            case "password": setPassword(value); break;
        }
    }

    @Override
    public String getFieldValue(String fieldName) {
        switch (fieldName.toLowerCase()) {
            case "id": return getId();
            case "name": return getName();
            case "gender": return getGender();
            case "email": return getEmail();
            case "phone": return getPhone();
            case "password": return getPassword();
            case "dob": return getDob();
            default: throw new IllegalArgumentException("Unknown field: " + fieldName);
        }
    }
}
