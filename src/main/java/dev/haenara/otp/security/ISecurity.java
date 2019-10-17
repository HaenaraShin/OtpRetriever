package dev.haenara.otp.security;

public interface ISecurity {
    String encrypt(String plainText);
    String decrypt(String encryptedText);
}
