package net.corda.node.internal

import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatingFlow
import net.corda.core.utilities.contextLogger
import net.corda.core.utilities.debug
import net.corda.node.services.statemachine.flowVersionAndInitiatingClass
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

interface FlowManager {

    // These registerInitiated* functions should be merged into one. If impossible, they should default to on that takes a FlowType as an additional parameter.
    fun <F : FlowLogic<*>> registerInitiatedFlowFactory(initiatingFlowClass: Class<out FlowLogic<*>>, flowFactory: InitiatedFlowFactory<F>)

    fun registerInitiatedCoreFlowFactory(clientFlowClass: KClass<out FlowLogic<*>>, flowFactory: (FlowSession) -> FlowLogic<*>)

    fun <F : FlowLogic<*>> registerInitiatedCoreFlowFactory(clientFlowClass: KClass<out FlowLogic<*>>, flowFactory: (FlowSession) -> F, serverFlowClass: KClass<out F>?)

    fun getFlowFactoryForInitiatingFlow(initiatingFlowClass: Class<out FlowLogic<*>>): InitiatedFlowFactory<*>?
}

class NodeFlowManager : FlowManager {

    companion object {
        private val log = contextLogger()
    }

    private val flowFactories = ConcurrentHashMap<Class<out FlowLogic<*>>, RegisteredFlowContainer<*>>()

    override fun <F : FlowLogic<*>> registerInitiatedFlowFactory(initiatingFlowClass: Class<out FlowLogic<*>>, flowFactory: InitiatedFlowFactory<F>) {

        flowFactories[initiatingFlowClass]?.let { currentRegistered ->
            if (currentRegistered.type == FlowType.CORE) {
                throw IllegalStateException("Attempting to replace an existing platform flow: $initiatingFlowClass")
            }

            if (currentRegistered.initiatedFlowClass?.isAssignableFrom(flowFactory.initiatedFlowClass) != true) {
                throw IllegalStateException("$initiatingFlowClass is attempting to register multiple flow initiated by: $initiatingFlowClass")
            }
            val initiatedFlowFactory = flowFactory as InitiatedFlowFactory.CorDapp<F>
            //the class to be registered is a subtype of the current registered class
            flowFactories[initiatedFlowFactory.initiatedFlowClass] = RegisteredFlowContainer(initiatingFlowClass, initiatedFlowFactory.initiatedFlowClass, flowFactory, FlowType.CORDAPP)
        } ?: {
            flowFactories[initiatingFlowClass] = RegisteredFlowContainer(initiatingFlowClass, flowFactory.initiatedFlowClass, flowFactory, FlowType.CORDAPP)
        }.invoke()
    }

    override fun <F : FlowLogic<*>> registerInitiatedCoreFlowFactory(clientFlowClass: KClass<out FlowLogic<*>>, flowFactory: (FlowSession) -> F, serverFlowClass: KClass<out F>?) {
        require(clientFlowClass.java.flowVersionAndInitiatingClass.first == 1) {
            "${InitiatingFlow::class.java.name}.version not applicable for core flows; their version is the node's platform version"
        }
        // TODO sollecitom check
        flowFactories[clientFlowClass.java] = RegisteredFlowContainer(clientFlowClass.java, serverFlowClass?.java, InitiatedFlowFactory.Core(flowFactory, serverFlowClass?.javaObjectType as Class<F>?), FlowType.CORE)
        log.debug { "Installed core flow ${clientFlowClass.java.name}" }
    }

    override fun registerInitiatedCoreFlowFactory(clientFlowClass: KClass<out FlowLogic<*>>, flowFactory: (FlowSession) -> FlowLogic<*>) {
        registerInitiatedCoreFlowFactory(clientFlowClass, flowFactory, null)
    }

    override fun getFlowFactoryForInitiatingFlow(initiatingFlowClass: Class<out FlowLogic<*>>): InitiatedFlowFactory<*>? {
        return flowFactories[initiatingFlowClass]?.flowFactory
    }


    enum class FlowType {
        CORE, CORDAPP
    }

    data class RegisteredFlowContainer<F : FlowLogic<*>>(val initiatingFlowClass: Class<out FlowLogic<*>>,
                                       val initiatedFlowClass: Class<out FlowLogic<*>>?,
                                       val flowFactory: InitiatedFlowFactory<F>,
                                       val type: FlowType)
}