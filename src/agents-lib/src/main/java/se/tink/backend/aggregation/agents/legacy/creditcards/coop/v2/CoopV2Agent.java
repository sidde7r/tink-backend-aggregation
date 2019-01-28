package se.tink.backend.aggregation.agents.creditcards.coop.v2;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.creditcards.coop.v2.model.AccountEntity;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.strings.StringUtils;

public class CoopV2Agent extends AbstractAgent implements RefreshCreditCardAccountsExecutor {
    private static ImmutableSortedSet<Integer> TRANSACTION_PAGE_SIZES = ImmutableSortedSet
            .<Integer>naturalOrder()
            .add(200)
            .add(1000)
            .add(10000).build();

    private final Credentials credentials;
    private CoopApiClient apiClient;
    private List<AccountEntity> accounts = null;

    public CoopV2Agent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);
        this.credentials = request.getCredentials();
        this.apiClient = new CoopApiClient(
                clientFactory.createBasicClient(context.getLogOutputStream()),
                request.getCredentials(),
                context.getMetricRegistry());
    }

    /**
     * The Coop api is a bit cumbersome, since we cannot select a start date and end date of transaction paging. It's
     * only possible to give the size. Therefore we start at a small pagesize and increase if needed. Usually people
     * don't have many transactions each day. So the start size of 50 should almost always be enough.
     *
     * This method could be nice to have inside the ApiClient, but the helper methods for content date is available
     * only from the AbstractAgent currently.
     */
    private List<Transaction> getTransactions(Account account, AccountEntity accountEntity) {
        int accountType = accountEntity.getAccountType();

        // If no date, it's probably the first refresh. Fetch the largest size directly.
        if (getContentWithRefreshDate(account) == null) {
            return apiClient.getTransactions(accountType, Iterables.getLast(TRANSACTION_PAGE_SIZES));
        }

        // Try a small page size first, then increase if needed with larger page size.
        List<Transaction> transactions = null;
        for (Integer pageSize : TRANSACTION_PAGE_SIZES) {
            transactions = apiClient.getTransactions(accountType, pageSize);

            // If the page size was large enough, just return
            if (isContentWithRefresh(account, transactions)) {
                return transactions;
            }
        }

        // If we were not content with the refresh, return the last result
        return transactions;
    }

    private List<AccountEntity> getAccounts() {
        if (accounts != null) {
            return accounts;
        }

        accounts = apiClient.getAccounts();
        return accounts;
    }

    /**
     * Preferrably we'd use the account number instead, but that'll require merging of data since V1 version of Coop
     * used same kind of logic for hashing the BankId.
     */
    private String hashLegacyBankId(AccountType accountType) {
        return StringUtils.hashAsStringMD5(credentials.getId() + accountType.getLegacyBankIdPart());
    }

    private Account parseAccount(AccountEntity accountEntity) {
        Account account = accountEntity.toAccount();
        // TODO: Start using accountNumber as bankId (stop overriding it)
        account.setBankId(hashLegacyBankId(accountEntity.getAccountTypeEnum()));

        if (Strings.isNullOrEmpty(account.getAccountNumber())) {
            // Before we start using accountNumber as bankId, we need to ensure that it's always present
            log.warn("Found account without account number");
        }

        return account;
    }

    @Override
    public boolean login() throws Exception {
        return apiClient.loginWithPassword();
    }

    @Override
    public void logout() throws Exception {

    }

    /**
     * The transactions in the app API are not bound to any specific account, but rather
     * to an account type. Though, users can have multiple accounts that map to the same
     * account type: Type 7 and 9 are both named MedMera Efter, of which we think one of them are
     * previous Visa card and the other is a regular MedMera. Since V2 we now start to separate these to two accounts
     * instead of putting them together.
     *
     * The user probably cannot have two accounts on the same type since it's not possible to separate them in the API.
     */
    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return new FetchAccountsResponse(getAccounts().stream()
                .map(x -> parseAccount(x))
                .collect(Collectors.toList()));
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        Map<Account, List<Transaction>> result = new HashMap<>();

        for(AccountEntity entity : getAccounts()) {
            Account account = entity.toAccount();
            List<Transaction> transactions = getTransactions(account, entity);
            result.put(account, transactions);
        }
        return new FetchTransactionsResponse(result);
    }
}
