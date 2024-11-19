package ai.billy.stellarwallet.create

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ai.billy.stellarwallet.R
import ai.billy.stellarwallet.databinding.ActivityCreateWalletBinding
import ai.billy.stellarwallet.restore.RestoreWalletActivity
import ai.billy.stellarwallet.walletdashboard.WalletDashboardActivity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels


class CreateWalletActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityCreateWalletBinding.inflate(layoutInflater)
    }

    private val viewModel: CreateWalletViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupClickListeners()
        observeWalletState()
    }

    private fun setupClickListeners() {
        binding.buttonYes.setOnClickListener {
            viewModel.createWallet()
        }

        binding.buttonNo.setOnClickListener {
            val intent = RestoreWalletActivity.newIntent(this)
            startActivity(intent)
            finish()
        }
    }

    private fun observeWalletState() {
        viewModel.walletState.observe(this) { state ->
            when (state) {
                is WalletState.Loading -> {
                    showLoading(true)
                }

                is WalletState.Success -> {
                    showLoading(false)
                    handleWalletCreationSuccess()
                }

                is WalletState.Error -> {
                    showLoading(false)
                    handleWalletCreationError(state.message)
                }
            }
        }
    }

    private fun handleWalletCreationSuccess() {
        // Navigate to main wallet screen or show success message
        Toast.makeText(
            this,
            "Wallet created successfully!",
            Toast.LENGTH_SHORT
        ).show()

        // Navigate to the main wallet dashboard screen
        startWalletDashboardActivity()
    }

    private fun handleWalletCreationError(error: String) {
        Toast.makeText(
            this,
            "Failed to create wallet: $error",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            // Disable buttons during loading
            buttonYes.isEnabled = !isLoading
            buttonNo.isEnabled = !isLoading
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun startWalletDashboardActivity() {
        val intent = WalletDashboardActivity.newIntent(this)
        startActivity(intent)
        finish()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, CreateWalletActivity::class.java)
            return intent
        }
    }
}