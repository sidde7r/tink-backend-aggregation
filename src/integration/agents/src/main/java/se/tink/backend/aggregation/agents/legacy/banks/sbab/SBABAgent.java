package se.tink.backend.aggregation.agents.banks.sbab;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.MORTGAGE_AGGREGATION;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.common.base.Objects;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.banks.sbab.SBABConstants.BankId;
import se.tink.backend.aggregation.agents.banks.sbab.client.AuthenticationClient;
import se.tink.backend.aggregation.agents.banks.sbab.client.IdentityDataClient;
import se.tink.backend.aggregation.agents.banks.sbab.client.UserDataClient;
import se.tink.backend.aggregation.agents.banks.sbab.entities.AccountEntity;
import se.tink.backend.aggregation.agents.banks.sbab.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.banks.sbab.rpc.InitBankIdResponse;
import se.tink.backend.aggregation.agents.banks.sbab.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Loan;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.constants.CommonHeaders;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.i18n.Catalog;

@AgentCapabilities({SAVINGS_ACCOUNTS, LOANS, MORTGAGE_AGGREGATION, IDENTITY_DATA})
public class SBABAgent extends AbstractAgent
        implements RefreshSavingsAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshIdentityDataExecutor {

    private final Credentials credentials;
    private final Catalog catalog;

    private final AuthenticationClient authenticationClient;
    private final UserDataClient userDataClient;
    private final IdentityDataClient identityDataClient;

    private final TinkHttpClient client;

    // cache
    private AccountsResponse accountsResponse = null;

    @Inject
    public SBABAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider.getCredentialsRequest(), agentComponentProvider.getContext());
        this.catalog = context.getCatalog();
        credentials = request.getCredentials();
        client = agentComponentProvider.getTinkHttpClient();

        authenticationClient =
                new AuthenticationClient(
                        client.getInternalClient(), credentials, CommonHeaders.DEFAULT_USER_AGENT);
        userDataClient =
                new UserDataClient(
                        client.getInternalClient(), credentials, CommonHeaders.DEFAULT_USER_AGENT);
        identityDataClient =
                new IdentityDataClient(
                        client.getInternalClient(), credentials, CommonHeaders.DEFAULT_USER_AGENT);
    }

    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {

        switch (credentials.getType()) {
            case MOBILE_BANKID:
                return Objects.equal(loginWithMobileBankId(), BankIdStatus.DONE);
            default:
                throw new IllegalStateException(
                        String.format(
                                "#login-refactoring - SBAB - Unsupported credential: %s",
                                credentials.getType()));
        }
    }

    private AccountsResponse getAccounts() {
        if (accountsResponse != null) {
            return accountsResponse;
        }

        try {
            accountsResponse = userDataClient.getAccounts();
            return accountsResponse;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private BankIdStatus loginWithMobileBankId() throws BankIdException, AuthorizationException {
        InitBankIdResponse initBankIdResponse = authenticationClient.initiateBankIdLogin();
        String pendingAuthCode = initBankIdResponse.getPendingAuthorizationCode();
        String autostartToken = initBankIdResponse.getAutostartToken();

        supplementalInformationController.openMobileBankIdAsync(autostartToken);

        for (int i = 0; i < BankId.BANKID_MAX_ATTEMPTS; i++) {
            BankIdStatus bankIdStatus = authenticationClient.getLoginStatus(pendingAuthCode);

            switch (bankIdStatus) {
                case DONE:
                    fetchAndSetCsrfToken();
                    return bankIdStatus;
                case CANCELLED:
                    throw BankIdError.CANCELLED.exception();
                case TIMEOUT:
                    initBankIdResponse = authenticationClient.initiateBankIdLogin();
                    pendingAuthCode = initBankIdResponse.getPendingAuthorizationCode();
                    autostartToken = initBankIdResponse.getAutostartToken();
                    supplementalInformationController.openMobileBankIdAsync(autostartToken);
                    break;
                case FAILED_UNKNOWN:
                    throw new IllegalStateException("[SBAB - BankId failed with unknown error]");
                default:
                    break;
            }

            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
        }

        throw BankIdError.TIMEOUT.exception();
    }

    /**
     * Some requests need Csrf authentication. This method makes sure that the clients which need it
     * have it.
     */
    private void fetchAndSetCsrfToken() throws AuthorizationException {
        String token = authenticationClient.getCsrfToken();
        userDataClient.setCsrfToken(token);
        // store token in the sensitive payload so that it will be masked
        credentials.setSensitivePayload(Key.ACCESS_TOKEN, token);
    }

    private List<Transaction> fetchTransactions(Account account) throws Exception {
        String accountNumber = account.getAccountNumber();
        TransactionsResponse completedTransaction =
                userDataClient.fetchCompletedTransactions(accountNumber);

        return completedTransaction.stream()
                .map(transactionEntity -> transactionEntity.toTinkTransaction(false))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<Account> toTinkAccounts(List<AccountEntity> accounts) {

        return accounts.stream()
                .map(AccountEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public void logout() throws Exception {}

    ///// Refresh Executor Refactor /////

    @Override
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        try {
            Map<Account, AccountFeatures> accounts = new HashMap<>();
            Map<Account, Loan> loanAccountMapping = userDataClient.getLoans();

            for (Account account : loanAccountMapping.keySet()) {
                Loan loan = loanAccountMapping.get(account);
                accounts.put(account, AccountFeatures.createForLoan(loan));
            }
            return new FetchLoanAccountsResponse(accounts);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public se.tink.backend.aggregation.agents.FetchTransactionsResponse fetchLoanTransactions() {
        return new se.tink.backend.aggregation.agents.FetchTransactionsResponse(
                Collections.emptyMap());
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return new FetchAccountsResponse(toTinkAccounts(getAccounts()));
    }

    @Override
    public se.tink.backend.aggregation.agents.FetchTransactionsResponse fetchSavingsTransactions() {
        try {
            Map<Account, List<Transaction>> transactionsMap = new HashMap<>();
            for (Account account : toTinkAccounts(getAccounts())) {
                List<Transaction> transactions = fetchTransactions(account);
                transactionsMap.put(account, transactions);
            }
            return new se.tink.backend.aggregation.agents.FetchTransactionsResponse(
                    transactionsMap);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(identityDataClient.fetchIdentityData());
    }

    /////////////////////////////////////
}
