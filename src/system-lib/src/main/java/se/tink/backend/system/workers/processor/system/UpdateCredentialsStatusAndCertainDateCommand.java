package se.tink.backend.system.workers.processor.system;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimaps;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.system.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorCommandResult;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;

public class UpdateCredentialsStatusAndCertainDateCommand implements TransactionProcessorCommand {
    private final AccountRepository accountRepository;
    private int batchSize = 0;
    private Catalog catalog;
    private int count = 0;
    private Credentials credentials;
    private Set<String> enabledAccountIds;
    private final SystemServiceFactory systemServiceFactory;
    private final TransactionProcessorContext context;

    public UpdateCredentialsStatusAndCertainDateCommand(TransactionProcessorContext context,
            SystemServiceFactory systemServiceFactory, AccountRepository accountRepository) {
        this.context = context;
        this.accountRepository = accountRepository;
        this.systemServiceFactory = systemServiceFactory;
    }

    @Override
    public TransactionProcessorCommandResult execute(Transaction transaction) {

        if (credentials != null && enabledAccountIds.contains(transaction.getAccountId())) {
            count++;

            if (count == 1 || count == batchSize || count % 100 == 0) {
                credentials.setStatusPayload(Catalog.format(catalog.getString("Analyzed {0} of {1} transactions..."),
                        count, batchSize));

                UpdateCredentialsStatusRequest updateCredentialsRequest = new UpdateCredentialsStatusRequest();
                updateCredentialsRequest.setCredentials(credentials);
                updateCredentialsRequest.setUserId(credentials.getUserId());
                updateCredentialsRequest.setUpdateContextTimestamp(true);

                systemServiceFactory.getUpdateService().updateCredentials(updateCredentialsRequest);
            }
        }

        return TransactionProcessorCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() {
        if (credentials == null) {
            return;
        }

        Iterable<Account> accounts = Iterables.filter(context.getUserData().getAccounts(),
                a -> (Objects.equal(a.getCredentialsId(), credentials.getId())));

        Iterable<Transaction> transactions = Iterables.filter(context.getUserData().getInStoreTransactions().values(),
                t -> (Objects.equal(t.getCredentialsId(), credentials.getId())));

        // Determine the certainDate as newest date of 30 days since last transaction date 
        // and 50 transactions since last transaction.

        ImmutableListMultimap<String, Transaction> transactionsByAccountId = Multimaps.index(transactions,
                Transaction::getAccountId);

        for (Account account : accounts) {
            ImmutableList<Transaction> transactionsForAccount = transactionsByAccountId.get(account.getId());

            if (Iterables.size(transactionsForAccount) > 0) {
                Date certainDate = CertainDateCalculator.calculateCertainDate(transactionsForAccount);
                account.setCertainDate(certainDate);
                accountRepository.setCertainDateById(account.getId(), certainDate);
            }
        }
    }

    @Override
    public TransactionProcessorCommandResult initialize() {
        catalog = Catalog.getCatalog(context.getUser().getProfile().getLocale());

        String credentialsId = context.getCredentialsId();

        if (Strings.isNullOrEmpty(credentialsId)) {
            return TransactionProcessorCommandResult.CONTINUE;
        }

        credentials = context.getUserData().getCredentials().stream()
                .filter(c -> Objects.equal(c.getId(), credentialsId)).findFirst().get();

        enabledAccountIds = context.getUserData().getAccounts().stream()
                .filter(a -> Objects.equal(a.getCredentialsId(), credentialsId) && !a
                        .isExcluded()).map(Account::getId).collect(Collectors.toSet());

        // Only include transactions that belong to enabled accounts, in the official batch size.
        batchSize = (int) context.getInBatchTransactions().stream()
                .filter(t -> enabledAccountIds.contains(t.getAccountId())).count();

        return TransactionProcessorCommandResult.CONTINUE;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).toString();
    }
}
