package information.security.securemessaging

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import information.security.securemessaging.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAlgorithmAndKeySizeSelector()
        setupButtons()
    }

    private fun setupAlgorithmAndKeySizeSelector() {
        binding.algorithmSelector.setOnCheckedChangeListener { _, _ ->
            updateKeySizeSpinner()
        }
        // Initial setup
        updateKeySizeSpinner()
    }

    private fun updateKeySizeSpinner() {
        val keySizes = if (binding.rsaButton.isChecked) {
            arrayOf("1024", "2048")
        } else {
            arrayOf("256", "384", "521")
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, keySizes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.keySizeSelector.adapter = adapter
    }

    private fun setupButtons() {
        binding.encryptButton.setOnClickListener {
            log("Encrypt & Send button clicked")
        }

        binding.decryptButton.setOnClickListener {
            log("Decrypt button clicked")
        }
    }

    private fun log(message: String) {
        val currentLog = binding.logView.text.toString()
        binding.logView.text = if (currentLog == "Logs...") {
            message
        } else {
            "$currentLog\n$message"
        }
    }
}