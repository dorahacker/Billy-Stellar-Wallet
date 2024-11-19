package ai.billy.stellarwallet.create

sealed class WalletState {
    data object Loading : WalletState()
    data class Success(
        val publicKey: String,
        val secretKey: String
    ) : WalletState()
    data class Error(val message: String) : WalletState()
}