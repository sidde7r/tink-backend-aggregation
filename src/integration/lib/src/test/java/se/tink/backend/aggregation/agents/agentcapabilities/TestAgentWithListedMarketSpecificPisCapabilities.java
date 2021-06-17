package se.tink.backend.aggregation.agents.agentcapabilities;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.PisCapability.FASTER_PAYMENTS;

import org.junit.Ignore;

@Ignore
@AgentPisCapability(capabilities = FASTER_PAYMENTS, markets = "GB")
public class TestAgentWithListedMarketSpecificPisCapabilities extends BaseTestAgent {}
