package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.authenticator.AbnAmroAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.configuration.AbnAmroConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.AbnAmroAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.AbnAmroTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.session.AbnAmroSessionHandler;
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
import se.tink.libraries.credentials.service.CredentialsRequest;

public class AbnAmroAgent extends NextGenerationAgent {

    private final AbnAmroApiClient apiClient;
    private final String clientName;
    private AbnAmroConfiguration configuration;

    public AbnAmroAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        this.apiClient = new AbnAmroApiClient(client, persistentStorage);
        this.clientName = request.getProvider().getPayload();
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        final AbnAmroConfiguration abnAmroConfiguration =
                configuration
                        .getIntegrations()
                        .getClientConfiguration(
                                AbnAmroConstants.INTEGRATION_NAME,
                                clientName,
                                AbnAmroConfiguration.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                String.format(
                                                        "No abnamro client configured for name: %s",
                                                        clientName)));

        this.configuration = abnAmroConfiguration;
        apiClient.setConfiguration(abnAmroConfiguration);

        final String password = abnAmroConfiguration.getClientSSLKeyPassword();

        final byte[] p12 = abnAmroConfiguration.getClientSSLP12Bytes();
        client.setSslClientCertificate(p12, password);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new AbnAmroAuthenticator(apiClient, persistentStorage, configuration));

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
        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        new AbnAmroAccountFetcher(apiClient),
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionDatePaginationController<>(
                                        new AbnAmroTransactionFetcher(apiClient)))));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new AbnAmroSessionHandler();
    }
}
