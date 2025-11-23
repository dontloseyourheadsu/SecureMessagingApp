package information.security.securemessaging

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import information.security.securemessaging.databinding.ActivityMainBinding
import java.security.KeyPair

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var keyPair: KeyPair? = null
    private var encryptedMessage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAlgorithmAndKeySizeSelector()
        setupButtons()
        // Initial generation
        updateKeySizeSpinner()
    }

    private fun setupAlgorithmAndKeySizeSelector() {
        // Listener for Radio Buttons (RSA vs ECC)
        binding.algorithmSelector.setOnCheckedChangeListener { _, _ ->
            updateKeySizeSpinner()
        }

        // Listener for Spinner (Key Size)
        binding.keySizeSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                generateKeys() // Regenerate keys whenever size changes
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun updateKeySizeSpinner() {
        val keySizes = if (binding.rsaButton.isChecked) {
            arrayOf("1024", "2048")
        } else {
            // Placeholder for ECC key sizes
            arrayOf("256", "384", "521")
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, keySizes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.keySizeSelector.adapter = adapter

        // Note: Setting the adapter usually triggers onItemSelected,
        // so generateKeys() will be called automatically here.
    }

    private fun setupButtons() {
        binding.encryptButton.setOnClickListener {
            val message = binding.messageField.text.toString()

            if (message.isEmpty()) {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (keyPair == null) {
                Toast.makeText(this, "Keys not generated yet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val startTime = System.currentTimeMillis()

                // CALLING YOUR UTILS CLASS HERE
                encryptedMessage = RsaUtils.encrypt(message, keyPair!!.public)

                val endTime = System.currentTimeMillis()

                log("--- Encryption ---")
                log("Plaintext: $message")
                log("Encrypted (Base64): $encryptedMessage")
                log("Time: ${endTime - startTime} ms")

            } catch (e: Exception) {
                log("Error Encrypting: ${e.message}")
                // Common error: Input too long for RSA key size
            }
        }

        binding.decryptButton.setOnClickListener {
            if (encryptedMessage == null) {
                Toast.makeText(this, "No encrypted message found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (keyPair == null) {
                Toast.makeText(this, "Keys missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val startTime = System.currentTimeMillis()

                // CALLING YOUR UTILS CLASS HERE
                val decryptedMessage = RsaUtils.decrypt(encryptedMessage!!, keyPair!!.private)

                val endTime = System.currentTimeMillis()

                log("--- Decryption ---")
                log("Result: $decryptedMessage")
                log("Time: ${endTime - startTime} ms")

            } catch (e: Exception) {
                log("Error Decrypting: ${e.message}")
            }
        }
    }

    private fun generateKeys() {
        // Clear previous state when generating new keys
        encryptedMessage = null
        binding.logView.text = "Logs..."

        if (binding.rsaButton.isChecked) {
            try {
                val keySizeString = binding.keySizeSelector.selectedItem.toString()
                val keySize = keySizeString.toInt()

                log("Generating RSA KeyPair ($keySize bits)...")
                val startTime = System.currentTimeMillis()

                // CALLING YOUR UTILS CLASS HERE
                keyPair = RsaUtils.generateKeyPair(keySize)

                val endTime = System.currentTimeMillis()
                log("Success! Key generation took ${endTime - startTime} ms")

            } catch (e: Exception) {
                log("Error generating keys: ${e.message}")
            }
        } else {
            log("ECC implementation pending...")
            keyPair = null
        }
    }

    private fun log(message: String) {
        val currentLog = binding.logView.text.toString()
        // Auto-scroll or simple append logic
        binding.logView.text = if (currentLog == "Logs...") {
            message
        } else {
            "$currentLog\n$message"
        }
    }
}