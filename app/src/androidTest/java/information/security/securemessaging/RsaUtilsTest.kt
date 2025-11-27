package information.security.securemessaging

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.security.KeyPair

@RunWith(AndroidJUnit4::class)
class RsaUtilsTest {

    @Test
    fun test_RSA_encryption_and_decryption() {
        // 1. Generate Key Pair (1024 bits is fast for testing, 2048 is standard for prod)
        val keyPair: KeyPair = RsaUtils.generateKeyPair(1024)

        // 2. Define a message
        val originalMessage = "This is a secret message for testing."

        // 3. Encrypt the message
        // Note: encrypt should return a Base64 String, not raw bytes, to be safe
        val encryptedMessage = RsaUtils.encrypt(originalMessage, keyPair.public)

        assertNotNull("Encrypted message should not be null", encryptedMessage)
        assertNotEquals("Encrypted message should not be the same as the original", originalMessage, encryptedMessage)
        println("Encrypted Data: $encryptedMessage") // Helpful for debugging

        // 4. Decrypt the message
        val decryptedMessage = RsaUtils.decrypt(encryptedMessage, keyPair.private)

        assertNotNull("Decrypted message should not be null", decryptedMessage)

        // 5. Verify the decrypted message matches the original
        assertEquals("Decrypted message should match the original message", originalMessage, decryptedMessage)
    }

    @Test
    fun test_with_different_key_size() {
        // Test with a 2048-bit key (More secure, slightly slower)
        val keyPair: KeyPair = RsaUtils.generateKeyPair(2048)
        val originalMessage = "Another test with a larger key."

        val encryptedMessage = RsaUtils.encrypt(originalMessage, keyPair.public)
        val decryptedMessage = RsaUtils.decrypt(encryptedMessage, keyPair.private)

        assertEquals(originalMessage, decryptedMessage)
    }

    @Test
    fun test_RSA_Performance_Evaluation() {
        val keySizes = listOf(1024, 2048, 4096)
        // RSA limit: key_size_bytes - 11 (PKCS1Padding)
        // 1024 bits = 128 bytes -> max 117 bytes
        // We stick to sizes that fit in the smallest key (1024) for consistency, 
        // and maybe one that fits in larger keys if we wanted, but let's keep it simple and safe.
        val messageSizes = listOf(16, 32, 64, 100) 

        println("--- RSA Performance Evaluation ---")
        println(String.format("%-10s %-15s %-15s %-15s", "Key Size", "Msg Size (B)", "Time (ms)", "Time (µs)"))

        for (keySize in keySizes) {
            val keyPair = RsaUtils.generateKeyPair(keySize)
            
            for (size in messageSizes) {
                val message = generateRandomString(size)
                
                val start = System.nanoTime()
                RsaUtils.encrypt(message, keyPair.public)
                val end = System.nanoTime()

                val durationNs = end - start
                val durationMs = durationNs / 1_000_000.0
                val durationUs = durationNs / 1_000.0

                println(String.format("%-10d %-15d %-15.4f %-15.2f", keySize, size, durationMs, durationUs))
                
                assertTrue("Encryption should take time", durationNs > 0)
            }
        }
        println("----------------------------------")
    }

    private fun generateRandomString(length: Int): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}