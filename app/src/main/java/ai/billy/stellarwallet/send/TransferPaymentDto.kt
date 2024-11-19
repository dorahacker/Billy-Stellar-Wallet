package ai.billy.stellarwallet.send

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class TransferPaymentDto(
    val amount: BigDecimal,
    val token: String,
    val fromAccount: String,
    val toAccount: String,
    val note: String = ""
) : Parcelable