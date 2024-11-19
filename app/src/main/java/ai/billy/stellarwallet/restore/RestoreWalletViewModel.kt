package ai.billy.stellarwallet.restore

import ai.billy.stellarwallet.create.WalletState
import ai.billy.stellarwallet.helper.LocalStoreImpl
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.stellar.sdk.KeyPair

class RestoreWalletViewModel(application: Application) : AndroidViewModel(application) {

    private var localStorage: LocalStoreImpl = LocalStoreImpl(application)

    private val _walletState = MutableLiveData<WalletState>()
    val walletState: LiveData<WalletState> = _walletState

    private val seedLiveData = MutableLiveData<String>()

    fun setSeed(seed: String) {
        seedLiveData.postValue(seed)
    }

    fun importWallet() {
        seedLiveData.value?.let { seed ->
            viewModelScope.launch {
                try {
                    _walletState.value = WalletState.Loading

                    // Import Stellar keypair
                    val keypair = KeyPair.fromSecretSeed(seed)
                    val publicKey = keypair.accountId
                    val secretKey = String(keypair.secretSeed)

                    localStorage.setStellarAccountId(publicKey)
                    localStorage.setEncryptedPhrase(secretKey)

                    _walletState.value = WalletState.Success(
                        publicKey = publicKey,
                        secretKey = secretKey
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    _walletState.value = WalletState.Error(e.message ?: "Unknown error occurred")
                }
            }
        }
    }

}