package ai.billy.stellarwallet.helper

import org.stellar.sdk.Network
import org.stellar.sdk.Server


val server = Server("https://horizon-testnet.stellar.org")
val network = Network.TESTNET

val USDC_CODE = "USDC"
val USDC_ISSUER = "GBBD47IF6LWK7P7MDEVSCWR7DPUWV3NY3DTQEVFL4NAT4AQH3ZLLFLA5"

class Constants {

    companion object {
        const val DEFAULT_ACCOUNT_BALANCE = "0.00"

        const val LUMENS_ASSET_TYPE = "native"
        const val LUMENS_ASSET_CODE = "XLM"
    }

}