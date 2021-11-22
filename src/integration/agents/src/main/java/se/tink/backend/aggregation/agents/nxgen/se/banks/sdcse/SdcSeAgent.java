package se.tink.backend.aggregation.agents.nxgen.se.banks.sdcse;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sdcse.fetcher.SdcSeCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sdcse.fetcher.SdcSeIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sdcse.parser.SdcSeTransactionParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.accountidentifierhandler.SdcAccountIdentifierHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.accountidentifierhandler.SparbankenSydSdcAccountIdentifierHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.SdcBankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.http.truststrategy.TrustPinnedCertificateStrategy;

/*
 * Configure market specific client, this is SE in KIRKBY
 */
@AgentCapabilities({
    CHECKING_ACCOUNTS,
    SAVINGS_ACCOUNTS,
    CREDIT_CARDS,
    INVESTMENTS,
    IDENTITY_DATA,
    LOANS
})
public final class SdcSeAgent extends SdcAgent
        implements RefreshIdentityDataExecutor, RefreshCreditCardAccountsExecutor {
    private final CreditCardRefreshController creditCardRefreshController;

    @Inject
    public SdcSeAgent(AgentComponentProvider componentProvider) {
        super(
                componentProvider,
                new SdcSeConfiguration(componentProvider.getCredentialsRequest().getProvider()),
                new SdcSeTransactionParser());

        creditCardRefreshController = constructCreditCardRefreshController();
        this.client.loadTrustMaterial(
                null,
                TrustPinnedCertificateStrategy.forCertificate(SdcSeConstants.Secret.PUBLIC_CERT));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BankIdAuthenticationController<>(
                supplementalInformationController,
                new SdcBankIdAuthenticator(bankClient, sdcSessionStorage, credentials),
                persistentStorage,
                request);
    }

    @Override
    protected SdcAccountIdentifierHandler getSdcAccountNumberHandler() {
        return new SparbankenSydSdcAccountIdentifierHandler();
    }

    @Override
    protected SdcApiClient createApiClient(SdcConfiguration agentConfiguration) {
        return new SdcApiClient(client, agentConfiguration, catalog);
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
        SdcSeCreditCardFetcher creditCardFetcher =
                new SdcSeCreditCardFetcher(
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
                        new TransactionDatePaginationController.Builder<>(creditCardFetcher)
                                .build()));
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new SdcSeIdentityDataFetcher(sdcSessionStorage, credentials).fetchIdentityData();
    }
}
