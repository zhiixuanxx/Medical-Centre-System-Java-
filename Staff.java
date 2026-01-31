package model;

import view.StaffDashboard;

public class Staff extends User implements ProfileEditable {

    public Staff(String id, String name, String gender, String email, String phone, String password, String dob) {
        super(id, name, gender, email, phone, password, dob);
    }

    @Override
    public void displayDashboard() {
         new GUI.StaffDashboard(this).setVisible(true);
        System.out.println("Staff dashboard displayed.");

    }
  
    @Override
    public String[] getEditableFields() {
        // staff can edit only email + password
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
