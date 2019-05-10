package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.SdcAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.SdcCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.SdcInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.SdcLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.SdcTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.parser.SdcTransactionParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.sessionhandler.SdcSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
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

public abstract class SdcAgent extends NextGenerationAgent {

    protected final SdcTransactionParser parser;
    protected SdcConfiguration agentConfiguration;
    protected SdcApiClient bankClient;
    protected SdcSessionStorage sdcSessionStorage;
    protected SdcPersistentStorage sdcPersistentStorage;

    public SdcAgent(
            CredentialsRequest request,
            AgentContext context,
            SignatureKeyPair signatureKeyPair,
            SdcConfiguration agentConfiguration,
            SdcTransactionParser parser) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);
        this.parser = parser;
        this.agentConfiguration = agentConfiguration;
        this.sdcSessionStorage = new SdcSessionStorage(this.sessionStorage);
        sdcPersistentStorage = new SdcPersistentStorage(this.persistentStorage);
        this.bankClient = this.createApiClient(agentConfiguration);
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.setTimeout(SdcConstants.HTTP_TIMEOUT);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        return Optional.of(
                new TransactionalAccountRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        new SdcAccountFetcher(
                                this.bankClient, this.sdcSessionStorage, this.agentConfiguration),
                        new TransactionFetcherController<>(
                                this.transactionPaginationHelper,
                                new TransactionDatePaginationController<>(
                                        new SdcTransactionFetcher(
                                                this.bankClient,
                                                this.sdcSessionStorage,
                                                this.parser)))));
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(
                new LoanRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        new SdcLoanFetcher(
                                this.bankClient, this.sdcSessionStorage, request.getProvider())));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new SdcSessionHandler(this.bankClient);
    }

    protected abstract SdcApiClient createApiClient(SdcConfiguration agentConfiguration);

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        SdcCreditCardFetcher creditCardFetcher =
                new SdcCreditCardFetcher(
                        this.bankClient,
                        this.sdcSessionStorage,
                        this.parser,
                        this.agentConfiguration);

        return Optional.of(
                new CreditCardRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        creditCardFetcher,
                        new TransactionFetcherController<>(
                                this.transactionPaginationHelper,
                                new TransactionDatePaginationController<>(creditCardFetcher))));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.of(
                new InvestmentRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        new SdcInvestmentFetcher(
                                this.bankClient, this.sdcSessionStorage, this.agentConfiguration)));
    }

    @Override
    protected Optional<TransferDestinationRefreshController>
            constructTransferDestinationRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
