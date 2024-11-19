package ai.billy.stellarwallet.history

import ai.billy.stellarwallet.helper.LocalStoreImpl
import ai.billy.stellarwallet.repository.StellarPaymentOperation
import ai.billy.stellarwallet.repository.StellarRepository
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class PaymentOperationsHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private var localStorage: LocalStoreImpl = LocalStoreImpl(application)

    private val _transactions = MutableLiveData<List<StellarPaymentOperation>>()
    val transactions: LiveData<List<StellarPaymentOperation>> = _transactions

    private val _balance = MutableLiveData<String>()
    val balance: LiveData<String> = _balance

    private var cursor: String? = "now"
    private val stellarRepository = StellarRepository()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun loadBalance() {

        _balance.value = localStorage.getAvailableBalance()

        viewModelScope.launch {
            try {
                val publicKey = localStorage.getStellarAccountId() ?: ""
                val balance = stellarRepository.checkStellarWalletBalance(publicKey)
                _balance.value = balance?.balance ?: "0"
            } catch (e: Exception) {
                // error handling
            }

        }
    }

    fun loadTransactions() {

        if (_isLoading.value == true || cursor == null) return

        _isLoading.value = true

        val publicKey = localStorage.getStellarAccountId() ?: ""
        viewModelScope.launch {
            try {
                val result = stellarRepository.getPaymentOperations(cursor, publicKey)
                cursor = result.cursor
                _transactions.value = result.paymentOperations
                _isLoading.value = false
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
                _isLoading.value = false
            }
        }
    }


}