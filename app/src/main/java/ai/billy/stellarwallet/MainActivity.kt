package ai.billy.stellarwallet

import ai.billy.stellarwallet.create.CreateWalletActivity
import ai.billy.stellarwallet.helper.LocalStoreImpl
import ai.billy.stellarwallet.walletdashboard.WalletDashboardActivity
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val localStorage = LocalStoreImpl(this)

        val newIntent = if (localStorage.getStellarAccountId().isNullOrEmpty()) {
            Intent(this, CreateWalletActivity::class.java)
        } else {
            Intent(this, WalletDashboardActivity::class.java)
        }

        startActivity(newIntent)
        finish()
    }
}