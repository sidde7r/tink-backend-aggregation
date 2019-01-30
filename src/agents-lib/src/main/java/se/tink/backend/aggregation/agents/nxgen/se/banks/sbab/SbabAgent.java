package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab;

import java.util.Optional;
import java.util.function.Supplier;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.SbabAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.SbabLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccount.SbabTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.session.SbabSessionHandler;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.configuration.integrations.SbabClientConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
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
import se.tink.libraries.credentials_requests.CredentialsRequest;

public class SbabAgent extends NextGenerationAgent {
    private final SbabApiClient apiClient;
    private final String clientName;

    public SbabAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new SbabApiClient(client, sessionStorage, persistentStorage);
        clientName = request.getProvider().getPayload();
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {}

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        final Supplier<IllegalStateException> illegalStateException =
                () ->
                        new IllegalStateException(
                                String.format(
                                        "No SBAB client configured for name: %s", clientName));
        final SbabClientConfiguration config =
                configuration
                        .getIntegrations()
                        .getSbab(clientName)
                        .orElseThrow(illegalStateException);

        persistentStorage.put(StorageKey.ENVIRONMENT, config.getEnvironment());
        persistentStorage.put(StorageKey.BASIC_AUTH_USERNAME, config.getBasicAuthUsername());
        persistentStorage.put(StorageKey.BASIC_AUTH_PASSWORD, config.getBasicAuthPassword());
        persistentStorage.put(StorageKey.CLIENT_ID, config.getClientId());
        persistentStorage.put(StorageKey.REDIRECT_URI, config.getRedirectUri());
        sessionStorage.put(StorageKey.ACCESS_TOKEN, config.getAccessToken());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new ThirdPartyAppAuthenticationController<>(
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new SbabAuthenticator(apiClient, sessionStorage, persistentStorage)),
                supplementalInformationHelper);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        final SbabTransactionalAccountFetcher fetcher =
                new SbabTransactionalAccountFetcher(apiClient);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        fetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionDatePaginationController<>(fetcher))));
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
        return Optional.of(
                new LoanRefreshController(
                        metricRefreshController, updateController, new SbabLoanFetcher(apiClient)));
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
        return new SbabSessionHandler(apiClient, sessionStorage);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
