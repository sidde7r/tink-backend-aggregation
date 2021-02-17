package se.tink.backend.aggregation.agents.agentcapabilities;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.PisCapability.PIS_UK_FASTER_PAYMENT;

import org.junit.Ignore;

@Ignore
@AgentPisCapability(capabilities = PIS_UK_FASTER_PAYMENT, markets = "GB")
public class TestAgentWithListedMarketSpecificPisCapabilities extends BaseTestAgent {}
