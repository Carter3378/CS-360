package com.example.inventory_application_carter;

import android.util.Base64;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.GeneralSecurityException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordHelper {

    //Password Hashing config
    private static final String PASSWORD_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int PASSWORD_ITERATIONS = 210000;
    private static final int PASSWORD_KEY_LENGTH = 256;
    private static final int PASSWORD_SALT_LENGTH = 16;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String hashPassword(String password)
            throws GeneralSecurityException {

        // Generate random salt creating a unique 16-byte value
        byte[] salt = new byte[PASSWORD_SALT_LENGTH];
        SECURE_RANDOM.nextBytes(salt);

        // Supply hashing algorithm with the password, salt, iteration, and hash length
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, PASSWORD_ITERATIONS, PASSWORD_KEY_LENGTH);

        try {
            // Generate password hash using algorithm implementation
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PASSWORD_ALGORITHM);

            byte[] hash = factory.generateSecret(spec).getEncoded();

            // Encode salt and hash for database storage
            String encodedSalt = Base64.encodeToString(salt, Base64.NO_WRAP);

            String encodedHash = Base64.encodeToString(hash, Base64.NO_WRAP);

            // Return algorithm, iterations, salt, and hash
            return PASSWORD_ALGORITHM + "$" + PASSWORD_ITERATIONS + "$" + encodedSalt + "$" + encodedHash;

        }
        finally {
            // Clear password from the key specification
            spec.clearPassword();
        }
    }

    public static boolean verifyPassword(String password, String storedPassword)
            throws GeneralSecurityException {
        //extract and split the stored hash
        String[] parts = storedPassword.split("\\$");

        // Verify stored password contains the expected information
        if (parts.length != 4 || !parts[0].equals(PASSWORD_ALGORITHM)) {
            return false;
        }

        int iterations;
        // retrieve the iteration count used when password was created and catch exceptions
        try {
            iterations = Integer.parseInt(parts[1]);
        }
        catch (NumberFormatException e) {
            return false;
        }
        // Validate iteration count is above 0
        if (iterations <= 0) {
            return false;
        }

        // Decode stored salt and password hash
        byte[] salt = Base64.decode(parts[2], Base64.NO_WRAP);
        byte[] storedHash = Base64.decode(parts[3], Base64.NO_WRAP);

        // Verify lengths of the decoded values are correct
        if (salt.length != PASSWORD_SALT_LENGTH || storedHash.length == 0) {
            return false;
        }

        // Recreate password hash using stored parameters
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, storedHash.length * 8);

        try {
            // use the same hashing algorithm that was used during the password creation
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PASSWORD_ALGORITHM);

            // Create a new hash using the password that was entered for login
            byte[] generatedHash = factory.generateSecret(spec).getEncoded();

            // Compare generated hash with stored hash
            return MessageDigest.isEqual(storedHash, generatedHash);

        }
        finally {
            // Clear password from the key specification
            spec.clearPassword();
        }
    }

}
