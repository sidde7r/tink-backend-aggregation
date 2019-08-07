package se.tink.backend.aggregation.agents.nxgen.fo.banks.sdcfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.fo.banks.sdcfo.parser.SdcFoTransactionParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.SdcAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.SdcSmsOtpAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.SdcAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.SdcTransactionFetcher;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp.SmsOtpAuthenticationPasswordController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SdcFoAgent extends SdcAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {
    private static Logger LOG = LoggerFactory.getLogger(SdcFoAgent.class);
    private static final int MAX_CONSECUTIVE_EMPTY_PAGES = 8;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public SdcFoAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(
                request,
                context,
                signatureKeyPair,
                new SdcFoConfiguration(request.getProvider()),
                new SdcFoTransactionParser());

        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return constructSmsAuthenticator();
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return transactionalAccountRefreshController.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return transactionalAccountRefreshController.fetchSavingsTransactions();
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                this.metricRefreshController,
                this.updateController,
                new SdcAccountFetcher(
                        this.bankClient, this.sdcSessionStorage, this.agentConfiguration),
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                new SdcTransactionFetcher(
                                        this.bankClient, this.sdcSessionStorage, this.parser),
                                MAX_CONSECUTIVE_EMPTY_PAGES)));
    }

    private Authenticator constructSmsAuthenticator() {
        SdcAutoAuthenticator autoAuthenticator =
                new SdcAutoAuthenticator(
                        bankClient, sdcSessionStorage, agentConfiguration, sdcPersistentStorage);
        SdcSmsOtpAuthenticator smsOtpAuthenticator =
                new SdcSmsOtpAuthenticator(
                        bankClient,
                        sdcSessionStorage,
                        agentConfiguration,
                        credentials,
                        sdcPersistentStorage);

        SmsOtpAuthenticationPasswordController smsOtpController =
                new SmsOtpAuthenticationPasswordController(
                        catalog, supplementalInformationHelper, smsOtpAuthenticator);

        return new AutoAuthenticationController(
                request, systemUpdater, smsOtpController, autoAuthenticator);
    }

    @Override
    protected SdcApiClient createApiClient(SdcConfiguration agentConfiguration) {
        return new SdcApiClient(client, agentConfiguration);
    }
}
