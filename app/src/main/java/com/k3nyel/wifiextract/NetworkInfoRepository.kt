package com.k3nyel.wifiextract

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.net.Inet4Address

data class NetworkInfo(
    val connected: Boolean,
    val transport: String,
    val networkType: String,
    val localAddress: String,
    val gateway: String,
    val dnsServers: String,
    val interfaceName: String
)

class NetworkInfoRepository(context: Context) {
    private val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun read(): NetworkInfo {
        val network = manager.activeNetwork ?: return NetworkInfo(
            false, "None", "No active network", "Not available", "Not available", "Not available", "Not available"
        )
        val caps = manager.getNetworkCapabilities(network)
        val links = manager.getLinkProperties(network)

        val transport = when {
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi-Fi"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Cellular"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true -> "VPN"
            else -> "Other"
        }
        val ipv4 = links?.linkAddresses.orEmpty()
            .map { it.address }
            .filterIsInstance<Inet4Address>()
            .joinToString { it.hostAddress.orEmpty() }
        val gateway = links?.routes?.firstOrNull { it.isDefaultRoute }?.gateway?.hostAddress
        val dns = links?.dnsServers.orEmpty().joinToString { it.hostAddress.orEmpty() }

        return NetworkInfo(
            connected = caps != null,
            transport = transport,
            networkType = if (caps != null) "Active network" else "No connection",
            localAddress = ipv4.ifBlank { "Not available" },
            gateway = gateway ?: "Not available",
            dnsServers = dns.ifBlank { "Not available" },
            interfaceName = links?.interfaceName ?: "Not available"
        )
    }
}
