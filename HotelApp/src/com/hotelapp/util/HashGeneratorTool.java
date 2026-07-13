package com.hotelapp.util;

/**
 * One-time utility: run this to generate a BCrypt hash for the admin password,
 * then paste the printed UPDATE statement into MySQL Workbench.
 * (Not part of the running application — just a setup helper.)
 */
public class HashGeneratorTool {
    public static void main(String[] args) {
        String plainPassword = "admin123"; // change this if you want a different password
        String hash = PasswordUtil.hash(plainPassword);
        System.out.println("Hash: " + hash);
        System.out.println();
        System.out.println("Run this in MySQL Workbench:");
        System.out.println("UPDATE users SET password_hash='" + hash + "' WHERE username='admin';");
    }
}
