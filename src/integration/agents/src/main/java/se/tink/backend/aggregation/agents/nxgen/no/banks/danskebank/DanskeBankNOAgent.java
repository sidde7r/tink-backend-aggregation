package se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank.authenticator.DanskeBankNOBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.selenium.WebDriverHelper;

public class DanskeBankNOAgent extends DanskeBankAgent {
    public DanskeBankNOAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new DanskeBankNOConfiguration());
    }

    @Override
    protected DanskeBankApiClient createApiClient(
            TinkHttpClient client, DanskeBankConfiguration configuration) {
        return new DanskeBankNOApiClient(
                client, (DanskeBankNOConfiguration) configuration, credentials);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        DanskeBankNOBankIdAuthenticator danskeBankNoBankIdAuthenticator =
                new DanskeBankNOBankIdAuthenticator(
                        (DanskeBankNOApiClient) apiClient,
                        persistentStorage,
                        credentials,
                        deviceId,
                        configuration,
                        new WebDriverHelper());
        return new AutoAuthenticationController(
                request,
                systemUpdater,
                danskeBankNoBankIdAuthenticator,
                danskeBankNoBankIdAuthenticator);
    }
}
