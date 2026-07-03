package it.gov.pagopa.mypay2pu.extractor.utils;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

/**
 * Utility class for AES encryption using GCM mode.
 */
public class AESUtils {
  private AESUtils() {
  }

  private static final String ALGORITHM = "AES/GCM/NoPadding";
  private static final String FACTORY_INSTANCE = "PBKDF2WithHmacSHA256";
  private static final int TAG_LENGTH_BIT = 128;
  private static final int IV_LENGTH_BYTE = 12;
  private static final int SALT_LENGTH_BYTE = 16;
  private static final String ALGORITHM_TYPE = "AES";
  private static final int KEY_LENGTH = 256;
  private static final int ITERATION_COUNT = 65536;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  public static final String CIPHER_EXTENSION = ".cipher";

  /** Generates a random byte array to be used as a nonce. */
  private static byte[] getRandomNonce(int length) {
    byte[] nonce = new byte[length];
    SECURE_RANDOM.nextBytes(nonce);
    return nonce;
  }

  /**
   * Derives an AES key from a password and a cryptographic salt using PBKDF2.
   * @throws IllegalStateException if the key derivation fails.
   */
  private static SecretKey getSecretKey(String password, byte[] salt) {
    KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);

    try {
      SecretKeyFactory factory = SecretKeyFactory.getInstance(FACTORY_INSTANCE);
      return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), ALGORITHM_TYPE);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new IllegalStateException("Cannot initialize cryptographic data", e);
    }
  }

  /** It will wrap the provided inputStream into a ciphered inputStream using AES GCM mode configured with the provided password */
  public static InputStream encrypt(String password, InputStream plainStream) {
    byte[] salt = getRandomNonce(SALT_LENGTH_BYTE);
    SecretKey secretKey = getSecretKey(password, salt);

    // GCM recommends 12 bytes iv
    byte[] iv = getRandomNonce(IV_LENGTH_BYTE);
    Cipher cipher = initCipher(Cipher.ENCRYPT_MODE, secretKey, iv);

    // prefix IV and Salt to cipher text
    byte[] prefix = new byte[iv.length + salt.length];
    System.arraycopy(iv, 0, prefix, 0, iv.length);
    System.arraycopy(salt, 0, prefix, iv.length, salt.length);

    return new SequenceInputStream(
      new ByteArrayInputStream(prefix),
      new CipherInputStream(new BufferedInputStream(plainStream), cipher));
  }

  /** It will cipher the provided file using AES GCM mode configured with the provided password.<BR />
   * If the ciphered file already exists, it will throw {@link java.nio.file.FileAlreadyExistsException} */
  public static File encrypt(String password, File plainFile) {
    File cipherFile = new File(plainFile.getAbsolutePath() + CIPHER_EXTENSION);
    try (FileInputStream fis = new FileInputStream(plainFile);
         InputStream cipherStream = encrypt(password, fis)) {
      Files.copy(cipherStream, cipherFile.toPath());
    } catch (IOException e) {
      throw new IllegalStateException("Something went wrong when ciphering input file " + plainFile.getAbsolutePath(), e);
    }
    return cipherFile;
  }

  /**
   * Initializes a Cipher instance with the specified mode, secret key, and IV.
   *
   * @param mode the cipher mode (Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE).
   * @param secretKey the secret key.
   * @param iv the initialization vector.
   * @return an initialized Cipher instance.
   * @throws IllegalStateException if cipher initialization fails.
   */
  private static Cipher initCipher(int mode, SecretKey secretKey, byte[] iv) {
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(mode, secretKey, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
      return cipher;
    } catch (NoSuchPaddingException | NoSuchAlgorithmException |
             InvalidKeyException
             | InvalidAlgorithmParameterException e) {
      throw new IllegalStateException("Cannot initialize cipher data", e);
    }
  }

}
