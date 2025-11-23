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
    fun test_ECC_performance_logging() {
        // This test is just to generate a log entry for your report data
        val keyPair = EccUtils.generateKeyPair(256)
        val msg = "Performance test message"

        val start = System.nanoTime()
        EccUtils.encrypt(msg, keyPair.public, keyPair.private)
        val end = System.nanoTime()

        val durationMs = (end - start) / 1_000_000.0
        println("ECC 256 Encryption Time: $durationMs ms")

        assertTrue("Encryption should take some time", durationMs > 0)
    }
}