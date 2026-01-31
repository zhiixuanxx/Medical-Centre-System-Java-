package controller;

import model.*;
import util.*;

import java.io.*;
import java.util.*;

public class AuthController {

    public static User login(String contact, String passwordInput) {

        for (Customer c : FileStorage.getAllCustomers()) {
            if ((c.getEmail().equalsIgnoreCase(contact) || c.getPhone().equals(contact)) &&
                    c.getPassword().equals(passwordInput)) {
                return c;
            }
        }

        for (Manager m : FileStorage.getAllManagers()) {
            if ((m.getEmail().equalsIgnoreCase(contact) || m.getPhone().equals(contact)) &&
                    m.getPassword().equals(passwordInput)) {
                return m;
            }
        }

        for (Doctor d : FileStorage.getAllDoctors()) {
            if ((d.getEmail().equalsIgnoreCase(contact) || d.getPhone().equals(contact)) &&
                    d.getPassword().equals(passwordInput)) {
                return d;
            }
        }

        for (Staff s : FileStorage.getAllStaffs()) {
            if ((s.getEmail().equalsIgnoreCase(contact) || s.getPhone().equals(contact)) &&
                    s.getPassword().equals(passwordInput)) {
                return s;
            }
        }

        return null;
    }

    public static boolean isValidLoginFormat(String emailOrPhone, String password) {
        boolean validContact = emailOrPhone.matches("^\\d{10,11}$") ||
                               emailOrPhone.matches("^[\\w.-]+@[\\w.-]+\\.(com|apu\\.my)$");
        boolean validPassword = password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{9,}$");

        return validContact && validPassword;
    }
}
