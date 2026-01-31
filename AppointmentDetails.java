package model;

public class AppointmentDetails {
    private final String appointmentId;
    private final double charges;
    private final String diagnosis;
    private final String notes;
    private final boolean followUpNeeded;

    // New constructor with follow-up flag
    public AppointmentDetails(String appointmentId, double charges, String diagnosis, String notes, boolean followUpNeeded) {
        this.appointmentId = appointmentId;
        this.charges = charges;
        this.diagnosis = diagnosis == null ? "" : diagnosis;
        this.notes = notes == null ? "" : notes;
        this.followUpNeeded = followUpNeeded;
    }

    // Old constructor kept for backward compatibility
    public AppointmentDetails(String appointmentId, double charges, String diagnosis, String notes) {
        this(appointmentId, charges, diagnosis, notes, false);
    }

    // Old 3-arg constructor also supported
    public AppointmentDetails(String appointmentId, double charges, String diagnosis) {
        this(appointmentId, charges, diagnosis, "", false);
    }

    // Getters
    public String getAppointmentId() { return appointmentId; }
    public double getCharges() { return charges; }
    public String getDiagnosis() { return diagnosis; }
    public String getNotes() { return notes; }
    public boolean isFollowUpNeeded() { return followUpNeeded; }
    
    
}
