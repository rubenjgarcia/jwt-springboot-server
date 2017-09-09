package es.rubenjgarcia.security;

import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;

public final class PasswordUtils {

  private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";

  private static final int SALT_BYTE_SIZE = 24;
  private static final int HASH_BYTE_SIZE = 18;
  private static final int PBKDF2_ITERATIONS = 64000;

  private static final int HASH_SECTIONS = 5;
  private static final int HASH_ALGORITHM_INDEX = 0;
  private static final int ITERATION_INDEX = 1;
  private static final int HASH_SIZE_INDEX = 2;
  private static final int SALT_INDEX = 3;
  private static final int PBKDF2_INDEX = 4;

  private PasswordUtils() {}

  public static String createHash(final String password) {
    return createHash(password.toCharArray());
  }

  private static String createHash(char[] password) {
    final SecureRandom random = new SecureRandom();
    final byte[] salt = new byte[SALT_BYTE_SIZE];
    random.nextBytes(salt);

    final byte[] hash = pbkdf2(password, salt, PBKDF2_ITERATIONS, HASH_BYTE_SIZE);
    final int hashSize = hash.length;

    return "sha1:" + PBKDF2_ITERATIONS + ":" + hashSize + ":" + toBase64(salt) + ":" + toBase64(hash);
  }

  public static boolean verifyPassword(final String password, final String correctHash) {
    return verifyPassword(password.toCharArray(), correctHash);
  }

  private static boolean verifyPassword(final char[] password, final String correctHash) {
    final String[] params = correctHash.split(":");
    if (params.length != HASH_SECTIONS) {
      throw new InvalidHashException("Fields are missing from the password hash.");
    }

    if (!params[HASH_ALGORITHM_INDEX].equals("sha1")) {
      throw new CannotPerformOperationException("Unsupported hash type.");
    }

    final int iterations;
    try {
      iterations = Integer.parseInt(params[ITERATION_INDEX]);
    } catch (NumberFormatException ex) {
      throw new InvalidHashException("Could not parse the iteration count as an integer.", ex);
    }

    if (iterations < 1) {
      throw new InvalidHashException("Invalid number of iterations. Must be >= 1.");
    }

    final byte[] salt = fromBase64(params[SALT_INDEX]);
    final byte[] hash = fromBase64(params[PBKDF2_INDEX]);

    final int storedHashSize;
    try {
      storedHashSize = Integer.parseInt(params[HASH_SIZE_INDEX]);
    } catch (NumberFormatException ex) {
      throw new InvalidHashException("Could not parse the hash size as an integer.", ex);
    }

    if (storedHashSize != hash.length) {
      throw new InvalidHashException("Hash length doesn't match stored hash length.");
    }

    final byte[] testHash = pbkdf2(password, salt, iterations, hash.length);
    return slowEquals(hash, testHash);
  }

  private static boolean slowEquals(final byte[] a, final byte[] b) {
    int diff = a.length ^ b.length;
    for (int i = 0; i < a.length && i < b.length; i++) {
      diff |= a[i] ^ b[i];
    }
    return diff == 0;
  }

  private static byte[] pbkdf2(final char[] password, final byte[] salt, final int iterations, final int bytes) {
    final PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
    try {
      final SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
      return skf.generateSecret(spec).getEncoded();
    } catch (Exception e) {
      throw new CannotPerformOperationException(e.getMessage(), e);
    }
  }

  private static byte[] fromBase64(final String hex) {
    return DatatypeConverter.parseBase64Binary(hex);
  }

  private static String toBase64(final byte[] array) {
    return DatatypeConverter.printBase64Binary(array);
  }

  static public class InvalidHashException extends RuntimeException {

    InvalidHashException(final String message) {
      super(message);
    }

    InvalidHashException(final String message, final Throwable source) {
      super(message, source);
    }
  }

  static public class CannotPerformOperationException extends RuntimeException {

    CannotPerformOperationException(final String message) {
      super(message);
    }

    CannotPerformOperationException(final String message, final Throwable source) {
      super(message, source);
    }
  }

}