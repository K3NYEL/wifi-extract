package com.k3nyel.wifiextract.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.net.ConnectException
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException

/** Bounded, cancellable checks against a single host. Each flow emits human-readable result lines. */
class DiagnosticsRepository {

    fun ping(host: String, timeoutMs: Int, count: Int = 4): Flow<String> = flow {
        val address = InetAddress.getByName(host)
        emit("Destino ${address.hostAddress}")
        val times = mutableListOf<Long>()
        repeat(count) { index ->
            currentCoroutineContext().ensureActive()
            val start = System.nanoTime()
            val reachable = runCatching { address.isReachable(timeoutMs) }.getOrDefault(false)
            val ms = (System.nanoTime() - start) / 1_000_000
            if (reachable) {
                times += ms
                emit("Respuesta de ${address.hostAddress} · $ms ms")
            } else {
                emit("Sin respuesta (${index + 1}/$count)")
            }
            delay(200)
        }
        val loss = 100 - times.size * 100 / count
        emit(
            if (times.isEmpty()) "0/$count recibidos · pérdida 100 %"
            else "${times.size}/$count recibidos · pérdida $loss % · media ${times.average().toLong()} ms"
        )
        emit("Nota: Android puede limitar ICMP; una ausencia de respuesta no prueba que el host esté caído.")
    }.flowOn(Dispatchers.IO)

    fun dnsLookup(host: String): Flow<String> = flow {
        val start = System.nanoTime()
        val addresses = InetAddress.getAllByName(host)
        val ms = (System.nanoTime() - start) / 1_000_000
        emit("Consulta de $host · $ms ms")
        addresses.forEach {
            emit("${if (it is Inet4Address) "A    " else "AAAA "} ${it.hostAddress}")
        }
    }.flowOn(Dispatchers.IO)

    fun tcpPort(host: String, port: Int, timeoutMs: Int): Flow<String> = flow {
        val address = InetAddress.getByName(host)
        emit("Conectando a ${address.hostAddress}:$port")
        val start = System.nanoTime()
        val result = try {
            Socket().use { it.connect(InetSocketAddress(address, port), timeoutMs) }
            "Puerto $port ABIERTO · ${(System.nanoTime() - start) / 1_000_000} ms"
        } catch (e: ConnectException) {
            if (e.message.orEmpty().contains("refused", ignoreCase = true)) "Puerto $port CERRADO (conexión rechazada)"
            else "Puerto $port inaccesible: ${e.message.orEmpty()}"
        } catch (e: SocketTimeoutException) {
            "Puerto $port sin respuesta (tiempo agotado o filtrado)"
        }
        emit(result)
    }.flowOn(Dispatchers.IO)
}
