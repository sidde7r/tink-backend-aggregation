package se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo.authenticator.MonzoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo.fetcher.transactional.MonzoTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo.session.MonzoSessionHandler;
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
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.common.config.MonzoConfiguration;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.config.SignatureKeyPair;

public class MonzoAgent extends NextGenerationAgent {

    private final MonzoApiClient apiClient;

    public MonzoAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        apiClient = new MonzoApiClient(client, persistentStorage);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        // client.setProxy("http://127.0.0.1:8888");
    }

    @Override
    public void setConfiguration(ServiceConfiguration configuration) {

        super.setConfiguration(configuration);

        MonzoConfiguration monzoConfiguration = configuration.getIntegrations().getMonzoConfiguration();
        persistentStorage.put(MonzoConstants.StorageKey.CLIENT_ID, monzoConfiguration.getClientId());
        persistentStorage.put(MonzoConstants.StorageKey.CLIENT_SECRET, monzoConfiguration.getClientSecret());
        persistentStorage.put(MonzoConstants.StorageKey.REDIRECT_URL, monzoConfiguration.getRedirectUrl());
    }

    @Override
    protected Authenticator constructAuthenticator() {

        MonzoAuthenticator authenticator = new MonzoAuthenticator(apiClient, persistentStorage);

        OAuth2AuthenticationController controller = new OAuth2AuthenticationController(persistentStorage,
                supplementalInformationController, authenticator);

        ThirdPartyAppAuthenticationController<String> thirdParty = new ThirdPartyAppAuthenticationController<>(
                controller,
                supplementalInformationController);

        return new AutoAuthenticationController(request, context, thirdParty, controller);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        MonzoTransactionalAccountFetcher fetcher = new MonzoTransactionalAccountFetcher(apiClient);
        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        fetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(fetcher)
                        )
                )
        );
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
    protected Optional<TransferDestinationRefreshController> constructTransferDestinationRefreshController() {
        return Optional.empty();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new MonzoSessionHandler(apiClient, persistentStorage);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }

}
