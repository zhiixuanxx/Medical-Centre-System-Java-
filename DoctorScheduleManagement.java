package GUI;

import model.Doctor;
import model.Manager;
import util.FileStorage;
import view.OncallRosterEditor;

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

public class DoctorScheduleManagement extends javax.swing.JFrame {
    private Manager loggedInManager;
    
    private static final DateTimeFormatter DATE_DDMM = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    
    private static final String ONCALL_FILE = "oncall.txt";
    
    private DefaultTableModel slotsModel, doctorModel;
   
    public DoctorScheduleManagement(Manager manager) {
        this.loggedInManager = manager;
        initComponents();
        initData();
        loadScheduleNote();
        
        //Customize bg
        getContentPane().setBackground(new java.awt.Color(230, 245, 255));
        jPanel1.setBackground(new java.awt.Color(0, 51, 102));
        jPanel2.setBackground(Color.WHITE);
        
        //Label
        jLabel1.setForeground(Color.WHITE);
        jLabel1.setFont(new Font("Perpetua", Font.PLAIN, 35));
        jLabel2.setFont(new Font("Perpetua", Font.BOLD, 17));
        jLabel5.setFont(new Font("Perpetua", Font.BOLD, 17));
        
        
        //button
        searchButton.setBackground(new java.awt.Color(72, 103, 150)); 
        searchButton.setForeground(Color.WHITE);
        searchButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        searchDayButton.setBackground(new java.awt.Color(72, 103, 150)); 
        searchDayButton.setForeground(Color.WHITE);
        searchDayButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        searchWeekButton.setBackground(new java.awt.Color(72, 103, 150)); 
        searchWeekButton.setForeground(Color.WHITE);
        searchWeekButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        autoGenerateSelectedButton.setBackground(new java.awt.Color(72, 103, 150)); 
        autoGenerateSelectedButton.setForeground(Color.WHITE);
        autoGenerateSelectedButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        autoGenerateAllButton.setBackground(new java.awt.Color(72, 103, 150)); 
        autoGenerateAllButton.setForeground(Color.WHITE);
        autoGenerateAllButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        editOnCallRosterButton.setBackground(new java.awt.Color(72, 103, 150)); 
        editOnCallRosterButton.setForeground(Color.WHITE);
        editOnCallRosterButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        loadSlotsButton.setBackground(new java.awt.Color(72, 103, 150)); 
        loadSlotsButton.setForeground(Color.WHITE);
        loadSlotsButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        removeSelectedButton.setBackground(new java.awt.Color(72, 103, 150)); 
        removeSelectedButton.setForeground(Color.WHITE);
        removeSelectedButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        saveButton.setBackground(new java.awt.Color(72, 103, 150)); 
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        resetBtn.setBackground(new java.awt.Color(0, 51, 102));
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setFont(new Font("Perpetua", Font.BOLD, 15));
        backButton.setBackground(new java.awt.Color(0, 51, 102)); 
        backButton.setForeground(Color.WHITE);
        backButton.setFont(new Font("Perpetua", Font.BOLD, 15));
        
    }
    
    
    private void loadScheduleNote() {
        File file = new File("schedule_info.txt");
        if (!file.exists()) {
            scheduleNoteLabel.setText("Doctor schedule is not generated yet.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String lastLine = null, line;
            while ((line = br.readLine()) != null) {
                lastLine = line;
            }
            if (lastLine != null && !lastLine.trim().isEmpty()) {
                scheduleNoteLabel.setText(lastLine.trim());
            } else {
                scheduleNoteLabel.setText("Doctor schedule is not generated yet.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            scheduleNoteLabel.setText("Error loading schedule info.");
        }
    }

    private LocalDate parseDate(String s) {
        try {
            return LocalDate.parse(s, DATE_DDMM);
        } catch (Exception e1) {
            try {
                return LocalDate.parse(s, DATE_DDMM);
            } catch (Exception e2) {
                throw new RuntimeException("Invalid date format in oncall.txt: " + s);
            }
        }
    }
     private void initData() {
        typeFilter.setModel(new DefaultComboBoxModel<>(new String[]{"All", "Clinical", "On-call"}));
        refreshDoctorTable(readAllDoctorsTolerant());
    }

    private void refreshDoctorTable(List<Doctor> doctors) {
    DefaultTableModel model = (DefaultTableModel) doctorTable.getModel();
    model.setRowCount(0);
    
    for (Doctor doctor : doctors) {
        model.addRow(new Object[]{
            doctor.getId(),
            doctor.getName(),
            doctor.getSpecialty(),
            doctor.getDoctorType() != null ? doctor.getDoctorType() : "Clinical" // Default to Clinical if null
        });
      }
    }

    private void refreshSlotsTable(List<LocalTime> slots) {
        DefaultTableModel model = (DefaultTableModel) slotsTable.getModel();
        model.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        for (LocalTime t : slots) {
            model.addRow(new Object[]{t.format(fmt)});
        }
    }

    private static LocalDate parseDDMM(String s) {
        try {
            return LocalDate.parse(s.trim(), DATE_DDMM);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        typeFilter = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        searchTextField = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        doctorTable = new javax.swing.JTable();
        searchButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        slotsTable = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        dateTextField = new javax.swing.JTextField();
        autoGenerateAllButton = new javax.swing.JButton();
        autoGenerateSelectedButton = new javax.swing.JButton();
        searchWeekButton = new javax.swing.JButton();
        searchDayButton = new javax.swing.JButton();
        editOnCallRosterButton = new javax.swing.JButton();
        loadSlotsButton = new javax.swing.JButton();
        removeSelectedButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        backButton = new javax.swing.JButton();
        scheduleNoteLabel = new javax.swing.JLabel();
        resetBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Doctor Schedule ");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 428, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(160, 160, 160))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(47, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(40, 40, 40))
        );

        jLabel2.setText("Search Doctor");

        jLabel3.setText("Type:");

        typeFilter.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        typeFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeFilterActionPerformed(evt);
            }
        });

        jLabel4.setText("ID/Name:");

        searchTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchTextFieldActionPerformed(evt);
            }
        });

        doctorTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Doctor ID", "Name", "Speciality", "Type"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(doctorTable);

        searchButton.setText("Search");
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(14, 14, 14)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(40, 40, 40)
                                        .addComponent(typeFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel2)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(searchTextField))))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(42, 42, 42)
                                .addComponent(searchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 23, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(typeFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(searchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(searchButton)
                .addGap(13, 13, 13)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(195, Short.MAX_VALUE))
        );

        slotsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "Time"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane2.setViewportView(slotsTable);

        jLabel5.setText("Time Slots (Selected Doctor & Date)");

        jLabel6.setText("Date (DD/MM/YYYY):");

        dateTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dateTextFieldActionPerformed(evt);
            }
        });

        autoGenerateAllButton.setText("AutoGenerate Week (All)");
        autoGenerateAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoGenerateAllButtonActionPerformed(evt);
            }
        });

        autoGenerateSelectedButton.setText("AutoGenerate Week (Selected)");
        autoGenerateSelectedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoGenerateSelectedButtonActionPerformed(evt);
            }
        });

        searchWeekButton.setText("Search Week");
        searchWeekButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchWeekButtonActionPerformed(evt);
            }
        });

        searchDayButton.setText("Search Day");
        searchDayButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchDayButtonActionPerformed(evt);
            }
        });

        editOnCallRosterButton.setText("Edit On-Call Roster");
        editOnCallRosterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editOnCallRosterButtonActionPerformed(evt);
            }
        });

        loadSlotsButton.setText("Load Slots");
        loadSlotsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadSlotsButtonActionPerformed(evt);
            }
        });

        removeSelectedButton.setText("Remove Selected");
        removeSelectedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSelectedButtonActionPerformed(evt);
            }
        });

        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        backButton.setText("Back");
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });

        resetBtn.setText("Reset");
        resetBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dateTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(45, 45, 45)
                                .addComponent(searchDayButton, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(34, 34, 34)
                                .addComponent(searchWeekButton, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(30, 30, 30))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addComponent(jScrollPane2)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(scheduleNoteLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                .addComponent(autoGenerateSelectedButton)
                                                .addGap(40, 40, 40)
                                                .addComponent(autoGenerateAllButton)))
                                        .addGap(67, 67, 67)
                                        .addComponent(editOnCallRosterButton)))
                                .addContainerGap())))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(53, 53, 53)
                        .addComponent(loadSlotsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(58, 58, 58)
                        .addComponent(removeSelectedButton, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(67, 67, 67)
                        .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(resetBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(94, 94, 94)
                        .addComponent(backButton, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(262, 262, 262))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(dateTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(searchDayButton)
                            .addComponent(searchWeekButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scheduleNoteLabel)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(autoGenerateSelectedButton)
                            .addComponent(autoGenerateAllButton)
                            .addComponent(editOnCallRosterButton))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel5)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(loadSlotsButton)
                            .addComponent(removeSelectedButton)
                            .addComponent(saveButton))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(backButton)
                            .addComponent(resetBtn))
                        .addGap(41, 41, 41))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void autoGenerateAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoGenerateAllButtonActionPerformed
        onAutogenerateWeek();
    }//GEN-LAST:event_autoGenerateAllButtonActionPerformed

    private void searchDayButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchDayButtonActionPerformed
        onSearchDay();
    }//GEN-LAST:event_searchDayButtonActionPerformed

    private void typeFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeFilterActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_typeFilterActionPerformed

    private void searchTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_searchTextFieldActionPerformed

    private void dateTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dateTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_dateTextFieldActionPerformed

    private void searchWeekButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchWeekButtonActionPerformed
        onSearchWeek();
    }//GEN-LAST:event_searchWeekButtonActionPerformed

    private void autoGenerateSelectedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoGenerateSelectedButtonActionPerformed
        onAutogenerateWeekSelected();
    }//GEN-LAST:event_autoGenerateSelectedButtonActionPerformed

    private void editOnCallRosterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editOnCallRosterButtonActionPerformed
        String dateInput = dateTextField.getText().trim();
        if (dateInput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a date (dd/MM/yyyy) first.");
            return;
        }
        LocalDate date = parseDDMM(dateInput);
        if (date == null) {
            JOptionPane.showMessageDialog(this, "Invalid date format.");
            return;
        }
        LocalDate monday = date.with(DayOfWeek.MONDAY);
        List<Doctor> oncallDoctors = readAllDoctorsTolerant().stream()
                .filter(d -> safeDoctorType(d).equalsIgnoreCase("On-call"))
                .collect(Collectors.toList());
        if (oncallDoctors.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No on-call doctors found.");
            return;
        }
        new OncallRosterEditor(monday, oncallDoctors);
    
    }//GEN-LAST:event_editOnCallRosterButtonActionPerformed

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
    String searchText = searchTextField.getText().trim();
        String selectedType = (String) typeFilter.getSelectedItem();
        List<Doctor> allDoctors = readAllDoctorsTolerant();
        List<Doctor> filtered = new ArrayList<>();
        for (Doctor doctor : allDoctors) {
            boolean matchesType = selectedType.equals("All") ||
                safeDoctorType(doctor).toLowerCase().contains(selectedType.toLowerCase());
            boolean matchesSearch = searchText.isEmpty() ||
                    doctor.getId().toLowerCase().contains(searchText.toLowerCase()) ||
                    doctor.getName().toLowerCase().contains(searchText.toLowerCase());
            if (matchesType && matchesSearch) filtered.add(doctor);
        }
        refreshDoctorTable(filtered);
        if (filtered.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No doctors found.");
        }
    }//GEN-LAST:event_searchButtonActionPerformed

    private void loadSlotsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadSlotsButtonActionPerformed
    loadExistingSlots();
    
    }//GEN-LAST:event_loadSlotsButtonActionPerformed

    private void removeSelectedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSelectedButtonActionPerformed
    removeSelectedSlot();
    }//GEN-LAST:event_removeSelectedButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
    saveSlots();
    }//GEN-LAST:event_saveButtonActionPerformed

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
        dispose();
        new ManagerDashboard(loggedInManager).setVisible(true);
    }//GEN-LAST:event_backButtonActionPerformed

    private void resetBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetBtnActionPerformed
        resetForm(); 
    }//GEN-LAST:event_resetBtnActionPerformed

    private void onAutogenerateWeek() {
        String dateInput = dateTextField.getText().trim();
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
                String key = doc.getId() + "," + d.format(DATE_DDMM);
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
                String key = doc.getId() + "," + d.format(DATE_DDMM);
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

                String key = doc.getId() + "," + d.format(DATE_DDMM);
                schedule.putIfAbsent(key, new ArrayList<>(daySlots));
            }
        }

        // === On-call doctors (emergency, 3 shifts per day) ===
        Map<LocalDate, List<String>> oncallRoster = readOncallRoster();
        if (!oncall.isEmpty()) {
            int needPerShift = 3; // enforce 3 doctors per shift
            int totalShifts = 3;

            int seed = monday.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear())
                    % Math.max(1, oncall.size());

            for (LocalDate d = monday; !d.isAfter(sunday); d = d.plusDays(1)) {
                List<String> dailyAssigned = new ArrayList<>();

                // Assign 3 shifts
                for (int shift = 0; shift < totalShifts; shift++) {
                List<String> assigned = pickRandomDoctors(oncall, needPerShift);

                //Ensure exactly 3 doctors per shift
                while (assigned.size() < needPerShift) {
                    List<String> extra = pickRandomDoctors(oncall, needPerShift - assigned.size());
                    for (String id : extra) {
                        if (!assigned.contains(id)) {
                            assigned.add(id);
                        }
                        if (assigned.size() == needPerShift) break;
                    }
                }

                dailyAssigned.addAll(assigned);

                for (String docId : assigned) {
                    String key = docId + "," + d.format(DATE_DDMM);

                    if (!schedule.containsKey(key)) {
                        List<String> shifts = new ArrayList<>();
                        if (shift == 0) shifts.add("00:00-08:00");
                        else if (shift == 1) shifts.add("08:00-16:00");
                        else shifts.add("16:00-00:00");

                        schedule.put(key, shifts);
                    }
                }
            }


                oncallRoster.put(d, dailyAssigned);
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

    private void onAutogenerateWeekSelected() {
        int row = doctorTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a doctor first.");
            return;
        }

        DefaultTableModel doctorModel = (DefaultTableModel) doctorTable.getModel();
        String doctorId = doctorModel.getValueAt(row, 0).toString();
        String doctorType = doctorModel.getValueAt(row, 3).toString();

        String dateInput = dateTextField.getText().trim();
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

        // === Clinical doctor ===
        if (doctorType.toLowerCase().contains("clinical")) {
            boolean skipThisDoctor = false;

            // check if already exists or partially booked
            for (LocalDate d = monday; !d.isAfter(sunday); d = d.plusDays(1)) {
                if (d.getDayOfWeek() == DayOfWeek.SUNDAY) continue;
                String key = doctorId + "," + d.format(DATE_DDMM);
                if (schedule.containsKey(key) && !schedule.get(key).isEmpty()) {
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
                JOptionPane.showMessageDialog(this,
                    "Doctor " + doctorId + " already has bookings this week. Skipping.");
                return;
            }

            // generate slots
            for (LocalDate d = monday; !d.isAfter(sunday); d = d.plusDays(1)) {
                if (d.getDayOfWeek() == DayOfWeek.SUNDAY) continue;
                boolean isSat = d.getDayOfWeek() == DayOfWeek.SATURDAY;
                List<String> daySlots = isSat
                        ? buildSlots(LocalTime.of(8, 0), LocalTime.of(14, 30), 30, true)
                        : buildSlots(LocalTime.of(8, 0), LocalTime.of(17, 30), 30, true);

                String key = doctorId + "," + d.format(DATE_DDMM);
                schedule.putIfAbsent(key, new ArrayList<>(daySlots));
            }

        } else { 
            // === On-call doctor (emergency) ===
            Map<LocalDate, List<String>> oncallRoster = readOncallRoster();
            int totalShifts = 3;

            for (LocalDate d = monday; !d.isAfter(sunday); d = d.plusDays(1)) {
                for (int shift = 0; shift < totalShifts; shift++) {
                    String key = doctorId + "," + d.format(DATE_DDMM);
                    if (!schedule.containsKey(key)) {
                        List<String> shifts = new ArrayList<>();
                        if (shift == 0) shifts.add("00:00-08:00");
                        else if (shift == 1) shifts.add("08:00-16:00");
                        else shifts.add("16:00-00:00");

                        schedule.put(key, shifts);

                        List<String> assigned = oncallRoster.getOrDefault(d, new ArrayList<>());
                        assigned.add(doctorId);
                        oncallRoster.put(d, assigned);
                    }
                }
            }
            writeOncallRoster(oncallRoster);
        }

        FileStorage.writeSchedule(schedule);
        saveScheduleInfo(monday, sunday);

        JOptionPane.showMessageDialog(this,
            "Week " + monday.format(DATE_DDMM) + " - " + sunday.format(DATE_DDMM) +
            " generated successfully for doctor " + doctorId +
            ".\nIf the doctor already had bookings, generation was skipped.");
        scheduleNoteLabel.setText("Doctor schedule is updated until week " +
            monday.format(DATE_DDMM) + " - " + sunday.format(DATE_DDMM));
    }

    private void onSearchDay() {
        String dateInput = dateTextField.getText().trim();
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

        String ymd = date.format(DATE_DDMM);

        // Clinical
        String[] clinicalCols = {"Doctor ID", "Name", "Specialty", "Type", "Duty Time"};
        DefaultTableModel clinicalModel = new DefaultTableModel(clinicalCols, 0);
        for (Doctor d : all) {
            if (!safeDoctorType(d).equalsIgnoreCase("Clinical")) continue;
            String key = d.getId() + "," + ymd;
            List<String> slots = schedule.getOrDefault(key, Collections.emptyList());
            if (!slots.isEmpty()) {
                String dutyTime = slots.get(0) + " - " + slots.get(slots.size() - 1);
                clinicalModel.addRow(new Object[]{d.getId(), d.getName(), d.getSpecialty(), "Clinical", dutyTime});
            }
        }

        JTable clinicalTable = new JTable(clinicalModel);
        JScrollPane clinicalPane = new JScrollPane(clinicalTable);
        clinicalPane.setBorder(BorderFactory.createTitledBorder("Clinical (slots exist)"));

        // On-call
        String[] oncallCols = {"Doctor ID", "Name", "Specialty", "Type", "Duty Time"};
        DefaultTableModel oncallModel = new DefaultTableModel(oncallCols, 0);
        List<String> assignedIds = oncall.getOrDefault(date, Collections.emptyList());
        for (int i = 0; i < assignedIds.size(); i++) {
        String docId = assignedIds.get(i);
        Doctor d = all.stream().filter(dd -> dd.getId().equals(docId)).findFirst().orElse(null);
        if (d == null) continue;

        String shift;
        if (i < 3) shift = "00:00-08:00";        // first 3 doctors
        else if (i < 6) shift = "08:00-16:00";   // next 3 doctors
        else shift = "16:00-00:00";              // last 3 doctors

        oncallModel.addRow(new Object[]{d.getId(), d.getName(), d.getSpecialty(), "On-call", shift});
    }

        JTable oncallTable = new JTable(oncallModel);
        JScrollPane oncallPane = new JScrollPane(oncallTable);
        oncallPane.setBorder(BorderFactory.createTitledBorder("On-call (shifts)"));

        // Dialog
        JDialog dlg = new JDialog(this, "Roster for " + date.format(DATE_DDMM), true);
        dlg.setLayout(new GridLayout(1, 2, 10, 10));
        dlg.add(clinicalPane);
        dlg.add(oncallPane);

        dlg.setSize(900, 400);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }


    private void onSearchWeek() {
        String dateInput = dateTextField.getText().trim();
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
        Map<String, Doctor> byId = all.stream().collect(Collectors.toMap(Doctor::getId, d -> d));

        // Now 5 columns
        String[] cols = {"Date", "Clinical Doctors (Duty Time)",
                         "On-call (00:00-08:00)", "On-call (08:00-16:00)", "On-call (16:00-00:00)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setPreferredWidth(120);  // Date
        table.getColumnModel().getColumn(1).setPreferredWidth(1000);  // Clinical
        table.getColumnModel().getColumn(2).setPreferredWidth(250);  // On-call Morning
        table.getColumnModel().getColumn(3).setPreferredWidth(250);  // On-call Afternoon
        table.getColumnModel().getColumn(4).setPreferredWidth(250);  // On-call Night

        for (int i = 0; i < 7; i++) {
            LocalDate d = monday.plusDays(i);
            String ymd = d.format(DATE_DDMM);

            // Clinical
            List<String> clinicalRows = new ArrayList<>();
            for (Doctor doc : all) {
                if (!safeDoctorType(doc).equalsIgnoreCase("Clinical")) continue;
                String key = doc.getId() + "," + ymd;
                List<String> slots = schedule.getOrDefault(key, Collections.emptyList());
                if (!slots.isEmpty()) {
                    String duty = slots.get(0) + " - " + slots.get(slots.size() - 1);
                    clinicalRows.add(doc.getName() + " (" + duty + ")");
                }
            }

            // On-call split by shift
            List<String> oncallIds = oncall.getOrDefault(d, Collections.emptyList());
            String morning = "", afternoon = "", night = "";

            for (int j = 0; j < oncallIds.size(); j++) {
                Doctor doc = byId.get(oncallIds.get(j));
                if (doc == null) continue;

                if (j < 3) morning += doc.getName() + ", ";
                else if (j < 6) afternoon += doc.getName() + ", ";
                else night += doc.getName() + ", ";
            }

            if (morning.endsWith(", ")) morning = morning.substring(0, morning.length() - 2);
            if (afternoon.endsWith(", ")) afternoon = afternoon.substring(0, afternoon.length() - 2);
            if (night.endsWith(", ")) night = night.substring(0, night.length() - 2);

            model.addRow(new Object[]{
                d.getDayOfWeek().toString().substring(0, 3) + " " + d.format(DATE_DDMM),
                String.join(", ", clinicalRows),
                morning, afternoon, night
            });
        }

        JDialog dlg = new JDialog(this,
                "Week Roster (" + monday.format(DATE_DDMM) + " - " + monday.plusDays(6).format(DATE_DDMM) + ")",
                true);
        dlg.setLayout(new BorderLayout(6, 6));
        JScrollPane tableScroll = new JScrollPane(
                table,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        dlg.add(tableScroll, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton close = new JButton("Close");
        close.addActionListener(e -> dlg.dispose());
        south.add(close);
        dlg.add(south, BorderLayout.SOUTH);

        dlg.setSize(1200, 350);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }



    private void loadExistingSlots() {
        int row = doctorTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a doctor first.");
            return;
        }
        DefaultTableModel model = (DefaultTableModel) doctorTable.getModel();
        String doctorId = model.getValueAt(row, 0).toString();
        String type = model.getValueAt(row, 3).toString();
        String dateInput = dateTextField.getText().trim();
        if (dateInput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a date (dd/MM/yyyy).");
            return;
        }
        LocalDate date = parseDDMM(dateInput);
        if (date == null) {
            JOptionPane.showMessageDialog(this, "Invalid date format.");
            return;
        }
        String ymd = date.format(DATE_DDMM);

        DefaultTableModel slotsModel = (DefaultTableModel) slotsTable.getModel();
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

    private void saveSlots() {
        int docRow = doctorTable.getSelectedRow();
        if (docRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a doctor first.");
            return;
        }

        DefaultTableModel doctorModel = (DefaultTableModel) doctorTable.getModel();
        String doctorId = doctorModel.getValueAt(docRow, 0).toString();

        String dateInput = dateTextField.getText().trim();
        if (dateInput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a date (dd/MM/yyyy).");
            return;
        }
        LocalDate date = parseDDMM(dateInput);
        if (date == null) {
            JOptionPane.showMessageDialog(this, "Invalid date format.");
            return;
        }
        String ymd = date.format(DATE_DDMM);

        // Always overwrite with current table content
        Map<String, List<String>> schedule = FileStorage.readSchedule();
        String key = doctorId + "," + ymd;

        DefaultTableModel slotsModel = (DefaultTableModel) slotsTable.getModel();
        List<String> times = new ArrayList<>();
        for (int i = 0; i < slotsModel.getRowCount(); i++) {
            String t = String.valueOf(slotsModel.getValueAt(i, 0)).trim();
            if (!t.isEmpty()) times.add(t);
        }

        // Sort by time
        times.sort((a, b) -> {
            try { 
                return LocalTime.parse(a.split("-")[0], TIME_FMT)
                        .compareTo(LocalTime.parse(b.split("-")[0], TIME_FMT)); 
            } catch (Exception e) { 
                return a.compareTo(b); 
            }
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

        // Remove from UI
        DefaultTableModel slotsModel = (DefaultTableModel) slotsTable.getModel();
        String removedSlot = String.valueOf(slotsModel.getValueAt(row, 0));
        slotsModel.removeRow(row);

        // === Also remove from schedule.txt ===
        int docRow = doctorTable.getSelectedRow();
        if (docRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a doctor first.");
            return;
        }

        DefaultTableModel doctorModel = (DefaultTableModel) doctorTable.getModel();
        String doctorId = doctorModel.getValueAt(docRow, 0).toString();

        String dateInput = dateTextField.getText().trim();
        if (dateInput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a date (dd/MM/yyyy).");
            return;
        }
        LocalDate date = parseDDMM(dateInput);
        if (date == null) {
            JOptionPane.showMessageDialog(this, "Invalid date format.");
            return;
        }
        String ymd = date.format(DATE_DDMM);

        // Update schedule map
        Map<String, List<String>> schedule = FileStorage.readSchedule();
        String key = doctorId + "," + ymd;

        if (schedule.containsKey(key)) {
            List<String> slots = schedule.get(key);
            slots.remove(removedSlot); // remove that time slot
            schedule.put(key, slots);
            FileStorage.writeSchedule(schedule);
        }

        JOptionPane.showMessageDialog(this,
                "Removed slot '" + removedSlot + "' for Doctor " + doctorId + " on " + ymd);
    }

    // === Utility ===
    
    
    private List<Doctor> readAllDoctorsTolerant() {
        List<Doctor> list = new ArrayList<>();
        File f = new File("doctor.txt");
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split(",");
                if (p.length >= 8) {
                    String id = p[0].trim();
                    String name = p[1].trim();
                    String gender = p[2].trim();
                    String email = p[3].trim();
                    String phone = p[4].trim();
                    String password = p[5].trim();
                    String specialty = p[6].trim();
                    String type = (p.length >= 9) ? p[7].trim() : "Clinical";
                    String dob = (p.length >= 9) ? p[8].trim() : "";
                    list.add(new Doctor(id, name, gender, email, phone, password, specialty, type, dob));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton autoGenerateAllButton;
    private javax.swing.JButton autoGenerateSelectedButton;
    private javax.swing.JButton backButton;
    private javax.swing.JTextField dateTextField;
    private javax.swing.JTable doctorTable;
    private javax.swing.JButton editOnCallRosterButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton loadSlotsButton;
    private javax.swing.JButton removeSelectedButton;
    private javax.swing.JButton resetBtn;
    private javax.swing.JButton saveButton;
    private javax.swing.JLabel scheduleNoteLabel;
    private javax.swing.JButton searchButton;
    private javax.swing.JButton searchDayButton;
    private javax.swing.JTextField searchTextField;
    private javax.swing.JButton searchWeekButton;
    private javax.swing.JTable slotsTable;
    private javax.swing.JComboBox<String> typeFilter;
    // End of variables declaration//GEN-END:variables

    private void resetForm() {
        dateTextField.setText("");
        searchTextField.setText("");
        typeFilter.setSelectedItem("All"); // reset combo box

        DefaultTableModel doctorModel = (DefaultTableModel) doctorTable.getModel();
        doctorModel.setRowCount(0);

        DefaultTableModel slotsModel = (DefaultTableModel) slotsTable.getModel();
        slotsModel.setRowCount(0);

        loadDoctors("", "All");
    }

    private void loadDoctors(String filter, String typeFilterValue) {
        DefaultTableModel doctorModel = (DefaultTableModel) doctorTable.getModel();
        doctorModel.setRowCount(0);

        for (Doctor d : readAllDoctorsTolerant()) {
            boolean typeMatch = typeFilterValue.equals("All")
                    || safeDoctorType(d).toLowerCase().contains(typeFilterValue.toLowerCase());
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
            if (t.toLowerCase().contains("clinical")) return "Clinical";
            if (t.toLowerCase().contains("on-call")) return "On-call";
            return t;
        } catch (Throwable ignored) {
            return "Clinical";
        }
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
                try { d = LocalDate.parse(p[0].trim(), DATE_DDMM); } catch (Exception e) { continue; }
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
    
    private List<String> pickRandomDoctors(List<Doctor> docs, int k) {
        if (docs.isEmpty() || k <= 0) return Collections.emptyList();
        List<Doctor> shuffled = new ArrayList<>(docs);
        Collections.shuffle(shuffled, new Random());
        return shuffled.stream()
                .limit(k)
                .map(Doctor::getId)
                .collect(Collectors.toList());
    }

    private void writeOncallRoster(Map<LocalDate, List<String>> oncallRoster) {
        List<LocalDate> dates = new ArrayList<>(oncallRoster.keySet());
        Collections.sort(dates);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ONCALL_FILE))) {
            for (int i = 0; i < dates.size(); i++) {
                LocalDate d = dates.get(i);
                List<String> ids = oncallRoster.getOrDefault(d, Collections.emptyList());
                bw.write(d.format(DATE_DDMM) + "," + String.join(";", ids));
                if (i < dates.size() - 1) bw.newLine();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void saveScheduleInfo(LocalDate monday, LocalDate sunday) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("schedule_info.txt"))) {
            bw.write("Doctor schedule is updated until week "
                    + monday.format(DATE_DDMM) + " - " + sunday.format(DATE_DDMM));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
