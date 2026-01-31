package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public abstract class User implements ProfileEditable {
    protected String id;
    protected String name;
    protected String gender;
    protected String email;
    protected String phone;
    protected String password;
    protected String dob;
    
    // Define which fields are editable for all users
    protected static final String[] COMMON_EDITABLE_FIELDS = {"email", "password"};
    protected static final String[] COMMON_NON_EDITABLE_FIELDS = {"id", "name", "gender", "phone", "dob"};
    
    protected static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    public User(String id, String name, String gender, String email, String phone, String password, String dob) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.dob = dob;
    }
    
    // Default implementation - can be overridden by subclasses
    @Override
    public String[] getEditableFields() {
        return COMMON_EDITABLE_FIELDS.clone();
    }
    
    @Override
    public String[] getNonEditableFields() {
        return COMMON_NON_EDITABLE_FIELDS.clone();
    }
    
    @Override
    public boolean isFieldEditable(String fieldName) {
        for (String field : getEditableFields()) {
            if (field.equalsIgnoreCase(fieldName)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void updateEditableField(String fieldName, String value) {
        switch (fieldName.toLowerCase()) {
            case "email":
                this.email = value;
                break;
            case "password":
                this.password = value;
                break;
            default:
                throw new IllegalArgumentException("Field '" + fieldName + "' is not editable");
        }
    }
    
    @Override
    public String getFieldValue(String fieldName) {
        switch (fieldName.toLowerCase()) {
            case "id": return id;
            case "name": return name;
            case "gender": return gender;
            case "email": return email;
            case "phone": return phone;
            case "password": return password;
            case "dob": return dob;
            default: throw new IllegalArgumentException("Unknown field: " + fieldName);
        }
    }
    
    // Abstract method that subclasses must implement
    public abstract void displayDashboard();
    
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getDob() { return dob; }
    public void setDob(String dob) {
        try {
            // Validate and normalize
            LocalDate parsed = LocalDate.parse(dob, DATE_FORMAT);
            this.dob = parsed.format(DATE_FORMAT); // always stored as dd/MM/yyyy
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format for DOB. Use dd/MM/yyyy. Provided: " + dob);
        }
    }

    @Override
    public String toString() {
        return id + "," + name + "," + gender + "," + email + "," + phone + "," + password + "," + dob;
    }
}