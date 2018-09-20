package net.corda.node.services.transactions

import net.corda.core.flows.FlowSession
import net.corda.core.internal.notary.NotaryServiceFlow
import net.corda.core.internal.notary.TrustedAuthorityNotaryService
import net.corda.core.schemas.MappedSchema
import net.corda.node.services.api.ServiceHubInternal
import net.corda.node.services.schema.NodeSchemaService
import java.security.PublicKey

/** An embedded notary service that uses the node's database to store committed states. */
class SimpleNotaryService(override val services: ServiceHubInternal, override val notaryIdentityKey: PublicKey) : TrustedAuthorityNotaryService() {
    companion object {
        @JvmStatic
        fun getSchemas(): List<MappedSchema> = listOf(NodeSchemaService.NodeNotaryV1)
    }

    private val notaryConfig = services.configuration.notary
            ?: throw IllegalArgumentException("Failed to register ${this::class.java}: notary configuration not present")

    override val uniquenessProvider = PersistentUniquenessProvider(services.clock, services.database)

    override fun createServiceFlow(otherPartySession: FlowSession): NotaryServiceFlow {
        return if (notaryConfig.validating) {
            ValidatingNotaryFlow(otherPartySession, this)
        } else {
            NonValidatingNotaryFlow(otherPartySession, this)
        }
    }

    override fun start() {}
    override fun stop() {}
}
