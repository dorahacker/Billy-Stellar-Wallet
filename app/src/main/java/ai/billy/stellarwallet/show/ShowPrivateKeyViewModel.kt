package ai.billy.stellarwallet.show

import ai.billy.stellarwallet.helper.LocalStoreImpl
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ShowPrivateKeyViewModel(application: Application) : AndroidViewModel(application) {

    private var localStorage: LocalStoreImpl = LocalStoreImpl(application)

    private val _secretKey = MutableLiveData<String>()
    val secretKey: LiveData<String> get() = _secretKey

    fun loadSecretKey() {
        val stellarAccountId = localStorage.getEncryptedPhrase() ?: ""
        _secretKey.postValue(stellarAccountId)
    }

}