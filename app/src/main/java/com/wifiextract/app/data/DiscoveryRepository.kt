package com.wifiextract.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.net.ConnectException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.atomic.AtomicInteger

data class Device(val ip: String, val hostName: String?, val latencyMs: Long)

sealed interface ScanEvent {
    data class Progress(val scanned: Int) : ScanEvent
    data class Found(val device: Device) : ScanEvent
}

/**
 * Bounded, cancellable reachability sweep of the local subnet. It only runs on explicit user action,
 * never probes more than [MAX_CONCURRENCY] hosts at once and only sends a reachability probe
 * (ICMP/TCP echo as allowed by the OS) plus a single TCP connect to port 80.
 */
class DiscoveryRepository {
    fun scan(hosts: List<String>, timeoutMs: Int, resolveNames: Boolean): Flow<ScanEvent> = channelFlow {
        val gate = Semaphore(MAX_CONCURRENCY)
        val done = AtomicInteger(0)
        hosts.forEach { host ->
            launch(Dispatchers.IO) {
                gate.withPermit {
                    ensureActive()
                    val latency = probe(host, timeoutMs)
                    if (latency != null) {
                        val name = if (resolveNames) resolveName(host) else null
                        send(ScanEvent.Found(Device(host, name, latency)))
                    }
                    send(ScanEvent.Progress(done.incrementAndGet()))
                }
            }
        }
    }

    private fun probe(host: String, timeoutMs: Int): Long? {
        val address = InetAddress.getByName(host) // numeric literal, no DNS lookup
        val start = System.nanoTime()
        val reachable = runCatching { address.isReachable(timeoutMs) }.getOrDefault(false) ||
            tcpAlive(address, 80, minOf(timeoutMs, 400))
        return if (reachable) (System.nanoTime() - start) / 1_000_000 else null
    }

    /** A completed connection or an explicit refusal both prove the host is up. */
    private fun tcpAlive(address: InetAddress, port: Int, timeoutMs: Int): Boolean = try {
        Socket().use { it.connect(InetSocketAddress(address, port), timeoutMs) }
        true
    } catch (e: ConnectException) {
        e.message.orEmpty().contains("refused", ignoreCase = true)
    } catch (e: Exception) {
        false
    }

    private fun resolveName(host: String): String? =
        runCatching { InetAddress.getByName(host).hostName }.getOrNull()?.takeIf { it != host }

    companion object {
        const val MAX_CONCURRENCY = 32
    }
}
