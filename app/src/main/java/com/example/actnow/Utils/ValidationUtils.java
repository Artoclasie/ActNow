package com.example.actnow.Utils;

import android.util.Patterns;
import java.util.Calendar;
import java.util.regex.Pattern;

public class ValidationUtils {
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MIN_AGE = 14;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern CUSTOM_TAG_PATTERN = Pattern.compile("^@[a-zA-Z0-9_]{2,19}$");

    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            return false;
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }

        return hasLetter && hasDigit && hasSpecial;
    }

    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    public static boolean isValidCustomTag(String tag) {
        return tag != null && CUSTOM_TAG_PATTERN.matcher(tag).matches();
    }

    public static boolean isValidBirthDate(Calendar birthDate) {
        if (birthDate == null) return false;

        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -MIN_AGE);

        return !birthDate.after(minDate);
    }

    public static String getPasswordRequirements() {
        return "Пароль должен содержать минимум " + MIN_PASSWORD_LENGTH + 
               " символов, включая буквы, цифры и специальные символы";
    }

    public static String getUsernameRequirements() {
        return "Имя пользователя должно содержать от 3 до 20 символов, " +
               "может включать буквы, цифры и знак подчеркивания";
    }

    public static String getCustomTagRequirements() {
        return "Тег должен начинаться с @ и содержать от 2 до 19 символов, " +
               "может включать буквы, цифры и знак подчеркивания";
    }

    public static String getAgeRequirements() {
        return "Минимальный возраст для регистрации: " + MIN_AGE + " лет";
    }
} 