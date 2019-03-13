package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.authenticator.IngAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.configuration.IngConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.IngAccountsFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.IngTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.session.IngSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.utils.IngUtils;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class IngAgent extends NextGenerationAgent {

    private final IngApiClient apiClient;

    public IngAgent(
        CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new IngApiClient(client, sessionStorage, persistentStorage,
            request.getProvider().getMarket().toLowerCase());
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        final IngConfiguration ingConfiguration =
            configuration
                .getIntegrations()
                .getClientConfiguration(
                    IngConstants.Market.INTEGRATION_NAME,
                    IngConstants.Market.CLIENT_NAME,
                    IngConfiguration.class)
                .orElseThrow(() -> new IllegalStateException("ING configuration missing."));

        client.setSslClientCertificate(IngUtils.readFile(ingConfiguration.getClientKeyStorePath()),
            ingConfiguration.getClientKeyStorePassword());

        persistentStorage.put(IngConstants.StorageKeys.BASE_URL, ingConfiguration.getBaseUrl());
        persistentStorage.put(IngConstants.StorageKeys.CLIENT_ID, ingConfiguration.getClientId());
        persistentStorage.put(
            IngConstants.StorageKeys.CLIENT_SIGNING_KEY_PATH,
            ingConfiguration.getClientSigningKeyPath());
        persistentStorage.put(
            IngConstants.StorageKeys.CLIENT_SIGNING_CERTIFICATE_PATH,
            ingConfiguration.getClientSigningCertificatePath());
        persistentStorage
            .put(IngConstants.StorageKeys.REDIRECT_URL, ingConfiguration.getRedirectUrl());
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
    }

    @Override
    protected Authenticator constructAuthenticator() {
        IngAuthenticator authenticator = new IngAuthenticator(apiClient, sessionStorage);
        OAuth2AuthenticationController oAuth2AuthenticationController =
            new OAuth2AuthenticationController(
                persistentStorage, supplementalInformationHelper, authenticator);
        return new AutoAuthenticationController(
            request,
            context,
            new ThirdPartyAppAuthenticationController<>(
                oAuth2AuthenticationController, supplementalInformationHelper),
            oAuth2AuthenticationController);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
    constructTransactionalAccountRefreshController() {
        return Optional.of(
            new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new IngAccountsFetcher(apiClient,
                    request.getProvider().getCurrency().toUpperCase()),
                new TransactionFetcherController<>(
                    transactionPaginationHelper,
                    new TransactionDatePaginationController<>(
                        new IngTransactionsFetcher(apiClient)))));
    }

    @Override
    public Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<EInvoiceRefreshController> constructEInvoiceRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<TransferDestinationRefreshController>
    constructTransferDestinationRefreshController() {
        return Optional.empty();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new IngSessionHandler(apiClient, sessionStorage);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
