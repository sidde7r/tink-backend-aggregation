package se.tink.libraries.account_data_cache;

import com.google.common.base.Preconditions;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;

public class AccountData {
    private final Account account;
    private boolean isProcessed;
    private AccountFeatures accountFeatures;
    private List<Transaction> transactions;
    private LocalDate transactionDateLimit;
    private final List<TransferDestinationPattern> transferDestinationPatterns = new LinkedList<>();

    public AccountData(Account account) {
        this.account = account;
        this.isProcessed = false;
        this.accountFeatures = AccountFeatures.createEmpty();
        this.transactions = new ArrayList<>();
    }

    public void setProcessedTinkAccountId(String tinkAccountId) {
        this.account.setId(tinkAccountId);
        this.isProcessed = true;
    }

    public boolean isProcessed() {
        return isProcessed;
    }

    public void updateAccountFeatures(AccountFeatures accountFeatures) {
        if (accountFeatures.isEmpty()) {
            // Note: This method can be called multiple times with empty accountFeatures after an
            // agent has already populated this with good data.
            return;
        }
        this.accountFeatures = accountFeatures;
    }

    public void updateTransactions(List<Transaction> transactions) {
        // Note: This is how it worked previously!
        // Instead of appending to the list of transactions we overwrite any
        // previous transactions with the new ones.
        this.transactions = transactions;
    }

    public void updateTransferDestinationPatterns(List<TransferDestinationPattern> patterns) {
        this.transferDestinationPatterns.addAll(patterns);
    }

    public Account getAccount() {
        return account;
    }

    public AccountFeatures getAccountFeatures() {
        return accountFeatures;
    }

    public boolean hasTransactions() {
        return !getTransactions().isEmpty();
    }

    public void updateTransactionsAccountId() {
        Preconditions.checkState(
                this.isProcessed,
                "The AccountData must be processed before we can update the transactions' accountId.");
        String tinkAccountId = account.getId();
        transactions.forEach(transaction -> transaction.setAccountId(tinkAccountId));
    }

    public List<Transaction> getTransactions() {
        Stream<Transaction> stream = transactions.stream();

        if (transactionDateLimit != null) {
            stream = stream.filter(t -> !getTransactionLocalDate(t).isBefore(transactionDateLimit));
        }

        return stream.collect(Collectors.toList());
    }

    public boolean hasTransferDestinationPatterns() {
        return !transferDestinationPatterns.isEmpty();
    }

    public List<TransferDestinationPattern> getTransferDestinationPatterns() {
        return transferDestinationPatterns;
    }

    public void setTransactionDateLimit(LocalDate transactionDateLimit) {
        this.transactionDateLimit = transactionDateLimit;
    }

    public LocalDate getTransactionLocalDate(Transaction t) {
        return ZonedDateTime.ofInstant(t.getDate().toInstant(), ZoneId.of("UTC")).toLocalDate();
    }
}
