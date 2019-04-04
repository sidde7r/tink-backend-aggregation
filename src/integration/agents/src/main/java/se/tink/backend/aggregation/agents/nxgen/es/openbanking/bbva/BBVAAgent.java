package se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.authenticator.BBVAAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.configuration.BBVAConfiguration;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount.BBVATransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount.BBVATransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.session.BBVASessionHandler;
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
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class BBVAAgent extends NextGenerationAgent {
    private final BBVAApiClient apiClient;

    public BBVAAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new BBVAApiClient(client, sessionStorage, persistentStorage);
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        final BBVAConfiguration bbvaConfiguration =
                configuration
                        .getIntegrations()
                        .getClientConfiguration(
                                BBVAConstants.Market.INTEGRATION_NAME,
                                BBVAConstants.Market.CLIENT_NAME,
                                BBVAConfiguration.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                BBVAConstants.Exceptions.MISSING_CONFIGURATION));
        if (!bbvaConfiguration.isValid()) {
            throw new IllegalStateException(BBVAConstants.Exceptions.INVALID_CONFIGURATION);
        }

        persistentStorage.put(
                BBVAConstants.StorageKeys.BASE_AUTH_URL, bbvaConfiguration.getBaseAuthUrl());
        persistentStorage.put(
                BBVAConstants.StorageKeys.BASE_API_URL, bbvaConfiguration.getBaseApiUrl());
        persistentStorage.put(BBVAConstants.StorageKeys.CLIENT_ID, bbvaConfiguration.getClientId());
        persistentStorage.put(
                BBVAConstants.StorageKeys.CLIENT_SECRET, bbvaConfiguration.getClientSecret());
        persistentStorage.put(
                BBVAConstants.StorageKeys.REDIRECT_URI, bbvaConfiguration.getRedirectUrl());
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {}

    @Override
    protected Authenticator constructAuthenticator() {

        BBVAAuthenticator authenticator = new BBVAAuthenticator(apiClient);
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
        BBVATransactionalAccountFetcher accountFetcher =
                new BBVATransactionalAccountFetcher(apiClient);

        BBVATransactionFetcher transactionFetcher = new BBVATransactionFetcher(apiClient);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        accountFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionPagePaginationController<>(
                                        transactionFetcher, BBVAConstants.Pagination.START_PAGE))));
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
        return new BBVASessionHandler(apiClient, sessionStorage);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
