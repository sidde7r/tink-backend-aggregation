package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.authenticator.SEBAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.configuration.SEBConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.transactionalaccount.SEBTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.session.SEBSessionHandler;
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

public final class SEBAgent extends NextGenerationAgent {
    private final SEBApiClient apiClient;

    public SEBAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new SEBApiClient(client, sessionStorage, persistentStorage);
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        final SEBConfiguration sebConfiguration =
                configuration
                        .getIntegrations()
                        .getClientConfiguration(SEBConstants.Market.INTEGRATION_NAME, SEBConstants.Market.CLIENT_NAME,
                                SEBConfiguration.class)
                        .orElseThrow(
                                () -> new IllegalStateException("SEB configuration missing."));

        persistentStorage.put(
                SEBConstants.STORAGE.CLIENT_ID, sebConfiguration.getClientId());
        persistentStorage.put(
                SEBConstants.STORAGE.CLIENT_SECRET,
                sebConfiguration.getClientSecret());
        persistentStorage.put(
                SEBConstants.STORAGE.REDIRECT_URI, sebConfiguration.getRedirectUrl());
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
    }

    @Override
    protected Authenticator constructAuthenticator() {
        SEBAuthenticator authenticator = new SEBAuthenticator(apiClient, sessionStorage);
        OAuth2AuthenticationController oAuth2AuthenticationController = new OAuth2AuthenticationController(
                persistentStorage,
                supplementalInformationHelper, authenticator);
        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        oAuth2AuthenticationController,
                        supplementalInformationHelper
                ),
                oAuth2AuthenticationController);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
    constructTransactionalAccountRefreshController() {
        SEBTransactionalAccountFetcher accountFetcher =
                new SEBTransactionalAccountFetcher(apiClient);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        accountFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionPagePaginationController<>(accountFetcher,
                                        SEBConstants.FETCHER.START_PAGE))));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
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
        return new SEBSessionHandler(apiClient, sessionStorage);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
