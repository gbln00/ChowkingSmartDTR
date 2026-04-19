package com.chowking.smartdtr.utils;

import org.mindrot.jbcrypt.BCrypt;

public class HashUtils {

    // Call this when CREATING a user (Admin screen)
    public static String hashPassword(String plainText) {
        return BCrypt.hashpw(plainText, BCrypt.gensalt(12));
    }

    // Call this during LOGIN to check entered password
    public static boolean verifyPassword(String plainText, String storedHash) {
        return BCrypt.checkpw(plainText, storedHash);
    }
}