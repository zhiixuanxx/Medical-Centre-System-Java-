package model;

import GUI.*;

public class Doctor extends User {
    private String specialty;
    private String doctorType;// "Clinical Doctor" or "On-call Doctor"

    private static final String[] DOCTOR_EDITABLE_FIELDS = {"email", "password", "specialty", "doctor type"};
    
    public Doctor(String id, String name, String gender, String email, String phone, String password,
                  String specialty, String doctorType, String dob) {
        super(id, name, gender, email, phone, password, dob);
        this.specialty = specialty;
        this.doctorType = doctorType;
    }
    
    @Override
    public String[] getEditableFields() {
        return DOCTOR_EDITABLE_FIELDS.clone();
    }
    
    @Override
    public void updateEditableField(String fieldName, String value) {
        if ("specialty".equalsIgnoreCase(fieldName)) {
            this.specialty = value;
        } else if ("doctor type".equalsIgnoreCase(fieldName)) {
            this.doctorType = value;
        } else {
            super.updateEditableField(fieldName, value);
        }
    }
    
    @Override
    public String getFieldValue(String fieldName) {
        if ("specialty".equalsIgnoreCase(fieldName)) {
            return specialty;
        } else if ("doctor type".equalsIgnoreCase(fieldName)) {
            return doctorType;
        }
        return super.getFieldValue(fieldName);
    }
    
    public String getSpecialty() {
        return specialty;
    }
    
    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }
    
    public String getDoctorType() {
        return doctorType;
    }

    public void setDoctorType(String doctorType) {
        this.doctorType = doctorType;
    }

    @Override
    public void displayDashboard() {
        new GUI.DoctorDashboard1(this).setVisible(true);
        System.out.println("Doctor dashboard displayed.");
    }
    
    @Override
    public String toString() {
        return getName();
    }
    
    public boolean isOnCall() {
        return "On-call Doctor".equalsIgnoreCase(this.doctorType);
    }
}
