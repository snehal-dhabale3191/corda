package net.corda.node.services.keys

import net.corda.cryptoservice.CryptoService
import java.security.PublicKey

class BCCryptoService(private val conf: String? = null) : CryptoService {

    override fun generateKeyPair(alias: String, schemeId: String): PublicKey {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun containsKey(alias: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPublicKey(alias: String): PublicKey {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sign(alias: String, data: ByteArray): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}