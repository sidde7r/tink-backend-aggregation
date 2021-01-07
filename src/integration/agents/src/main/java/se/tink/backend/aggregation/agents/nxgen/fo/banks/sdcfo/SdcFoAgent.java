package se.tink.backend.aggregation.agents.nxgen.fo.banks.sdcfo;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.fo.banks.sdcfo.SdcFoConstants.Secret;
import se.tink.backend.aggregation.agents.nxgen.fo.banks.sdcfo.parser.SdcFoTransactionParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.SdcAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.SdcSmsOtpAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.converter.AccountNumberToIbanConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.converter.DefaultAccountNumberToIbanConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.SdcAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.SdcTransactionFetcher;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp.SmsOtpAuthenticationPasswordController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.http.truststrategy.TrustPinnedCertificateStrategy;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS, INVESTMENTS, LOANS})
public final class SdcFoAgent extends SdcAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {
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
        this.client.loadTrustMaterial(
                null, TrustPinnedCertificateStrategy.forCertificate(Secret.PUBLIC_CERT));
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
        SdcTransactionFetcher sdcTransactionFetcher =
                new SdcTransactionFetcher(this.bankClient, this.sdcSessionStorage, this.parser);

        return new TransactionalAccountRefreshController(
                this.metricRefreshController,
                this.updateController,
                new SdcAccountFetcher(this.bankClient, this.sdcSessionStorage, getIbanConverter()),
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(sdcTransactionFetcher)
                                .setConsecutiveEmptyPagesLimit(MAX_CONSECUTIVE_EMPTY_PAGES)
                                .build()));
    }

    @Override
    protected AccountNumberToIbanConverter getIbanConverter() {
        return DefaultAccountNumberToIbanConverter.FO_CONVERTER;
    }

    private Authenticator constructSmsAuthenticator() {
        SdcAutoAuthenticator autoAuthenticator =
                new SdcAutoAuthenticator(
                        bankClient,
                        sdcSessionStorage,
                        agentConfiguration,
                        credentials,
                        sdcPersistentStorage);
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
        return new SdcApiClient(client, agentConfiguration, catalog);
    }
}
