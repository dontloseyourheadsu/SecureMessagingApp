package information.security.securemessaging

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import information.security.securemessaging.databinding.ActivityMainBinding
import java.security.KeyPair

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var rsaKeyPair: KeyPair? = null
    private var eccKeyPair: KeyPair? = null // Store ECC keys separately
    private var encryptedMessage: String? = null

    // Permission Request Code
    private val SMS_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAlgorithmAndKeySizeSelector()
        setupButtons()

        // Initial setup
        updateKeySizeSpinner()
    }

    private fun setupAlgorithmAndKeySizeSelector() {
        binding.algorithmSelector.setOnCheckedChangeListener { _, _ ->
            updateKeySizeSpinner()
            // Clear logs to avoid confusion between algorithms
            binding.logView.text = "Logs cleared..."
            encryptedMessage = null
        }

        binding.keySizeSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                generateKeys()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    /**
     * Updates the spinner with available key sizes based on the selected algorithm.
     */
    private fun updateKeySizeSpinner() {
        val keySizes = if (binding.rsaButton.isChecked) {
            arrayOf("1024", "2048", "4096")
        } else {
            arrayOf("256", "384", "521")
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, keySizes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.keySizeSelector.adapter = adapter
    }

    /**
     * Generates a KeyPair for the selected algorithm and key size.
     * Logs the time taken for generation.
     */
    private fun generateKeys() {
        val keySize = binding.keySizeSelector.selectedItem.toString().toInt()
        val isRsa = binding.rsaButton.isChecked
        val algoName = if (isRsa) "RSA" else "ECC"

        try {
            log("Generating $algoName KeyPair ($keySize bits)...")
            val startTime = System.nanoTime()

            if (isRsa) {
                rsaKeyPair = RsaUtils.generateKeyPair(keySize)
            } else {
                eccKeyPair = EccUtils.generateKeyPair(keySize)
            }

            val endTime = System.nanoTime()
            val duration = (endTime - startTime) / 1_000_000.0
            log("KeyGen Success! Time: $duration ms")

        } catch (e: Exception) {
            log("Error generating keys: ${e.message}")
        }
    }

    private fun setupButtons() {
        binding.encryptButton.setOnClickListener {
            val phone = binding.phoneNumberField.text.toString()
            val message = binding.messageField.text.toString()

            if (message.isEmpty()) {
                Toast.makeText(this, "Enter a message", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check permissions before encrypting/sending
            if (checkSmsPermission()) {
                performEncryptionAndSend(phone, message)
            } else {
                requestSmsPermission()
            }
        }

        binding.decryptButton.setOnClickListener {
            performDecryption()
        }
    }

    /**
     * Encrypts the message using the generated keys and sends it via SMS.
     * Logs the encryption time and cipher length.
     */
    private fun performEncryptionAndSend(phone: String, message: String) {
        val isRsa = binding.rsaButton.isChecked

        // Ensure we have keys
        if ((isRsa && rsaKeyPair == null) || (!isRsa && eccKeyPair == null)) {
            Toast.makeText(this, "Please generate keys first", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val startTime = System.nanoTime()

            if (isRsa) {
                // RSA Encryption
                encryptedMessage = RsaUtils.encrypt(message, rsaKeyPair!!.public)
            } else {
                // ECC Encryption (Hybrid)
                encryptedMessage = EccUtils.encrypt(message, eccKeyPair!!.public, eccKeyPair!!.private)
            }

            val endTime = System.nanoTime()
            val duration = (endTime - startTime) / 1_000_000.0

            log("--- ENCRYPTION (${if (isRsa) "RSA" else "ECC"}) ---")
            log("Time: $duration ms")
            log("Cipher Length: ${encryptedMessage!!.length} chars")

            // SEND SMS LOGIC
            if (phone.isNotEmpty()) {
                sendSms(phone, encryptedMessage!!)
            } else {
                log("No phone number entered. Encryption only.")
            }

        } catch (e: Exception) {
            log("Encryption Failed: ${e.message}")
        }
    }

    /**
     * Sends the encrypted message via SMS.
     * Handles multipart messages for long ciphertexts.
     */
    private fun sendSms(phoneNumber: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            // Standard SMS is 160 chars. We MUST use multipart.
            val parts = smsManager.divideMessage(message)

            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)

            log("SMS Sent to $phoneNumber (${parts.size} parts)")
            Toast.makeText(this, "SMS Sent!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            log("SMS Failed: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun performDecryption() {
        // 1. Get the text currently on the screen
        val textToDecrypt = binding.messageField.text.toString().trim()

        if (textToDecrypt.isEmpty()) {
            Toast.makeText(this, "Paste encrypted text first", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Ensure keys exist
        if (binding.rsaButton.isChecked && rsaKeyPair == null) {
            Toast.makeText(this, "Generate keys first!", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val startTime = System.nanoTime()
            val decryptedText: String

            // 3. Decrypt the text from the screen, NOT the internal variable
            if (binding.rsaButton.isChecked) {
                decryptedText = RsaUtils.decrypt(textToDecrypt, rsaKeyPair!!.private)
            } else {
                decryptedText = EccUtils.decrypt(textToDecrypt, eccKeyPair!!.public, eccKeyPair!!.private)
            }

            val endTime = System.nanoTime()
            val duration = (endTime - startTime) / 1_000_000.0

            // 4. Update the text box with the result
            binding.messageField.setText(decryptedText)

            log("--- DECRYPTION ---")
            log("Time: $duration ms")

        } catch (e: Exception) {
            log("Decryption Failed: ${e.message}")
            Toast.makeText(this, "Decryption Failed (Wrong Key?)", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Permission Handling Boilerplate ---
    private fun checkSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestSmsPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE), SMS_PERMISSION_CODE)
    }

    private fun log(message: String) {
        val currentLog = binding.logView.text.toString()
        binding.logView.text = "$currentLog\n$message"
        // Auto scroll is handled by the ScrollView in XML
    }
}