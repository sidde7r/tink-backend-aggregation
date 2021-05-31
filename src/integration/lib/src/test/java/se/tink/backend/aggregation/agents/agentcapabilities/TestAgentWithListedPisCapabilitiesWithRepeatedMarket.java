package se.tink.backend.aggregation.agents.agentcapabilities;

import org.junit.Ignore;
import se.tink.backend.aggregation.client.provider_configuration.rpc.PisCapability;

@Ignore
@AgentPisCapability(
        capabilities = {PisCapability.SEPA_CREDIT_TRANSFER},
        markets = {"GB"})
@AgentPisCapability(
        capabilities = {PisCapability.SEPA_INSTANT_CREDIT_TRANSFER},
        markets = {"GB"})
public class TestAgentWithListedPisCapabilitiesWithRepeatedMarket extends BaseTestAgent {}
