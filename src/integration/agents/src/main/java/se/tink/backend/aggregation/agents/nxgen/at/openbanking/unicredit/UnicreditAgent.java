package se.tink.backend.aggregation.agents.nxgen.at.openbanking.unicredit;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.configuration.UnicreditProviderConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class UnicreditAgent extends UnicreditBaseAgent {

    private static final UnicreditProviderConfiguration PROVIDER_CONFIG =
            new UnicreditProviderConfiguration("BUSINESSNET", "https://api.bankaustria.at");

    @Inject
    public UnicreditAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    protected UnicreditBaseApiClient getApiClient(boolean manualRequest) {
        return new UnicreditApiClient(
                client,
                persistentStorage,
                sessionStorage,
                credentials,
                manualRequest,
                PROVIDER_CONFIG);
    }
}
