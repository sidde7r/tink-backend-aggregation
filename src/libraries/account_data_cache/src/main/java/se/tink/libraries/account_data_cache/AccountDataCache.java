package se.tink.libraries.account_data_cache;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;

public class AccountDataCache {
    private final Map<String, AccountData> accountDataByBankAccountId;
    private final List<Pair<Predicate<Account>, FilterReason>> accountFilters;
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountDataCache.class);

    public AccountDataCache() {
        this.accountDataByBankAccountId = new HashMap<>();
        this.accountFilters = new ArrayList<>();
    }

    public void clear() {
        this.accountDataByBankAccountId.clear();
        this.accountFilters.clear();
    }

    public void addFilter(Predicate<Account> predicate, FilterReason filterReason) {
        accountFilters.add(Pair.of(predicate, filterReason));
    }

    private Optional<AccountData> getAccountData(String bankAccountId) {
        return Optional.ofNullable(accountDataByBankAccountId.get(bankAccountId));
    }

    // We will be given a `TinkAccountId` from system when it has processed an account.
    // This id is needed for other account data to be properly set in order to send
    // it to system for processing (e.g. transactions).
    public void setProcessedTinkAccountId(String bankAccountId, String tinkAccountId) {
        getAccountData(bankAccountId)
                .ifPresent(accountData -> accountData.setProcessedTinkAccountId(tinkAccountId));
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

    // Return only AccountData that passes the added filter predicates.
    // A filter predicate can for example be responsible for removing non-whitelisted accounts.
    // It is therefore extremely important that general access of AccountData is done through this
    // filtered method.
    private Stream<AccountData> getFilteredAccountDataStream() {
        LOGGER.info(
                "getFilteredAccountDataStream: cache size {}", accountDataByBankAccountId.size());
        return accountDataByBankAccountId.values().stream()
                .filter(
                        accountData ->
                                accountFilters.stream()
                                        .allMatch(
                                                pair ->
                                                        pair.getLeft()
                                                                .test(accountData.getAccount())));
    }

    public List<AccountData> getFilteredAccountData() {
        return getFilteredAccountDataStream().collect(Collectors.toList());
    }

    public Optional<AccountData> getFilteredAccountDataByBankAccountId(String bankAccountId) {
        return getFilteredAccountDataStream()
                .filter(
                        filteredAccount ->
                                Objects.equals(
                                        filteredAccount.getAccount().getBankId(), bankAccountId))
                .findFirst();
    }

    public List<Pair<AccountData, List<FilterReason>>> getFilteredOutAccountDataWithFilterReason() {
        List<Pair<AccountData, List<FilterReason>>> filteredOutAccountData = new ArrayList<>();
        accountDataByBankAccountId
                .values()
                .forEach(
                        accountData -> {
                            List<FilterReason> filterReasons = new ArrayList<>();
                            accountFilters.forEach(
                                    accountFilterData -> {
                                        Predicate<Account> filter = accountFilterData.getLeft();
                                        FilterReason filterReason = accountFilterData.getRight();
                                        boolean shouldFilterOut =
                                                !filter.test(accountData.getAccount());
                                        if (shouldFilterOut) {
                                            filterReasons.add(filterReason);
                                        }
                                    });
                            if (!filterReasons.isEmpty()) {
                                filteredOutAccountData.add(Pair.of(accountData, filterReasons));
                            }
                        });
        LOGGER.info(
                "getFilteredOutAccountDataWithFilterReason - size {}",
                filteredOutAccountData.size());
        return filteredOutAccountData;
    }

    // Returns only AccountData that is both filtered and has been processed (see
    // getFilteredAccountDataStream).
    // Note: Only filtered AccountData can be processed!
    // A processed AccountData means that the attached Account has been processed by System and been
    // given a `TinkAccountId` (`Account.getId()`).
    // A proper `TinkAccountId` is needed in order to process the other bits of AccountData, e.g.
    // Transaction.
    private Stream<AccountData> getProcessedAccountDataStream() {
        return getFilteredAccountDataStream().filter(AccountData::isProcessed);
    }

    public Optional<AccountData> getProcessedAccountDataByBankAccountId(String bankAccountId) {
        return getProcessedAccountDataStream()
                .filter(
                        processedAccountData ->
                                Objects.equals(
                                        processedAccountData.getAccount().getBankId(),
                                        bankAccountId))
                .findFirst();
    }

    public List<AccountData> getAllAccountData() {
        return new ArrayList<>(accountDataByBankAccountId.values());
    }

    public List<Account> getFilteredAccounts() {
        return getFilteredAccountDataStream()
                .map(AccountData::getAccount)
                .collect(Collectors.toList());
    }

    public List<AccountData> getProcessedAccountData() {
        return getProcessedAccountDataStream().collect(Collectors.toList());
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

    public Map<Account, List<Transaction>> getTransactionsByAccountToBeProcessed() {
        return getProcessedAccountDataStream()
                .filter(AccountData::hasTransactions)
                .peek(AccountData::updateTransactionsAccountId)
                .collect(Collectors.toMap(AccountData::getAccount, AccountData::getTransactions));
    }

    public List<Transaction> getTransactionsToBeProcessed() {
        return getTransactionsByAccountToBeProcessed().values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
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
