package ai.billy.stellarwallet.history


import ai.billy.stellarwallet.R
import ai.billy.stellarwallet.helper.StringFormat.Companion.formatNumber
import ai.billy.stellarwallet.helper.StringFormat.Companion.formatPublicKey
import ai.billy.stellarwallet.repository.StellarPaymentOperation
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class PaymentsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val payments = mutableListOf<StellarPaymentOperation>()
    private var isLoadingMore = false

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_LOADING = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ITEM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_transaction, parent, false)
                PaymentOperationViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_loading, parent, false)
                LoadingViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PaymentOperationViewHolder) {
            holder.bind(payments[position])
        }
    }

    override fun getItemCount(): Int = if (isLoadingMore) payments.size + 1 else payments.size

    override fun getItemViewType(position: Int): Int {
        return if (position == payments.size && isLoadingMore) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    fun addPayments(newPayments: List<StellarPaymentOperation>) {
        val startPos = payments.size
        payments.addAll(newPayments)
        notifyItemRangeInserted(startPos, newPayments.size)
    }

    fun setLoadingMore(loading: Boolean) {
        if (isLoadingMore != loading) {
            isLoadingMore = loading
            if (loading) {
                notifyItemInserted(payments.size)
            } else {
                notifyItemRemoved(payments.size)
            }
        }
    }
}

class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
}



class PaymentOperationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val addressText: TextView = view.findViewById(R.id.address_text_view)
    private val amountText: TextView = view.findViewById(R.id.amount_text_view)
    private val dateText: TextView = view.findViewById(R.id.date_text_view)

    fun bind(transaction: StellarPaymentOperation) {
        dateText.text = convertToLocalDateTime(transaction.timestamp)
        if(transaction.deposit){
            addressText.text = "${formatPublicKey(transaction.from)}"
            amountText.setTextColor(itemView.context.getColor(R.color.brand_color_green))
            amountText.text = "${formatNumber(transaction.amount)} ${transaction.asset}"
        }else{
            addressText.text = "${formatPublicKey(transaction.to)}"
            amountText.setTextColor(itemView.context.getColor(R.color.brand_color_red))
            amountText.text = "-${formatNumber(transaction.amount)} ${transaction.asset}"
        }
    }


    private fun convertToLocalDateTime(utcString : String): String {

        // Define the input and output formats
        val utcFormat: SimpleDateFormat =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        utcFormat.timeZone = TimeZone.getTimeZone("UTC")

        val localFormat: SimpleDateFormat =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        localFormat.timeZone = TimeZone.getDefault()

        try {
            // Parse the UTC date string
            val date: Date = utcFormat.parse(utcString) ?: Date()

            // Format it to local time
            val localDateTime: String = localFormat.format(date)

            return localDateTime
        } catch (e: ParseException) {
            e.printStackTrace()
            return utcString
        }
    }
}