package ai.billy.stellarwallet.restore

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ai.billy.stellarwallet.R
import ai.billy.stellarwallet.create.WalletState
import ai.billy.stellarwallet.databinding.ActivityRestoreWalletBinding
import ai.billy.stellarwallet.helper.validateStellarKey
import ai.billy.stellarwallet.walletdashboard.WalletDashboardActivity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged


class RestoreWalletActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityRestoreWalletBinding.inflate(layoutInflater)
    }

    private val viewModel: RestoreWalletViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupViews()
        observeWalletState()
    }

    private fun setupViews() {
        setupClickListeners()
        setUpTextChangedListener()
    }

    private fun setupClickListeners() {
        binding.btnRecoverKey.setOnClickListener {
            viewModel.importWallet()
        }
    }

    private fun setUpTextChangedListener() {
        binding.secretInput.doAfterTextChanged { seed ->
            if (validateStellarSecretKey(seed.toString())) {
                viewModel.setSeed(seed.toString())
                binding.btnRecoverKey.isEnabled = true
            } else {
                binding.btnRecoverKey.isEnabled = false
            }
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

    private fun validateStellarSecretKey(seed: String): Boolean {
        with(binding) {
            return if (seed.isEmpty() || validateStellarKey(seed)) {
                secretInputLayout.error = null
                secretInputLayout.isEndIconVisible = true;
                true
            } else {
                secretInputLayout.error = "Invalid Stellar Secret key"
                secretInputLayout.isEndIconVisible = true
                false
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            // Disable buttons during loading
            btnRecoverKey.isEnabled = !isLoading
            secretInputLayout.isEnabled = !isLoading
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun handleWalletCreationSuccess() {
        // Navigate to main wallet screen or show success message
        Toast.makeText(
            this,
            "Wallet Import successfully!",
            Toast.LENGTH_SHORT
        ).show()

        // Navigate to the main wallet dashboard screen
        startWalletDashboardActivity()
    }

    private fun startWalletDashboardActivity() {
        val intent = WalletDashboardActivity.newIntent(this)
        startActivity(intent)
        finish()
    }

    private fun handleWalletCreationError(error: String) {
        Toast.makeText(
            this,
            "Failed to create wallet: $error",
            Toast.LENGTH_LONG
        ).show()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, RestoreWalletActivity::class.java)
            return intent
        }
    }

}