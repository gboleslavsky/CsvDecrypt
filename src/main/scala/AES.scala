
import javax.crypto.spec.SecretKeySpec
import play.api.libs.Codecs
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import java.util.UUID


/**
  * AES 128 bit encryption helper.
  *
  * In a real app should use a crypto.secret from the config file to
  * derive a private key. These need to be configured
  * differently for dev, stage and production environments. In this demo
  * it uses a random UUID instead
  */
object AES {
  private lazy val simulatedConfigPrivateKey: String = UUID.randomUUID().toString
  private lazy val transformation: String = "AES/CBC/PKCS5Padding"

  /**
    * Hash function.
    */
  private def hash(str: String) = {
    // Use a different hash function than SHA-256 so that the resulting private key is not the same
    // as used in the ECDSA algorithm (otherwise, in case a future weakness in AES exposes the
    // used private key, and, our database is stolen, then an attacker may derive the first 16 bytes
    // of the EC private key by cracking the AES private key of our stolen database).
    // Need only 128 bits, but prefer SHA-512 over SHA-1 for now and truncate (see hash16() method).
    java.security.MessageDigest.getInstance("SHA-512").digest(str.getBytes("utf-8"))
  }

  /**
    * Get 16 byte private key for AES encryptions
    */
  private def privateKey = {
    // hash with sha256 to preserve as much entropy of the application secret string as possible
    hash16(simulatedConfigPrivateKey)
  }

  /**
    * Get 16 bytes of max entropy from string.
    */
  private def hash16(str: String) = hash(str).take(16)

  /**
    * helper function to initialize the cipher for encryption or decryption
    * initializes a cipher to encrypt or decrypt a String with the AES encryption
    * standard and the supplied private key.
    *
    * The private key must have a length of 16 bytes.
    * The transformation algorithm used is `AES/CBC/PKCS5Padding`.
    *
    * @param ivSalt
    * @param encryptOrDecrypt is either Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE
    * @return Cipher initialized to encrypt or to decrypt depending on the value of encryptOrDecrypt
    */
  type CipherMode = Int
  private def initCipher(ivSalt: String, encryptOrDecrypt: CipherMode) = {
    val skeySpec = new SecretKeySpec(privateKey, "AES")
    val cipher = Cipher.getInstance(transformation)
    val iv = hash16(ivSalt)
    cipher.init(encryptOrDecrypt, skeySpec, new IvParameterSpec(iv))
    cipher
  }

  /**
    * Encrypt a String with the AES encryption standard and the supplied private key used to initialize the cipher.
    *
    * The private key must have a length of 16 bytes.
    *
    * The provider used is by default this uses the platform default JSSE provider.  This can be overridden by defining
    * `application.crypto.provider` in `application.conf`.
    *
    * The transformation algorithm used is `AES/CBC/PKCS5Padding`.
    *
    * @param value The String to encrypt.
    * @param ivSalt The initialization vector salt.
    * @return An hexadecimal encrypted string or empty String is value was empty.
    */
  def encrypt(value: String, ivSalt: String): String = {
    val cipher: Cipher = initCipher(ivSalt, Cipher.ENCRYPT_MODE)
    Codecs.toHexString(cipher.doFinal(value.getBytes("utf-8")))
  }

  /**
    * Decrypt a String with the AES encryption standard.
    *
    * The private key must have a length of 16 bytes.
    *
    * The transformation used is `AES/CBC/PKCS5Padding`.
    *
    * @param value An hexadecimal encrypted string.
    * @param ivSalt The initialization vector salt.
    * @return The decrypted String or empty String is value was empty.
    */
  def decrypt(value: String, ivSalt: String): String = {
    val cipher: Cipher = initCipher(ivSalt, Cipher.DECRYPT_MODE)
    new String(cipher.doFinal(Codecs.hexStringToByte(value)))
  }

}
