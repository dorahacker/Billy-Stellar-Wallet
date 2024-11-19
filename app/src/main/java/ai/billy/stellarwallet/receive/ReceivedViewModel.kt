package ai.billy.stellarwallet.receive

import ai.billy.stellarwallet.helper.LocalStoreImpl
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ReceivedViewModel(application: Application) : AndroidViewModel(application) {

    private val localStorage: LocalStoreImpl = LocalStoreImpl(application)

    private val _accountID = MutableLiveData<String>()
    val accountID: LiveData<String> get() = _accountID

    fun loadPublicKey() {
        val stellarAccountId = localStorage.getStellarAccountId() ?: ""
        _accountID.postValue(stellarAccountId)
    }

}