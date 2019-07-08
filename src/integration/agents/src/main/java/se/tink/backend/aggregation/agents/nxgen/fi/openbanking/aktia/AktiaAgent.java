package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.AktiaAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.AktiaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.configuration.AktiaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.fetcher.transactionalaccount.AktiaTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class AktiaAgent extends NextGenerationAgent {
    private final AktiaApiClient apiClient;

    public AktiaAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new AktiaApiClient(client, persistentStorage);
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        final AktiaConfiguration aktiaConfiguration =
                configuration
                        .getIntegrations()
                        .getClientConfiguration(
                                AktiaConstants.Market.INTEGRATION_NAME,
                                request.getProvider().getPayload(),
                                AktiaConfiguration.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                AktiaConstants.ErrorMessages
                                                        .MISSING_CONFIGURATION));
        apiClient.setConfiguration(aktiaConfiguration);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        AktiaAuthenticator authenticator =
                new AktiaAuthenticator(
                        apiClient, persistentStorage, credentials.getField(CredentialKeys.IBAN));

        AktiaAuthenticationController controller =
                new AktiaAuthenticationController(supplementalInformationHelper, authenticator);

        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        AktiaTransactionalAccountFetcher accountFetcher =
                new AktiaTransactionalAccountFetcher(apiClient);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        accountFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionDatePaginationController<>(accountFetcher))));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
