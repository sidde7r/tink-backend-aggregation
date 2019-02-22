package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking;


import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.authenticator.UkOpenBankingAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.configuration.UkOpenBankingConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.UkOpenBankingAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.UkOpenBankingTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingBankTransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.session.UkOpenBankingSessionHandler;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdAuthenticationFlow;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.serialization.utils.SerializationUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public abstract class UkOpenBankingBaseAgent extends NextGenerationAgent {

    private final Provider tinkProvider;
    protected UkOpenBankingApiClient apiClient;

    protected SoftwareStatement softwareStatement;
    protected ProviderConfiguration providerConfiguration;

    // Separate httpClient used for payments since PIS and AIS are two different
    // authenticated flows.
    private final TinkHttpClient paymentsHttpClient;

    // Lazy loaded
    private UkOpenBankingAis aisSupport;
    private UkOpenBankingAccountFetcher<?, ?, TransactionalAccount> transactionalAccountFetcher;

    public UkOpenBankingBaseAgent(CredentialsRequest request, AgentContext context,
            SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        this.paymentsHttpClient = new TinkHttpClient(context.getAggregatorInfo(), context.getMetricRegistry(),
                context.getLogOutputStream(), signatureKeyPair, request.getProvider());
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
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        UkOpenBankingConfiguration ukOpenBankingConfiguration = SerializationUtils.deserializeFromString(
                configuration.getIntegrations().getUkOpenBankingJson(), UkOpenBankingConfiguration.class);

        String softwareStatementName = getSoftwareStatementName();
        String providerName = getProviderName();

        softwareStatement = ukOpenBankingConfiguration.getSoftwareStatement(softwareStatementName)
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Could not find softwareStatement: %s", softwareStatementName)));

        providerConfiguration = softwareStatement.getProviderConfiguration(providerName)
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Could not find provider conf: %s", providerName)));

        client.trustRootCaCertificate(ukOpenBankingConfiguration.getRootCAData(),
                ukOpenBankingConfiguration.getRootCAPassword());

        paymentsHttpClient.trustRootCaCertificate(ukOpenBankingConfiguration.getRootCAData(),
                ukOpenBankingConfiguration.getRootCAPassword());

        apiClient = new UkOpenBankingApiClient(client, softwareStatement, providerConfiguration,
                OpenIdConstants.ClientMode.ACCOUNTS);


        // -    We cannot configure the paymentsHttpClient from `configureHttpClient()` because it will be null
        //      at that stage.
        // -    Some banks are extremely slow at PIS operations (esp. the payment submission step), increase the the
        //      timeout on that http client.
        int timeoutInMilliseconds = (int) TimeUnit.SECONDS.toMillis(120);
        paymentsHttpClient.setTimeout(timeoutInMilliseconds);

        configureAisHttpClient(client);
        configurePisHttpClient(paymentsHttpClient);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        UkOpenBankingAuthenticator authenticator = new UkOpenBankingAuthenticator(apiClient);
        return createOpenIdFlowWithAuthenticator(authenticator);
    }

    protected final Authenticator createOpenIdFlowWithAuthenticator(
            UkOpenBankingAuthenticator authenticator) {
         return OpenIdAuthenticationFlow.create(
                request,
                context,
                persistentStorage,
                supplementalInformationHelper,
                authenticator,
                apiClient
        );
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        UkOpenBankingAis ais = getAisSupport();

        return Optional.of(new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        getTransactionalAccountFetcher(),
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                ais.makeAccountTransactionPaginatorController(apiClient),
                                ais.makeUpcomingTransactionFetcher(apiClient)
                                        .orElse(null))
                )
        );
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        UkOpenBankingAis ais = getAisSupport();

        return Optional.of(new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                ais.makeCreditCardAccountFetcher(apiClient),
                new TransactionFetcherController<>(transactionPaginationHelper,
                        ais.makeCreditCardTransactionPaginatorController(apiClient)))
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
        return Optional.of(
                new TransferDestinationRefreshController(
                        metricRefreshController,
                        new UkOpenBankingTransferDestinationFetcher()
                )
        );
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new UkOpenBankingSessionHandler();
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        Optional<UkOpenBankingPis> optionalPis = makePis();
        if (!optionalPis.isPresent()) {
            return Optional.empty();
        }

        UkOpenBankingPis pis = optionalPis.get();

        return Optional.of(
                new TransferController(
                        null,
                        new UkOpenBankingBankTransferExecutor(
                                catalog,
                                credentials,
                                supplementalInformationHelper,
                                softwareStatement,
                                providerConfiguration,
                                paymentsHttpClient,
                                getTransactionalAccountFetcher(),
                                pis
                        ),
                        null,
                        null
                )
        );
    }

    private UkOpenBankingAccountFetcher<?, ?, TransactionalAccount> getTransactionalAccountFetcher() {
        if (Objects.nonNull(transactionalAccountFetcher)) {
            return transactionalAccountFetcher;
        }

        transactionalAccountFetcher = getAisSupport().makeTransactionalAccountFetcher(apiClient);
        return transactionalAccountFetcher;
    }

    private UkOpenBankingAis getAisSupport() {
        if (Objects.nonNull(aisSupport)) {
            return aisSupport;
        }
        aisSupport = makeAis();
        return aisSupport;
    }

    protected abstract UkOpenBankingAis makeAis();
    protected abstract Optional<UkOpenBankingPis> makePis();
    protected abstract void configureAisHttpClient(TinkHttpClient httpClient);
    protected abstract void configurePisHttpClient(TinkHttpClient httpClient);
}
