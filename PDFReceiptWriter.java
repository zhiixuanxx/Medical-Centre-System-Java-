package util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.awt.Color;

import model.Appointment;
import model.AppointmentDetails;
import model.Staff;

import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PDFReceiptWriter {

    public static void writeReceiptPdf(String filePath,
                                       Appointment appt,
                                       AppointmentDetails details,
                                       Staff staff,
                                       String receiptId,
                                       String paymentMethod,
                                       double paidAmount,
                                       double returnAmount) throws Exception {

        Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(doc, new FileOutputStream(filePath));
        doc.open();

        // Title
        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font subFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.GRAY);
        Font boldFont = new Font(Font.HELVETICA, 12, Font.BOLD);

        Paragraph title = new Paragraph("APU Medical Centre\n", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        Paragraph sub = new Paragraph("Lot 1, Jalan Teknologi, 57000 Kuala Lumpur • +60 3-1234 5678\n\n", subFont);
        sub.setAlignment(Element.ALIGN_CENTER);
        doc.add(sub);

        Paragraph header = new Paragraph("RECEIPT\n\n", boldFont);
        header.setAlignment(Element.ALIGN_CENTER);
        doc.add(header);

        // Info
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String now = LocalDateTime.now().format(dtf);

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(10);

        addRow(infoTable, "Receipt ID:", receiptId);
        addRow(infoTable, "Date/Time:", now);
        addRow(infoTable, "Staff:", staff.getName());
        addRow(infoTable, "Customer:", appt.getCustomerName());
        addRow(infoTable, "Doctor:", appt.getDoctorName());
        addRow(infoTable, "Appointment ID:", appt.getAppointmentId());
        addRow(infoTable, "Diagnosis:", details.getDiagnosis());

        doc.add(infoTable);

        // Charges table
        PdfPTable charges = new PdfPTable(2);
        charges.setWidthPercentage(100);
        charges.setSpacingBefore(20);

        double total = details.getCharges();
        double tax = total * 0.06;
        double grandTotal = total + tax;

        addRow(charges, "Charges:", String.format("RM %.2f", total));
        addRow(charges, "Tax (6%):", String.format("RM %.2f", tax));
        addRow(charges, "Total:", String.format("RM %.2f", grandTotal));
        addRow(charges, "Payment Method:", paymentMethod);

        if ("Cash".equalsIgnoreCase(paymentMethod)) {
            addRow(charges, "Paid:", String.format("RM %.2f", paidAmount));
            addRow(charges, "Change:", String.format("RM %.2f", returnAmount));
        }

        doc.add(charges);

        // Footer
        Paragraph footer = new Paragraph("\nThis is a system-generated receipt. Thank you for your payment.", subFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);

        doc.close();
    }

    private static void addRow(PdfPTable table, String label, String value) {
        Font labelFont = new Font(Font.HELVETICA, 11, Font.NORMAL, Color.DARK_GRAY);
        Font valFont = new Font(Font.HELVETICA, 11, Font.BOLD);

        PdfPCell c1 = new PdfPCell(new Phrase(label, labelFont));
        PdfPCell c2 = new PdfPCell(new Phrase(value, valFont));

        c1.setBorder(Rectangle.NO_BORDER);
        c2.setBorder(Rectangle.NO_BORDER);

        c1.setPadding(5);
        c2.setPadding(5);

        table.addCell(c1);
        table.addCell(c2);
    }
}
