package se.tink.backend.aggregation.agents.nxgen.dk.banks.sdcdk;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.sdcdk.SdcDkConstants.Secret;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.sdcdk.parser.SdcDkTransactionParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.SdcAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.SdcPinAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.SdcSmsOtpAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.converter.AccountNumberToIbanConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.converter.DefaultAccountNumberToIbanConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.SdcAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.SdcCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.SdcTransactionFetcher;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp.SmsOtpAuthenticationPasswordController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.http.truststrategy.TrustPinnedCertificateStrategy;
import se.tink.libraries.credentials.service.CredentialsRequest;

/*
 * Configure market specific client, this is DK
 */
@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS, INVESTMENTS, LOANS})
public final class SdcDkAgent extends SdcAgent
        implements RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(SdcDkAgent.class);
    private static final int DK_MAX_CONSECUTIVE_EMPTY_PAGES = 8;

    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public SdcDkAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(
                request,
                context,
                signatureKeyPair,
                new SdcDkConfiguration(request.getProvider()),
                new SdcDkTransactionParser());

        creditCardRefreshController = constructCreditCardRefreshController();
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
        this.client.loadTrustMaterial(
                null, TrustPinnedCertificateStrategy.forCertificate(Secret.PUBLIC_CERT));
    }

    @Override
    protected Authenticator constructAuthenticator() {

        if (SdcDkConstants.Authentication.BANKS_WITH_PIN_AUTHENTICATION.contains(
                agentConfiguration.getBankCode())) {
            return constructPinAuthenticator();
        }

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
                new SdcAccountFetcher(this.bankClient, this.sdcSessionStorage, getIbanConverter()),
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                new SdcTransactionFetcher(
                                        this.bankClient, this.sdcSessionStorage, this.parser),
                                DK_MAX_CONSECUTIVE_EMPTY_PAGES)));
    }

    @Override
    protected AccountNumberToIbanConverter getIbanConverter() {
        return DefaultAccountNumberToIbanConverter.DK_CONVERTER;
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        SdcCreditCardFetcher creditCardFetcher =
                new SdcCreditCardFetcher(
                        this.bankClient,
                        this.sdcSessionStorage,
                        this.parser,
                        this.agentConfiguration);

        return new CreditCardRefreshController(
                this.metricRefreshController,
                this.updateController,
                creditCardFetcher,
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                creditCardFetcher, DK_MAX_CONSECUTIVE_EMPTY_PAGES)));
    }

    private Authenticator constructSmsAuthenticator() {
        LOG.info("SDC bank using SMS authentication");
        SdcAutoAuthenticator dkAutoAuthenticator =
                new SdcAutoAuthenticator(
                        bankClient,
                        sdcSessionStorage,
                        agentConfiguration,
                        credentials,
                        sdcPersistentStorage);
        SdcSmsOtpAuthenticator dkSmsOtpAuthenticator =
                new SdcSmsOtpAuthenticator(
                        bankClient,
                        sdcSessionStorage,
                        agentConfiguration,
                        credentials,
                        sdcPersistentStorage);

        SmsOtpAuthenticationPasswordController smsOtpController =
                new SmsOtpAuthenticationPasswordController(
                        catalog, supplementalInformationHelper, dkSmsOtpAuthenticator);

        return new AutoAuthenticationController(
                request, systemUpdater, smsOtpController, dkAutoAuthenticator);
    }

    private Authenticator constructPinAuthenticator() {
        LOG.info("SDC bank using pin authentication");

        SdcPinAuthenticator dkAuthenticator =
                new SdcPinAuthenticator(bankClient, sdcSessionStorage, agentConfiguration);

        return new PasswordAuthenticationController(dkAuthenticator);
    }

    @Override
    protected SdcApiClient createApiClient(SdcConfiguration agentConfiguration) {
        return new SdcApiClient(client, agentConfiguration, catalog);
    }
}
