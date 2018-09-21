package net.corda.cryptoservice

import java.security.PublicKey

interface CryptoService {

    /** schemeID is Corda specific. */
    fun generateKeyPair(alias: String, schemeId: String): PublicKey
    fun containsKey(alias: String): Boolean
    fun getPublicKey(alias: String): PublicKey
    fun sign(alias: String, data: ByteArray): ByteArray
}