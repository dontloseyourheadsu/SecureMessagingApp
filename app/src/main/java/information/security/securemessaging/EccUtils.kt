package information.security.securemessaging

import android.util.Base64
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.spec.SecretKeySpec

object EccUtils {

    // ECC is usually faster and uses smaller keys than RSA for same security [cite: 456]
    fun generateKeyPair(keySize: Int): KeyPair {
        val kpg = KeyPairGenerator.getInstance("EC")
        // Mapping bits to standard curve names
        val curveName = when (keySize) {
            256 -> "secp256r1"
            384 -> "secp384r1"
            521 -> "secp521r1"
            else -> "secp256r1" // Default
        }
        kpg.initialize(ECGenParameterSpec(curveName))
        return kpg.generateKeyPair()
    }

    // Hybrid Encryption: ECDH (Key Agreement) -> AES
    // In a real scenario, you need the Recipient's Public Key.
    // For this local simulation, we are using the local key pair to demonstrate the math speed.
    fun encrypt(msg: String, publicKey: PublicKey, privateKey: PrivateKey): String {
        val secretKey = generateSharedSecret(privateKey, publicKey)

        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val encryptedBytes = cipher.doFinal(msg.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
    }

    fun decrypt(encryptedMsg: String, publicKey: PublicKey, privateKey: PrivateKey): String {
        val secretKey = generateSharedSecret(privateKey, publicKey)

        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)

        val decodedBytes = Base64.decode(encryptedMsg, Base64.NO_WRAP)
        val decryptedBytes = cipher.doFinal(decodedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    private fun generateSharedSecret(privateKey: PrivateKey, publicKey: PublicKey): SecretKeySpec {
        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(privateKey)
        keyAgreement.doPhase(publicKey, true)
        // Use the first 32 bytes for AES-256
        val sharedSecret = keyAgreement.generateSecret()
        return SecretKeySpec(sharedSecret.copyOf(32), "AES")
    }
}