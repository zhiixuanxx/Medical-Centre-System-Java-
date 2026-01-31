package view;

import model.Doctor;
import model.Manager;
import util.FileStorage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class DoctorScheduleManagement extends JFrame {

    // ===== UI =====
    private JTable doctorTable, slotsTable;
    private DefaultTableModel doctorModel, slotsModel;

    private JTextField searchField;
    private JButton searchBtn;

    private JTextField dateField;             // dd/MM/yyyy
    private JButton btnAutoWeek, btnAutoWeekSelected, btnSearchDay, btnSearchWeek;
    private JCheckBox chkOverwrite;
    private JLabel scheduleNoteLabel;

    private JButton loadSlotsBtn, removeSlotBtn, saveBtn, resetBtn, backBtn, editOnCallRosterBtn;

    private JComboBox<String> doctorTypeFilter;

    private final Manager loggedInManager;

    // ===== Time formats =====
    private static final DateTimeFormatter DATE_DDMM = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // ===== On-call file =====
    private static final String ONCALL_FILE = "oncall.txt";
    private boolean overwrite;

    public DoctorScheduleManagement(Manager manager) {
        this.loggedInManager = manager;

        setTitle("Doctor Schedule Management");
        setSize(1500, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // ===== Left: Doctor list + search =====
        JPanel leftPanel = new JPanel(new BorderLayout(8, 8));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Doctor"));

        doctorTypeFilter = new JComboBox<>(new String[]{"All", "Clinical", "On-call"});
        searchField = new JTextField(10);
        searchBtn = new JButton("Search");

        searchPanel.add(new JLabel("Type:"));
        searchPanel.add(doctorTypeFilter);
        searchPanel.add(new JLabel("ID/Name:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);

        doctorModel = new DefaultTableModel(new Object[]{"Doctor ID", "Name", "Specialty", "Type"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        doctorTable = new JTable(doctorModel);
        doctorTable.setRowHeight(26);
        doctorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane doctorScroll = new JScrollPane(doctorTable);
        doctorScroll.setBorder(BorderFactory.createTitledBorder("Doctors"));

        leftPanel.add(searchPanel, BorderLayout.NORTH);
        leftPanel.add(doctorScroll, BorderLayout.CENTER);

        loadDoctors("", "All");

       // ===== Top controls =====
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createTitledBorder("Controls"));

        // First row: buttons + date field
        JPanel controlRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        controlRow.add(new JLabel("Date (dd/MM/yyyy):"));
        dateField = new JTextField(12);
        controlRow.add(dateField);

        btnAutoWeek = new JButton("Autogenerate Week (All)");
        controlRow.add(btnAutoWeek);

        btnAutoWeekSelected = new JButton("Autogenerate Week (Selected)");
        controlRow.add(btnAutoWeekSelected);

        btnSearchDay = new JButton("Search Day");
        controlRow.add(btnSearchDay);

        btnSearchWeek = new JButton("Search Week");
        controlRow.add(btnSearchWeek);

        JButton editOncallRosterBtn = new JButton("Edit On-call Roster");
        controlRow.add(editOncallRosterBtn);

        topPanel.add(controlRow, BorderLayout.NORTH);

        // Second row: note label
        scheduleNoteLabel = new JLabel(readScheduleInfo());
        scheduleNoteLabel.setForeground(Color.BLUE);
        JPanel noteRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        noteRow.add(scheduleNoteLabel);

        topPanel.add(noteRow, BorderLayout.SOUTH);

        // ===== Right: Slots =====
        slotsModel = new DefaultTableModel(new Object[]{"Time"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        slotsTable = new JTable(slotsModel);
        slotsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane slotsScroll = new JScrollPane(slotsTable);
        slotsScroll.setBorder(BorderFactory.createTitledBorder("Time Slots (Selected Doctor & Date)"));

        JPanel slotButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        loadSlotsBtn = new JButton("Load Slots");
        removeSlotBtn = new JButton("Remove Selected");
        saveBtn = new JButton("Save");
        slotButtons.add(loadSlotsBtn);
        slotButtons.add(removeSlotBtn);
        slotButtons.add(saveBtn);

        JPanel rightPanel = new JPanel(new BorderLayout(6, 6));
        rightPanel.add(topPanel, BorderLayout.NORTH);
        rightPanel.add(slotsScroll, BorderLayout.CENTER);
        rightPanel.add(slotButtons, BorderLayout.SOUTH);

        // ===== Bottom =====
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        resetBtn = new JButton("Reset");
        backBtn = new JButton("Back");
        bottomPanel.add(resetBtn);
        bottomPanel.add(backBtn);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        split.setResizeWeight(0.35);

        add(split, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // ===== Events =====
        searchBtn.addActionListener(e ->
            loadDoctors(searchField.getText().trim(), doctorTypeFilter.getSelectedItem().toString())
        );
        doctorTypeFilter.addActionListener(e ->
            loadDoctors(searchField.getText().trim(), doctorTypeFilter.getSelectedItem().toString())
        );

        btnAutoWeek.addActionListener(e -> onAutogenerateWeek());                 // all doctors
        btnAutoWeekSelected.addActionListener(e -> onAutogenerateWeekSelected()); // selected doctor
        btnSearchDay.addActionListener(e -> onSearchDay());
        btnSearchWeek.addActionListener(e -> onSearchWeek());
        editOncallRosterBtn.addActionListener(e -> {
            String dateInput = dateField.getText().trim();
            if (dateInput.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter a date (dd/MM/yyyy).");
                return;
            }
            LocalDate any = parseDDMM(dateInput);
            if (any == null) {
                JOptionPane.showMessageDialog(this, "Invalid date format.");
                return;
            }
            LocalDate monday = any.with(java.time.DayOfWeek.MONDAY);

            // get all on-call doctors
            List<Doctor> oncallDoctors = readAllDoctorsTolerant().stream()
                    .filter(d -> safeDoctorType(d).toLowerCase().contains("on-call"))
                    .toList();

            new OncallRosterEditor(monday, oncallDoctors);
        });
        loadSlotsBtn.addActionListener(e -> loadExistingSlots());
        removeSlotBtn.addActionListener(e -> removeSelectedSlot());
        saveBtn.addActionListener(e -> saveSlots());
        resetBtn.addActionListener(e -> resetForm());
        backBtn.addActionListener(e -> {
            dispose();
            new ManagerDashboard(loggedInManager).setVisible(true);
        });

        setVisible(true);
    }

    /* ========================= Autogenerate Week (Selected Doctor only) ========================= */
    private void onAutogenerateWeekSelected() {
        int row = doctorTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a doctor first.");
            return;
        }

        String doctorId = doctorModel.getValueAt(row, 0).toString();
        String doctorType = doctorModel.getValueAt(row, 3).toString();
        String dateInput = dateField.getText().trim();
        LocalDate any = dateInput.isEmpty() ? LocalDate.now() : parseDDMM(dateInput);
        if (any == null) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use dd/MM/yyyy.");
            return;
        }

        LocalDate monday = any.with(DayOfWeek.MONDAY);
        LocalDate sunday = monday.plusDays(6);
        Map<String, List<String>> schedule = FileStorage.readSchedule();

        // ✅ STEP 1: Check if this doctor's schedule for the week already exists
        boolean alreadyGenerated = true;
        for (LocalDate d = monday; !d.isAfter(sunday); d = d.plusDays(1)) {
            String key = doctorId + "," + d.format(DATE_ISO);
            if (!schedule.containsKey(key) || schedule.get(key).isEmpty()) {
                alreadyGenerated = false;
                break;
            }
        }

        if (alreadyGenerated) {
            JOptionPane.showMessageDialog(this,
                "Schedule is already generated for week " +
                monday.format(DATE_DDMM) + " - " + sunday.format(DATE_DDMM) +
                " for doctor " + doctorId);
            return; // stop here, don’t regenerate
        }

        // ✅ STEP 2: Proceed with generation if not already generated
        for (LocalDate d = monday; !d.isAfter(sunday); d = d.plusDays(1)) {
            if (doctorType.toLowerCase().contains("clinical")) {
                if (d.getDayOfWeek() != DayOfWeek.SUNDAY) {
                    boolean isSat = d.getDayOfWeek() == DayOfWeek.SATURDAY;
                    List<String> daySlots = isSat
                            ? buildSlots(LocalTime.of(8, 0), LocalTime.of(14, 30), 30, true)
                            : buildSlots(LocalTime.of(8, 0), LocalTime.of(17, 30), 30, true);

                    String key = doctorId + "," + d.format(DATE_ISO);
                    schedule.putIfAbsent(key, new ArrayList<>(daySlots));
                }
            } else { // on-call
                Map<LocalDate, List<String>> oncallRoster = readOncallRoster();
                List<String> assigned = oncallRoster.getOrDefault(d, new ArrayList<>());
                if (!assigned.contains(doctorId)) {
                    assigned.add(doctorId);
                    oncallRoster.put(d, assigned);
                    writeOncallRoster(oncallRoster);
                }

                String key = doctorId + "," + d.format(DATE_ISO);
                if (!schedule.containsKey(key)) {
                    List<String> shifts = new ArrayList<>();
                    shifts.add("18:00-22:00"); // simple default
                    schedule.put(key, shifts);
                }
            }
        }

        FileStorage.writeSchedule(schedule);
        saveScheduleInfo(monday, sunday);
        JOptionPane.showMessageDialog(this,
            "Week " + monday.format(DATE_DDMM) + " - " + sunday.format(DATE_DDMM) +
            " generated successfully for doctor " + doctorId + "." +
            "\nDoctor schedule is updated until week " +
            monday.format(DATE_DDMM) + " - " + sunday.format(DATE_DDMM));
        
        scheduleNoteLabel.setText("Doctor schedule is updated until week " +
        monday.format(DATE_DDMM) + " - " + sunday.format(DATE_DDMM));
    }


    /* ========================= Doctor list ========================= */

    private void loadDoctors(String filter, String typeFilter) {
        doctorModel.setRowCount(0);
        for (Doctor d : readAllDoctorsTolerant()) {
            boolean typeMatch = typeFilter.equals("All")
                    || safeDoctorType(d).toLowerCase().contains(typeFilter.toLowerCase());
            boolean textMatch = filter.isEmpty()
                    || d.getId().toLowerCase().contains(filter.toLowerCase())
                    || d.getName().toLowerCase().contains(filter.toLowerCase());

            if (typeMatch && textMatch) {
                doctorModel.addRow(new Object[]{
                        d.getId(),
                        d.getName(),
                        d.getSpecialty(),
                        safeDoctorType(d)
                });
            }
        }
    }

    private String safeDoctorType(Doctor d) {
        try {
            String t = d.getDoctorType();
            if (t == null || t.trim().isEmpty()) return "Clinical";
            return t;
        } catch (Throwable ignored) { return "Clinical"; }
    }

    /* ========================= Slots (per doctor & date) ========================= */

    private void loadExistingSlots() {
        int row = doctorTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a doctor first.");
            return;
        }
        String doctorId = doctorModel.getValueAt(row, 0).toString();
        String type = doctorModel.getValueAt(row, 3).toString();
        String dateInput = dateField.getText().trim();
        if (dateInput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a date (dd/MM/yyyy).");
            return;
        }
        LocalDate date = parseDDMM(dateInput);
        if (date == null) {
            JOptionPane.showMessageDialog(this, "Invalid date format.");
            return;
        }
        String ymd = date.format(DATE_ISO);

        slotsModel.setRowCount(0);

        if (type.toLowerCase().contains("on-call")) {
            Map<LocalDate, List<String>> oncallRoster = readOncallRoster();
            List<String> assigned = oncallRoster.getOrDefault(date, Collections.emptyList());

            int idx = assigned.indexOf(doctorId);
            if (idx == 0) {
                slotsModel.addRow(new Object[]{"00:00-08:00"});
            } else if (idx == 1) {
                slotsModel.addRow(new Object[]{"08:00-16:00"});
            } else if (idx == 2) {
                slotsModel.addRow(new Object[]{"16:00-24:00"});
            }
        } else {
            List<String> slots = FileStorage.getAvailableSlotsByDoctorIdAndDate(doctorId, ymd);
            for (String t : slots) if (!t.trim().isEmpty()) slotsModel.addRow(new Object[]{t});
        }
        
        if (slotsModel.getRowCount() == 0) {
        slotsModel.addRow(new Object[]{"No schedule for this doctor on " + dateInput});
}
    }

    /* ========================= Autogenerate Week ========================= */

    private void onAutogenerateWeek() {
        String dateInput = dateField.getText().trim();
        LocalDate any;

        if (dateInput.isEmpty()) {
            any = LocalDate.now();
        } else {
            any = parseDDMM(dateInput);
            if (any == null) {
                JOptionPane.showMessageDialog(this, "Invalid date. Use dd/MM/yyyy.");
                return;
            }
        }

        LocalDate monday = any.with(DayOfWeek.MONDAY);
        LocalDate sunday = monday.plusDays(6);

        Map<String, List<String>> schedule = FileStorage.readSchedule();
        List<Doctor> all = readAllDoctorsTolerant();
        
        boolean allGenerated = true;
        for (Doctor doc : all) {
            for (LocalDate d = monday; !d.isAfter(sunday); d = d.plusDays(1)) {
                if (d.getDayOfWeek() == DayOfWeek.SUNDAY) continue;
                String key = doc.getId() + "," + d.format(DATE_ISO);
                if (!schedule.containsKey(key) || schedule.get(key).isEmpty()) {
                    allGenerated = false;
                    break;
                }
            }
            if (!allGenerated) break;
        }

        if (allGenerated) {
            JOptionPane.showMessageDialog(this,
                "Schedule is already generated for week " +
                monday.format(DATE_DDMM) + " - " + sunday.format(DATE_DDMM));
            return;
        }
        
        List<Doctor> clinical = all.stream()
                .filter(d -> safeDoctorType(d).toLowerCase().contains("clinical"))
                .toList();
        List<Doctor> oncall = all.stream()
                .filter(d -> safeDoctorType(d).toLowerCase().contains("on-call"))
                .toList();

        // === Clinical doctors ===
        for (Doctor doc : clinical) {
            boolean skipThisDoctor = false;

            for (LocalDate d = monday; !d.isAfter(sunday); d = d.plusDays(1)) {
                if (d.getDayOfWeek() == DayOfWeek.SUNDAY) continue;
                String key = doc.getId() + "," + d.format(DATE_ISO);
                if (schedule.containsKey(key) && !schedule.get(key).isEmpty()) {
                    // If the slots already exist but are LESS than full daySlots => someone booked → skip doctor
                    boolean isSat = d.getDayOfWeek() == DayOfWeek.SATURDAY;
                    List<String> fullSlots = isSat
                            ? buildSlots(LocalTime.of(8, 0), LocalTime.of(14, 30), 30, true)
                            : buildSlots(LocalTime.of(8, 0), LocalTime.of(17, 30), 30, true);

                    if (schedule.get(key).size() < fullSlots.size()) {
                        skipThisDoctor = true;
                        break;
                    }
                }
            }

            if (skipThisDoctor) {
                System.out.println("⏭ Skipping doctor " + doc.getId() + " because they already have bookings this week.");
                continue; // skip this doctor completely
            }
            

            // Generate schedule
            for (LocalDate d = monday; !d.isAfter(sunday); d = d.plusDays(1)) {
                if (d.getDayOfWeek() == DayOfWeek.SUNDAY) continue;
                boolean isSat = d.getDayOfWeek() == DayOfWeek.SATURDAY;
                List<String> daySlots = isSat
                        ? buildSlots(LocalTime.of(8, 0), LocalTime.of(14, 30), 30, true)
                        : buildSlots(LocalTime.of(8, 0), LocalTime.of(17, 30), 30, true);

                String key = doc.getId() + "," + d.format(DATE_ISO);
                schedule.putIfAbsent(key, new ArrayList<>(daySlots));
            }
        }

        // === On-call doctors (emergency, 3 shifts per day) ===
        Map<LocalDate, List<String>> oncallRoster = readOncallRoster();
        if (!oncall.isEmpty()) {
            int needPerDay = 3; // minimum 3 doctors per day
            int seed = monday.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear())
                    % Math.max(1, oncall.size());

            for (LocalDate d = monday; !d.isAfter(sunday); d = d.plusDays(1)) {
                List<String> assigned = pickRoundRobin(oncall, needPerDay, seed + d.getDayOfYear()); 
                oncallRoster.putIfAbsent(d, assigned);

                for (int i = 0; i < assigned.size(); i++) {
                    String docId = assigned.get(i);
                    String key = docId + "," + d.format(DATE_ISO);

                    if (!schedule.containsKey(key)) {
                        List<String> shifts = new ArrayList<>();
                        if (i == 0) shifts.add("00:00-08:00");
                        else if (i == 1) shifts.add("08:00-16:00");
                        else shifts.add("16:00-00:00");

                        schedule.put(key, shifts);
                    }
                }
            }
            writeOncallRoster(oncallRoster);
        }

        FileStorage.writeSchedule(schedule);
        saveScheduleInfo(monday, sunday);

        JOptionPane.showMessageDialog(this,
        "Week " + monday.format(DATE_DDMM) + " - " + sunday.format(DATE_DDMM) +
        " generated successfully.\nDoctors with existing bookings or schedules were skipped." +
        "\nDoctor schedule is updated until week " +
        monday.format(DATE_DDMM) + " - " + sunday.format(DATE_DDMM));
        scheduleNoteLabel.setText("Doctor schedule is updated until week " +
        monday.format(DATE_DDMM) + " - " + sunday.format(DATE_DDMM));
    }


    private List<String> pickRoundRobin(List<Doctor> docs, int k, int startIndex) {
        if (docs.isEmpty() || k <= 0) return Collections.emptyList();
        List<String> out = new ArrayList<>();
        int n = docs.size();
        for (int i = 0; i < Math.min(k, n); i++) {
            int idx = (startIndex + i) % n;
            out.add(docs.get(idx).getId());
        }
        return out;
    }

    /* ========================= Search Day / Week ========================= */

    private void onSearchDay() {
        String dateInput = dateField.getText().trim();
        if (dateInput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a date (dd/MM/yyyy).");
            return;
        }
        LocalDate date = parseDDMM(dateInput);
        if (date == null) {
            JOptionPane.showMessageDialog(this, "Invalid date. Use dd/MM/yyyy.");
            return;
        }

        Map<String, List<String>> schedule = FileStorage.readSchedule();
        Map<LocalDate, List<String>> oncall = readOncallRoster();
        List<Doctor> all = readAllDoctorsTolerant();

        String ymd = date.format(DATE_ISO);

        // Clinical working that day
        List<Doctor> clinicalWorking = new ArrayList<>();
        for (Doctor d : all) {
            if (!safeDoctorType(d).toLowerCase().contains("clinical")) continue;
            String key = d.getId() + "," + ymd;
            List<String> slots = schedule.getOrDefault(key, Collections.emptyList());
            if (!slots.isEmpty()) clinicalWorking.add(d);
        }

        // On-call working
        Set<String> oncallIds = new LinkedHashSet<>(oncall.getOrDefault(date, Collections.emptyList()));
        List<Doctor> oncallWorking = all.stream()
                .filter(d -> oncallIds.contains(d.getId()))
                .collect(Collectors.toList());

        // Show dialog
        JDialog dlg = new JDialog(this, "Roster for " + date.format(DATE_DDMM), true);
        dlg.setLayout(new BorderLayout(6, 6));

        JPanel top = new JPanel(new GridLayout(1, 2, 10, 10));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        top.add(buildRosterPanel("Clinical (slots exist)", clinicalWorking));
        top.add(buildRosterPanel("On-call (shifts)", oncallWorking));

        dlg.add(top, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton close = new JButton("Close");
        close.addActionListener(e -> dlg.dispose());
        south.add(close);
        dlg.add(south, BorderLayout.SOUTH);

        dlg.setSize(760, 420);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void onSearchWeek() {
        String dateInput = dateField.getText().trim();
        if (dateInput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a date (dd/MM/yyyy).");
            return;
        }
        LocalDate any = parseDDMM(dateInput);
        if (any == null) {
            JOptionPane.showMessageDialog(this, "Invalid date. Use dd/MM/yyyy.");
            return;
        }
        LocalDate monday = any.with(DayOfWeek.MONDAY);

        Map<String, List<String>> schedule = FileStorage.readSchedule();
        Map<LocalDate, List<String>> oncall = readOncallRoster();
        List<Doctor> all = readAllDoctorsTolerant();
        Map<String, Doctor> byId = all.stream().collect(Collectors.toMap(Doctor::getId, d -> d, (a,b)->a, LinkedHashMap::new));

        String[] cols = {"Date", "Clinical Doctors", "On-call Doctors (with shifts)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);

        for (int i = 0; i < 7; i++) {
            LocalDate d = monday.plusDays(i);
            String ymd = d.format(DATE_ISO);

            // Clinical names
            List<String> clinicalNames = all.stream()
                    .filter(doc -> safeDoctorType(doc).toLowerCase().contains("clinical"))
                    .filter(doc -> {
                        String key = doc.getId() + "," + ymd;
                        List<String> slots = schedule.getOrDefault(key, Collections.emptyList());
                        return !slots.isEmpty();
                    })
                    .map(Doctor::getName)
                    .sorted()
                    .collect(Collectors.toList());

            // On-call names + shifts
           List<String> oncallIds = oncall.getOrDefault(d, Collections.emptyList());
            List<String> oncallNames = new ArrayList<>();
            for (int j = 0; j < oncallIds.size(); j++) {
                String id = oncallIds.get(j);
                Doctor doc = byId.get(id);
                String name = (doc == null ? id : doc.getName());

                if (j == 0) oncallNames.add(name + " (00:00-08:00)");
                else if (j == 1) oncallNames.add(name + " (08:00-16:00)");
                else oncallNames.add(name + " (16:00-00:00)");
            }

            model.addRow(new Object[]{
                    d.getDayOfWeek().toString().substring(0, 3) + " " + d.format(DATE_DDMM),
                    String.join(", ", clinicalNames),
                    String.join(", ", oncallNames)
            });
        }

        JDialog dlg = new JDialog(this, "Week Roster (" + monday.format(DATE_DDMM) + " - " + monday.plusDays(6).format(DATE_DDMM) + ")", true);
        dlg.setLayout(new BorderLayout(6, 6));
        dlg.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton close = new JButton("Close");
        close.addActionListener(e -> dlg.dispose());
        south.add(close);
        dlg.add(south, BorderLayout.SOUTH);

        dlg.setSize(950, 420);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    /* ========================= Utilities ========================= */

    private void resetForm() {
        dateField.setText("");
        searchField.setText("");
        doctorModel.setRowCount(0);
        slotsModel.setRowCount(0);
        loadDoctors("", "All");
    }

    private List<String> buildSlots(LocalTime start, LocalTime endInclusive, int minutesStep, boolean withLunchBreak) {
        List<String> out = new ArrayList<>();
        LocalTime t = start;
        while (!t.isAfter(endInclusive)) {
            boolean isLunch = withLunchBreak && !t.isBefore(LocalTime.NOON) && t.isBefore(LocalTime.NOON.plusHours(1));
            if (!isLunch) out.add(t.format(TIME_FMT));
            t = t.plusMinutes(minutesStep);
        }
        return out;
    }

    private static LocalDate parseDDMM(String s) {
        try { return LocalDate.parse(s.trim(), DATE_DDMM); } catch (Exception e) { return null; }
    }

    /* ========================= On-call roster I/O ========================= */

    private Map<LocalDate, List<String>> readOncallRoster() {
        Map<LocalDate, List<String>> map = new HashMap<>();
        File f = new File(ONCALL_FILE);
        if (!f.exists()) return map;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim(); if (line.isEmpty()) continue;
                String[] p = line.split(",", 2);
                if (p.length < 1) continue;
                LocalDate d;
                try { d = LocalDate.parse(p[0].trim(), DATE_ISO); } catch (Exception e) { continue; }
                List<String> ids = new ArrayList<>();
                if (p.length == 2) {
                    for (String s : p[1].split(";")) {
                        String id = s.trim();
                        if (!id.isEmpty()) ids.add(id);
                    }
                }
                map.put(d, ids);
            }
        } catch (IOException e) { e.printStackTrace(); }
        return map;
    }

    private void writeOncallRoster(Map<LocalDate, List<String>> map) {
        List<LocalDate> dates = new ArrayList<>(map.keySet());
        Collections.sort(dates);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ONCALL_FILE))) {
            for (int i = 0; i < dates.size(); i++) {
                LocalDate d = dates.get(i);
                List<String> ids = map.getOrDefault(d, Collections.emptyList());
                bw.write(d.format(DATE_ISO) + "," + String.join(";", ids));
                if (i < dates.size() - 1) bw.newLine();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    /* ========================= Doctor reader (tolerant) ========================= */

    private List<Doctor> readAllDoctorsTolerant() {
        List<Doctor> list = new ArrayList<>();
        File f = new File("doctor.txt");
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim(); if (line.isEmpty()) continue;
                String[] p = line.split(",");
                if (p.length >= 8) {
                    String id = p[0].trim();
                    String name = p[1].trim();
                    String gender = p[2].trim();
                    String email = p[3].trim();
                    String phone = p[4].trim();
                    String password = p[5].trim();
                    String specialty = p[6].trim();

                    String type, dob;
                    if (p.length >= 9) {
                        type = p[7].trim();
                        dob = p[8].trim();
                    } else {
                        type = "Clinical";
                        dob = p[7].trim();
                    }
                    list.add(new Doctor(id, name, gender, email, phone, password, specialty, type, dob));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    private void saveSlots() {
        int docRow = doctorTable.getSelectedRow();
        if (docRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a doctor first.");
            return;
        }
        String doctorId = doctorModel.getValueAt(docRow, 0).toString();
        String dateInput = dateField.getText().trim();
        if (dateInput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a date (dd/MM/yyyy).");
            return;
        }
        LocalDate date = parseDDMM(dateInput);
        if (date == null) {
            JOptionPane.showMessageDialog(this, "Invalid date format.");
            return;
        }
        String ymd = date.format(DATE_ISO);

        // Always overwrite with current table content
        Map<String, List<String>> schedule = FileStorage.readSchedule();
        String key = doctorId + "," + ymd;

        List<String> times = new ArrayList<>();
        for (int i = 0; i < slotsModel.getRowCount(); i++) {
            String t = String.valueOf(slotsModel.getValueAt(i, 0)).trim();
            if (!t.isEmpty()) times.add(t);
        }

        // Save new slot list (sorted)
        times.sort((a, b) -> {
            try { return LocalTime.parse(a.split("-")[0], TIME_FMT).compareTo(LocalTime.parse(b.split("-")[0], TIME_FMT)); }
            catch (Exception e) { return a.compareTo(b); }
        });

        schedule.put(key, times);
        FileStorage.writeSchedule(schedule);

        JOptionPane.showMessageDialog(this, "Schedule updated for " + doctorId + " on " + ymd);
    }

    private void removeSelectedSlot() {
        int row = slotsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a slot to remove.");
            return;
        }
        slotsModel.removeRow(row);
    }


    private JPanel buildRosterPanel(String title, List<Doctor> docs) {
        String[] cols = {"Doctor ID", "Name", "Specialty", "Type"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Doctor d : docs) {
            m.addRow(new Object[]{d.getId(), d.getName(), d.getSpecialty(), safeDoctorType(d)});
        }
        JTable t = new JTable(m);
        t.setRowHeight(24);
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createTitledBorder(title));

        JPanel p = new JPanel(new BorderLayout());
        p.add(sp, BorderLayout.CENTER);
        return p;
    }
    
    private void saveScheduleInfo(LocalDate monday, LocalDate sunday) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("schedule_info.txt"))) {
            bw.write("Doctor schedule is updated until week "
                    + monday.format(DATE_DDMM) + " - " + sunday.format(DATE_DDMM));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void openOncallRosterEditor() {
        Map<LocalDate, List<String>> roster = readOncallRoster();
        List<Doctor> allDoctors = readAllDoctorsTolerant();

        String[] cols = {"Date", "Assigned Doctors"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);

        // Load existing roster
        List<LocalDate> dates = new ArrayList<>(roster.keySet());
        Collections.sort(dates);
        for (LocalDate d : dates) {
            List<String> ids = roster.getOrDefault(d, Collections.emptyList());
            List<String> names = new ArrayList<>();

            for (int j = 0; j < ids.size(); j++) {
                String id = ids.get(j);
                Doctor doc = allDoctors.stream()
                        .filter(x -> x.getId().equals(id))
                        .findFirst()
                        .orElse(null);
                String name = (doc == null ? id : doc.getName());

                if (j == 0) names.add(name + " (00:00-08:00)");
                else if (j == 1) names.add(name + " (08:00-16:00)");
                else if (j == 2) names.add(name + " (16:00-00:00)");
            }

            model.addRow(new Object[]{d.format(DATE_DDMM), String.join(", ", names)});
        }


        // Show dialog
        JDialog dlg = new JDialog(this, "Edit On-call Roster", true);
        dlg.setLayout(new BorderLayout(6,6));
        dlg.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton close = new JButton("Close");
        close.addActionListener(ev -> dlg.dispose());
        bottom.add(close);
        dlg.add(bottom, BorderLayout.SOUTH);

        dlg.setSize(600, 400);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private String readScheduleInfo() {
        File f = new File("schedule_info.txt");
        if (!f.exists()) {
            return "Doctor schedule not generated yet.";
        }
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            return br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "Doctor schedule not generated yet.";
        }
    }


}
