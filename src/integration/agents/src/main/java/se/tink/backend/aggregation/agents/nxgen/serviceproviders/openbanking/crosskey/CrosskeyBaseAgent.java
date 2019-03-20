package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.CrosskeyBaseAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.configuration.CrosskeyBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.creditcardaccount.CreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.creditcardaccount.CreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.transactionalaccount.TransactionalAccountAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.transactionalaccount.TransactionalAccountTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.session.CrosskeySessionHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.utils.JWTUtils;
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

public abstract class CrosskeyBaseAgent extends NextGenerationAgent {

    protected final CrosskeyBaseApiClient apiClient;

    public CrosskeyBaseAgent(
        CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        apiClient = new CrosskeyBaseApiClient(client, sessionStorage, persistentStorage);

    }

    protected abstract String getIntegrationName();

    protected abstract String getClientName();

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        final CrosskeyBaseConfiguration crosskeyBaseConfiguration;
        crosskeyBaseConfiguration = configuration
            .getIntegrations()
            .getClientConfiguration(
                getIntegrationName(),
                getClientName(),
                CrosskeyBaseConfiguration.class)
            .orElseThrow(
                () -> new IllegalStateException(
                    CrosskeyBaseConstants.Exceptions.MISSING_CONFIGURATION));
        if (!crosskeyBaseConfiguration.isValid()) {
            throw new IllegalStateException(CrosskeyBaseConstants.Exceptions.INVALID_CONFIGURATION);
        }

        persistentStorage
            .put(CrosskeyBaseConstants.StorageKeys.BASE_AUTH_URL,
                crosskeyBaseConfiguration.getBaseAuthUrl());
        persistentStorage
            .put(CrosskeyBaseConstants.StorageKeys.BASE_API_URL,
                crosskeyBaseConfiguration.getBaseAPIUrl());
        persistentStorage
            .put(CrosskeyBaseConstants.StorageKeys.CLIENT_ID,
                crosskeyBaseConfiguration.getClientId());
        persistentStorage.put(
            CrosskeyBaseConstants.StorageKeys.CLIENT_SECRET,
            crosskeyBaseConfiguration.getClientSecret());
        persistentStorage
            .put(CrosskeyBaseConstants.StorageKeys.REDIRECT_URI,
                crosskeyBaseConfiguration.getRedirectUrl());
        persistentStorage
            .put(CrosskeyBaseConstants.StorageKeys.CERTIFICATE_PATH,
                crosskeyBaseConfiguration.getClientSigningCertificatePath());
        persistentStorage
            .put(CrosskeyBaseConstants.StorageKeys.KEY_PATH,
                crosskeyBaseConfiguration.getClientSigningKeyPath());
        persistentStorage
            .put(CrosskeyBaseConstants.StorageKeys.KEY_STORE_PWD,
                crosskeyBaseConfiguration.getClientKeyStorePassword());
        persistentStorage
            .put(CrosskeyBaseConstants.StorageKeys.KEY_STORE_PATH,
                crosskeyBaseConfiguration.getClientKeyStorePath());
        persistentStorage
            .put(CrosskeyBaseConstants.StorageKeys.X_FAPI_FINANCIAL_ID,
                crosskeyBaseConfiguration.getXFapiFinancialId());

        client.setSslClientCertificate(
            JWTUtils.readFile(crosskeyBaseConfiguration.getClientKeyStorePath()),
            crosskeyBaseConfiguration.getClientKeyStorePassword());
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        client.setDebugOutput(true);
    }

    @Override
    protected Authenticator constructAuthenticator() {

        CrosskeyBaseAuthenticator authenticator = new CrosskeyBaseAuthenticator(apiClient);
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
        TransactionalAccountAccountFetcher accountFetcher =
            new TransactionalAccountAccountFetcher(apiClient);

        TransactionalAccountTransactionFetcher transactionFetcher =
            new TransactionalAccountTransactionFetcher(apiClient);

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
    public Optional<CreditCardRefreshController> constructCreditCardRefreshController() {

        CreditCardAccountFetcher accountFetcher =
            new CreditCardAccountFetcher(apiClient);

        CreditCardTransactionFetcher transactionFetcher =
            new CreditCardTransactionFetcher(apiClient);

        return Optional.of(
            new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                    transactionPaginationHelper,
                    new TransactionDatePaginationController<>(transactionFetcher))));
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
        return new CrosskeySessionHandler(apiClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }


}
