package ai.billy.stellarwallet.walletdashboard

import ai.billy.stellarwallet.helper.LocalStoreImpl
import ai.billy.stellarwallet.repository.StellarRepository
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.math.BigDecimal


class WalletViewModel(application: Application) : AndroidViewModel(application) {

    private var localStorage: LocalStoreImpl = LocalStoreImpl(application)

    private val stellarRepository = StellarRepository()

    private val _balance = MutableLiveData<BigDecimal>()
    val balance: LiveData<BigDecimal> = _balance


    fun loadBalance() {

        _balance.value = BigDecimal(localStorage.getAvailableBalance())

        viewModelScope.launch {
            try {
                val publicKey = localStorage.getStellarAccountId() ?: ""
                val balance = stellarRepository.checkStellarWalletBalance(publicKey)

                balance?.let {
                    localStorage.setAvailableBalance(it.balance)
                    _balance.value = BigDecimal(it.balance)
                } ?: {
                    _balance.value = BigDecimal.ZERO
                }

            } catch (e: Exception) {
                // error handling
            }

        }
    }

}