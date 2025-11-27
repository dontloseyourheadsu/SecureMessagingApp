package information.security.securemessaging

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.security.KeyPair

@RunWith(AndroidJUnit4::class)
class EccUtilsTest {

    @Test
    fun test_ECC_encryption_and_decryption_flow() {
        // 1. Generate Key Pair (256 bits is standard for ECC)
        // This corresponds to the "secp256r1" curve
        val keyPair: KeyPair = EccUtils.generateKeyPair(256)
        assertNotNull("KeyPair should be generated", keyPair)

        // 2. Define a message
        val originalMessage = "This is a test message for Elliptic Curve Cryptography."

        // 3. Encrypt the message
        // Note: In your implementation, we used the SAME key pair for simulation.
        // In real life, you use the Recipient's Public and Your Private.
        val encryptedMessage = EccUtils.encrypt(originalMessage, keyPair.public, keyPair.private)

        assertNotNull("Encrypted message should not be null", encryptedMessage)
        assertNotEquals("Encrypted message should not match original", originalMessage, encryptedMessage)

        println("ECC Encrypted: $encryptedMessage")

        // 4. Decrypt the message
        val decryptedMessage = EccUtils.decrypt(encryptedMessage, keyPair.public, keyPair.private)

        // 5. Verify equality
        assertEquals("Decrypted text must match original", originalMessage, decryptedMessage)
    }

    @Test
    fun test_ECC_Performance_Evaluation() {
        val keySizes = listOf(256, 384, 521)
        // ECC uses hybrid encryption (AES), so it can handle large messages.
        val messageSizes = listOf(64, 1024, 100 * 1024) // 64B, 1KB, 100KB

        println("--- ECC Performance Evaluation ---")
        println(String.format("%-10s %-15s %-15s %-15s", "Key Size", "Msg Size (B)", "Time (ms)", "Time (µs)"))

        for (keySize in keySizes) {
            // Generate key pair once per size to focus on encryption time
            val keyPair = EccUtils.generateKeyPair(keySize)
            
            for (size in messageSizes) {
                val message = generateRandomString(size)

                val start = System.nanoTime()
                EccUtils.encrypt(message, keyPair.public, keyPair.private)
                val end = System.nanoTime()

                val durationNs = end - start
                val durationMs = durationNs / 1_000_000.0
                val durationUs = durationNs / 1_000.0

                println(String.format("%-10d %-15d %-15.4f %-15.2f", keySize, size, durationMs, durationUs))
                
                // Basic assertion to ensure it ran
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