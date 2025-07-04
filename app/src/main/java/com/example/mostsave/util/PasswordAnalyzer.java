package com.example.mostsave.util;

import com.example.mostsave.data.model.Password;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for analyzing passwords and determining their strength.
 */
public class PasswordAnalyzer {

    // Constants for password strength thresholds
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int STRONG_PASSWORD_LENGTH = 12;

    // Constants for password update recommendation (in days)
    private static final long PASSWORD_UPDATE_DAYS = 180; // 6 months

    /**
     * Enum representing password strength levels
     */
    public enum PasswordStrength {
        WEAK,
        MEDIUM,
        STRONG
    }

    /**
     * Analyzes a password and returns its strength
     * @param password The password to analyze
     * @return The strength level of the password
     */
    public static PasswordStrength getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return PasswordStrength.WEAK;
        }

        // Check length
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return PasswordStrength.WEAK;
        }

        // Check complexity
        boolean hasLowerCase = false;
        boolean hasUpperCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;

        for (char c : password.toCharArray()) {
            if (Character.isLowerCase(c)) hasLowerCase = true;
            else if (Character.isUpperCase(c)) hasUpperCase = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecialChar = true;
        }

        // Calculate complexity score (0-4)
        int complexityScore = 0;
        if (hasLowerCase) complexityScore++;
        if (hasUpperCase) complexityScore++;
        if (hasDigit) complexityScore++;
        if (hasSpecialChar) complexityScore++;

        // Determine strength based on length and complexity
        if (password.length() >= STRONG_PASSWORD_LENGTH && complexityScore >= 3) {
            return PasswordStrength.STRONG;
        } else if (password.length() >= MIN_PASSWORD_LENGTH && complexityScore >= 2) {
            return PasswordStrength.MEDIUM;
        } else {
            return PasswordStrength.WEAK;
        }
    }

    /**
     * Checks if a password needs to be updated (older than 6 months)
     * @param lastUpdated Timestamp when the password was last updated
     * @return True if the password needs to be updated
     */
    public static boolean needsUpdate(long lastUpdated) {
        long currentTime = System.currentTimeMillis();
        long ageInDays = TimeUnit.MILLISECONDS.toDays(currentTime - lastUpdated);
        return ageInDays >= PASSWORD_UPDATE_DAYS;
    }

    /**
     * Gets the age of a password in a human-readable format
     * @param lastUpdated Timestamp when the password was last updated
     * @return A string describing the password age (e.g., "2 months" or "5 days")
     */
    public static String getPasswordAge(long lastUpdated) {
        long currentTime = System.currentTimeMillis();
        long diffInMillis = currentTime - lastUpdated;

        long days = TimeUnit.MILLISECONDS.toDays(diffInMillis);

        if (days < 1) {
            return "Today";
        } else if (days < 30) {
            return days + " day" + (days > 1 ? "s" : "");
        } else {
            long months = days / 30;
            return months + " month" + (months > 1 ? "s" : "");
        }
    }

    /**
     * Finds duplicate passwords in a list of Password objects
     * @param passwords List of passwords to check
     * @return List of passwords that have duplicate values
     */
    public static List<Password> findDuplicatePasswords(List<Password> passwords) {
        Map<String, List<Password>> passwordMap = new HashMap<>();
        List<Password> duplicates = new ArrayList<>();

        // Group passwords by their actual password value
        for (Password password : passwords) {
            String passwordValue = password.password;
            if (!passwordMap.containsKey(passwordValue)) {
                passwordMap.put(passwordValue, new ArrayList<>());
            }
            passwordMap.get(passwordValue).add(password);
        }

        // Add passwords that appear more than once to the duplicates list
        for (Map.Entry<String, List<Password>> entry : passwordMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                duplicates.addAll(entry.getValue());
            }
        }

        return duplicates;
    }

    /**
     * Finds passwords with missing information (username, URL, or category)
     * @param passwords List of passwords to check
     * @return List of passwords that have missing information
     */
    public static List<Password> findPasswordsWithMissingInfo(List<Password> passwords) {
        List<Password> missingInfo = new ArrayList<>();
        if (passwords == null) return missingInfo;
        for (Password password : passwords) {
            if (password == null) continue;
            String username = password.username != null ? password.username.trim() : "";
            String url = password.url != null ? password.url.trim() : "";
            boolean missingCategory = (password.categoryId == null);
            if (username.isEmpty() || url.isEmpty() || missingCategory) {
                missingInfo.add(password);
            }
        }
        return missingInfo;
    }

    /**
     * Finds weak passwords in a list of Password objects
     * @param passwords List of passwords to check
     * @return List of passwords that are weak
     */
    public static List<Password> findWeakPasswords(List<Password> passwords) {
        List<Password> weakPasswords = new ArrayList<>();

        for (Password password : passwords) {
            if (getPasswordStrength(password.password) == PasswordStrength.WEAK) {
                weakPasswords.add(password);
            }
        }

        return weakPasswords;
    }

    /**
     * Finds passwords that need to be updated
     * @param passwords List of passwords to check
     * @return List of passwords that need to be updated
     */
    public static List<Password> findPasswordsNeedingUpdate(List<Password> passwords) {
        List<Password> needsUpdate = new ArrayList<>();

        for (Password password : passwords) {
            if (needsUpdate(password.getLastUpdated())) {
                needsUpdate.add(password);
            }
        }

        return needsUpdate;
    }
}
