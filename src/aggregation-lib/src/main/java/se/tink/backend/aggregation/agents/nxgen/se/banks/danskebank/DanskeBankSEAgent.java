package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.authenticator.bankid.DanskeBankBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.filters.DanskeBankSeHttpFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.DanskeBankPasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class DanskeBankSEAgent extends DanskeBankAgent {
    public DanskeBankSEAgent(CredentialsRequest request, AgentContext context) {
        super(request, context, new DanskeBankSEConfiguration());
    }

    @Override
    protected DanskeBankApiClient createApiClient(TinkHttpClient client, DanskeBankConfiguration configuration) {
        return new DanskeBankSEApiClient(client, (DanskeBankSEConfiguration) configuration);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        client.setUserAgent("Mobilbank/813854 CFNetwork/808.2.16 Darwin/16.3.0");
        client.setDebugOutput(false);
        client.addFilter(new DanskeBankSeHttpFilter());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new TypedAuthenticationController(
                new BankIdAuthenticationController<>(context,
                        new DanskeBankBankIdAuthenticator((DanskeBankSEApiClient) apiClient, deviceId, configuration)),
                new PasswordAuthenticationController(
                        new DanskeBankPasswordAuthenticator(apiClient, deviceId, configuration)));
    }
}
