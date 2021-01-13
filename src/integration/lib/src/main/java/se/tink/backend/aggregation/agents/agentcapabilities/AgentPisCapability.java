package se.tink.backend.aggregation.agents.agentcapabilities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import se.tink.backend.aggregation.client.provider_configuration.rpc.PisCapability;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(AgentPisCapability.AgentPisCapabilities.class)
public @interface AgentPisCapability {
    /**
     * Agent's PIS Capabilities are read from {@link #capabilities()}.
     * Capabilities have to be listed explicitly.
     * By default, any capability defined in the annotation will be available for all markets unless a market is specified in the annotation as well
     *
     * <pre>{@code
     *     @AgentPisCapabilities(value = {PisCapability.PIS_SE_BG}, markets = {"SE"})
     *     public final class SomeAgent {}
     * }<pre/>
     */
    PisCapability[] capabilities() default {};

    String[] markets() default {};

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface AgentPisCapabilities {
        AgentPisCapability[] value();
    }
}
