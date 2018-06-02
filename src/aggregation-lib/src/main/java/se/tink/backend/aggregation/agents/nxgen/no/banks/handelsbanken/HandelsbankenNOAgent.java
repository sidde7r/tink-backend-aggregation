package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.HandelsbankenNOAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.HandelsbankenNOMultiFactorAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.HandelsbankenNOAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.HandelsbankenNOTransactionFetcher;
import se.tink.backend.aggregation.agents.utils.authentication.encap.EncapClient;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid.BankIdAuthenticationControllerNO;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.index.TransactionIndexPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.Field;

public class HandelsbankenNOAgent extends NextGenerationAgent {

    private final HandelsbankenNOApiClient apiClient;
    private EncapClient encapClient;

    public HandelsbankenNOAgent(CredentialsRequest request, AgentContext context) {
        super(request, context);
        apiClient = new HandelsbankenNOApiClient(client, sessionStorage);
        encapClient = new EncapClient(new HandelsbankenNOEncapConfiguration(), persistentStorage, client, true,
                credentials.getField(Field.Key.USERNAME));
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        client.setDebugOutput(true);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        HandelsbankenNOMultiFactorAuthenticator multiFactorAuthenticator =
                new HandelsbankenNOMultiFactorAuthenticator(apiClient, sessionStorage,
                        supplementalInformationController, catalog, encapClient);

        HandelsbankenNOAutoAuthenticator autoAuthenticator = new HandelsbankenNOAutoAuthenticator(apiClient,
                encapClient, sessionStorage);

        return new AutoAuthenticationController(request, context, new BankIdAuthenticationControllerNO(context,
                multiFactorAuthenticator), autoAuthenticator);

    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        return Optional.of(
                new TransactionalAccountRefreshController(metricRefreshController, updateController,
                        new HandelsbankenNOAccountFetcher(apiClient),
                        new TransactionFetcherController<>(transactionPaginationHelper,
                                new TransactionIndexPaginationController<>(
                                        new HandelsbankenNOTransactionFetcher(apiClient)))
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
        return new HandelsbankenNOSessionHandler(apiClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }

    public void populateSessionStorage(String key, String value){
        this.sessionStorage.put(key, value);
    }
}
