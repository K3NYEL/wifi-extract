package com.k3nyel.wifiextract

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.k3nyel.wifiextract.core.Subnet
import java.net.Inet4Address

data class NetworkInfo(
    val connected: Boolean,
    val transport: String,
    val networkType: String,
    val localAddress: String,
    val prefixLength: Int,
    val subnetMask: String,
    val gateway: String,
    val dnsServers: String,
    val interfaceName: String
)

class NetworkInfoRepository(context: Context) {
    private val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun read(): NetworkInfo {
        val network = manager.activeNetwork ?: return NetworkInfo(
            false, "Ninguna", "Sin red activa", NOT_AVAILABLE, 0, NOT_AVAILABLE, NOT_AVAILABLE, NOT_AVAILABLE, NOT_AVAILABLE
        )
        val caps = manager.getNetworkCapabilities(network)
        val links = manager.getLinkProperties(network)

        val transport = when {
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi-Fi"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Móvil"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true -> "VPN"
            else -> "Otra"
        }
        val ipv4 = links?.linkAddresses.orEmpty().firstOrNull { it.address is Inet4Address }
        val prefix = ipv4?.prefixLength ?: 0
        val gateway = links?.routes?.firstOrNull { it.isDefaultRoute && it.gateway is Inet4Address }?.gateway?.hostAddress
        val dns = links?.dnsServers.orEmpty().joinToString(" · ") { it.hostAddress.orEmpty() }

        return NetworkInfo(
            connected = caps != null,
            transport = transport,
            networkType = if (caps != null) "Red activa" else "Sin conexión",
            localAddress = ipv4?.address?.hostAddress ?: NOT_AVAILABLE,
            prefixLength = prefix,
            subnetMask = Subnet.maskString(prefix).ifBlank { NOT_AVAILABLE },
            gateway = gateway ?: NOT_AVAILABLE,
            dnsServers = dns.ifBlank { NOT_AVAILABLE },
            interfaceName = links?.interfaceName ?: NOT_AVAILABLE
        )
    }

    companion object {
        const val NOT_AVAILABLE = "No disponible"
    }
}
