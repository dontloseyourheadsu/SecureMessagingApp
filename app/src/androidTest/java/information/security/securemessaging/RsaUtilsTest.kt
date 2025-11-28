package information.security.securemessaging

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.security.KeyPair

@RunWith(AndroidJUnit4::class)
class RsaUtilsTest {

    /**
     * Tests the full RSA encryption and decryption flow.
     * Verifies that the decrypted message matches the original.
     */
    @Test
    fun test_RSA_encryption_and_decryption() {
        val keyPair: KeyPair = RsaUtils.generateKeyPair(1024)

        val originalMessage = "This is a secret message for testing."

        val encryptedMessage = RsaUtils.encrypt(originalMessage, keyPair.public)

        assertNotNull("Encrypted message should not be null", encryptedMessage)
        assertNotEquals("Encrypted message should not be the same as the original", originalMessage, encryptedMessage)
        println("Encrypted Data: $encryptedMessage")

        val decryptedMessage = RsaUtils.decrypt(encryptedMessage, keyPair.private)

        assertNotNull("Decrypted message should not be null", decryptedMessage)

        assertEquals("Decrypted message should match the original message", originalMessage, decryptedMessage)
    }

    /**
     * Tests RSA encryption with a larger key size (2048 bits).
     */
    @Test
    fun test_with_different_key_size() {
        val keyPair: KeyPair = RsaUtils.generateKeyPair(2048)
        val originalMessage = "Another test with a larger key."

        val encryptedMessage = RsaUtils.encrypt(originalMessage, keyPair.public)
        val decryptedMessage = RsaUtils.decrypt(encryptedMessage, keyPair.private)

        assertEquals(originalMessage, decryptedMessage)
    }

    /**
     * Evaluates RSA performance across different key sizes and message lengths.
     * Logs encryption and decryption times.
     */
    @Test
    fun test_RSA_Performance_Evaluation() {
        val keySizes = listOf(1024, 2048, 4096)
        val messageSizes = listOf(16, 32, 64, 100) 

        println("--- RSA Performance Evaluation ---")

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
        println(String.format("%-10s %-15s %-15s %-15s %-15s %-15s", "Key Size", "Msg Size (B)", "Enc (ms)", "Enc (µs)", "Dec (ms)", "Dec (µs)"))

        for (keySize in keySizes) {
            val keyPair = RsaUtils.generateKeyPair(keySize)
            
            for (size in messageSizes) {
                val message = generateRandomString(size)
                
                // Measure Encryption
                val startEnc = System.nanoTime()
                val encryptedMessage = RsaUtils.encrypt(message, keyPair.public)
                val endEnc = System.nanoTime()

                // Measure Decryption
                val startDec = System.nanoTime()
                RsaUtils.decrypt(encryptedMessage, keyPair.private)
                val endDec = System.nanoTime()

                val encDurationNs = endEnc - startEnc
                val encMs = encDurationNs / 1_000_000.0
                val encUs = encDurationNs / 1_000.0

                val decDurationNs = endDec - startDec
                val decMs = decDurationNs / 1_000_000.0
                val decUs = decDurationNs / 1_000.0

                println(String.format("%-10d %-15d %-15.4f %-15.2f %-15.4f %-15.2f", keySize, size, encMs, encUs, decMs, decUs))
                
                assertTrue("Encryption should take time", encDurationNs > 0)
                assertTrue("Decryption should take time", decDurationNs > 0)
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