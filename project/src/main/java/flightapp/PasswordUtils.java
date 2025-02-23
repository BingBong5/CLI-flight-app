package flightapp;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


/**
 * A collection of utility methods to help with managing passwords
 */
public class PasswordUtils {
  /**
   * Generates a cryptographically-secure salted password.
   */
  public static byte[] saltAndHashPassword(String password) {

    // len = 16;
    byte[] salt = generateSalt();

    // len = 128
    byte[] saltedHash = hashWithSalt(password, salt);

    // len = 16 + 128
    byte[] res = new byte[salt.length + saltedHash.length];

    // add salt to beginning
    for(int i = 0; i < salt.length; i++){
      res[i] = salt[i];
    }

    // add hashedPassword to end
    for(int i = 0; i < saltedHash.length; i++){
      res[i + salt.length] = saltedHash[i];
    }

    return res;
  }

  /**
   * Verifies whether the plaintext password can be hashed to provided salted hashed password.
   */
  public static boolean plaintextMatchesSaltedHash(String plaintext, byte[] saltedHashed) {
    byte[] salt = new byte[SALT_LENGTH_BYTES];

    // first 16 bytes should be salt, in the 'saltedHashed'
    for(int i = 0; i < SALT_LENGTH_BYTES; i++){
      salt[i] = saltedHashed[i];
    }

    // turn passed plaintext into hashed password
    byte[] hashedPlaintext = hashWithSalt(plaintext, salt);

    // compare last 128 bytes of saltedhashed (should be 128 + 16 = 144 bytes long)
    // with 128 byte long hashedPlaintext
    for(int i = 0; i < saltedHashed.length - SALT_LENGTH_BYTES; i++){
      if(hashedPlaintext[i] != saltedHashed[i + SALT_LENGTH_BYTES]){
        return false;
      }
    }
    return true;
  }
  
  // Password hashing parameter constants.
  private static final int HASH_STRENGTH = 65536;
  private static final int KEY_LENGTH_BYTES = 128;
  private static final int SALT_LENGTH_BYTES = 16;

  /**
   * Generate a small bit of randomness to serve as a password "salt"
   */
  static byte[] generateSalt() {
    SecureRandom Random = new SecureRandom();
    byte[] salt = new byte[SALT_LENGTH_BYTES];
    Random.nextBytes(salt);
    return salt;
  }

  /**
   * Uses the provided salt to generate a cryptographically-secure hash of the provided password.
   * The resultant byte array will be KEY_LENGTH_BYTES bytes long.
   */
  static byte[] hashWithSalt(String password, byte[] salt)
    throws IllegalStateException {
    // Specify the hash parameters, including the salt
    KeySpec spec = new PBEKeySpec(password.toCharArray(), salt,
                                  HASH_STRENGTH, KEY_LENGTH_BYTES * 8 /* length in bits */);

    // Hash the whole thing
    SecretKeyFactory factory = null;
    byte[] hash = null; 
    try {
      factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      hash = factory.generateSecret(spec).getEncoded();
      return hash;
    } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
      throw new IllegalStateException();
    }
  }

}
