package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken;

import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.IcaBankenAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.configuration.IcaBankenConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.IcaBankenTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.IcaBankenTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class IcaBankenAgent extends NextGenerationAgent {

    private final IcaBankenApiClient apiClient;
    private final String clientName;
    private IcaBankenConfiguration icaBankenConfiguration;
    private Credentials credentialsRequest;

    public IcaBankenAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new IcaBankenApiClient(client, sessionStorage);
        clientName = request.getProvider().getPayload();
        credentialsRequest = request.getCredentials();
    }

    private void configureHttpClient(TinkHttpClient client) {
        client.setEidasProxy(
                icaBankenConfiguration.getEidasUrl(), icaBankenConfiguration.getCertificateId());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new IcaBankenAuthenticator(
                                apiClient,
                                sessionStorage,
                                icaBankenConfiguration,
                                credentialsRequest));

        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        icaBankenConfiguration =
                configuration
                        .getIntegrations()
                        .getClientConfiguration(
                                IcaBankenConstants.INTEGRATION_NAME,
                                clientName,
                                IcaBankenConfiguration.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                ErrorMessages.MISSING_CONFIGURATION));

        apiClient.setConfiguration(icaBankenConfiguration);
        configureHttpClient(client);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        IcaBankenTransactionalAccountFetcher accountFetcher =
                new IcaBankenTransactionalAccountFetcher(apiClient);

        IcaBankenTransactionFetcher transactionFetcher = new IcaBankenTransactionFetcher(apiClient);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        accountFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionDatePaginationController<>(transactionFetcher))));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
