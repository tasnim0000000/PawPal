package com.example.pawpal.util;

public class ValidationUtil {

    public static boolean isValidName(String name) {
        return name != null && name.matches("^[A-Z][a-zA-Z ]*$");
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^\\+8801[3-9]\\d{8}$");
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    public static boolean isPositiveInteger(String value) {
        try {
            return Integer.parseInt(value.trim()) >= 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isPositiveDecimal(String value) {
        try {
            return Double.parseDouble(value.trim()) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
