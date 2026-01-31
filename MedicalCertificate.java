package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MedicalCertificate {
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private String appointmentId;
    private String customerId;
    private String issueDate;
    private String restFrom;
    private String restTo;
    private String doctorId;
    private String remarks;

    public MedicalCertificate(String appointmentId, String customerId, String issueDate,
                              String restFrom, String restTo, String doctorId, String remarks) {
        this.appointmentId = appointmentId;
        this.customerId = customerId;
        this.issueDate = issueDate;
        this.restFrom = restFrom;
        this.restTo = restTo;
        this.doctorId = doctorId;
        this.remarks = remarks;
    }
    
    public void setIssueDate(String issueDate) {
        LocalDate parsed = LocalDate.parse(issueDate, DATE_FORMAT);
        this.issueDate = parsed.format(DATE_FORMAT);
    }

    // Getters
    public String getAppointmentId() { return appointmentId; }
    public String getCustomerId() { return customerId; }
    public String getIssueDate() { return issueDate; }
    public String getRestFrom() { return restFrom; }
    public String getRestTo() { return restTo; }
    public String getDoctorId() { return doctorId; }
    public String getRemarks() { return remarks; }
}
