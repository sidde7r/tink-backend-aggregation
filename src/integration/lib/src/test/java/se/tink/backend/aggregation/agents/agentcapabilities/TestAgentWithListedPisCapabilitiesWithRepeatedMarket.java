package se.tink.backend.aggregation.agents.agentcapabilities;

import org.junit.Ignore;
import se.tink.backend.aggregation.client.provider_configuration.rpc.PisCapability;

@Ignore
@AgentPisCapability(
        capabilities = {PisCapability.PIS_SEPA},
        markets = {"GB"})
@AgentPisCapability(
        capabilities = {PisCapability.PIS_SEPA_ICT},
        markets = {"GB"})
public class TestAgentWithListedPisCapabilitiesWithRepeatedMarket extends BaseTestAgent {}
