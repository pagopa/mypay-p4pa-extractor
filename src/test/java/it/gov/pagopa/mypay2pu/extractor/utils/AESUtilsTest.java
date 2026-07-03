package it.gov.pagopa.mypay2pu.extractor.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.spec.KeySpec;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AESUtilsTest {

  private static final String PASSWORD = "PSW";

  @TempDir
  Path tempDir;

  @Test
  void givenPlainStreamWhenEncryptThenPrefixAndPayloadAreDecryptable() throws Exception {
    byte[] plain = "PLAINTEXT".getBytes(StandardCharsets.UTF_8);
    byte[] cipher = AESUtils.encrypt(PASSWORD, new ByteArrayInputStream(plain)).readAllBytes();

    assertTrue(cipher.length > 28);
    byte[] decrypted = decrypt(cipher, PASSWORD);
    assertArrayEquals(plain, decrypted);
  }

  @Test
  void givenPlainFileWhenEncryptThenCreateCipherFileWithExpectedExtension() throws Exception {
    Path plainPath = tempDir.resolve("plain.txt");
    Files.writeString(plainPath, "PLAINTEXT");

    File cipherFile = AESUtils.encrypt(PASSWORD, plainPath.toFile());

    assertTrue(cipherFile.exists());
    assertEquals(plainPath.toString() + AESUtils.CIPHER_EXTENSION, cipherFile.getAbsolutePath());

    byte[] decrypted = decrypt(Files.readAllBytes(cipherFile.toPath()), PASSWORD);
    assertEquals("PLAINTEXT", new String(decrypted, StandardCharsets.UTF_8));
  }

  private byte[] decrypt(byte[] cipherMessage, String password) throws Exception {
    ByteBuffer byteBuffer = ByteBuffer.wrap(cipherMessage);
    byte[] iv = new byte[12];
    byte[] salt = new byte[16];
    byteBuffer.get(iv);
    byteBuffer.get(salt);
    byte[] encrypted = new byte[byteBuffer.remaining()];
    byteBuffer.get(encrypted);

    KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
    byte[] key = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
      .generateSecret(spec)
      .getEncoded();

    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
    return cipher.doFinal(encrypted);
  }
}
