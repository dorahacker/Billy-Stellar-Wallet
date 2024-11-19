package ai.billy.stellarwallet.helper

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

class StringFormat {
    companion object {

        fun formatNumber(input: String?): String {
            val number = input?.toDoubleOrNull() ?: 0.0
            val numberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
                minimumFractionDigits = 2
                maximumFractionDigits = 2
            }
            return numberFormat.format(number)
        }

        fun formatBigDecimal(value: BigDecimal): String {
            if (value == BigDecimal.ZERO) return Constants.DEFAULT_ACCOUNT_BALANCE

            val scale = value.scale() // Number of decimal places
            val pattern = StringBuilder("#,###")

            // Append the decimal portion dynamically based on the scale
            if (scale > 0) {
                pattern.append(".")
                repeat(scale) { pattern.append("#") }
            }

            // Use DecimalFormat to format the BigDecimal value with the
            // dynamically created pattern
            val decimalFormat = DecimalFormat(pattern.toString())
            // Ensures proper locale settings for commas
            decimalFormat.decimalFormatSymbols = DecimalFormatSymbols.getInstance()
            return decimalFormat.format(value)
        }


        fun formatPublicKey(publicKey: String): String {
            if (publicKey.length >= 12) {
                val start = publicKey.take(6)
                val end = publicKey.takeLast(6)
                return "$start...$end"
            }
            return publicKey
        }

        fun formatTransactionHash(hash: String): String {
            if (hash.length >= 12) {
                val start = hash.take(6)
                val end = hash.takeLast(6)
                return "$start...$end"
            }
            return hash
        }

    }
}