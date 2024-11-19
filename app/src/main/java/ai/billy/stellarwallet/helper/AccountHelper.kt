package ai.billy.stellarwallet.helper

import org.stellar.sdk.KeyPair

fun validateStellarPublic(
    recipientPublicKey: String
): Boolean {
    // Validate Stellar public key
    return try {
        KeyPair.fromAccountId(recipientPublicKey)
        true
    } catch (e: Exception) {
        false
    }
}

fun validateStellarKey(seed : String) : Boolean {
    // Validate Stellar Secret key
    return try {
        KeyPair.fromSecretSeed(seed)
        true
    } catch (e: Exception) {
        false
    }
}