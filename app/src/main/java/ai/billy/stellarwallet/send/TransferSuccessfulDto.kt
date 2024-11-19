package ai.billy.stellarwallet.send

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class TransferSuccessfulDto(
    val id: String,
    val amount: String,
    val token: String,
    val fromAccount: String,
    val toAccount: String,
    val note: String = ""
) : Parcelable