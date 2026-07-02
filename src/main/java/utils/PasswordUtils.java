package utils;

import java.security.MessageDigest;

public class PasswordUtils {

    public static String hash(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes("UTF-8"));
        return bytesToHex(hash);
    }

    public static boolean verify(String rawPassword, String storedHash) throws Exception {
        if (storedHash == null) return false;
        if (storedHash.length() == 64 && storedHash.matches("[0-9a-fA-F]+")) {
            return hash(rawPassword).equalsIgnoreCase(storedHash);
        }
        return rawPassword.equals(storedHash);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
