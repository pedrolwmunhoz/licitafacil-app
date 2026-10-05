package com.licitafacil.app.data.local

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object NetworkMonitor {
    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED).let { validated ->
                // Em alguns devices VALIDATED atrasa; aceita INTERNET+NOT_RESTRICTED
                validated || caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
            }
    }
}
