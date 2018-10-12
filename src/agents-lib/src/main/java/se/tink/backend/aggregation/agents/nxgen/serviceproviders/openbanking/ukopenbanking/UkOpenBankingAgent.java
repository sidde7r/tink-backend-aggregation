package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.authenticator.UkOpenBankingAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.configuration.UkOpenBankingConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.UkOpenBankingAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.UkOpenBankingTransactionPaginator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.UkOpenBankingUpcomingTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.session.UkOpenBankingSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;
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
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.config.SignatureKeyPair;
import se.tink.libraries.serialization.utils.SerializationUtils;

public abstract class UkOpenBankingAgent extends NextGenerationAgent {

    private final Provider tinkProvider;
    private UkOpenBankingApiClient apiClient;

    public UkOpenBankingAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        tinkProvider = request.getProvider();
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        client.disableSignatureRequestHeader();
    }

    private String getSoftwareStatementName() {
        return tinkProvider.getPayload().split(":")[0];
    }

    private String getProviderName() {
        return tinkProvider.getPayload().split(":")[1];
    }

    @Override
    public void setConfiguration(ServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        UkOpenBankingConfiguration ukOpenBankingConfiguration = SerializationUtils.deserializeFromString(
                configuration.getIntegrations().getUkOpenBankingJson(), UkOpenBankingConfiguration.class);

        String softwareStatementName = getSoftwareStatementName();
        String providerName = getProviderName();

        SoftwareStatement softwareStatement = ukOpenBankingConfiguration.getSoftwareStatement(softwareStatementName)
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Could not find softwareStatement: %s", softwareStatementName)));

        ProviderConfiguration providerConfiguration = softwareStatement.getProviderConfiguration(providerName)
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Could not find provider conf: %s", providerName)));

        client.trustRootCaCertificate(ukOpenBankingConfiguration.getRootCAData(),
                ukOpenBankingConfiguration.getRootCAPassword());

        apiClient = new UkOpenBankingApiClient(client, softwareStatement, providerConfiguration);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        UkOpenBankingAuthenticator authenticator = new UkOpenBankingAuthenticator(apiClient);

        OpenIdAuthenticationController openIdAuthenticationController = new OpenIdAuthenticationController(
                persistentStorage,
                supplementalInformationController,
                apiClient,
                authenticator
        );

        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        openIdAuthenticationController,
                        supplementalInformationController
                ),
                openIdAuthenticationController
        );
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        return Optional.of(new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        makeTransactionalAccountFetcher(apiClient),
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(
                                        makeAccountTransactionPaginator(apiClient)),
                                makeUpcomingTransactionFetcher(apiClient))
                )
        );
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.of(new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                makeCreditCardAccountFetcher(apiClient),
                new TransactionFetcherController<>(transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                makeCreditCardTransactionPaginator(apiClient))))
        );
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
        return new UkOpenBankingSessionHandler();
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }

    protected abstract UkOpenBankingAccountFetcher<?, ?, TransactionalAccount> makeTransactionalAccountFetcher(
            UkOpenBankingApiClient apiClient);

    protected abstract UkOpenBankingTransactionPaginator<?, TransactionalAccount> makeAccountTransactionPaginator(
            UkOpenBankingApiClient apiClient);

    protected abstract UkOpenBankingUpcomingTransactionFetcher<?> makeUpcomingTransactionFetcher(
            UkOpenBankingApiClient apiClient);

    protected abstract UkOpenBankingAccountFetcher<?, ?, CreditCardAccount> makeCreditCardAccountFetcher(
            UkOpenBankingApiClient apiClient);

    protected abstract UkOpenBankingTransactionPaginator<?, CreditCardAccount> makeCreditCardTransactionPaginator(
            UkOpenBankingApiClient apiClient);

}
