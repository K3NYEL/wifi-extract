package com.k3nyel.wifiextract.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationTest {
    @Test fun acceptsIpv4AndHostnames() {
        assertTrue(Validation.isValidHost("192.168.1.1"))
        assertTrue(Validation.isValidHost("router.lan"))
        assertTrue(Validation.isValidHost("one.one.one.one"))
    }

    @Test fun rejectsInjectionAndGarbage() {
        assertFalse(Validation.isValidHost(""))
        assertFalse(Validation.isValidHost("a b"))
        assertFalse(Validation.isValidHost("host;rm -rf /"))
        assertFalse(Validation.isValidHost("-bad.example"))
        assertFalse(Validation.isValidHost("x".repeat(254)))
    }

    @Test fun parsesPorts() {
        assertEquals(443, Validation.parsePort(" 443 "))
        assertNull(Validation.parsePort("0"))
        assertNull(Validation.parsePort("65536"))
        assertNull(Validation.parsePort("abc"))
    }
}

class VersionComparatorTest {
    @Test fun detectsNewerVersions() {
        assertTrue(VersionComparator.isNewer("v0.2.0", "0.1.2"))
        assertTrue(VersionComparator.isNewer("1.0.0", "0.9.9"))
        assertTrue(VersionComparator.isNewer("0.1.10", "0.1.9"))
    }

    @Test fun ignoresEqualOlderAndInvalid() {
        assertFalse(VersionComparator.isNewer("v0.1.2", "0.1.2"))
        assertFalse(VersionComparator.isNewer("0.1.1", "0.1.2"))
        assertFalse(VersionComparator.isNewer("latest", "0.1.2"))
    }
}

class SubnetTest {
    @Test fun buildsHostListWithoutOwnAddress() {
        val hosts = Subnet.hosts(byteArrayOf(192.toByte(), 168.toByte(), 1, 34), 24)
        assertEquals(253, hosts.size)
        assertFalse(hosts.contains("192.168.1.34"))
        assertTrue(hosts.contains("192.168.1.1"))
        assertFalse(hosts.contains("192.168.1.0"))
        assertFalse(hosts.contains("192.168.1.255"))
    }

    @Test fun clampsLargeNetworksToSlash24() {
        assertEquals(253, Subnet.hosts(byteArrayOf(10, 0, 5, 9), 16).size)
    }

    @Test fun formatsMask() {
        assertEquals("255.255.255.0", Subnet.maskString(24))
        assertEquals("255.255.240.0", Subnet.maskString(20))
    }
}
