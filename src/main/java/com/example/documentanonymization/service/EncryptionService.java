package com.example.documentanonymization.service;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String AES_ALGORITHM = "AES";
    private static final int KEY_SIZE = 256;
    private static final String SECRET_KEY = "documentAnonymizationSecretKey";

    public byte[] encryptFile(byte[] fileData) {
        try {
            SecretKey secretKey = generateSecretKey();
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(fileData);
        } catch (Exception e) {
            throw new RuntimeException("Dosya şifreleme sırasında hata oluştu", e);
        }
    }

    public byte[] decryptFile(byte[] encryptedData) {
        try {
            SecretKey secretKey = generateSecretKey();
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("Dosya şifre çözme sırasında hata oluştu", e);
        }
    }

    private SecretKey generateSecretKey() {
        try {
            byte[] keyBytes = SECRET_KEY.getBytes();
            // SHA-256 hash kullanarak 32 byte (256 bit) anahtar oluştur
            java.security.MessageDigest sha = java.security.MessageDigest.getInstance("SHA-256");
            keyBytes = sha.digest(keyBytes);
            return new SecretKeySpec(keyBytes, AES_ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("Anahtar oluşturma hatası", e);
        }
    }

    public SecretKey generateRandomKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
            SecureRandom secureRandom = new SecureRandom();
            keyGenerator.init(KEY_SIZE, secureRandom);
            return keyGenerator.generateKey();
        } catch (Exception e) {
            throw new RuntimeException("Anahtar oluşturma hatası", e);
        }
    }

    public String convertKeyToString(SecretKey secretKey) {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }


    public SecretKey convertStringToKey(String encodedKey) {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, AES_ALGORITHM);
    }
}