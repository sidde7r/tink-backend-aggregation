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
    private final Map<String, AccountData> accountDataByBankAccountId;
    private final List<Predicate<Account>> accountFilters;

    public AccountDataCache() {
        this.accountDataByBankAccountId = new HashMap<>();
        this.accountFilters = new ArrayList<>();
    }

    public void clear() {
        this.accountDataByBankAccountId.clear();
        this.accountFilters.clear();
    }

    public void addFilter(Predicate<Account> predicate) {
        accountFilters.add(predicate);
    }

    private Optional<AccountData> getAccountData(String bankAccountId) {
        return Optional.ofNullable(accountDataByBankAccountId.get(bankAccountId));
    }

    // We will be given a tinkAccountId from system when it has processed an account.
    // This id is needed for other account data to be properly set in order to send
    // it to system for processing (e.g. transactions).
    public void processAccount(String bankAccountId, String tinkAccountId) {
        getAccountData(bankAccountId)
                .ifPresent(accountData -> accountData.processAccount(tinkAccountId));
    }

    public void cacheAccount(Account account) {
        String bankAccountId = account.getBankId();
        if (accountDataByBankAccountId.containsKey(bankAccountId)) {
            return;
        }

        accountDataByBankAccountId.put(bankAccountId, new AccountData(account));
    }

    public void cacheAccountFeatures(String bankAccountId, AccountFeatures accountFeatures) {
        getAccountData(bankAccountId)
                .ifPresent(cacheItem -> cacheItem.updateAccountFeatures(accountFeatures));
    }

    public void cacheTransactions(String bankAccountId, List<Transaction> transactions) {
        // This crashes if agent is implemented incorrectly. You have to cache Account before you
        // cache Transactions.
        Preconditions.checkArgument(accountDataByBankAccountId.containsKey(bankAccountId));
        getAccountData(bankAccountId)
                .ifPresent(cacheItem -> cacheItem.updateTransactions(transactions));
    }

    public void cacheTransferDestinationPatterns(
            String bankAccountId, List<TransferDestinationPattern> patterns) {
        getAccountData(bankAccountId)
                .ifPresent(cacheItem -> cacheItem.updateTransferDestinationPatterns(patterns));
    }

    private Stream<AccountData> getFilteredAccountDataStream() {
        return accountDataByBankAccountId.values().stream()
                .filter(
                        accountData ->
                                accountFilters.stream()
                                        .allMatch(filter -> filter.test(accountData.getAccount())));
    }

    public List<AccountData> getFilteredAccountData() {
        return getFilteredAccountDataStream().collect(Collectors.toList());
    }

    private Stream<AccountData> getProcessedAccountDataStream() {
        return getFilteredAccountDataStream().filter(AccountData::isProcessed);
    }

    public List<AccountData> getAllAccountData() {
        return new ArrayList<>(accountDataByBankAccountId.values());
    }

    public List<Account> getFilteredAccounts() {
        return getFilteredAccountDataStream()
                .map(AccountData::getAccount)
                .collect(Collectors.toList());
    }

    public List<Account> getProcessedAccounts() {
        return getProcessedAccountDataStream()
                .map(AccountData::getAccount)
                .collect(Collectors.toList());
    }

    public List<Account> getAllAccounts() {
        return accountDataByBankAccountId.values().stream()
                .map(AccountData::getAccount)
                .collect(Collectors.toList());
    }

    public Map<Account, List<Transaction>> getTransactionsToBeProcessed() {
        return getProcessedAccountDataStream()
                .filter(AccountData::hasTransactions)
                .peek(AccountData::updateTransactionsAccountId)
                .collect(Collectors.toMap(AccountData::getAccount, AccountData::getTransactions));
    }

    public Map<Account, List<TransferDestinationPattern>>
            getTransferDestinationPatternsToBeProcessed() {
        return getProcessedAccountDataStream()
                .filter(AccountData::hasTransferDestinationPatterns)
                .collect(
                        Collectors.toMap(
                                AccountData::getAccount,
                                AccountData::getTransferDestinationPatterns));
    }
}
