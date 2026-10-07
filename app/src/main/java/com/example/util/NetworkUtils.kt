package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object NetworkUtils {
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
    }

    fun getNetworkTypeName(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return "Offline"
        val activeNetwork = connectivityManager.activeNetwork ?: return "Offline"
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return "Offline"
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile Network"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Connected"
        }
    }
}
