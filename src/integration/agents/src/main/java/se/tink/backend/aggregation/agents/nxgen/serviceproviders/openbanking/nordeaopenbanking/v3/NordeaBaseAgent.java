package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.configuration.NordeaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.sessionhandler.NordeaBaseSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.transactionalaccount.NordeaAccountParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.transactionalaccount.NordeaBaseAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.transactionalaccount.NordeaBaseTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.transactionalaccount.NordeaTransactionParser;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class NordeaBaseAgent extends NextGenerationAgent {
    private final NordeaBaseApiClient apiClient;
    private final NordeaSessionStorage nordeaSessionStorage;
    private final NordeaPersistentStorage nordeaPersistentStorage;
    private final String clientName;

    public NordeaBaseAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        nordeaSessionStorage = new NordeaSessionStorage(sessionStorage);
        nordeaPersistentStorage = new NordeaPersistentStorage(persistentStorage);
        apiClient = new NordeaBaseApiClient(client, nordeaSessionStorage, nordeaPersistentStorage);
        clientName = request.getProvider().getPayload();
    }

    protected NordeaPersistentStorage getNordeaPersistentStorage() {
        return nordeaPersistentStorage;
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        NordeaConfiguration nordeaConfiguration =
                configuration
                        .getIntegrations()
                        .getClientConfiguration(NordeaBaseConstants.INTEGRATION_NAME, clientName, NordeaConfiguration.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                String.format(
                                                        "No Nordea client configured for name: %s",
                                                        clientName)));

        nordeaPersistentStorage.setClientId(nordeaConfiguration.getClientId());
        nordeaPersistentStorage.setClientSecret(nordeaConfiguration.getClientSecret());
        nordeaPersistentStorage.setRedirectUrl(nordeaConfiguration.getRedirectUrl());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return constructAuthenticator(apiClient);
    }

    protected abstract Authenticator constructAuthenticator(NordeaBaseApiClient apiClient);

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        NordeaBaseAccountFetcher accountFetcher = new NordeaBaseAccountFetcher(apiClient, createAccountParser());
        TransactionKeyPaginator<TransactionalAccount, LinkEntity> transactionFetcher =
                new NordeaBaseTransactionFetcher(apiClient, createTransactionParser());

        return Optional.of(
                new TransactionalAccountRefreshController(metricRefreshController,
                        updateController,
                        accountFetcher,
                        new TransactionFetcherController<>(transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(
                                        transactionFetcher))
                ));
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
        return new NordeaBaseSessionHandler(nordeaSessionStorage);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }

    protected NordeaAccountParser createAccountParser() {
        return new NordeaAccountParser();
    }

    protected NordeaTransactionParser createTransactionParser() {
        return new NordeaTransactionParser();

    }
}
