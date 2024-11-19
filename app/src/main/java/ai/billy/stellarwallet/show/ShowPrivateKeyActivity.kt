package ai.billy.stellarwallet.show

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ai.billy.stellarwallet.R
import ai.billy.stellarwallet.databinding.ActivityShowPrivateKeyBinding
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.widget.Toast
import androidx.activity.viewModels


class ShowPrivateKeyActivity : AppCompatActivity() {

    private val binding by lazy { ActivityShowPrivateKeyBinding.inflate(layoutInflater) }

    private val viewModel: ShowPrivateKeyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        observeViewModel()
        initializeUI()

        viewModel.loadSecretKey()
    }

    private fun observeViewModel() {
        viewModel.secretKey.observe(this) {
            binding.secretKeyText.text = it
        }
    }

    private fun initializeUI() {
        binding.clipboardButton.setOnClickListener {
            copyAccountId(viewModel.secretKey.value ?: "")
        }
    }

    private fun copyAccountId(textToCopy: String) {
        // Copy text to clipboard
        val clipboard: ClipboardManager =
            getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", textToCopy)
        clipboard.setPrimaryClip(clip)
        // Show a toast to indicate text has been copied
        Toast.makeText(this@ShowPrivateKeyActivity, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun newIntent(activity: Activity): Intent {
            val intent = Intent(activity, ShowPrivateKeyActivity::class.java)
            return intent
        }
    }
}