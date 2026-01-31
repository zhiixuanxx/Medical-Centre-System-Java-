package util;

import model.*;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class FileStorage {

    private static final Map<LocalDate, Integer> lastAssignedIndex = new HashMap<>();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private static final String APPOINTMENTS_FILE = "appointments.txt";
    private static final String MC_FILE = "mc.txt";
    private static final String PAYMENTS_FILE = "payments.txt";

    /* ===================== Users ===================== */

    public static List<User> getAllUsers() {
        List<User> allUsers = new ArrayList<>();
        allUsers.addAll(getAllCustomers());
        allUsers.addAll(getAllDoctors());
        allUsers.addAll(getAllManagers());
        allUsers.addAll(getAllStaffs());
        return allUsers;
    }

    public static List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("customer.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 7) {
                    String id = parts[0].trim();
                    String name = parts[1].trim();
                    String gender = parts[2].trim();
                    String email = parts[3].trim();
                    String phone = parts[4].trim();
                    String password = parts[5].trim();
                    String dob = parts[6].trim();
                    customers.add(new Customer(id, name, gender, email, phone, password, dob));
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return customers;
    }

    public static List<Doctor> getAllDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        for (String line : readLines("doctor.txt")) {
            String[] p = line.split(",");
            // id, name, gender, email, phone, password, doctortype, specialty, dob
            if (p.length >= 9) {
                doctors.add(new Doctor(
                        p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(),
                        p[4].trim(), p[5].trim(), p[6].trim(), p[7].trim(), p[8].trim()
                ));
            }
        }
        return doctors;
    }
    
    public static List<Appointment> getAllAppointments() {
        List<Appointment> list = new ArrayList<>();
        File f = new File("appointments.txt");
        if (!f.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] p = line.split(",", -1); // Split all fields, even empty ones

                // Expected format: id,customerId,description,doctorId,doctorName,date,time,status,paymentStatus
                if (p.length >= 9) {
                    String id = p[0].trim();
                    String customerId = p[1].trim();
                    String description = p[2].trim();
                    String doctorId = p[3].trim();
                    String doctorName = p[4].trim();
                    String date = p[5].trim();
                    String time = p[6].trim();
                    String status = p[7].trim();
                    String paymentStatus = p[8].trim();

                    list.add(new Appointment(id, customerId, description, doctorId, doctorName, date, time, status, paymentStatus));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    
    public static List<Manager> getAllManagers(){
        List<Manager> managers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("manager.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 7) {
                    String id = parts[0].trim();
                    String name = parts[1].trim();
                    String gender = parts[2].trim();
                    String email = parts[3].trim();
                    String phone = parts[4].trim();
                    String password = parts[5].trim();
                    String dob = parts[6].trim();
                    managers.add(new Manager(id, name, gender, email, phone, password, dob));
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return managers;        
    }
    
    public static List<Staff> getAllStaffs(){
        List<Staff> staffs = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("staff.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 7) {
                    String id = parts[0].trim();
                    String name = parts[1].trim();
                    String gender = parts[2].trim();
                    String email = parts[3].trim();
                    String phone = parts[4].trim();
                    String password = parts[5].trim();
                    String dob = parts[6].trim();
                    staffs.add(new Staff(id, name, gender, email, phone, password, dob));
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return staffs;        
    }
    
    public static boolean isEmailOrPhoneDuplicate(String email, String phone) {
        for (User u : getAllUsers()) {
            if (u.getEmail().equalsIgnoreCase(email) || u.getPhone().equals(phone)) return true;
        }
        return false;
    }

    public static void saveCustomer(Customer customer) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("customer.txt", true))) {
            bw.write(String.join(",",
                    customer.getId(),
                    customer.getName(),
                    customer.getGender(),
                    customer.getEmail(),
                    customer.getPhone(),
                    customer.getPassword(),
                    customer.getDob()
            ));
            bw.newLine();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void saveManager(Manager manager) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("manager.txt", true))) {
            bw.write(String.join(",",
                    manager.getId(),
                    manager.getName(),
                    manager.getGender(),
                    manager.getEmail(),
                    manager.getPhone(),
                    manager.getPassword(),
                    manager.getDob()
            ));
            bw.newLine();
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    public static void saveStaff(Staff staff) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("staff.txt", true))) {
            bw.write(String.join(",",
                    staff.getId(),
                    staff.getName(),
                    staff.getGender(),
                    staff.getEmail(),
                    staff.getPhone(),
                    staff.getPassword(),
                    staff.getDob()
            ));
            bw.newLine();
        } catch (IOException e) { e.printStackTrace(); }
    }
        
    public static void saveDoctor(Doctor doctor) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("doctor.txt", true))) {
            bw.write(String.join(",",
                    doctor.getId(),
                    doctor.getName(),
                    doctor.getGender(),
                    doctor.getEmail(),
                    doctor.getPhone(),
                    doctor.getPassword(),
                    doctor.getSpecialty(),
                    doctor.getDoctorType(),
                    doctor.getDob()
            ));
            bw.newLine();
        } catch (IOException e) { 
            e.printStackTrace(); 
        }
    }
        

    public static void updateCustomer(Customer updatedCustomer) {
        List<Customer> customers = getAllCustomers();
        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getId().equals(updatedCustomer.getId())) {
                customers.set(i, updatedCustomer);
                break;
            }
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("customer.txt"))) {
            for (Customer c : customers) {
                bw.write(String.join(",",
                        c.getId(), c.getName(), c.getGender(),
                        c.getEmail(), c.getPhone(), c.getPassword(), c.getDob()));
                bw.newLine();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    public static void updateDoctor(Doctor updatedDoctor) {
        List<Doctor> doctors = getAllDoctors();

        for (int i = 0; i < doctors.size(); i++) {
            if (doctors.get(i).getId().equals(updatedDoctor.getId())) {
                doctors.set(i, updatedDoctor);
                break;
            }
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("doctor.txt"))) {
            for (Doctor d : doctors) {
                bw.write(String.join(",",
                        d.getId(),
                        d.getName(),
                        d.getGender(),
                        d.getEmail(),
                        d.getPhone(),
                        d.getPassword(),
                        d.getSpecialty(),
                        d.getDoctorType(),
                        d.getDob()
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isEmailDuplicateForUpdate(String email, String excludeCustomerId) {
        for (User u : getAllUsers()) {
            if (u.getId().equals(excludeCustomerId)) continue;
            if (u.getEmail().equalsIgnoreCase(email)) return true;
        }
        return false;
    }

    public static boolean isEmailDuplicateForDoctorUpdate(String email, String excludeDoctorId) {
        for (User u : getAllUsers()) {
            if (u.getId().equals(excludeDoctorId)) continue;
            if (u.getEmail().equalsIgnoreCase(email)) return true;
        }
        return false;
    }

    /* ===================== Appointments ===================== */

    public static void writeAppointment(Appointment appointment) {
        try {
            File f = new File(APPOINTMENTS_FILE);
            if (!f.exists()) f.createNewFile();

            String status = appointment.getStatus();
            String paymentStatus = appointment.getpaymentStatus();
            String date = appointment.getDate();

            try (BufferedWriter w = new BufferedWriter(new FileWriter(f, true))) {
                w.write(String.join(",",
                        appointment.getAppointmentId(),
                        appointment.getCustomerId(),
                        appointment.getCustomerName(),
                        appointment.getDoctorId(),
                        appointment.getDoctorName(),
                        date,
                        appointment.getTime(),
                        status,
                        paymentStatus
                ));
                w.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing appointment: " + e.getMessage());
        }
    }


    public static String generateAppointmentID() {
        int id = 1000;
        File f = new File(APPOINTMENTS_FILE);
        if (f.exists()) {
            try (BufferedReader r = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = r.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] p = line.split(",");
                    if (p.length > 0 && p[0].startsWith("A")) {
                        try {
                            id = Math.max(id, Integer.parseInt(p[0].substring(1)));
                        } catch (NumberFormatException ignored) {}
                    }
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
        return "A" + (id + 1);
    }

    public static List<Appointment> readAppointments() {
        List<Appointment> list = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new FileReader(APPOINTMENTS_FILE))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",");
                // id,custId,custName,docId,docName,date,time,status,paymentStatus
                if (p.length >= 8) {
                    String id = p[0].trim();
                    String custId = p[1].trim();
                    String custName = p[2].trim();
                    String docId = p[3].trim();
                    String docName = p[4].trim();
                    String date = p[5].trim();
                    String time = p[6].trim();
                    String status = p[7].trim();
                    String paymentStatus = (p.length >= 9) ? p[8].trim() : "Unpaid"; // default
                    list.add(new Appointment(id, custId, custName, docId, docName, date, time, status, paymentStatus));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Appointments file not found, returning empty list");
        } catch (IOException e) {
            System.err.println("Error reading appointments: " + e.getMessage());
        }
        return list;
    }

    
    public static List<AppointmentDetails> readAppointmentDetails() {
        List<AppointmentDetails> list = new ArrayList<>();
        File f = new File("appointment_details.txt");
        if (!f.exists()) {
            return list;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] p = line.split(",", -1); // keep empty fields
                try {
                    if (p.length >= 5) {
                        // Format: id,charges,diagnosis,notes,followUp
                        String id = p[0].trim();
                        double charges = Double.parseDouble(p[1].trim());
                        String diagnosis = p[2].trim();
                        String notes = p[3].trim();
                        boolean followUp = Boolean.parseBoolean(p[4].trim());
                        list.add(new AppointmentDetails(id, charges, diagnosis, notes, followUp));

                    } else if (p.length == 4) {
                        // Format: id,charges,diagnosis,notes  (no follow-up flag)
                        String id = p[0].trim();
                        double charges = Double.parseDouble(p[1].trim());
                        String diagnosis = p[2].trim();
                        String notes = p[3].trim();
                        list.add(new AppointmentDetails(id, charges, diagnosis, notes, false));

                    } else if (p.length == 3) {
                        // Format: id,charges,diagnosis  (old version)
                        String id = p[0].trim();
                        double charges = Double.parseDouble(p[1].trim());
                        String diagnosis = p[2].trim();
                        list.add(new AppointmentDetails(id, charges, diagnosis, "", false));
                    }

                } catch (Exception parseEx) {
                    System.err.println("Skipping invalid line in appointment_details.txt: " + line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static void overwriteAppointments(List<Appointment> appointments) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(APPOINTMENTS_FILE))) {
            for (Appointment a : appointments) {
                w.write(String.join(",",
                        a.getAppointmentId(), a.getCustomerId(), a.getCustomerName(),
                        a.getDoctorId(), a.getDoctorName(), a.getDate(), a.getTime(), a.getStatus(), a.getpaymentStatus()));
                w.newLine();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static List<Appointment> getAppointmentsByCustomer(String customerId) {
        List<Appointment> out = new ArrayList<>();
        for (Appointment a : readAppointments()) {
            if (a.getCustomerId().equals(customerId)) out.add(a);
        }
        return out;
    }

    public static List<Appointment> getAppointmentsByCustomerId(String customerId) {
        return getAppointmentsByCustomer(customerId);
    }

    public static Appointment getAppointmentById(String appointmentId) {
        try (BufferedReader r = new BufferedReader(new FileReader(APPOINTMENTS_FILE))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] d = line.split(",");
                if (d.length >= 8 && d[0].equals(appointmentId)) {
                    String paymentStatus = (d.length >= 9) ? d[8].trim() : "Unpaid";
                    return new Appointment(d[0], d[1], d[2], d[3], d[4], d[5], d[6], d[7], paymentStatus);
                }
            }
        } catch (IOException e) { 
            e.printStackTrace(); 
        }
        return null;
    }


    public static List<Appointment> getUpcomingAppointmentsByCustomer(String customerId) {
        List<Appointment> out = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Appointment a : readAppointments()) {
            if (!a.getCustomerId().equals(customerId)) continue;
            if (!"Upcoming".equalsIgnoreCase(a.getStatus())) continue;
            try {
                String s = a.getDate().trim();
                LocalDate ad = LocalDate.parse(s, formatter); // always dd/MM/yyyy
                if (!ad.isBefore(today)) {
                    out.add(a);
                }
            } catch (Exception e) {
                System.err.println("Invalid date format for appointment: " + a.getDate());
            }
        }
        return out;
    }

    public static synchronized boolean updateAppointmentStatus(String appointmentId, String newStatus) {
        List<String> lines = new ArrayList<>();
        boolean found = false;
        File file = new File(APPOINTMENTS_FILE);
        try {
            if (file.exists()) {
                try (BufferedReader r = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = r.readLine()) != null) {
                        if (line.trim().isEmpty()) { lines.add(line); continue; }
                        String[] p = line.split(",");
                        if (p.length >= 7 && p[0].trim().equals(appointmentId)) {
                            if (p.length >= 8) p[7] = newStatus;
                            else {
                                String[] np = new String[8];
                                System.arraycopy(p, 0, np, 0, p.length);
                                np[7] = newStatus;
                                p = np;
                            }
                            lines.add(String.join(",", p));
                            found = true;
                        } else lines.add(line);
                    }
                }
            }
            if (!found) return false;
            try (PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter(file)))) {
                for (String l : lines) w.println(l);
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error updating appointment status: " + e.getMessage());
            return false;
        }
    }
    
    public static synchronized boolean updatePaymentStatus(String appointmentId, String newPaymentStatus) {
        List<String> lines = new ArrayList<>();
        boolean found = false;
        File file = new File(APPOINTMENTS_FILE);
        try {
            if (file.exists()) {
                try (BufferedReader r = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = r.readLine()) != null) {
                        if (line.trim().isEmpty()) { 
                            lines.add(line); 
                            continue; 
                        }
                        String[] p = line.split(",");
                        if (p.length >= 8 && p[0].trim().equals(appointmentId)) {
                            // If file has paymentStatus column already
                            if (p.length >= 9) {
                                p[8] = newPaymentStatus; 
                            } else {
                                // Expand to add PaymentStatus field
                                String[] np = new String[9];
                                System.arraycopy(p, 0, np, 0, p.length);
                                np[8] = newPaymentStatus;
                                p = np;
                            }
                            lines.add(String.join(",", p));
                            found = true;
                        } else {
                            lines.add(line);
                        }
                    }
                }
            }
            if (!found) return false;

            try (PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter(file)))) {
                for (String l : lines) w.println(l);
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error updating payment status: " + e.getMessage());
            return false;
        }
    }

    public static void cancelAppointment(String appointmentId) throws Exception {
        List<Appointment> list = readAppointments();
        Appointment target = null;
        for (Appointment a : list) {
            if (a.getAppointmentId().equals(appointmentId)) {
                target = a;
                a.setStatus("Cancelled");
                break;
            }
        }
        if (target == null) throw new Exception("Appointment not found");
        overwriteAppointments(list);

        // ✅ only return if slot is not a shift range
        if (!target.getTime().contains("-")) {
            returnSlotToSchedule(
            target.getDoctorId(),
            target.getDate(), // keep dd/MM/yyyy
            target.getTime()
        );
        }
    }

    /* ===================== Schedules ===================== */

    public static Map<String, List<String>> readSchedule() {
        Map<String, List<String>> schedule = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("schedule.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 3);
                if (parts.length >= 2) {
                    String doctorId = parts[0].trim();
                    String date = parts[1].trim();

                    List<String> slots = new ArrayList<>();
                    if (parts.length == 3) slots.addAll(Arrays.asList(parts[2].split(";")));
                    schedule.put(doctorId + "," + date, slots);
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return schedule;
    }


    public static List<String> getAvailableSlotsByDoctorId(String doctorId) {
        List<String> slots = new ArrayList<>();
        Map<String, List<String>> schedule = readSchedule(); 

        for (Map.Entry<String, List<String>> entry : schedule.entrySet()) {
            String key = entry.getKey(); 
            String[] parts = key.split(",");
            if (parts.length != 2) continue;

            String id = parts[0];
            String dateISO = parts[1];

            if (id.equalsIgnoreCase(doctorId)) {
                for (String time : entry.getValue()) {
                    if (!time.contains("-")) { // ✅ skip shift ranges
                        slots.add(dateISO + " " + time);
                    }
                }
            }
        }
        return slots;
    }


    public static List<String> getAvailableSlotsByDoctorIdAndDate(String doctorId, String date) {
        List<String> out = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("schedule.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",", 3);
                if (p.length >= 3 && p[0].equals(doctorId) && p[1].equals(date)) {
                    for (String t : p[2].split(";")) {
                        if (!t.trim().isEmpty()) out.add(t.trim());
                    }
                    break;
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return out;
    }


    public static void removeSlot(String doctorId, String dateDisplay, String time) {
        String date = dateDisplay.trim(); // already dd/MM/yyyy
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("schedule.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",", 3);
                if (p.length >= 3 && p[0].equals(doctorId) && p[1].trim().equals(date)) {
                    List<String> times = new ArrayList<>(Arrays.asList(p[2].split(";")));
                    times.removeIf(s -> s.trim().equals(time));
                    times.sort((a, b) -> {
                        try { return LocalTime.parse(a).compareTo(LocalTime.parse(b)); }
                        catch (Exception e) { return a.compareTo(b); }
                    });
                    if (!times.isEmpty())
                        lines.add(p[0] + "," + p[1] + "," + String.join(";", times));
                    // else drop line
                } else lines.add(line);
            }
        } catch (IOException e) { e.printStackTrace(); }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("schedule.txt"))) {
            for (int i = 0; i < lines.size(); i++) {
                bw.write(lines.get(i));
                if (i < lines.size() - 1) bw.newLine();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }


    public static void returnSlotToSchedule(String doctorId, String date, String time) {
        File file = new File("schedule.txt");
        List<String> lines = new ArrayList<>();
        boolean slotAdded = false;

        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split(",", 3);
                if (p.length < 3) {
                    lines.add(line);
                    continue;
                }
                if (p[0].equals(doctorId) && p[1].equals(date)) { // ✅ dd/MM/yyyy
                    Set<String> set = new LinkedHashSet<>(Arrays.asList(p[2].split(";")));
                    set.add(time); // add back slot
                    List<String> sorted = new ArrayList<>(set);
                    sorted.sort(Comparator.naturalOrder());
                    lines.add(p[0] + "," + p[1] + "," + String.join(";", sorted));
                    slotAdded = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!slotAdded) {
            // doctor/date not in schedule → create new entry
            lines.add(doctorId + "," + date + "," + time);
        }

        try (BufferedWriter w = new BufferedWriter(new FileWriter(file))) {
            for (int i = 0; i < lines.size(); i++) {
                w.write(lines.get(i));
                if (i < lines.size() - 1) w.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void writeSchedule(Map<String, List<String>> schedule) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter("schedule.txt"))) {
            for (Map.Entry<String, List<String>> e : schedule.entrySet()) {
                String key = e.getKey(); // doctorId,dd/MM/yyyy
                w.write(key + "," + String.join(";", e.getValue()));
                w.newLine();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }


    public static List<String> getAllSpecialties() {
        Set<String> set = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader("doctor.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length >= 7) set.add(p[6].trim());
            }
        } catch (IOException e) { e.printStackTrace(); }
        List<String> list = new ArrayList<>(set);
        Collections.sort(list);
        return list;
    }

    public static List<String> readLines(String filename) {
        List<String> out = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line; while ((line = br.readLine()) != null) if (!line.trim().isEmpty()) out.add(line.trim());
        } catch (IOException e) { e.printStackTrace(); }
        return out;
    }

    public static List<Doctor> getDoctorsBySpecialty(String specialty) {
        List<Doctor> out = new ArrayList<>();
        for (Doctor d : getAllDoctors())
            if (d.getSpecialty() != null && d.getSpecialty().equalsIgnoreCase(specialty)) out.add(d);
        return out;
    }

    public static Doctor getDoctorById(String doctorId) {
        for (Doctor d : getAllDoctors()) if (d.getId().equals(doctorId)) return d;
        return null;
    }

    /* ===================== Appointment Details (Charges, Diagnosis, Notes) ===================== */

    // Backward compatible: supports 3 fields (ID, charges, diagnosis) or new 4th (notes)

    
    public static AppointmentDetails getAppointmentDetailsById(String appointmentId) {
        File file = new File("appointment_details.txt");
        if (!file.exists()) return null;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts[0].trim().equalsIgnoreCase(appointmentId.trim())) {
                    double charges = Double.parseDouble(parts[1].trim());
                    String diagnosis = parts.length > 2 ? parts[2].trim() : "";
                    String notes = parts.length > 3 ? parts[3].trim() : "";
                    boolean followUpNeeded = parts.length > 4 && Boolean.parseBoolean(parts[4].trim());
                    return new AppointmentDetails(appointmentId.trim(), charges, diagnosis, notes, followUpNeeded);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Map<String, AppointmentDetails> getAllAppointmentDetailsMap() {
        Map<String, AppointmentDetails> map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("appointment_details.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",", -1); // <-- split all parts, not just 4

                if (p.length >= 5) {
                    double charges = Double.parseDouble(p[1].trim());
                    String diagnosis = p[2].trim();
                    String notes = p[3].trim();
                    boolean followUp = Boolean.parseBoolean(p[4].trim());
                    map.put(p[0].trim(), new AppointmentDetails(p[0].trim(), charges, diagnosis, notes, followUp));
                } else if (p.length == 4) {
                    double charges = Double.parseDouble(p[1].trim());
                    String diagnosis = p[2].trim();
                    String notes = p[3].trim();
                    map.put(p[0].trim(), new AppointmentDetails(p[0].trim(), charges, diagnosis, notes, false));
                } else if (p.length == 3) {
                    double charges = Double.parseDouble(p[1].trim());
                    String diagnosis = p[2].trim();
                    map.put(p[0].trim(), new AppointmentDetails(p[0].trim(), charges, diagnosis, "", false));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }


    public static void saveOrUpdateAppointmentDetails(AppointmentDetails details) {
        File file = new File("appointment_details.txt");
        List<String> lines = new ArrayList<>();
        boolean updated = false;

        try {
            if (file.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.trim().isEmpty()) { 
                            lines.add(line); 
                            continue; 
                        }
                        String[] parts = line.split(",", -1);
                        if (parts.length > 0 && parts[0].equals(details.getAppointmentId())) {
                            // Replace with updated record
                            lines.add(buildDetailsLine(details));
                            updated = true;
                        } else {
                            lines.add(line);
                        }
                    }
                }
            }
            if (!updated) {
                // Append new record
                lines.add(buildDetailsLine(details));
            }

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                for (int i = 0; i < lines.size(); i++) {
                    bw.write(lines.get(i));
                    if (i < lines.size() - 1) bw.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static String buildDetailsLine(AppointmentDetails details) {
        return String.format("%s,%s,%s,%s,%s",
                details.getAppointmentId(),
                details.getCharges(),
                details.getDiagnosis() == null ? "" : details.getDiagnosis(),
                details.getNotes() == null ? "" : details.getNotes(),
                details.isFollowUpNeeded());
    }

    private static String sanitizeCsv(String s) {
        return s == null ? "" : s.replace(",", ";");
    }


    /* ===================== Feedback ===================== */

    public static void saveFeedback(Feedback feedback) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("feedback.txt", true))) {
            bw.write(String.join(",",
                    feedback.getAppointmentId().trim(),
                    feedback.getCustomerId().trim(),
                    sanitizeCsv(feedback.getDoctorFeedback().trim()),
                    String.valueOf(feedback.getDoctorRating()),
                    sanitizeCsv(feedback.getStaffFeedback().trim()),
                    String.valueOf(feedback.getStaffRating())
            ));
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Feedback getFeedbackByAppointmentId(String appointmentId) {
        try (BufferedReader br = new BufferedReader(new FileReader("feedback.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",", -1);
                if (p.length >= 6 && p[0].equalsIgnoreCase(appointmentId)) {
                    return new Feedback(
                            p[0].trim(),  // appointmentId
                            p[1].trim(),  // customerId
                            p[2].trim(),  // doctorComment
                            Integer.parseInt(p[3].trim()), // doctorRating
                            p[4].trim(),  // staffComment
                            Integer.parseInt(p[5].trim())  // staffRating
                    );
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return null;
    }

    public static List<Feedback> getAllFeedbackByCustomer(String customerId) {
        List<Feedback> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("feedback.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",", -1);
                if (p.length >= 6 && p[1].equalsIgnoreCase(customerId)) {
                    list.add(new Feedback(
                            p[0].trim(),
                            p[1].trim(),
                            p[2].trim(),
                            Integer.parseInt(p[3].trim()),
                            p[4].trim(),
                            Integer.parseInt(p[5].trim())
                    ));
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return list;
    }

    public static List<Feedback> getAllFeedback() {
        List<Feedback> list = new ArrayList<>();
        File file = new File("feedback.txt");
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] p = line.split(",", -1); // keep empty columns
                if (p.length >= 6) {
                    String appointmentId = p[0].trim();
                    String customerId    = p[1].trim();
                    String doctorFb      = p[2].trim();
                    int doctorRating     = safeParseInt(p[3].trim(), -1);
                    String staffFb       = p[4].trim();
                    int staffRating      = safeParseInt(p[5].trim(), -1);

                    list.add(new Feedback(
                            appointmentId,
                            customerId,
                            doctorFb,
                            doctorRating,
                            staffFb,
                            staffRating
                    ));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    
    // helper
    private static int safeParseInt(String s, int defaultVal) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return defaultVal;
        }
    }


/* ===================== Medical Certificates (MC) ===================== */
    // mc.txt format:
    // APPT_ID,CUSTOMER_ID,ISSUE_DATE,REST_FROM,REST_TO,DOCTOR_ID,Remarks

    public static synchronized void saveMedicalCertificate(MedicalCertificate mc) {
        // Validate dates before writing
        assertValidMcDates(mc.getIssueDate(), mc.getRestFrom(), mc.getRestTo());

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(MC_FILE, true))) {
            bw.write(String.join(",",
                    mc.getAppointmentId(),
                    mc.getCustomerId(),
                    mc.getIssueDate(),
                    mc.getRestFrom(),
                    mc.getRestTo(),
                    mc.getDoctorId(),
                    sanitizeCsv(mc.getRemarks())
            ));
            bw.newLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    private static LocalDate parseDateStrict(String s) {
    return LocalDate.parse(s.trim(), DATE_FORMATTER); // dd/MM/uuuu
}

    private static void assertValidMcDates(String issueDate, String restFrom, String restTo) {
        LocalDate today = LocalDate.now();
        LocalDate issue = parseDateStrict(issueDate);
        LocalDate from  = parseDateStrict(restFrom);
        LocalDate to    = parseDateStrict(restTo);

        // 1) Rest dates cannot be in the past (today is OK)
        if (from.isBefore(today)) {
            throw new IllegalArgumentException("Rest From cannot be in the past.");
        }
        if (to.isBefore(today)) {
            throw new IllegalArgumentException("Rest To cannot be in the past.");
        }

        // 2) Order must be valid
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("Rest To must be on or after Rest From.");
        }

        // 3) (Optional but sensible) Rest cannot start before the issue date
        if (from.isBefore(issue)) {
            throw new IllegalArgumentException("Rest From cannot be before the Issue Date.");
        }
    }

    public static MedicalCertificate getMedicalCertificateByAppt(String appointmentId) {
        try (BufferedReader br = new BufferedReader(new FileReader(MC_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",", 7);
                if (p.length >= 7 && p[0].trim().equalsIgnoreCase(appointmentId)) {
                    return new MedicalCertificate(
                            p[0].trim(), // apptId
                            p[1].trim(), // customerId
                            p[2].trim(), // issueDate
                            p[3].trim(), // restFrom
                            p[4].trim(), // restTo
                            p[5].trim(), // doctorId
                            p.length >= 7 ? p[6].trim() : "" // remarks
                    );
                }
            }
        } catch (FileNotFoundException e) {
            // No MC file yet — fine
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<MedicalCertificate> getAllMedicalCertificatesByCustomer(String customerId) {
        List<MedicalCertificate> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(MC_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",", 7);
                if (p.length >= 7 && p[1].trim().equalsIgnoreCase(customerId)) {
                    list.add(new MedicalCertificate(
                            p[0].trim(), p[1].trim(), p[2].trim(),
                            p[3].trim(), p[4].trim(), p[5].trim(),
                            p.length >= 7 ? p[6].trim() : ""
                    ));
                }
            }
        } catch (FileNotFoundException e) {
            // Ok if file not present yet
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }


    /* ===================== Utils ===================== */

    public static void printAllAppointments() {
        File file = new File(APPOINTMENTS_FILE);
        if (!file.exists()) { System.out.println("appointments.txt does not exist"); return; }
        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String line;
            System.out.println("=== All Appointments ===");
            while ((line = r.readLine()) != null) System.out.println(line);
            System.out.println("=== End of Appointments ===");
        } catch (IOException e) {
            System.err.println("Error reading appointments: " + e.getMessage());
        }
    }
    
    
        public static User findUserByEmailOrPhone(String input) {
        List<User> allUsers = getAllUsers();
        for (User user : allUsers) {
            if (user.getEmail().equalsIgnoreCase(input) || user.getPhone().equals(input)) {
                return user;
            }
        }
        return null;
    }


    public static String findUserIdByEmailOrPhone(String input) {
        User u = findUserByEmailOrPhone(input);
        return u != null ? u.getId() : null;
    }
    
    public static void updateUserPassword(String userId, String newPassword) {
        String[] files = {"customer.txt", "manager.txt", "doctor.txt", "staff.txt"};

        for (String filename : files) {
            File file = new File(filename);
            if (!file.exists()) continue;

            List<String> updatedLines = new ArrayList<>();
            boolean updated = false;

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts[0].equals(userId)) {
                        switch (filename) {
                            case "customer.txt":
                                parts[5] = newPassword;
                                break;
                            case "manager.txt":
                                parts[5] = newPassword;
                                break;
                            case "doctor.txt":
                                parts[5] = newPassword;
                                break;
                            case "staff.txt":
                                parts[5] = newPassword;
                                break;
                        }
                        updated = true;
                    }
                    updatedLines.add(String.join(",", parts));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (updated) {
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                    for (String updatedLine : updatedLines) {
                        bw.write(updatedLine);
                        bw.newLine();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
        }
    }
    
    public static void updateManager(Manager updatedManager) {
        List<Manager> managers = getAllManagers();
        for (int i = 0; i < managers.size(); i++) {
            if (managers.get(i).getId().equals(updatedManager.getId())) {
                managers.set(i, updatedManager);
                break;
            }
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("manager.txt"))) {
            for (Manager c : managers) {
                bw.write(String.join(",",
                        c.getId(), c.getName(), c.getGender(),
                        c.getEmail(), c.getPhone(), c.getPassword(), c.getDob()));
                bw.newLine();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void deleteManager(String id) {
        String filePath = "manager.txt"; 
        File inputFile = new File(filePath);
        File tempFile = new File("temp_manager.txt");

        try (
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        ) {
            String line;
            boolean found = false;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 0 && parts[0].equals(id)) {
                    // Skip this line (delete manager)
                    found = true;
                    continue;
                }
                writer.write(line + System.lineSeparator());
            }

            writer.close();
            reader.close();

            // Replace old file with new file
            if (inputFile.delete()) {
                tempFile.renameTo(inputFile);
            }

            if (found) {
                System.out.println("Manager with ID " + id + " deleted successfully.");
            } else {
                System.out.println("Manager with ID " + id + " not found.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateStaff(Staff updatedStaff) {
        List<Staff> staffList = getAllStaffs();
        for (int i = 0; i < staffList.size(); i++) {
            if (staffList.get(i).getId().equals(updatedStaff.getId())) {
                staffList.set(i, updatedStaff);
                break;
            }
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("staff.txt"))) {
            for (Staff s : staffList) {
                bw.write(String.join(",",
                        s.getId(),
                        s.getName(),
                        s.getGender(),
                        s.getEmail(),
                        s.getPhone(),
                        s.getPassword(),
                        s.getDob()
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteStaff(String id) {
        List<Staff> staffList = getAllStaffs();
        staffList.removeIf(s -> s.getId().equals(id));

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("staff.txt"))) {
            for (Staff s : staffList) {
                bw.write(String.join(",",
                        s.getId(),
                        s.getName(),
                        s.getGender(),
                        s.getEmail(),
                        s.getPhone(),
                        s.getPassword(),
                        s.getDob()
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteDoctor(String id) {
        List<Doctor> doctors = getAllDoctors();

        doctors.removeIf(d -> d.getId().equals(id));

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("doctor.txt"))) {
            for (Doctor d : doctors) {
                bw.write(String.join(",",
                        d.getId(),
                        d.getName(),
                        d.getGender(),
                        d.getEmail(),
                        d.getPhone(),
                        d.getPassword(),
                        d.getSpecialty(),
                        d.getDoctorType(),
                        d.getDob()
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int extractRating(String part) {
        try {
            int start = part.indexOf("Rating:");
            if (start != -1) {
                String sub = part.substring(start);
                // Match number before the slash
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)/5").matcher(sub);
                if (m.find()) {
                    return Integer.parseInt(m.group(1)); // just "5" instead of "55"
                }
            }
        } catch (Exception ignored) {}
        return -1;
    }

    public static void deleteCustomer(String id) {
        File inputFile = new File("customer.txt");
        File tempFile = new File("customers_temp.txt");

        try (
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 0 && parts[0].equals(id)) {
                    // Skip this line (customer with given ID)
                    continue;
                }
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Replace original file with updated file
        if (!inputFile.delete()) {
            System.out.println("Could not delete original file");
        }
        if (!tempFile.renameTo(inputFile)) {
            System.out.println("Could not rename temp file");
        }
    }

    public static Customer getCustomerById(String id) {
        List<Customer> customers = getAllCustomers();
        for (Customer c : customers) {
            if (c.getId().equalsIgnoreCase(id)) {
                return c;
            }
        }
        return null; // not found
    }

    public static Doctor getOncallDoctorForNow() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        List<String> todayDoctorIds = new ArrayList<>();

        // Read oncall.txt to find today's doctors
        try (BufferedReader br = new BufferedReader(new FileReader("oncall.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2 && parts[0].trim().equals(today.format(formatter))) {
                    todayDoctorIds = Arrays.stream(parts[1].split(";"))
                                           .map(String::trim)
                                           .filter(id -> !id.isEmpty())
                                           .collect(Collectors.toList());
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        if (todayDoctorIds.isEmpty()) {
            return null; // No on-call doctors for today
        }

        // Map doctor IDs -> Doctor objects
        List<Doctor> allDoctors = getAllDoctors();
        List<Doctor> eligible = todayDoctorIds.stream()
                .map(id -> allDoctors.stream()
                                     .filter(d -> d.getId().equalsIgnoreCase(id))
                                     .findFirst().orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (eligible.isEmpty()) {
            return null; // none matched
        }

        // Round-robin assignment per day
        int lastIndex = lastAssignedIndex.getOrDefault(today, -1);
        int nextIndex = (lastIndex + 1) % eligible.size();
        lastAssignedIndex.put(today, nextIndex);

        return eligible.get(nextIndex);
    }

    private static boolean isNowWithinShift(LocalTime now, String shift) {
        try {
            String[] parts = shift.split("-");
            if (parts.length != 2) return false;

            LocalTime start = LocalTime.parse(parts[0].trim());
            LocalTime end = LocalTime.parse(parts[1].trim());

            if (end.isAfter(start)) {
                // normal range: e.g. 18:00–22:00
                return !now.isBefore(start) && !now.isAfter(end);
            } else {
                // overnight range: e.g. 23:00–03:00
                return !now.isBefore(start) || !now.isAfter(end);
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static List<Doctor> getClinicalDoctorsBySpecialty(String specialty) {
        return getAllDoctors().stream()
                .filter(d -> d.getSpecialty().equalsIgnoreCase(specialty))
                .filter(d -> "Clinical".equalsIgnoreCase(d.getDoctorType()) 
                          || "Clinical Doctor".equalsIgnoreCase(d.getDoctorType()))
                .collect(Collectors.toList());
    }

    public static List<Doctor> getOncallDoctorsBySpecialty(String specialty) {
        return getAllDoctors().stream()
                .filter(d -> d.getSpecialty().equalsIgnoreCase(specialty))
                .filter(d -> "On-call".equalsIgnoreCase(d.getDoctorType()) 
                          || "On-call Doctor".equalsIgnoreCase(d.getDoctorType()))
                .collect(Collectors.toList());
    }

    public static String generatePaymentID() {
        int id = 1000;
        File f = new File(PAYMENTS_FILE);
        if (f.exists()) {
            try (BufferedReader r = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = r.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] p = line.split(",");
                    if (p[0].startsWith("P")) {
                        id = Math.max(id, Integer.parseInt(p[0].substring(1)));
                    }
                }
            } catch (Exception ignored) {}
        }
        return "P" + (id + 1);
    }

    public static void writePayment(String receiptId,
                                    model.Appointment appt,
                                    model.AppointmentDetails details,
                                    model.Staff staff,
                                    String paymentMethod,
                                    double paidAmount,
                                    double changeAmount,
                                    String timestamp) {
        try {
            java.io.File f = new java.io.File(PAYMENTS_FILE);
            if (!f.exists()) f.createNewFile();

            double charges = details.getCharges();
            double tax = charges * 0.06;
            double total = charges + tax;

            String line = String.join(",",
                    sanitizeCsv(receiptId),
                    sanitizeCsv(appt.getAppointmentId()),
                    sanitizeCsv(appt.getCustomerId()),
                    sanitizeCsv(appt.getCustomerName()),
                    sanitizeCsv(appt.getDoctorId()),
                    sanitizeCsv(appt.getDoctorName()),
                    sanitizeCsv(appt.getDate()),           // dd/MM/yyyy
                    sanitizeCsv(appt.getTime()),           // HH:mm
                    sanitizeCsv(details.getDiagnosis()),
                    String.format("%.2f", charges),
                    String.format("%.2f", tax),
                    String.format("%.2f", total),
                    sanitizeCsv(paymentMethod),
                    String.format("%.2f", paidAmount),
                    String.format("%.2f", changeAmount),
                    sanitizeCsv(staff.getId()),
                    sanitizeCsv(staff.getName()),
                    sanitizeCsv(timestamp)
            );

            try (java.io.BufferedWriter w = new java.io.BufferedWriter(new java.io.FileWriter(f, true))) {
                w.write(line);
                w.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error writing payment: " + e.getMessage());
        }
    }

    public static List<model.Payment> readPayments() {
        List<model.Payment> list = new ArrayList<>();
        File f = new File(PAYMENTS_FILE);
        if (!f.exists()) return list;

        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",", -1); // keep empty values
                if (p.length < 18) continue; // skip invalid rows

                model.Payment pay = new model.Payment(
                        p[0],  // receiptId
                        p[1],  // appointmentId
                        p[2],  // customerId
                        p[3],  // customerName
                        p[4],  // doctorId
                        p[5],  // doctorName
                        p[6],  // date
                        p[7],  // time
                        p[8],  // diagnosis
                        Double.parseDouble(p[9]),
                        Double.parseDouble(p[10]),
                        Double.parseDouble(p[11]),
                        p[12], // paymentMethod
                        Double.parseDouble(p[13]),
                        Double.parseDouble(p[14]),
                        p[15], // staffId
                        p[16], // staffName
                        p[17]  // timestamp
                );
                list.add(pay);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public static model.Payment getPaymentByAppt(String appointmentId) {
        List<model.Payment> all = readPayments(); // implement this similar to readAppointments()
        for (model.Payment p : all) {
            if (p.getAppointmentId().equals(appointmentId)) {
                return p;
            }
        }
        return null;
    }

    public static Staff getStaffById(String staffId) {
        List<Staff> staffList = getAllStaffs();  // implement similar to readAppointments()
        for (Staff s : staffList) {
            if (s.getId().equals(staffId)) {
                return s;
            }
        }
        return null;
    }


    

}

