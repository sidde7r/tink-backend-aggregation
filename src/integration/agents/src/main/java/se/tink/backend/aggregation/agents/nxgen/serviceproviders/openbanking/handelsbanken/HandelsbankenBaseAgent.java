package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.HandelsbankenBaseAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.configuration.HandelsbankenBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.HandelsbankenBaseTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.liveenrolement.HandelsbankenBaseLiveEnrolement;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

import java.util.Optional;

public abstract class HandelsbankenBaseAgent extends NextGenerationAgent {

    private final HandelsbankenBaseApiClient apiClient;
    private final HandelsbankenBaseLiveEnrolement enrolement;
    private final String clientName;
    private HandelsbankenBaseConfiguration handelsbankenBaseConfiguration;

    public HandelsbankenBaseAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        apiClient = new HandelsbankenBaseApiClient(client, sessionStorage);
        enrolement = new HandelsbankenBaseLiveEnrolement(client);
        clientName = request.getProvider().getPayload();
    }

    protected abstract HandelsbankenBaseAccountConverter getAccountConverter();

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        handelsbankenBaseConfiguration =
                configuration
                        .getIntegrations()
                        .getClientConfiguration(
                                HandelsbankenBaseConstants.INTEGRATION_NAME,
                                clientName,
                                HandelsbankenBaseConfiguration.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                HandelsbankenBaseConstants.ExceptionMessages
                                                        .CONFIG_MISSING));

        apiClient.setConfiguration(handelsbankenBaseConfiguration);
        enrolement.setConfiguration(handelsbankenBaseConfiguration);
        configureHttpClient(client);
    }

    private void configureHttpClient(TinkHttpClient client) {
        client.setEidasProxy(handelsbankenBaseConfiguration.getEidasUrl(), "Tink");
    }

    @Override
    protected Authenticator constructAuthenticator() {

        return new BankIdAuthenticationController<SessionResponse>(
                        supplementalRequester,
                        new HandelsbankenBaseAuthenticator(apiClient, sessionStorage));
    }


    @Override
    protected Optional<TransactionalAccountRefreshController>
    constructTransactionalAccountRefreshController() {
        final HandelsbankenBaseTransactionalAccountFetcher accountFetcher =
            new HandelsbankenBaseTransactionalAccountFetcher(apiClient);

        accountFetcher.setConverter(getAccountConverter());

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
