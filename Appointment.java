package model;

public class Appointment {
    private String appointmentId;
    private String customerId;
    private String customerName;
    private String doctorId;
    private String doctorName;
    private String date;
    private String time;
    private String status;
    private String paymentStatus;

    public Appointment(String appointmentId, String customerId, String customerName,
                       String doctorId, String doctorName, String date, String time,
                       String status, String paymentStatus) {
        this.appointmentId = appointmentId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.date = date;
        this.time = time;
        this.status = status;
        this.paymentStatus = (paymentStatus == null || paymentStatus.isEmpty()) ? "Unpaid" : paymentStatus;
    }

    // --- Getters ---
    public String getAppointmentId() { return appointmentId; }
    public String getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getDoctorId() { return doctorId; }
    public String getDoctorName() { return doctorName; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getStatus() { return status; }
    public String getpaymentStatus() { return paymentStatus; }  // ✅ fixed case

    // --- Setters ---
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public void setDate(String date) { this.date = date; }
    public void setTime(String time) { this.time = time; }
    public void setStatus(String status) { this.status = status; }
    public void setpaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; } // ✅ fixed case

    // --- File format ---
    public String toFileString() {
        return String.join(",", appointmentId, customerId, customerName,
                doctorId, doctorName, date, time, status, paymentStatus);
    }

    public static Appointment fromFileFormat(String line) {
        String[] parts = line.split(",");
        if (parts.length < 9) { // ✅ expect at least 9 fields
            throw new IllegalArgumentException("Invalid appointment format: " + line);
        }
        return new Appointment(parts[0], parts[1], parts[2],
                parts[3], parts[4], parts[5], parts[6],
                parts[7], parts[8]);
    }

    // --- Debugging ---
    @Override
    public String toString() {
        return "Appointment{" +
                "appointmentId='" + appointmentId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", customerName='" + customerName + '\'' +
                ", doctorId='" + doctorId + '\'' +
                ", doctorName='" + doctorName + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", status='" + status + '\'' +
                ", paymentStatus='" + paymentStatus + '\'' +
                '}';
    }
}



