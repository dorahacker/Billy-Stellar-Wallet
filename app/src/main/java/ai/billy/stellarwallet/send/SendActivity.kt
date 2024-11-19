package ai.billy.stellarwallet.send

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ai.billy.stellarwallet.R
import ai.billy.stellarwallet.databinding.ActivitySendBinding
import ai.billy.stellarwallet.helper.validateStellarPublic
import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import java.math.BigDecimal
import java.math.RoundingMode


class SendActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivitySendBinding.inflate(layoutInflater)
    }

    private val viewModel: SendViewModel by viewModels()

    private val barcodeLauncher = registerForActivityResult<ScanOptions, ScanIntentResult>(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents.isNullOrEmpty()) {
            Toast.makeText(this@SendActivity, "Cancelled", Toast.LENGTH_LONG)
                .show()
        } else {

            val scannedData: String = result.contents

            if (scannedData.startsWith("stellar:")) {
                try {
                    // Format: "staller:walletAddress?amount=value"
                    val parts: List<String> = scannedData.split("\\?".toRegex())
                    val walletAddress = parts[0].replace("stellar:", "")

                    // Check if amount is present
                    var amount = "0"
                    // Default amount to 0 if not provided
                    if (parts.size > 1 && parts[1].contains("amount=")) {
                        amount = parts[1].replace("amount=", "")
                    }

                    binding.addressInput.setText(walletAddress)
                    binding.amountInput.setText(amount)

                } catch (e: Exception) {
                    Toast.makeText(this, "Invalid QR Code format", Toast.LENGTH_LONG).show()
                }
            } else {
                binding.addressInput.setText(result.contents)
            }
        }
    }


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
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadBalance()
    }

    private fun setupViews() {

        binding.scanButton.setOnClickListener {
            barcodeLauncher.launch(getScanOptions())
        }

        binding.maxValueText.setOnClickListener {
            viewModel.setMaxAmount()
        }

        binding.addressInput.doAfterTextChanged {
            if (validateStellarAddress(it.toString())) {
                viewModel.setToAddress(it.toString())
            }
        }

        binding.addressInputLayout.setErrorIconOnClickListener {
            binding.addressInput.text?.clear()
        }

        binding.amountInput.doAfterTextChanged {
            viewModel.setAmount(it.toString())
        }

        binding.nextButton.setOnClickListener {
            // Implement next step logic
            val intent = ConfirmTransactionActivity.newIntent(
                this,
                TransferPaymentDto(
                    amount = viewModel.amount.value ?: BigDecimal.ZERO,
                    token = "USDC",
                    fromAccount = "",
                    toAccount = viewModel.toAddress.value ?: "",
                    note = ""
                )
            )
            startActivity(intent)
            finish()
        }
    }


    private fun observeViewModel() {

        viewModel.currentBalance.observe(this) {
            binding.amountInputLayout.helperText =
                "Available Balance : ${it.setScale(2, RoundingMode.HALF_UP)}"
        }

        viewModel.usdtBalance.observe(this) { usdc ->
            val value = "${usdc.setScale(2, RoundingMode.HALF_UP)}"
            binding.amountInput.setText(value)
        }

        viewModel.usdValue.observe(this) { usdValue ->
            binding.usdValueText.text = "≈ $${usdValue.setScale(2, RoundingMode.HALF_UP)}"
        }

        viewModel.invalidAmount.observe(this) { invalidAmount ->
            binding.amountInputLayout.error = invalidAmount
        }

        viewModel.isNextEnabled.observe(this) { isEnabled ->
            binding.nextButton.isEnabled = isEnabled
        }
    }


    private fun validateStellarAddress(address: String): Boolean {
        with(binding) {
            return if (address.isEmpty() || validateStellarPublic(address)) {
                addressInputLayout.error = null
                addressInputLayout.isEndIconVisible = true;
                true
            } else if(viewModel.getPublicAddress() == address) {
                addressInputLayout.error = "Cannot send yor Address"
                addressInputLayout.isEndIconVisible = true
                false
            } else {
                addressInputLayout.error = "Invalid Stellar Public Address"
                addressInputLayout.isEndIconVisible = true
                false
            }
        }
    }

    private fun getScanOptions(): ScanOptions {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("Scan QR Code")
        // Use a specific camera of the device
        options.setCameraId(0)
        options.setBeepEnabled(true)
        options.setBarcodeImageEnabled(true)
        return options
    }

    companion object {
        fun newIntent(activity: Activity): Intent {
            val intent = Intent(activity, SendActivity::class.java)
            return intent
        }
    }

}