package model;

public class Payment {
    private final String receiptId;
    private final String appointmentId;
    private final String customerId;
    private final String customerName;
    private final String doctorId;
    private final String doctorName;
    private final String date;       // dd/MM/yyyy
    private final String time;       // HH:mm
    private final String diagnosis;
    private final double charges;
    private final double tax;
    private final double total;
    private final String paymentMethod;
    private final double paidAmount;
    private final double changeAmount;
    private final String staffId;
    private final String staffName;
    private final String timestamp;

    public Payment(String receiptId, String appointmentId, String customerId, String customerName,
                   String doctorId, String doctorName, String date, String time,
                   String diagnosis, double charges, double tax, double total,
                   String paymentMethod, double paidAmount, double changeAmount,
                   String staffId, String staffName, String timestamp) {
        this.receiptId = receiptId;
        this.appointmentId = appointmentId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.date = date;
        this.time = time;
        this.diagnosis = diagnosis;
        this.charges = charges;
        this.tax = tax;
        this.total = total;
        this.paymentMethod = paymentMethod;
        this.paidAmount = paidAmount;
        this.changeAmount = changeAmount;
        this.staffId = staffId;
        this.staffName = staffName;
        this.timestamp = timestamp;
    }

    // ✅ Add getters for all fields
    public String getReceiptId() { return receiptId; }
    public String getAppointmentId() { return appointmentId; }
    public String getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getDoctorId() { return doctorId; }
    public String getDoctorName() { return doctorName; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getDiagnosis() { return diagnosis; }
    public double getCharges() { return charges; }
    public double getTax() { return tax; }
    public double getTotal() { return total; }
    public String getPaymentMethod() { return paymentMethod; }
    public double getPaidAmount() { return paidAmount; }
    public double getChangeAmount() { return changeAmount; }
    public String getStaffId() { return staffId; }
    public String getStaffName() { return staffName; }
    public String getTimestamp() { return timestamp; }
}
