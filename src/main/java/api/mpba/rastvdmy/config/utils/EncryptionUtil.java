package api.mpba.rastvdmy.config.utils;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;
import java.util.Base64;

/**
 * Utility class for encryption, decryption, and hashing operations.
 * This class handles AES encryption and decryption with CBC and PKCS5 padding,
 * Argon2 hashing, and secret key management, including key generation, saving, and loading from the file.
 * <p>
 * The class uses BouncyCastle as a security provider for cryptographic operations.
 * It ensures the presence of a secret key for encryption/decryption purposes,
 * either by loading an existing one or generating a new one if none is found.
 */
@Slf4j
public class EncryptionUtil {
    private static final String ALGORITHM = "AES"; // AES encryption algorithm
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding"; // Cipher transformation
    private static final Argon2 argon2 = Argon2Factory.create(); // Argon2 hashing instance

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
     * Generates a new AES secret key with a key size of 256 bits.
     *
     * @return a new {@link SecretKey} for AES encryption.
     * @throws NoSuchAlgorithmException if the AES algorithm is not available.
     */
    public static synchronized SecretKey generateKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(256); // Key size of 256 bits
        return keyGen.generateKey();
    }

    /**
     * Encrypts the given plaintext using AES with CBC mode and PKCS5 padding.
     * A unique IV is generated for each encryption and prepended to the encrypted data.
     *
     * @param data the plaintext string to encrypt.
     * @param key  the AES secret key to use for encryption.
     * @return the Base64-encoded string of the IV and encrypted data.
     * @throws GeneralSecurityException if encryption fails.
     */
    public static synchronized String encrypt(String data, SecretKey key) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);

        // Generate a random IV for encryption
        IvParameterSpec iv = generateIv();
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);

        // Encrypt the data
        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        // Combine IV and encrypted data
        byte[] combinedData = ByteBuffer.allocate(16 + encryptedData.length)
                .put(iv.getIV())
                .put(encryptedData)
                .array();

        // Return the Base64-encoded combined data
        return Base64.getEncoder().encodeToString(combinedData);
    }

    /**
     * Decrypts the given Base64-encoded string using AES with CBC mode and PKCS5 padding.
     * The IV is extracted from the first 16 bytes of the encrypted data.
     *
     * @param encryptedData the Base64-encoded string of the IV and encrypted data.
     * @param key           the AES secret key to use for decryption.
     * @return the decrypted plaintext string.
     * @throws GeneralSecurityException if decryption fails.
     */
    public static synchronized String decrypt(String encryptedData, SecretKey key) throws GeneralSecurityException {
        byte[] combinedData = Base64.getDecoder().decode(encryptedData);

        // Extract IV from the first 16 bytes
        byte[] ivBytes = Arrays.copyOfRange(combinedData, 0, 16);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        // Extract encrypted data (remaining bytes)
        byte[] encryptedBytes = Arrays.copyOfRange(combinedData, 16, combinedData.length);

        // Initialize cipher for decryption
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);

        // Decrypt the data
        byte[] decryptedData = cipher.doFinal(encryptedBytes);

        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    /**
     * Generates a random Initialization Vector (IV) of 16 bytes for AES encryption.
     *
     * @return an {@link IvParameterSpec} object containing the generated IV.
     */
    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[16]; // AES block size is 16 bytes
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    /**
     * Hashes the given string using the Argon2 hashing algorithm.
     *
     * @param data the plaintext string to hash.
     * @return the hashed string generated by Argon2.
     */
    public static String hash(String data) {
        char[] dataChars = data.toCharArray();
        try {
            return argon2.hash(10, 65536, 1, dataChars); // Argon2 hash with specified parameters
        } finally {
            Arrays.fill(dataChars, '\0');
        }
    }

    /**
     * Saves the AES secret key to persistent storage.
     * The key is saved in both a primary and backup file for redundancy.
     *
     * @param key the AES secret key to save.
     * @throws IOException if an error occurs while writing the key to the files.
     */
    public static synchronized void saveKey(SecretKey key) throws IOException {
        saveKeyToFile(key, "secret.key");
        saveKeyToFile(key, "backup_secret.key");
    }

    /**
     * Saves the provided AES secret key to a specified file.
     *
     * @param key      the AES secret key to save.
     * @param fileName the name of the file to save the key to.
     * @throws IOException if an error occurs while writing the key to the file.
     */
    private static void saveKeyToFile(SecretKey key, String fileName) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(key.getEncoded());
        }
    }

    /**
     * Loads the AES secret key from persistent storage.
     * It first attempts to load the key from a primary file, and if not found, attempts to load it from a backup file.
     *
     * @return the loaded {@link SecretKey}, or {@code null} if no key is found.
     * @throws IOException if an error occurs while reading the key from a file.
     */
    public static synchronized SecretKey loadKey() throws IOException {
        File keyFile = new File("secret.key");
        if (!keyFile.exists()) {
            keyFile = new File("backup_secret.key");
            if (!keyFile.exists()) {
                log.warn("No encryption key found.");
                return null;
            }
            log.debug("Backup key loaded successfully.");
        }
        byte[] keyBytes = Files.readAllBytes(keyFile.toPath());
        log.debug("Primary key loaded successfully.");
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }
}