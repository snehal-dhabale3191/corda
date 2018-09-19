package net.corda.node.internal

import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession

sealed class InitiatedFlowFactory<F : FlowLogic<*>> {

    protected abstract val factory: (FlowSession) -> F
    abstract val initiatedFlowClass: Class<F>?

    fun createFlow(initiatingFlowSession: FlowSession): F = factory(initiatingFlowSession)

    data class Core<F : FlowLogic<*>>(override val factory: (FlowSession) -> F, override val initiatedFlowClass: Class<F>?) : InitiatedFlowFactory<F>() {

        companion object {

            inline fun <reified FLOW : FlowLogic<*>> of(noinline factory: (FlowSession) -> FLOW): Core<FLOW> = Core(factory, FLOW::class.javaObjectType)
        }
    }

    data class CorDapp<F : FlowLogic<*>>(val flowVersion: Int, val appName: String, override val initiatedFlowClass: Class<F>, override val factory: (FlowSession) -> F) : InitiatedFlowFactory<F>() {

        companion object {

            inline fun <reified FLOW : FlowLogic<*>> of(flowVersion: Int, appName: String, noinline factory: (FlowSession) -> FLOW): CorDapp<FLOW> = CorDapp(flowVersion, appName, FLOW::class.javaObjectType, factory)
        }
    }
}
