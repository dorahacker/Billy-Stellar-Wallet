package ai.billy.stellarwallet.helper

import android.content.Context
import android.content.Context.MODE_PRIVATE

class LocalStoreImpl(context: Context)  {

    private companion object {
        const val PREF_NAME = "ai.billy.stellarwallet.PREFERENCE_FILE_KEY"
        const val KEY_ENCRYPTED_PHRASE = "kEncryptedPhrase"
        const val KEY_ENCRYPTED_PASSPHRASE = "kEncryptedPassphrase"
        const val KEY_PIN_DATA = "kPinData"
        const val KEY_STELLAR_ACCOUNT_PUBLIC_KEY = "kStellarAccountPublicKey"
        const val KEY_STELLAR_BALANCES_KEY = "kStellarBalancesKey"
        const val KEY_STELLAR_AVAILABLE_BALANCE_KEY = "kAvailableBalanceKey"
        const val KEY_IS_RECOVERY_PHRASE = "kIsRecoveryPhrase"
        const val KEY_PIN_SETTINGS_SEND = "kPinSettingsSend"
        const val KEY_IS_PASSPHRASE_USED = "kIsPassphraseUsed"
    }

    private val sharedPreferences = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE)

    fun getEncryptedPhrase() : String? {
        return getString(KEY_ENCRYPTED_PHRASE)
    }

    fun setEncryptedPhrase(encryptedPassphrase : String?) {
        set(KEY_ENCRYPTED_PHRASE, encryptedPassphrase)
    }

    fun getEncryptedPassphrase(): String? {
        return getString(KEY_ENCRYPTED_PASSPHRASE)
    }

    fun setEncryptedPassphrase(encryptedPassphrase : String) {
        set(KEY_ENCRYPTED_PASSPHRASE, encryptedPassphrase)
    }

    fun getStellarAccountId() : String? {
        return getString(KEY_STELLAR_ACCOUNT_PUBLIC_KEY)
    }

    fun setStellarAccountId(accountId : String) {
        return set(KEY_STELLAR_ACCOUNT_PUBLIC_KEY, accountId)
    }

    fun getAvailableBalance(): String {
        return getString(KEY_STELLAR_AVAILABLE_BALANCE_KEY) ?: Constants.DEFAULT_ACCOUNT_BALANCE
    }

    fun setAvailableBalance(availableBalance:String?) {
        return set(KEY_STELLAR_AVAILABLE_BALANCE_KEY, availableBalance)
    }


    fun getIsRecoveryPhrase() : Boolean {
        return if (contains(KEY_IS_RECOVERY_PHRASE)) {
            getBoolean(KEY_IS_RECOVERY_PHRASE)
        } else {
            //default recovery method is recovery phrase
            true
        }
    }

    fun setIsRecoveryPhrase(isRecoveryPhrase : Boolean) {
        set(KEY_IS_RECOVERY_PHRASE, isRecoveryPhrase)
    }

    fun setShowPinOnSend(showPinOnSend: Boolean) {
        set(KEY_PIN_SETTINGS_SEND, showPinOnSend)
    }

    fun getShowPinOnSend() : Boolean {
        return if(contains(KEY_PIN_SETTINGS_SEND)) {
            getBoolean(KEY_PIN_SETTINGS_SEND)
        } else {
            //default logic is show pin on send
            true
        }
    }

    private operator fun set(key: String, value: String?) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    private operator fun set(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    private fun getString(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    private fun contains(key:String) : Boolean {
        return sharedPreferences.contains(key)
    }

    private fun getBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    fun clearLocalStore() : Boolean {
        val editor = sharedPreferences.edit()
        editor.remove(KEY_ENCRYPTED_PHRASE)
        editor.remove(KEY_ENCRYPTED_PASSPHRASE)
        editor.remove(KEY_PIN_DATA)
        editor.remove(KEY_STELLAR_ACCOUNT_PUBLIC_KEY)
        editor.remove(KEY_STELLAR_BALANCES_KEY)
        editor.remove(KEY_STELLAR_AVAILABLE_BALANCE_KEY)
        editor.remove(KEY_IS_RECOVERY_PHRASE)
        editor.remove(KEY_IS_PASSPHRASE_USED)
        return editor.commit()
    }
}