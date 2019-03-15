package se.tink.backend.aggregation.agents.nxgen.dk.banks.danskebank;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.DanskeBankChallengeAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.DanskeBankLoanFetcher;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

import java.util.Optional;

public class DanskeBankDKAgent extends DanskeBankAgent {
    public DanskeBankDKAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new DanskeBankDKConfiguration());
    }

    @Override
    protected DanskeBankApiClient createApiClient(TinkHttpClient client, DanskeBankConfiguration configuration) {
        return new DanskeBankDKApiClient(client, (DanskeBankDKConfiguration) configuration);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        client.disableSignatureRequestHeader();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        DanskeBankChallengeAuthenticator danskeBankChallengeAuthenticator = new DanskeBankChallengeAuthenticator(
                apiClient, persistentStorage, credentials, deviceId, configuration);

        return new AutoAuthenticationController(request, systemUpdater,
                new KeyCardAuthenticationController(
                        catalog, supplementalInformationHelper, danskeBankChallengeAuthenticator),
                danskeBankChallengeAuthenticator);
    }

    // DK fetches loans at a separate loan endpoint
    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(new LoanRefreshController(this.metricRefreshController, this.updateController,
                new DanskeBankLoanFetcher(this.credentials, this.apiClient, this.configuration)));
    }
}
