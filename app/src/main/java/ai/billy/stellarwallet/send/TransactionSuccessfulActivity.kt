package ai.billy.stellarwallet.send

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ai.billy.stellarwallet.databinding.ActivityTransactionSuccessfulBinding
import ai.billy.stellarwallet.helper.StringFormat.Companion.formatNumber
import ai.billy.stellarwallet.helper.StringFormat.Companion.formatPublicKey
import ai.billy.stellarwallet.helper.StringFormat.Companion.formatTransactionHash
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Date


class TransactionSuccessfulActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityTransactionSuccessfulBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(binding.root)

        setupWindowInsets()
        setupTransactionDetails()
        setupClickListeners()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupTransactionDetails() {

        val transactionStatus = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("transaction_status", TransferSuccessfulDto::class.java)
        } else {
            intent.getParcelableExtra("transaction_status")
        }

        transactionStatus?.let { transaction ->

            val amount = formatNumber(transaction.amount)

            binding.apply {
                transactionTimeTextView.text = Date().toString()
                amountLargeTextView.text = "${amount} ${transaction.token}"
                amountTextView.text = amount
                tokenTextView.text = transaction.token
                fromTextView.text = formatPublicKey(transaction.fromAccount)
                toTextView.text = formatPublicKey(transaction.toAccount)
                idTextView.text = formatTransactionHash(transaction.id)
                noteTextView.text = transaction.note
            }
        }
    }

    private fun setupClickListeners() {
        binding.doneButton.setOnClickListener {
            finish()
        }
    }

    companion object {
        fun newIntent(
            context: Context,
            transferSuccessfulDto: TransferSuccessfulDto
        ): Intent {
            return Intent(context, TransactionSuccessfulActivity::class.java).apply {
                putExtra("transaction_status", transferSuccessfulDto)
            }
        }
    }
}