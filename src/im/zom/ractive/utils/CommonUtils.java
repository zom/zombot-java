package com.theah64.caesar.utils;

import java.util.regex.Pattern;

/**
 * Created by shifar on 7/3/16.
 */
public class CommonUtils {


    private static final Pattern VALID_EMAIL_REGEX_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);


    /**
     * To check the passed email is valid or not.
     *
     * @param email String
     * @return TRUE on valid email, false otherwise.
     */
    public static boolean isValidEmail(String email) {
        final boolean isValidEmail = VALID_EMAIL_REGEX_PATTERN.matcher(email).matches();
        if (!isValidEmail) {
            System.out.println("Invalid email : " + email);
        }
        return isValidEmail;
    }
}
