package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking;

import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.authenticator.UkOpenBankingAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.configuration.UkOpenBankingConfiguration;
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
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.libraries.serialization.utils.SerializationUtils;

public abstract class UkOpenBankingBaseAgent extends NextGenerationAgent {

    private final Provider tinkProvider;
    private UkOpenBankingApiClient apiClient;

    // Lazy loaded
    private UkOpenBankingAis aisSupport;

    public UkOpenBankingBaseAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        tinkProvider = request.getProvider();
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        client.disableSignatureRequestHeader();
        configureAisHttpClient(client);
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

        SoftwareStatement softwareStatement = ukOpenBankingConfiguration.getSoftwareStatement(softwareStatementName)
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Could not find softwareStatement: %s", softwareStatementName)));

        ProviderConfiguration providerConfiguration = softwareStatement.getProviderConfiguration(providerName)
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Could not find provider conf: %s", providerName)));

        client.trustRootCaCertificate(ukOpenBankingConfiguration.getRootCAData(),
                ukOpenBankingConfiguration.getRootCAPassword());

        apiClient = new UkOpenBankingApiClient(client, softwareStatement, providerConfiguration,
                OpenIdConstants.ClientMode.ACCOUNTS);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        UkOpenBankingAuthenticator authenticator = new UkOpenBankingAuthenticator(apiClient);
        return OpenIdAuthenticationFlow.create(
                request,
                context,
                persistentStorage,
                supplementalInformationController,
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
                        ais.makeTransactionalAccountFetcher(apiClient),
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

    private UkOpenBankingAis getAisSupport() {
        if (Objects.nonNull(aisSupport)) {
            return aisSupport;
        }
        aisSupport = makeAis();
        return aisSupport;
    }

    protected abstract UkOpenBankingAis makeAis();
    protected abstract void configureAisHttpClient(TinkHttpClient httpClient);
}
