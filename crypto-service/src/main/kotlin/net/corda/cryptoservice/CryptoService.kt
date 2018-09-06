package net.corda.cryptoservice

import java.security.KeyStore
import java.security.PublicKey

interface CryptoService {

    /** schemeID is corda specific. */
    fun generateKeyPair(alias: String, schemeID: String): PublicKey

    fun containsKey(alias: String): Boolean

    fun getPublicKey(alias: String): PublicKey

    fun sign(alias: String, bytes: ByteArray): ByteArray

    fun getKeyStore(): KeyStore
}