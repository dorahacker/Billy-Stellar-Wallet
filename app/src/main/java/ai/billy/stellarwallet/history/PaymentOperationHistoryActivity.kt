package ai.billy.stellarwallet.history

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ai.billy.stellarwallet.R
import ai.billy.stellarwallet.databinding.ActivityPaymentOperationHistoryBinding
import ai.billy.stellarwallet.helper.StringFormat.Companion.formatNumber
import android.content.Context
import android.content.Intent
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar


class PaymentOperationHistoryActivity : AppCompatActivity() {

    private val binding by lazy { ActivityPaymentOperationHistoryBinding.inflate(layoutInflater) }

    private val viewModel: PaymentOperationsHistoryViewModel by viewModels()

    private lateinit var adapter: PaymentsAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        observeViewModel()

        viewModel.loadBalance()
        viewModel.loadTransactions()
    }


    private fun setupRecyclerView() {
        adapter = PaymentsAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                val loading: Boolean = viewModel.isLoading.value ?: false

                if (loading.not()
                    && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0
                ) {
                    viewModel.loadTransactions()
                }


            }
        })
    }

    private fun observeViewModel() {
        viewModel.transactions.observe(this) { transactions ->
            if(transactions.isNotEmpty()) {
                adapter.addPayments(transactions)
            }
        }

        viewModel.balance.observe(this) { balance ->
            binding.balanceText.text = "$ ${formatNumber(balance)}"
        }

        viewModel.isLoading.observe(this) { isLoading ->
            adapter.setLoadingMore(isLoading)
        }
    }

    companion object {

        fun newIntent(context: Context): Intent {
            val intent = Intent(context, PaymentOperationHistoryActivity::class.java)
            return intent
        }
    }
}