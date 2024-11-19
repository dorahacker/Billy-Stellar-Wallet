package ai.billy.stellarwallet.send

import ai.billy.stellarwallet.helper.LocalStoreImpl
import ai.billy.stellarwallet.helper.StringFormat.Companion.formatBigDecimal
import ai.billy.stellarwallet.helper.USDC_CODE
import ai.billy.stellarwallet.helper.USDC_ISSUER
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.stellar.sdk.AssetTypeCreditAlphaNum4
import org.stellar.sdk.Network
import org.stellar.sdk.PaymentOperation
import org.stellar.sdk.Transaction
import org.stellar.sdk.TransactionBuilder
import org.stellar.walletsdk.StellarConfiguration
import org.stellar.walletsdk.Wallet
import org.stellar.walletsdk.horizon.SigningKeyPair


class ConfirmTransactionViewModel(application: Application) : AndroidViewModel(application) {

    var localStorage: LocalStoreImpl = LocalStoreImpl(application)

    private val _paymentTransactionState = MutableLiveData<PaymentTransactionState>()
    val paymentTransactionState: LiveData<PaymentTransactionState> = _paymentTransactionState

    private var transferPaymentDto = MutableLiveData<TransferPaymentDto>()

    fun setTransferPaymentDto(transferPaymentDto: TransferPaymentDto) {
        this.transferPaymentDto.value = transferPaymentDto
    }

    fun getPublicAddress(): String {
        return localStorage.getStellarAccountId() ?: ""
    }

    fun processPayment() {
        viewModelScope.launch {
            transferPaymentDto.value?.let { payment ->
                transferUSDC(
                    localStorage.getEncryptedPhrase() ?: "",
                    payment.toAccount,
                    formatBigDecimal(payment.amount)
                )
            }
        }
    }

    private suspend fun transferUSDC(
        sourceAccountSecret: String,
        destinationAccountId: String,
        amount: String
    ) = withContext(Dispatchers.IO) {

        _paymentTransactionState.postValue(PaymentTransactionState.Loading)

        // Setup Stellar wallet for Testnet
        val wallet = Wallet(StellarConfiguration.Testnet)

        try {
            // Load sender's account using the secret key (replace with your own secret key)
            val senderAccount = SigningKeyPair.fromSecret(sourceAccountSecret)

            // USDC Asset ကို Issuer နဲ့ သတ်မှတ်ခြင်း
            val usdcAsset = AssetTypeCreditAlphaNum4(
                USDC_CODE,
                USDC_ISSUER
            )

            // Fetch sender account details from Horizon
            val horizon = wallet.stellar().server
            val account = horizon.accounts().account(senderAccount.address)

            // Build Payment operation (USDC transfer)
            val paymentOperation = PaymentOperation.Builder(
                destinationAccountId,  // Receiver's Public Key
                usdcAsset,          // USDC Asset
                amount              // Amount to transfer (in USDC)
            ).build()

            // Create and sign transaction
            val transaction = TransactionBuilder(account, Network.TESTNET)
                .addOperation(paymentOperation)
                .setTimeout(180)
                .setBaseFee(Transaction.MIN_BASE_FEE.toLong())
                .build()

            // Sign the transaction with the sender's key
            transaction.sign(senderAccount.keyPair)

            // Submit transaction to Horizon Testnet
            val response = horizon.submitTransaction(transaction)


            // Output transaction result
            if (response.isSuccess) {
                _paymentTransactionState.postValue(
                    PaymentTransactionState.Success(
                        TransferSuccessfulDto(
                            id = response.hash,
                            amount = amount,
                            token = "USDC",
                            fromAccount = senderAccount.address,
                            toAccount = destinationAccountId,
                            note = ""
                        )
                    )
                )
            } else {
                _paymentTransactionState.postValue(
                    PaymentTransactionState.Error("Transaction Failed")
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _paymentTransactionState.postValue(
                PaymentTransactionState.Error(e.message ?: "Unknown error occurred")
            )
        }

        wallet.close()
    }

}