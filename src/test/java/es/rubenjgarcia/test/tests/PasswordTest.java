package es.rubenjgarcia.test.tests;

import es.rubenjgarcia.security.PasswordUtils;
import org.junit.Assert;
import org.junit.Test;

public class PasswordTest {

  @Test
  public void truncatedHashTest() {
    String userString = "password!";
    String badHash;
    int badHashLength;

    String goodHash = PasswordUtils.createHash(userString);
    badHashLength = goodHash.length();

    do {
      badHashLength -= 1;
      badHash = goodHash.substring(0, badHashLength);

      boolean raised = false;
      try {
        PasswordUtils.verifyPassword(userString, badHash);
      } catch (PasswordUtils.InvalidHashException ex) {
        raised = true;
      }

      Assert.assertTrue("Truncated hash test: FAIL (At hash length of " + badHashLength + ")", raised);
    } while (badHash.charAt(badHashLength - 3) != ':');
  }

  @Test
  public void basicTests() {
    for (int i = 0; i < 10; i++) {
      String password = "" + i;
      String hash = PasswordUtils.createHash(password);
      String secondHash = PasswordUtils.createHash(password);

      Assert.assertNotEquals("FAILURE: TWO HASHES ARE EQUAL!", hash, secondHash);

      String wrongPassword = "" + (i + 1);
      boolean wrongPasswordVerify = PasswordUtils.verifyPassword(wrongPassword, hash);
      Assert.assertFalse("FAILURE: WRONG PASSWORD ACCEPTED!", wrongPasswordVerify);

      boolean correctPasswordVerify = PasswordUtils.verifyPassword(password, hash);
      Assert.assertTrue("FAILURE: GOOD PASSWORD NOT ACCEPTED!", correctPasswordVerify);
    }
  }

  @Test
  public void testHashFunctionChecking() {
    String hash = PasswordUtils.createHash("foobar");
    hash = hash.replaceFirst("sha1:", "sha256:");

    boolean raised = false;
    try {
      PasswordUtils.verifyPassword("foobar", hash);
    } catch (PasswordUtils.CannotPerformOperationException ex) {
      raised = true;
    }

    Assert.assertTrue("Algorithm swap: FAIL", raised);
  }

  @Test
  public void testWrongHashes() {
    {
      boolean raised = false;
      String wrongHash = "sha1:0:18:IVsbD3/eUBkQdPih9K43IZt1EPZ4vqUg:eLbVnbuFmkgfzdA9FdZ1lpzA";
      try {
        PasswordUtils.verifyPassword("foobar", wrongHash);
      } catch (PasswordUtils.InvalidHashException ex) {
        raised = true;
      }

      Assert.assertTrue(String.format("Hash %s must be wrong", wrongHash), raised);
    }

    {
      boolean raised = false;
      String wrongHash = "sha1:64000:a:IVsbD3/eUBkQdPih9K43IZt1EPZ4vqUg:eLbVnbuFmkgfzdA9FdZ1lpzA";
      try {
        PasswordUtils.verifyPassword("foobar", wrongHash);
      } catch (PasswordUtils.InvalidHashException ex) {
        raised = true;
      }

      Assert.assertTrue(String.format("Hash %s must be wrong", wrongHash), raised);
    }

    {
      boolean raised = false;
      String wrongHash = "sha1:a:18:IVsbD3/eUBkQdPih9K43IZt1EPZ4vqUg:eLbVnbuFmkgfzdA9FdZ1lpzA";
      try {
        PasswordUtils.verifyPassword("foobar", wrongHash);
      } catch (PasswordUtils.InvalidHashException ex) {
        raised = true;
      }

      Assert.assertTrue(String.format("Hash %s must be wrong", wrongHash), raised);
    }


    {
      boolean raised = false;
      String wrongHash = "sha1:64000:18:IVsbD3/eUBkQdPih9K43IZt1EPZ4vqUg";
      try {
        PasswordUtils.verifyPassword("foobar", wrongHash);
      } catch (PasswordUtils.InvalidHashException ex) {
        raised = true;
      }

      Assert.assertTrue(String.format("Hash %s must be wrong", wrongHash), raised);
    }
  }

}