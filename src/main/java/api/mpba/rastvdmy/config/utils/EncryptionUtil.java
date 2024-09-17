package api.mpba.rastvdmy.config.utils;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import lombok.Getter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;
import java.util.Base64;

public class EncryptionUtil {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final Argon2 argon2 = Argon2Factory.create();
    private static final Logger log = LoggerFactory.getLogger(EncryptionUtil.class);

    @Getter
    private static SecretKey secretKey;

    static {
        Security.addProvider(new BouncyCastleProvider());
        try {
            secretKey = loadKey();
            if (secretKey == null) {
                secretKey = generateKey();
                saveKey(secretKey);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate or load secret key", e);
        }
    }

    public static synchronized SecretKey generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(256);
        return keyGen.generateKey();
    }

    public static synchronized String encrypt(String data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);

        // Generate a new IV for this encryption
        IvParameterSpec iv = generateIv();
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);

        // Encrypt the data
        byte[] encryptedData = cipher.doFinal(data.getBytes());

        // Combine the IV and encrypted data
        byte[] ivBytes = iv.getIV();
        byte[] combinedData = new byte[ivBytes.length + encryptedData.length];

        // Copy the IV and encrypted data into the combinedData array
        System.arraycopy(ivBytes, 0, combinedData, 0, ivBytes.length);
        System.arraycopy(encryptedData, 0, combinedData, ivBytes.length, encryptedData.length);

        // Return the Base64-encoded combined data
        return Base64.getEncoder().encodeToString(combinedData);
    }

    public static synchronized String decrypt(String encryptedData, SecretKey key) throws Exception {
        // Decode the Base64-encoded string
        byte[] combinedData = Base64.getDecoder().decode(encryptedData);

        // Extract the IV (first 16 bytes for AES with 16-byte IV)
        if (combinedData.length < 16) {
            throw new IllegalArgumentException("Invalid encrypted data length");
        }
        byte[] ivBytes = Arrays.copyOfRange(combinedData, 0, 16);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        // Extract the encrypted data (remaining bytes)
        byte[] encryptedBytes = Arrays.copyOfRange(combinedData, 16, combinedData.length);

        // Initialize cipher for decryption
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);

        // Decrypt the data
        byte[] decryptedData = cipher.doFinal(encryptedBytes);

        return new String(decryptedData);
    }

    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public static String hash(String data) {
        char[] dataChars = data.toCharArray();
        return argon2.hash(10, 65536, 1, dataChars);
    }

    public static synchronized void saveKey(SecretKey key) throws IOException {
        try (FileOutputStream fos = new FileOutputStream("secret.key")) {
            fos.write(key.getEncoded());
        }
        try (FileOutputStream backupFos = new FileOutputStream("backup_secret.key")) {
            backupFos.write(key.getEncoded());
        }
    }

    public static synchronized SecretKey loadKey() throws IOException {
        File keyFile = new File("secret.key");
        if (!keyFile.exists()) {
            File backupFile = new File("backup_secret.key");
            if (backupFile.exists()) {
                byte[] keyBytes = Files.readAllBytes(backupFile.toPath());
                log.debug("Backup key loaded successfully.");
                return new SecretKeySpec(keyBytes, ALGORITHM);
            } else {
                return null;
            }
        }
        byte[] keyBytes = Files.readAllBytes(keyFile.toPath());
        log.debug("Primary key loaded successfully.");
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }
}