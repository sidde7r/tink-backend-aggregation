package se.tink.backend.aggregation.agents.nxgen.fi.banks.danskebank;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.DanskeBankChallengeAuthenticator;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticationController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class DanskeBankFIAgent extends DanskeBankAgent {
    public DanskeBankFIAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new DanskeBankFIConfiguration());
    }

    @Override
    protected DanskeBankApiClient createApiClient(TinkHttpClient client, DanskeBankConfiguration configuration) {
        return new DanskeBankFIApiClient(client, (DanskeBankFIConfiguration) configuration);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        client.disableSignatureRequestHeader();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        DanskeBankChallengeAuthenticator danskeBankChallengeAuthenticator = new DanskeBankChallengeAuthenticator(
                (DanskeBankFIApiClient) apiClient, persistentStorage, credentials, deviceId, configuration);

        return new AutoAuthenticationController(request, systemUpdater,
                new KeyCardAuthenticationController(
                        catalog, supplementalInformationHelper, danskeBankChallengeAuthenticator),
                danskeBankChallengeAuthenticator);
    }
}
