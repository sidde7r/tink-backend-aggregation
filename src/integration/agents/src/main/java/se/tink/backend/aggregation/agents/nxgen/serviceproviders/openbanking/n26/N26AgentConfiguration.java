package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class N26AgentConfiguration extends Xs2aDevelopersProviderConfiguration
        implements ClientConfiguration {
    public N26AgentConfiguration(String clientId, String baseUrl, String redirectUrl) {
        super(clientId, baseUrl, redirectUrl);
    }
}
