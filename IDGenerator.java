package util;

import java.io.*;

public class IDGenerator {

    public static String generateID(String role) {
        int last = getLastId(role);
        int newId = last + 1;

        switch (role.toLowerCase()) {
            case "manager": return "M" + String.format("%04d", newId);
            case "customer": return "C" + String.format("%04d", newId);
            case "doctor": return "D" + String.format("%04d", newId);
            case "staff": return "S" + String.format("%04d", newId);
            default: return "U" + System.currentTimeMillis();
        }
    }

    private static int getLastId(String role) {
        String filename = switch (role.toLowerCase()) {
            case "manager" -> "manager.txt";
            case "customer" -> "customer.txt";
            case "doctor" -> "doctor.txt";
            case "staff" -> "staff.txt";
            default -> null;
        };

        if (filename == null) return 0;

        int max = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length > 0) {
                        String id = parts[0].trim();
                        if (id.length() > 1) {
                            String numPart = id.substring(1);
                            int num = Integer.parseInt(numPart);
                            if (num > max) {
                                max = num;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            // file might not exist yet, ignore
        }

        return max;
    }
}
