package se.tink.backend.aggregation.workers.operation;

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

    private Stream<AccountData> getFilteredAccountData() {
        return accountDataByAccountUniqueId.values().stream()
                .filter(
                        accountData ->
                                accountFilters.stream()
                                        .allMatch(filter -> filter.test(accountData.getAccount())));
    }

    public List<AccountData> getCurrentAccountData() {
        return getFilteredAccountData().collect(Collectors.toList());
    }

    public List<Account> getCurrentAccounts() {
        return getFilteredAccountData().map(AccountData::getAccount).collect(Collectors.toList());
    }
}
