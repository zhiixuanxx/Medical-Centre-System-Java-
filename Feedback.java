package model;

public class Feedback {
    private String appointmentId;
    private String customerId;
    private String doctorFeedback;
    private int doctorRating;
    private String staffFeedback;
    private int staffRating;

    // Full constructor
    public Feedback(String appointmentId, String customerId,
                    String doctorFeedback, int doctorRating,
                    String staffFeedback, int staffRating) {
        this.appointmentId = appointmentId;
        this.customerId = customerId;
        this.doctorFeedback = doctorFeedback;
        this.doctorRating = doctorRating;
        this.staffFeedback = staffFeedback;
        this.staffRating = staffRating;
    }

    // Simple constructor (if you still want to keep compatibility)
    public Feedback(String appointmentId, String customerId,
                    String doctorFeedback, String staffFeedback) {
        this(appointmentId, customerId, doctorFeedback, -1, staffFeedback, -1);
    }

    // ===== Getters =====
    public String getAppointmentId() { return appointmentId; }
    public String getCustomerId() { return customerId; }
    public String getDoctorFeedback() { return doctorFeedback; }
    public int getDoctorRating() { return doctorRating; }
    public String getStaffFeedback() { return staffFeedback; }
    public int getStaffRating() { return staffRating; }

    // ===== Setters =====
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public void setDoctorFeedback(String doctorFeedback) { this.doctorFeedback = doctorFeedback; }
    public void setDoctorRating(int doctorRating) { this.doctorRating = doctorRating; }
    public void setStaffFeedback(String staffFeedback) { this.staffFeedback = staffFeedback; }
    public void setStaffRating(int staffRating) { this.staffRating = staffRating; }

}
