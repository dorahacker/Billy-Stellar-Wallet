package ai.billy.stellarwallet.send

import ai.billy.stellarwallet.helper.LocalStoreImpl
import ai.billy.stellarwallet.repository.StellarRepository
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.BigDecimal


class SendViewModel(application: Application) : AndroidViewModel(application) {

    private var localStorage: LocalStoreImpl = LocalStoreImpl(application)

    private val stellarRepository = StellarRepository()

    private val _currentBalance = MutableLiveData<BigDecimal>()
    val currentBalance: LiveData<BigDecimal> = _currentBalance

    private val _usdtBalance = MutableLiveData<BigDecimal>()
    val usdtBalance: LiveData<BigDecimal> = _usdtBalance

    private val _toAddress = MutableLiveData<String>()
    val toAddress: LiveData<String> = _toAddress

    private val _amount = MutableLiveData<BigDecimal>()
    val amount: LiveData<BigDecimal> = _amount

    private val _usdValue = MutableLiveData<BigDecimal>()
    val usdValue: LiveData<BigDecimal> = _usdValue

    private val _isNextEnabled = MutableLiveData<Boolean>()
    val isNextEnabled: LiveData<Boolean> = _isNextEnabled

    private val _invalidAmount = MutableLiveData<String?>()
    val invalidAmount: LiveData<String?> = _invalidAmount

    fun getPublicAddress(): String {
        return localStorage.getStellarAccountId() ?: "private"
    }

    fun setToAddress(address: String) {
        _toAddress.value = address
        validateInput()
    }

    fun setAmount(amount: String) {
        try {
            _amount.value = BigDecimal(amount)
            validateInput()
            calculateUsdValue()
            checkAmountGreater()
        } catch (e: NumberFormatException) {
            _amount.value = BigDecimal.ZERO
            _usdValue.value = BigDecimal.ZERO
        }
    }

    fun setMaxAmount() {
        _usdtBalance.value = currentBalance.value
    }

    private fun calculateUsdValue() {
        // Implement price fetch from your preferred price feed
        val usdtPrice = BigDecimal("1.00") // Example: 1 USDT = $1.00
        _usdValue.value = _amount.value?.multiply(usdtPrice)
    }

    private fun validateInput() {
        val isValidAddress = !_toAddress.value.isNullOrEmpty()
        val isValidAmount = checkAmountGreater()
        _isNextEnabled.value = isValidAddress && isValidAmount
    }

    private fun checkAmountGreater(): Boolean {

        val isValidAmount = (_amount.value ?: BigDecimal.ZERO) > BigDecimal.ZERO
                && (_amount.value ?: BigDecimal.ZERO) <= currentBalance.value

        if (isValidAmount) {
            _invalidAmount.postValue(null)
        } else {
            _invalidAmount.postValue("Not enough balance")
        }

        return isValidAmount
    }

    fun loadBalance() {

        _currentBalance.value = BigDecimal(localStorage.getAvailableBalance())

        viewModelScope.launch {
            try {
                val publicKey = localStorage.getStellarAccountId() ?: ""
                val balance = stellarRepository.checkStellarWalletBalance(publicKey)

                balance?.let {
                    localStorage.setAvailableBalance(it.balance)
                    _currentBalance.value = BigDecimal(it.balance)
                } ?: {
                    _currentBalance.value = BigDecimal.ZERO
                }

            } catch (e: Exception) {
                Timber.tag("balance").d("error : %s", e.message);
            }

        }
    }
}