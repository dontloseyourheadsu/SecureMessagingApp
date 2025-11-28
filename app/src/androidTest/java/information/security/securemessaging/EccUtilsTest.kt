package information.security.securemessaging

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.security.KeyPair

@RunWith(AndroidJUnit4::class)
class EccUtilsTest {

    /**
     * Tests the full ECC encryption and decryption flow.
     * Verifies that the decrypted message matches the original.
     */
    @Test
    fun test_ECC_encryption_and_decryption_flow() {
        val keyPair: KeyPair = EccUtils.generateKeyPair(256)
        assertNotNull("KeyPair should be generated", keyPair)

        val originalMessage = "This is a test message for Elliptic Curve Cryptography."

        val encryptedMessage = EccUtils.encrypt(originalMessage, keyPair.public, keyPair.private)

        assertNotNull("Encrypted message should not be null", encryptedMessage)
        assertNotEquals("Encrypted message should not match original", originalMessage, encryptedMessage)

        println("ECC Encrypted: $encryptedMessage")

        val decryptedMessage = EccUtils.decrypt(encryptedMessage, keyPair.public, keyPair.private)

        assertEquals("Decrypted text must match original", originalMessage, decryptedMessage)
    }

    /**
     * Evaluates ECC performance across different key sizes and message lengths.
     * Logs encryption and decryption times.
     */
    @Test
    fun test_ECC_Performance_Evaluation() {
        val keySizes = listOf(256, 384, 521)
        val messageSizes = listOf(64, 1024, 100 * 1024) // 64B, 1KB, 100KB

        println("--- ECC Performance Evaluation ---")
        println(String.format("%-10s %-15s %-15s %-15s %-15s %-15s", "Key Size", "Msg Size (B)", "Enc (ms)", "Enc (µs)", "Dec (ms)", "Dec (µs)"))

        for (keySize in keySizes) {
            // Generate key pair once per size to focus on encryption time
            val keyPair = EccUtils.generateKeyPair(keySize)
            
            for (size in messageSizes) {
                val message = generateRandomString(size)

                // Measure Encryption
                val startEnc = System.nanoTime()
                val encryptedMessage = EccUtils.encrypt(message, keyPair.public, keyPair.private)
                val endEnc = System.nanoTime()

                // Measure Decryption
                val startDec = System.nanoTime()
                EccUtils.decrypt(encryptedMessage, keyPair.public, keyPair.private)
                val endDec = System.nanoTime()

                val encDurationNs = endEnc - startEnc
                val encMs = encDurationNs / 1_000_000.0
                val encUs = encDurationNs / 1_000.0

                val decDurationNs = endDec - startDec
                val decMs = decDurationNs / 1_000_000.0
                val decUs = decDurationNs / 1_000.0

                println(String.format("%-10d %-15d %-15.4f %-15.2f %-15.4f %-15.2f", keySize, size, encMs, encUs, decMs, decUs))
                
                // Basic assertion to ensure it ran
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