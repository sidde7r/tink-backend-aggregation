package se.tink.backend.system.workers.processor.storage;

import com.google.common.base.MoreObjects;
import org.joda.time.DateTime;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.core.Transaction;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorCommandResult;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.system.workers.processor.TransactionProcessorUserData;

public class LoadUserDataCommand implements TransactionProcessorCommand {
    private final AccountRepository accountRepository;
    private final CredentialsRepository credentialsRepository;
    private final LoanDataRepository loanDataRepository;
    private final TransactionDao transactionDao;

    private final TransactionProcessorContext context;

    public LoadUserDataCommand(
            TransactionProcessorContext context,
            CredentialsRepository credentialsRepository, LoanDataRepository loanDataRepository,
            TransactionDao transactionDao, AccountRepository accountRepository
    ) {
        this.context = context;
        this.accountRepository = accountRepository;
        this.credentialsRepository = credentialsRepository;
        this.loanDataRepository = loanDataRepository;
        this.transactionDao = transactionDao;
    }

    @Override
    public TransactionProcessorCommandResult execute(Transaction transaction) {
        return TransactionProcessorCommandResult.CONTINUE;
    }

    @Override
    public TransactionProcessorCommandResult initialize() {
        if (context.getUserData() == null) {
            String userId = context.getUser().getId();

            TransactionProcessorUserData transactionProcessorUserData = new TransactionProcessorUserData();

            transactionProcessorUserData.setCredentials(credentialsRepository.findAllByUserId(userId));
            transactionProcessorUserData.setAccounts(accountRepository.findByUserId(userId));
            transactionProcessorUserData.setInStoreTransactions(transactionDao
                    .findAllByUserIdAndTime(userId, DateTime.now().minusYears(1), DateTime.now().plusYears(1)));
            transactionProcessorUserData.setLoanDataByAccount(loanDataRepository.findAllByAccounts(transactionProcessorUserData.getAccounts()));

            context.setUserData(transactionProcessorUserData);
        }

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
        return MoreObjects.toStringHelper(this)
                .toString();
    }
}
