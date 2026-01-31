package view;

import model.Doctor;
import util.FileStorage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class OncallRosterEditor extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private JButton saveBtn, cancelBtn;
    private static final DateTimeFormatter DATE_DDMM = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private static final String ONCALL_FILE = "oncall.txt";

    private List<Doctor> oncallDoctors;
    private LocalDate monday;

    public OncallRosterEditor(LocalDate monday, List<Doctor> oncallDoctors) {
        this.monday = monday;
        this.oncallDoctors = oncallDoctors;

        setTitle("On-call Roster Editor (Week of " + monday.format(DATE_DDMM) + ")");
        setSize(950, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Table
        String[] cols = {"Date", "First Shift (00:00-08:00)", "Second Shift (08:00-16:00)", "Shift 3 (16:00-00:00)"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c > 0; }
            public Class<?> getColumnClass(int c) { return List.class; }
        };
        table = new JTable(model);
        table.setRowHeight(60);

        // Read existing roster
        Map<LocalDate, List<String>> existingRoster = readOncallRoster();

        // Populate rows for each day in the week
        for (int i = 0; i < 7; i++) {
            LocalDate d = monday.plusDays(i);
            List<String> ids = existingRoster.getOrDefault(d, new ArrayList<>());

            List<String> shift1 = new ArrayList<>();
            List<String> shift2 = new ArrayList<>();
            List<String> shift3 = new ArrayList<>();

            for (int j = 0; j < ids.size(); j++) {
                if (j < 3) shift1.add(ids.get(j));
                else if (j < 6) shift2.add(ids.get(j));
                else shift3.add(ids.get(j));
            }

            model.addRow(new Object[]{
                    d.getDayOfWeek() + " " + d.format(DATE_DDMM),
                    shift1, shift2, shift3
            });
        }

        // Attach checkbox editors to shift columns
        for (int col = 1; col <= 3; col++) {
            TableColumn column = table.getColumnModel().getColumn(col);
            column.setCellEditor(new CheckboxListEditor(oncallDoctors));
        }
        JScrollPane sp = new JScrollPane(table);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveBtn = new JButton("Save Roster");
        cancelBtn = new JButton("Cancel");
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        add(sp, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        // Events
        saveBtn.addActionListener(e -> saveRoster());
        cancelBtn.addActionListener(e -> dispose());

        setVisible(true);
    }

    // === Custom editor for checkboxes ===
    private static class CheckboxListEditor extends AbstractCellEditor implements TableCellEditor {
        private JScrollPane scrollPane;
        private JPanel panel;
        private List<JCheckBox> checkBoxes;
        private List<Doctor> doctors;

        public CheckboxListEditor(List<Doctor> doctors) {
            this.doctors = doctors;

            // Vertical BoxLayout
            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            checkBoxes = new ArrayList<>();
            for (Doctor d : doctors) {
                JCheckBox cb = new JCheckBox(d.getId() + " - " + d.getName());
                checkBoxes.add(cb);
                panel.add(cb);
            }

            // Wrap in scroll pane
            scrollPane = new JScrollPane(panel);
            scrollPane.setPreferredSize(new Dimension(200, 150)); // adjust size if needed
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        }

        @Override
        public Object getCellEditorValue() {
            return checkBoxes.stream()
                    .filter(JCheckBox::isSelected)
                    .map(cb -> cb.getText().split(" - ")[0]) // only store ID
                    .toList();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                             boolean isSelected, int row, int column) {
            panel.removeAll();   // clear old checkboxes
            checkBoxes.clear();

            for (Doctor d : doctors) {
                JCheckBox cb = new JCheckBox(d.getId() + " - " + d.getName());
                checkBoxes.add(cb);
                panel.add(cb);
            }

            if (value instanceof List<?>) {
                List<?> selected = (List<?>) value;
                for (Object o : selected) {
                    String id = String.valueOf(o);
                    for (JCheckBox cb : checkBoxes) {
                        if (cb.getText().startsWith(id)) cb.setSelected(true);
                    }
                }
            }

            panel.revalidate();
            panel.repaint();
            return scrollPane;
        }
    }

    private void saveRoster() {
        
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        
        Map<LocalDate, List<String>> roster = readOncallRoster();

        for (int i = 0; i < model.getRowCount(); i++) {
            LocalDate d = monday.plusDays(i);

            List<String> shift1Ids = (List<String>) model.getValueAt(i, 1);
            List<String> shift2Ids = (List<String>) model.getValueAt(i, 2);
            List<String> shift3Ids = (List<String>) model.getValueAt(i, 3);

            // validation: minimum 3 doctors per shift
            if (shift1Ids.size() < 3 || shift2Ids.size() < 3 || shift3Ids.size() < 3) {
                JOptionPane.showMessageDialog(this,
                        "Each shift must have at least 3 doctors!\nProblem at " + d.format(DATE_DDMM));
                return;
            }

            List<String> ids = new ArrayList<>();
            ids.addAll(shift1Ids);
            ids.addAll(shift2Ids);
            ids.addAll(shift3Ids);

            roster.put(d, ids);

            // update schedule.txt with shifts
            Map<String, List<String>> schedule = FileStorage.readSchedule();
            for (String id : shift1Ids) {
                schedule.put(id + "," + d.format(DATE_DDMM), Collections.singletonList("00:00-08:00"));
            }
            for (String id : shift2Ids) {
                schedule.put(id + "," + d.format(DATE_DDMM), Collections.singletonList("08:00-16:00"));
            }
            for (String id : shift3Ids) {
                schedule.put(id + "," + d.format(DATE_DDMM), Collections.singletonList("16:00-00:00"));
            }
            FileStorage.writeSchedule(schedule);
        }

        writeOncallRoster(roster);
        JOptionPane.showMessageDialog(this, "Roster saved successfully!");
   
    }

    private Map<LocalDate, List<String>> readOncallRoster() {
        Map<LocalDate, List<String>> map = new HashMap<>();
        File f = new File(ONCALL_FILE);
        if (!f.exists()) return map;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split(",", 2);
                LocalDate d = LocalDate.parse(p[0].trim(), DATE_DDMM);
                List<String> ids = new ArrayList<>();
                if (p.length == 2) {
                    for (String s : p[1].split(";")) {
                        String id = s.trim();
                        if (!id.isEmpty()) ids.add(id);
                    }
                }
                map.put(d, ids);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    private void writeOncallRoster(Map<LocalDate, List<String>> map) {
        List<LocalDate> dates = new ArrayList<>(map.keySet());
        Collections.sort(dates);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ONCALL_FILE))) {
            for (int i = 0; i < dates.size(); i++) {
                LocalDate d = dates.get(i);
                List<String> ids = map.getOrDefault(d, Collections.emptyList());
                bw.write(d.format(DATE_DDMM) + "," + String.join(";", ids));
                if (i < dates.size() - 1) bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
