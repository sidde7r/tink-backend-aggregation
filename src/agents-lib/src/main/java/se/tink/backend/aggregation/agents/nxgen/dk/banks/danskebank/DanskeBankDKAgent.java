package se.tink.backend.aggregation.agents.nxgen.dk.banks.danskebank;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.danskebank.filters.DanskeBankDKHttpFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.DanskeBankPasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class DanskeBankDKAgent extends DanskeBankAgent {
    public DanskeBankDKAgent(CredentialsRequest request, AgentContext context) {
        super(request, context, new DanskeBankDKConfiguration());
    }

    @Override
    protected DanskeBankApiClient createApiClient(TinkHttpClient client, DanskeBankConfiguration configuration) {
        return new DanskeBankDKApiClient(client, (DanskeBankDKConfiguration) configuration);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new DanskeBankDKHttpFilter());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                        new DanskeBankPasswordAuthenticator(apiClient, deviceId, configuration));
    }
}
