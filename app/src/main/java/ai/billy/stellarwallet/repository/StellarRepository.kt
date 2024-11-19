package ai.billy.stellarwallet.repository

import ai.billy.stellarwallet.helper.USDC_CODE
import ai.billy.stellarwallet.helper.server
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.stellar.sdk.requests.RequestBuilder
import org.stellar.sdk.responses.AccountResponse
import org.stellar.sdk.responses.operations.PaymentOperationResponse

class StellarRepository {

    data class TransactionResult(
        val transactions: List<StellarTransaction>,
        val cursor: String?,
        val balance: String
    )

    suspend fun getTransactions(cursor: String?, publicKey: String): TransactionResult =
        withContext(Dispatchers.IO) {

            val account = server.accounts().account(publicKey)

            val transactionRequest = server.transactions()
                .forAccount(account.accountId)
                .limit(20)

            cursor?.let { transactionRequest.cursor(it) }

            val transactions = transactionRequest.execute()
            val balance = account.balances[0].balance


            return@withContext TransactionResult(
                transactions = transactions.records.map {
                    StellarTransaction(
                        id = it.id,
                        amount = it.feeAccount,
                        asset = "XLM",
                        timestamp = it.createdAt
                    )
                },
                cursor = transactions.records.lastOrNull()?.pagingToken,
                balance = balance
            )
        }


    data class PaymentOperationsResult(
        val paymentOperations: List<StellarPaymentOperation>,
        val cursor: String?,
        val hasMoreRecords: Boolean
    )


    suspend fun getPaymentOperations(
        cursor: String?,
        publicKey: String,
        recordsPerPage: Int = 10
    ): PaymentOperationsResult = withContext(Dispatchers.IO) {

        val paymentsRequestBuilder = server.payments()
            .forAccount(publicKey)
            .order(RequestBuilder.Order.DESC)
            .limit(recordsPerPage)


        cursor?.let {
            paymentsRequestBuilder.cursor(cursor)
        }

        val paymentsPage = paymentsRequestBuilder.execute()

        var nextCursor: String? = null

        return@withContext PaymentOperationsResult(
            paymentOperations = paymentsPage.records.filterIsInstance<PaymentOperationResponse>()
                .map {
                    nextCursor = it.pagingToken
                    StellarPaymentOperation(
                        id = it.transactionHash,
                        from = it.from,
                        to = it.to,
                        amount = it.amount,
                        asset = it.assetCode,
                        timestamp = it.createdAt,
                        deposit = it.to == publicKey
                    )
                },
            cursor = nextCursor,
            hasMoreRecords = nextCursor != null && paymentsPage.records.isNotEmpty()
        )
    }

    suspend fun checkStellarWalletBalance(
        publicKey: String,
        assetCode : String = USDC_CODE
    ): AccountResponse.Balance? = withContext(Dispatchers.IO) {

        // Load the account details using the public key
        val accountResponse: AccountResponse = server.accounts().account(publicKey)

        val usdcBalance: AccountResponse.Balance? = accountResponse.balances.find {
            it.assetCode.isPresent && it.assetCode.get() == assetCode
        }

        return@withContext usdcBalance
    }

}

data class StellarTransaction(
    val id: String,
    val amount: String,
    val asset: String,
    val timestamp: String
)


data class StellarPaymentOperation(
    val id: String,
    val from: String,
    val to: String,
    val amount: String,
    val asset: String,
    val timestamp: String,
    val deposit: Boolean = false
)

