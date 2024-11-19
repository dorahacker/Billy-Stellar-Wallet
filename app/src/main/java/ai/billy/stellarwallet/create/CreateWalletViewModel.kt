package ai.billy.stellarwallet.create

import ai.billy.stellarwallet.helper.LocalStoreImpl
import ai.billy.stellarwallet.helper.USDC_CODE
import ai.billy.stellarwallet.helper.USDC_ISSUER
import ai.billy.stellarwallet.helper.network
import ai.billy.stellarwallet.helper.server
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import org.stellar.sdk.KeyPair

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.stellar.sdk.ChangeTrustAsset
import org.stellar.sdk.ChangeTrustOperation
import org.stellar.sdk.Server
import org.stellar.sdk.Transaction
import org.stellar.sdk.TransactionBuilder
import org.stellar.sdk.responses.SubmitTransactionResponse
import timber.log.Timber
import java.net.URL


class CreateWalletViewModel(application: Application) : AndroidViewModel(application) {

    private var localStorage: LocalStoreImpl = LocalStoreImpl(application)

    private val _walletState = MutableLiveData<WalletState>()
    val walletState: LiveData<WalletState> = _walletState

    fun createWallet() {
        viewModelScope.launch {
            try {

                _walletState.value = WalletState.Loading

                // Generate Stellar keypair
                val keypair = KeyPair.random()
                val publicKey = keypair.accountId
                val secretKey = String(keypair.secretSeed)

                // Fund the account on testnet
                fundTestnetAccount(publicKey)
                // Add USDC trustline
                addUSDTAsset(server, keypair)

                localStorage.setStellarAccountId(publicKey)
                localStorage.setEncryptedPhrase(secretKey)

                _walletState.value = WalletState.Success(
                    publicKey = publicKey,
                    secretKey = secretKey
                )

                Timber.tag("CREATE_WALLET").d("Public Key : $publicKey")

            } catch (e: Exception) {
                e.printStackTrace()
                _walletState.value = WalletState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    private suspend fun fundTestnetAccount(
        publicKey: String
    ) = withContext(Dispatchers.IO) {
        try {

            val friendBotUrl = String.format(
                "https://friendbot.stellar.org/?addr=%s",
                publicKey
            )

            URL(friendBotUrl).openStream()

            // Wait for account creation to be confirmed
//            server.accounts().account(publicKey)
        } catch (e: Exception) {
            throw Exception("Failed to fund account: ${e.message}")
        }
    }


    private suspend fun addUSDTAsset(
        server: Server,
        keypair: KeyPair
    ) = withContext(Dispatchers.IO) {

        try {

            val stellarAsset = ChangeTrustAsset.createNonNativeAsset(USDC_CODE, USDC_ISSUER)

            val trustLimit: String = Long.MAX_VALUE
                .toBigDecimal()
                .movePointLeft(7)
                .toPlainString()


            val usdcOperation = ChangeTrustOperation.Builder(stellarAsset, trustLimit)
                .setSourceAccount(keypair.accountId)
                .build()

            val usdcTransaction =
                TransactionBuilder(server.accounts().account(keypair.accountId), network)
                    .addOperation(usdcOperation)
                    .setTimeout(180)
                    .setBaseFee(Transaction.MIN_BASE_FEE.toLong())
                    .build()

            usdcTransaction.sign(keypair)

            val response: SubmitTransactionResponse = server.submitTransaction(usdcTransaction)

            if (!response.isSuccess) {
                throw Exception("Failed to create USDC trustline")
            }

            Timber.tag("CREATE_WALLET").d("Transaction Hash: ${response.hash}")
            Timber.tag("CREATE_WALLET").d("Result XDR: ${response.resultXdr}")
            Timber.tag("CREATE_WALLET").d("Envelope XDR: ${response.envelopeXdr}")
        } catch (e: Exception) {
            e.printStackTrace()
            println("Transaction Failed!")
            throw Exception("Failed to setup USDC trustline: ${e.message}")
        }

    }

}