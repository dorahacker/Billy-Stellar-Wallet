package ai.billy.stellarwallet.send

sealed class PaymentTransactionState {
    data object Loading : PaymentTransactionState()
    data class Success(val transactionSuccessfulDto: TransferSuccessfulDto) : PaymentTransactionState()
    data class Error(val message: String) : PaymentTransactionState()
}