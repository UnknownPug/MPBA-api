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

/**
 * Utility class for encryption, decryption, and hashing operations.
 * This class handles AES encryption and decryption with CBC and PKCS5 padding,
 * Argon2 hashing, and secret key management, including key generation, saving,
 * and loading from file.
 * <p>
 * The class uses BouncyCastle as a security provider for cryptographic operations.
 * It ensures the presence of a secret key for encryption/decryption purposes,
 * either by loading an existing one or generating a new one if none is found.
 */
public class EncryptionUtil {

    private static final String ALGORITHM = "AES"; // AES encryption algorithm
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding"; // Cipher transformation
    private static final Argon2 argon2 = Argon2Factory.create(); // Argon2 hashing instance
    private static final Logger log = LoggerFactory.getLogger(EncryptionUtil.class);

    @Getter
    private static SecretKey secretKey; // The AES secret key used for encryption/decryption

    static {
        // Static initializer to set up security provider and load or generate secret key
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

    /**
     * Generates a new AES secret key.
     *
     * @return A new {@link SecretKey} for AES encryption.
     * @throws Exception if there is an error generating the key.
     */
    public static synchronized SecretKey generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(256); // Key size of 256 bits
        return keyGen.generateKey();
    }

    /**
     * Encrypts the given string data using the provided AES secret key.
     * The encryption uses the AES algorithm with CBC mode and PKCS5 padding.
     *
     * @param data The plaintext string to encrypt.
     * @param key  The AES secret key to use for encryption.
     * @return The Base64-encoded string of the IV and encrypted data.
     * @throws Exception if any encryption error occurs.
     */
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

    /**
     * Decrypts the given Base64-encoded encrypted data using the provided AES secret key.
     * The decryption uses the AES algorithm with CBC mode and PKCS5 padding.
     *
     * @param encryptedData The Base64-encoded string of IV and encrypted data.
     * @param key           The AES secret key to use for decryption.
     * @return The decrypted plaintext string.
     * @throws Exception if any decryption error occurs.
     */
    public static synchronized String decrypt(String encryptedData, SecretKey key) throws Exception {
        // Decode the Base64-encoded string
        byte[] combinedData = Base64.getDecoder().decode(encryptedData);

        // Extract the IV (first 16 bytes for AES with 16-byte IV)
        if (combinedData.length < 16) {
            throw new IllegalArgumentException("Invalid encrypted data length");
        }

        // Extract IV and encrypted data
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

    /**
     * Generates a random Initialization Vector (IV) for AES encryption.
     *
     * @return An {@link IvParameterSpec} object containing the generated IV.
     */
    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[16]; // AES block size is 16 bytes
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    /**
     * Hashes the given string data using the Argon2 hashing algorithm.
     *
     * @param data The string data to hash.
     * @return The hashed string using Argon2.
     */
    public static String hash(String data) {
        char[] dataChars = data.toCharArray();
        return argon2.hash(10, 65536, 1, dataChars); // Argon2 hash with specified parameters
    }

    /**
     * Saves the AES secret key to a file for persistent storage.
     * A backup key file is also created.
     *
     * @param key The {@link SecretKey} to save.
     * @throws IOException if there is an error writing the key to file.
     */
    public static synchronized void saveKey(SecretKey key) throws IOException {
        try (FileOutputStream fos = new FileOutputStream("secret.key")) {
            fos.write(key.getEncoded());
        }
        try (FileOutputStream backupFos = new FileOutputStream("backup_secret.key")) {
            backupFos.write(key.getEncoded());
        }
    }

    /**
     * Loads the AES secret key from a file. If the primary key file does not exist,
     * it attempts to load the key from a backup file.
     *
     * @return The {@link SecretKey} if successfully loaded, or null if no key is found.
     * @throws IOException if there is an error reading the key from file.
     */
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