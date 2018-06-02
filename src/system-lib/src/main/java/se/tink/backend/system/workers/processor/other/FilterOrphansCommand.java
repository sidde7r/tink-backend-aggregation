package se.tink.backend.system.workers.processor.other;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorCommandResult;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.system.workers.processor.TransactionProcessorUserData;
import se.tink.backend.utils.LogUtils;

/**
 * Filter out transactions that, while being fetched from a provider, had their accounts, credentials or user deleted
 * while we were fetching. It could also be the case where we replay a transaction queue from a partner containing old
 * transactions belonging to deleted accounts/credentials.
 * <p>
 * For this to be fully usable, it is crucial that this code is not executed while credentials and/or accounts are being
 * deleted.
 */
public class FilterOrphansCommand implements TransactionProcessorCommand {

    private static final LogUtils log = new LogUtils(FilterOrphansCommand.class);
    private TransactionProcessorContext context;
    private String userId;
    private final TransactionProcessorUserData userData;

    public FilterOrphansCommand(TransactionProcessorContext context, String userId) {
        this.context = context;
        this.userId = userId;
        this.userData = context.getUserData();
    }

    @Override
    public TransactionProcessorCommandResult initialize() {
        if (userData == null) {
            log.info(userId, "UserData == null. Skipping command.");
            return TransactionProcessorCommandResult.CONTINUE;
        }

        final List<Account> accounts = userData.getAccounts();
        final List<Credentials> credentials = userData.getCredentials();

        if (accounts == null) {
            log.info(userId, "Accounts == null. Skipping command.");
            return TransactionProcessorCommandResult.CONTINUE;
        } else if (credentials == null) {
            log.info(userId, "Credentials == null. Skipping command.");
            return TransactionProcessorCommandResult.CONTINUE;
        }

        Set<String> accountIds = accounts.stream().map(Account::getId).collect(Collectors.toSet());
        Set<String> credentialsIds = credentials.stream().map(Credentials::getId).collect(Collectors.toSet());

        List<Transaction> filteredTransactions = Lists
                .newArrayListWithExpectedSize(context.getInBatchTransactions().size());
        
        for (Transaction transaction : context.getInBatchTransactions()) {
            String credentialsId = transaction.getCredentialsId();
            String accountId = transaction.getAccountId();

            if (!credentialsIds.contains(credentialsId)) {
                log.info(userId, credentialsId, "Ignoring transaction. No credentials found with id: " + credentialsId);
                continue;
            }

            if (!accountIds.contains(accountId)) {
                log.info(userId, credentialsId, "Ignoring transaction. No account found with id: " + accountId);
                continue;
            }

            filteredTransactions.add(transaction);
        }

        context.updateInBatchTransactions(filteredTransactions);

        return TransactionProcessorCommandResult.CONTINUE;
    }

    @Override
    public TransactionProcessorCommandResult execute(Transaction transaction) {
        return TransactionProcessorCommandResult.CONTINUE;
    }

    /**
     * Called for every command in command chain's reverse order at after processing all transactions.
     */
    @Override
    public void postProcess() {
        // Deliberately left empty.
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).toString();
    }
}
