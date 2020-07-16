package se.tink.libraries.account_data_cache;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;

public class AccountData {
    private final Account account;
    private AccountFeatures accountFeatures;
    private List<Transaction> transactions;
    private List<TransferDestinationPattern> transferDestinationPatterns;

    public AccountData(Account account) {
        this.account = account;
        this.accountFeatures = AccountFeatures.createEmpty();
        this.transactions = new ArrayList<>();
        this.transferDestinationPatterns = new ArrayList<>();
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

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public List<TransferDestinationPattern> getTransferDestinationPatterns() {
        return transferDestinationPatterns;
    }
}
