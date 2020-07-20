package se.tink.libraries.account_data_cache;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;

public class AccountDataCache {
    private final Map<String, AccountData> accountDataByAccountUniqueId;
    private final List<Predicate<Account>> accountFilters;

    public AccountDataCache() {
        this.accountDataByAccountUniqueId = new HashMap<>();
        this.accountFilters = new ArrayList<>();
    }

    public void clear() {
        this.accountDataByAccountUniqueId.clear();
        this.accountFilters.clear();
    }

    public void addFilter(Predicate<Account> predicate) {
        accountFilters.add(predicate);
    }

    private Optional<AccountData> getAccountData(String accountUniqueId) {
        return Optional.ofNullable(accountDataByAccountUniqueId.get(accountUniqueId));
    }

    // We will be given a tinkAccountId from system when it has processed an account.
    // This id is needed for other account data to be properly set in order to send
    // it to system for processing (e.g. transactions).
    public void processAccount(String accountUniqueId, String tinkAccountId) {
        getAccountData(accountUniqueId)
                .ifPresent(accountData -> accountData.processAccount(tinkAccountId));
    }

    public void cacheAccount(Account account) {
        String accountUniqueId = account.getBankId();
        if (accountDataByAccountUniqueId.containsKey(accountUniqueId)) {
            return;
        }

        accountDataByAccountUniqueId.put(accountUniqueId, new AccountData(account));
    }

    public void cacheAccountFeatures(String accountUniqueId, AccountFeatures accountFeatures) {
        getAccountData(accountUniqueId)
                .ifPresent(cacheItem -> cacheItem.updateAccountFeatures(accountFeatures));
    }

    public void cacheTransactions(String accountUniqueId, List<Transaction> transactions) {
        // This crashes if agent is implemented incorrectly. You have to cache Account before you
        // cache Transactions.
        Preconditions.checkArgument(accountDataByAccountUniqueId.containsKey(accountUniqueId));
        getAccountData(accountUniqueId)
                .ifPresent(cacheItem -> cacheItem.updateTransactions(transactions));
    }

    public void cacheTransferDestinationPatterns(
            String accountUniqueId, List<TransferDestinationPattern> patterns) {
        getAccountData(accountUniqueId)
                .ifPresent(cacheItem -> cacheItem.updateTransferDestinationPatterns(patterns));
    }

    private Stream<AccountData> getFilteredAccountDataStream() {
        return accountDataByAccountUniqueId.values().stream()
                .filter(
                        accountData ->
                                accountFilters.stream()
                                        .allMatch(filter -> filter.test(accountData.getAccount())));
    }

    public List<AccountData> getFilteredAccountData() {
        return getFilteredAccountDataStream().collect(Collectors.toList());
    }

    public List<AccountData> getAllAccountData() {
        return new ArrayList<>(accountDataByAccountUniqueId.values());
    }

    public List<Account> getFilteredAccounts() {
        return getFilteredAccountDataStream()
                .map(AccountData::getAccount)
                .collect(Collectors.toList());
    }

    public List<Account> getAllAccounts() {
        return accountDataByAccountUniqueId.values().stream()
                .map(AccountData::getAccount)
                .collect(Collectors.toList());
    }

    public Map<Account, List<Transaction>> getFilteredTransactions() {
        return getFilteredAccountDataStream()
                .filter(accountData -> !accountData.getTransactions().isEmpty())
                .collect(Collectors.toMap(AccountData::getAccount, AccountData::getTransactions));
    }

    public Map<Account, List<TransferDestinationPattern>> getFilteredTransferDestinationPatterns() {
        return getFilteredAccountDataStream()
                .filter(accountData -> !accountData.getTransferDestinationPatterns().isEmpty())
                .collect(
                        Collectors.toMap(
                                AccountData::getAccount,
                                AccountData::getTransferDestinationPatterns));
    }
}
