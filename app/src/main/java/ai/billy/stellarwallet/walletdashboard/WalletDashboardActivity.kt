package ai.billy.stellarwallet.walletdashboard

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ai.billy.stellarwallet.R
import ai.billy.stellarwallet.databinding.ActivityWalletDashboardBinding
import ai.billy.stellarwallet.history.PaymentOperationHistoryActivity
import ai.billy.stellarwallet.receive.ReceiveActivity
import ai.billy.stellarwallet.send.SendActivity
import ai.billy.stellarwallet.show.ShowPrivateKeyActivity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.viewModels
import java.math.RoundingMode


class WalletDashboardActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityWalletDashboardBinding.inflate(layoutInflater)
    }

    private val viewModel: WalletViewModel by viewModels()

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
    }

    private fun observeViewModel() {
        viewModel.balance.observe(this) { balance ->
            binding.tvBalanceAmount.text = "$ ${balance.setScale(2, RoundingMode.HALF_UP)}"
            binding.usdTextView.text = "${balance.setScale(2, RoundingMode.HALF_UP)} USD"
            binding.usdcAmountTextView.text = "$${balance.setScale(2, RoundingMode.HALF_UP)}"
        }
    }

    private fun initializeUI() {
        setUpReceiveButton()
        setUpSendButton()
        setUpTransactionsHistoryButton()
        setUpShowPrivate()
        setUpSellButton()
    }

    private fun setUpShowPrivate() {
        binding.btnShowPrivate.setOnClickListener {
            val intent = ShowPrivateKeyActivity.newIntent(this)
            startActivity(intent)
        }
    }

    private fun setUpTransactionsHistoryButton() {
        binding.assetItemUsdc.setOnClickListener {
            val intent = PaymentOperationHistoryActivity.newIntent(this)
            startActivity(intent)
        }
    }

    private fun setUpSendButton() {
        binding.btnSend.setOnClickListener {
            val intent = SendActivity.newIntent(this)
            startActivity(intent)
        }
    }

    private fun setUpReceiveButton() {
        binding.btnReceive.setOnClickListener {
            val intent = ReceiveActivity.newIntent(this)
            startActivity(intent)
        }
    }

    private fun setUpSellButton() {
        binding.btnSell.setOnClickListener {
            Toast.makeText(this, "Currently not available!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadBalance()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, WalletDashboardActivity::class.java)
            return intent
        }
    }
}