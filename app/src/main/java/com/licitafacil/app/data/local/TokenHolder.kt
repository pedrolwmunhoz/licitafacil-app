package com.licitafacil.app.data.local

/** Tokens em memória pra OkHttp Authenticator (thread-safe). */
object TokenHolder {
    @Volatile
    var jwt: String? = null
        private set

    @Volatile
    var refreshToken: String? = null
        private set

    @Volatile
    var onTokensUpdated: ((jwt: String, refresh: String) -> Unit)? = null

    fun set(jwt: String?, refresh: String?) {
        this.jwt = jwt
        this.refreshToken = refresh
    }

    fun clear() {
        jwt = null
        refreshToken = null
    }

    fun isLocal(): Boolean = jwt?.startsWith("local-") == true
}
