package com.wifiextract.app

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import com.wifiextract.app.core.Subnet
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
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

        val isConnected = caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

        return NetworkInfo(
            connected = isConnected,
            transport = transport,
            networkType = if (isConnected) "Red activa" else "Sin conexión",
            localAddress = ipv4?.address?.hostAddress ?: NOT_AVAILABLE,
            prefixLength = prefix,
            subnetMask = Subnet.maskString(prefix).ifBlank { NOT_AVAILABLE },
            gateway = gateway ?: NOT_AVAILABLE,
            dnsServers = dns.ifBlank { NOT_AVAILABLE },
            interfaceName = links?.interfaceName ?: NOT_AVAILABLE
        )
    }

    fun observeNetworkInfo(): Flow<NetworkInfo> = callbackFlow {
        trySend(read())

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(read())
            }

            override fun onLost(network: Network) {
                trySend(read())
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                trySend(read())
            }

            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                trySend(read())
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            manager.registerDefaultNetworkCallback(callback)
        } else {
            val request = NetworkRequest.Builder().build()
            manager.registerNetworkCallback(request, callback)
        }

        awaitClose {
            runCatching { manager.unregisterNetworkCallback(callback) }
        }
    }.distinctUntilChanged()

    companion object {
        const val NOT_AVAILABLE = "No disponible"
    }
}
