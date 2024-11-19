package ai.billy.stellarwallet.receive

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ai.billy.stellarwallet.R
import ai.billy.stellarwallet.databinding.ActivityReceiveBinding
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.io.File


class ReceiveActivity : AppCompatActivity() {

    private val binding: ActivityReceiveBinding by lazy {
        ActivityReceiveBinding.inflate(layoutInflater)
    }

    private val receiveViewModel: ReceivedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeUI()
        observeViewModel()

        receiveViewModel.loadPublicKey()
    }


    private fun observeViewModel() {
        receiveViewModel.apply {
            accountID.observe(this@ReceiveActivity) { accountId ->
                generateBarCode(accountId)
                binding.tvAccountId.text = accountId
            }
        }
    }

    private fun initializeUI() {
        binding.btnCopy.setOnClickListener {
            copyAccountId(binding.tvAccountId.text.toString())
        }
        binding.btnShare.setOnClickListener {
            shareWalletAddressAndQRCode(binding.tvAccountId.text.toString())
        }
        binding.btnSetAmount.setOnClickListener {
            showAmountInputDialog()
        }
    }

    private fun generateBarCode(contents: String) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(contents, BarcodeFormat.QR_CODE, 600, 600)
            binding.imgQrCode.setImageBitmap(bitmap)
        } catch (e: Exception) {
            // error handling
        }
    }

    private fun copyAccountId(textToCopy: String) {
        val clipboard: ClipboardManager =
            getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Wallet Address", textToCopy)
        clipboard.setPrimaryClip(clip)
        // Show a toast to indicate text has been copied
        Toast.makeText(this@ReceiveActivity, "Address copied to clipboard", Toast.LENGTH_SHORT)
            .show()
    }

    private fun shareWalletAddressAndQRCode(walletAddress: String) {
        // Generate the QR code bitmap
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(walletAddress, BarcodeFormat.QR_CODE, 600, 600)

            shareWalletAddressAndQRCode(walletAddress, bitmap!!)
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }



    private fun shareWalletAddressAndQRCode(walletAddress: String, qrCodeBitmap: Bitmap) {
        // First, save the QR code bitmap as an image to the cache directory
        val qrCodeUri = saveImageToCache(qrCodeBitmap)

        // Create a share intent
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, "Receive USDC \nNetwork Stellar\nWallet Address: $walletAddress")

            // Add the QR code image
            putExtra(Intent.EXTRA_STREAM, qrCodeUri)
            type = "image/png"  // Set type as image if you want to share an image
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Start the share intent
        startActivity(Intent.createChooser(shareIntent, "Share Wallet Address and QR Code"))
    }

    // Save the bitmap to the cache directory and return its Uri
    private fun saveImageToCache(bitmap: Bitmap): Uri? {
        // Create a cache path for storing the image
        val cachePath = File(applicationContext.cacheDir, "images")
        cachePath.mkdirs() // Create if not exists
        val file = File(cachePath, "qr_code.png")

        // Save the bitmap as a PNG image
        val fileOutputStream = file.outputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        fileOutputStream.close()

        // Return the Uri using FileProvider
        return FileProvider.getUriForFile(applicationContext, "${packageName}.fileprovider", file)
    }

    private fun showAmountInputDialog() {
        // Create an EditText for user input
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.hint = "Enter amount"

        // Set padding and other layout parameters if needed
        val padding = resources.getDimensionPixelSize(R.dimen.activity_horizontal_margin)
        input.setPadding(padding, padding, padding, padding)

        val margin = resources.displayMetrics.density.toInt() * 16
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(margin, 0, margin, 0)
        input.layoutParams = params

        // Build the dialog
        MaterialAlertDialogBuilder(this)
            .setTitle("Enter Amount")
//            .setMessage("Please enter the amount you wish to deposit.")
            .setView(input)
            .setPositiveButton("Confirm") { _: DialogInterface?, _: Int ->
                // Retrieve the input value on confirmation
                val enteredAmount = input.text.toString()
                if (enteredAmount.isNotEmpty()) {
                    handleAmountInput(enteredAmount)
                } else {
                    Toast.makeText(this, "Amount cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(
                "Cancel"
            ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            .show()
    }

    private fun handleAmountInput(amount: String) {
        val formatID = "stellar:${receiveViewModel.accountID.value}?amount=$amount"
        generateBarCode(formatID)
    }

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, ReceiveActivity::class.java)
            return intent
        }
    }
}