package se.tink.backend.system.cli.debug;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.guavaimpl.Predicates;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.uuid.UUIDUtils;

public class TransactionProcessorRollbackCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final LogUtils LOG = new LogUtils(TransactionProcessorRollbackCommand.class);
    private static final ThreadSafeDateFormat DATE_FORMAT = ThreadSafeDateFormat.FORMATTER_DAILY;

    private static final int MIN = 0;
    private static final int MAX = 90;

    private CredentialsRepository credentialsRepository;
    private AccountRepository accountRepository;
    private TransactionDao transactionDao;

    private int numberOfDays;

    public TransactionProcessorRollbackCommand() {
        super("transaction-processor-rollback", "Alter an accounts certainDate and delete all the transactions after "
                + "the new certainDate in order to fetch transactions from the bank as 'new transactions'");
    }

    private void initialize(ServiceContext serviceContext) {
        credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        accountRepository = serviceContext.getRepository(AccountRepository.class);
        transactionDao = serviceContext.getDao(TransactionDao.class);
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        initialize(serviceContext);

        String credentialsId = extractCredentialsId();
        numberOfDays = extractNumberOfDays();

        execute(credentialsId);
    }

    private void execute(String credentialsId) {
        Credentials credentials = getCredentialsFor(credentialsId);

        ImmutableMultimap<Account, Transaction> transactionsByAccount = getTransactionsFor(credentials.getUserId(),
                getAccountsFor(credentials.getId()));

        for (Account account : transactionsByAccount.keySet()) {
            rollback(account, transactionsByAccount.get(account).asList());
        }
    }

    private void rollback(Account account, List<Transaction> transactions) {
        alterCertainDateFor(account);

        transactions = transactions.stream()
                .filter(Predicates.filterOutTransactionsWithOriginalDateBefore(account.getCertainDate())::apply)
                .collect(Collectors.toList());

        transactionDao.delete(transactions);
        accountRepository.save(account);

        LOG.info(String.format("[accountId: %s] Deleted %s transactions",
                account.getId(), transactions.size()));
    }

    private void alterCertainDateFor(Account account) {
        Date certainDate = account.getCertainDate();
        account.setCertainDate(DateUtils.addDays(certainDate, -numberOfDays));

        accountRepository.save(account);

        LOG.info(String.format("[accountId: %s] Altered certainDate from: %s, to: %s",
                account.getId(), DATE_FORMAT.format(certainDate), DATE_FORMAT.format(account.getCertainDate())));
    }

    private Credentials getCredentialsFor(String credentialsId) {
        Credentials credentials = credentialsRepository.findOne(credentialsId);
        Preconditions.checkState(credentials != null,
                String.format("Couldn't find credential with id: %s", credentialsId));

        return credentials;
    }

    private ImmutableMultimap<Account, Transaction> getTransactionsFor(String userId, final List<Account> accounts) {
        List<Transaction> allTransactions = transactionDao.findAllByUserId(userId);
        Preconditions.checkState(allTransactions != null && !allTransactions.isEmpty(),
                String.format("Couldn't find transactions for userId: %s", userId));

        final Map<String, Account> accountsById = Maps.uniqueIndex(accounts, Account::getId);

        return FluentIterable.from(allTransactions)
                .filter(transaction -> accountsById.containsKey(transaction.getAccountId())).index(transaction -> accountsById.get(transaction.getAccountId()));
    }

    private List<Account> getAccountsFor(String credentialsId) {
        final List<Account> accounts = accountRepository.findByCredentialsId(credentialsId);
        Preconditions.checkState(accounts != null && !accounts.isEmpty(),
                String.format("Couldn't find accounts for credentials with id: %s", credentialsId));

        return accounts;
    }

    private String extractCredentialsId() {
        String credentialsId = System.getProperty("credentialsId");

        if (!Strings.isNullOrEmpty(credentialsId)) {
            if (UUIDUtils.isValidTinkUUID(credentialsId)) {
                return credentialsId;
            }
            if (UUIDUtils.isValidUUIDv4(credentialsId)) {
                return UUIDUtils.toTinkUUID(UUID.fromString(credentialsId));
            }
        }

        LOG.error(String.format("Invalid credentialsId: %s", credentialsId));
        return null;
    }

    private int extractNumberOfDays() {
        int numberOfDays = getIntegerProperty("numberOfDays");

        if (numberOfDays > MAX) {
            return MAX;
        } else if (numberOfDays > MIN) {
            return numberOfDays;
        }

        return MIN;
    }

    private int getIntegerProperty(String key) {
        String inputValue = System.getProperty(key);
        inputValue = inputValue != null ? inputValue.replaceAll("[^\\d]", "") : null;

        Preconditions.checkState(!Strings.isNullOrEmpty(inputValue),
                String.format("Missing required argument \"%s\" (int [0-90])", key));

        return Integer.parseInt(inputValue);
    }
}
