package ai.billy.stellarwallet.send

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ai.billy.stellarwallet.R
import ai.billy.stellarwallet.databinding.ActivityConfirmTransactionBinding
import ai.billy.stellarwallet.helper.StringFormat
import ai.billy.stellarwallet.helper.StringFormat.Companion.formatBigDecimal
import ai.billy.stellarwallet.helper.StringFormat.Companion.formatNumber
import ai.billy.stellarwallet.helper.StringFormat.Companion.formatPublicKey
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class ConfirmTransactionActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityConfirmTransactionBinding.inflate(layoutInflater)
    }

    private val viewModel: ConfirmTransactionViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupTransactionDetails()
        setupClickListeners()
        observePaymentTransactionState()
    }

    private fun setupTransactionDetails() {

        val transferPayment = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("transfer_payment", TransferPaymentDto::class.java)
        } else {
            intent.getParcelableExtra("transfer_payment")
        }

        transferPayment?.let { payment ->
            viewModel.setTransferPaymentDto(payment)
        }

        transferPayment?.let { payment ->
            val amount = formatBigDecimal(payment.amount)
            val amountFormat = formatNumber(amount)
            binding.apply {
                amountLarge.text = "$amountFormat ${payment.token}"
                amountValue.text = amountFormat
                tokenValue.text = payment.token
                toAccountValue.text = StringFormat.formatPublicKey(payment.toAccount)
                noteValue.text = payment.note
                confirmButton.isEnabled = true
            }
        }

        val publicAddress = viewModel.getPublicAddress()
        binding.fromAccountValue.text = formatPublicKey(publicAddress)

    }

    private fun setupClickListeners() {
        binding.confirmButton.setOnClickListener {
            showConfirmationDialog()
        }
    }

    private fun observePaymentTransactionState() {
        viewModel.paymentTransactionState.observe(this) { state ->
            when (state) {
                is PaymentTransactionState.Loading -> {
                    showLoading(true)
                }

                is PaymentTransactionState.Success -> {
                    showLoading(false)
                    showSuccessAndFinish(state.transactionSuccessfulDto)
                }

                is PaymentTransactionState.Error -> {
                    showLoading(false)
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Confirm Transaction")
            .setMessage("Are you sure you want to send this transaction?")
            .setPositiveButton("Confirm") { _, _ ->
                processTransaction()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun processTransaction() {
        viewModel.processPayment()
    }

    private fun showSuccessAndFinish(state : TransferSuccessfulDto) {
        val intent = TransactionSuccessfulActivity.newIntent(this, state)
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            confirmButton.isEnabled = !isLoading
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    companion object {
        fun newIntent(
            context: Context,
            transferPaymentDto: TransferPaymentDto
        ): Intent {
            return Intent(context, ConfirmTransactionActivity::class.java).apply {
                putExtra("transfer_payment", transferPaymentDto)
            }
        }
    }
}