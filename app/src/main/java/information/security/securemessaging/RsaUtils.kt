package information.security.securemessaging

import android.util.Base64
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.Cipher

object RsaUtils {

    private const val TRANSFORMATION = "RSA/ECB/PKCS1Padding"

    /**
     * Generates an RSA KeyPair based on the specified key size.
     *
     * @param keySize The size of the key in bits (e.g., 1024, 2048, 4096).
     * @return A KeyPair containing the public and private keys.
     */
    fun generateKeyPair(keySize: Int): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(keySize)
        return keyPairGenerator.genKeyPair()
    }

    /**
     * Encrypts a plaintext string using the RSA public key.
     *
     * @param plaintext The message to encrypt.
     * @param publicKey The RSA public key.
     * @return The Base64 encoded encrypted string.
     */
    fun encrypt(plaintext: String, publicKey: PublicKey): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)

        // Use UTF-8 explicitly
        val encryptedBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        // Use NO_WRAP to avoid unexpected newlines
        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
    }

    /**
     * Decrypts an encrypted string using the RSA private key.
     *
     * @param encryptedText The Base64 encoded encrypted message.
     * @param privateKey The RSA private key.
     * @return The decrypted plaintext string.
     */
    fun decrypt(encryptedText: String, privateKey: PrivateKey): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)

        // Use NO_WRAP here as well to match
        val decryptedBytes = cipher.doFinal(Base64.decode(encryptedText, Base64.NO_WRAP))

        // Convert bytes back to string using UTF-8
        return String(decryptedBytes, Charsets.UTF_8)
    }
}