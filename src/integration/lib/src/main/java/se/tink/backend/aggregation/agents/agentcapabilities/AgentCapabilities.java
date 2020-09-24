package se.tink.backend.aggregation.agents.agentcapabilities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import se.tink.backend.aggregation.client.provider_configuration.rpc.Capability;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AgentCapabilities {

    /**
     * By default agent Capabilities are read from {@link #value()}.
     * Capabilities can be computed from interfaces Agents implement. However in some cases the
     * capability is not provided even if interface is implemented. It might happen that the data
     * fetched by agent are not enough to cover capability.
     * In that case capabilities have to be listed explicitly. It happens for most most Agents that
     * is why this approach is default one.
     * <pre>{@code
     *     @AgentCapabilities({CREDIT_CARDS, IDENTITY_DATA})
     *     public final class SomeAgent {}
     * }<pre/>
     */
    Capability[] value() default {};

    /**
     * Agent Capabilities can be also computed from Executor interfaces implemented by agent.
     * Please make sure that CapabilityExecutor's implementation provides enough data to cover
     * capability. Only in that case Capabilities can be generated from implementation.
     * To make it happen set {@link #generateFromImplementedExecutors()} as true.
     * The mapping is done in {@link AgentCapabilitiesService}.
     * <pre>{@code
     *     @AgentCapabilities(generateFromImplementedExecutors = true)
     *     public final class SomeAgent implements AnExecutor {}
     * }<pre/>
     */
    boolean generateFromImplementedExecutors() default false;
}
