package se.tink.backend.aggregation.agents.nxgen.se.banks.sdcse;

import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sdcse.fetcher.SdcSeCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sdcse.fetcher.SdcSeIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sdcse.parser.SdcSeTransactionParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.SdcBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.converter.AccountNumberToIbanConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.converter.SparbankenSydAccountNumberToIbanConverter;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.libraries.credentials.service.CredentialsRequest;

/*
 * Configure market specific client, this is SE in KIRKBY
 */
public class SdcSeAgent extends SdcAgent
        implements RefreshIdentityDataExecutor, RefreshCreditCardAccountsExecutor {
    private final CreditCardRefreshController creditCardRefreshController;

    public SdcSeAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(
                request,
                context,
                signatureKeyPair,
                new SdcSeConfiguration(request.getProvider()),
                new SdcSeTransactionParser());

        creditCardRefreshController = constructCreditCardRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BankIdAuthenticationController<>(
                supplementalRequester,
                new SdcBankIdAuthenticator(bankClient, sdcSessionStorage, credentials),
                true,
                persistentStorage,
                credentials);
    }

    @Override
    protected AccountNumberToIbanConverter getIbanConverter() {
        return new SparbankenSydAccountNumberToIbanConverter();
    }

    @Override
    protected SdcApiClient createApiClient(SdcConfiguration agentConfiguration) {
        return new SdcApiClient(client, agentConfiguration);
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
                        new TransactionDatePaginationController<>(creditCardFetcher)));
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new SdcSeIdentityDataFetcher(sdcSessionStorage, credentials).fetchIdentityData();
    }
}
