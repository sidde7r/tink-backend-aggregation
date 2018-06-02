package se.tink.backend.system.cli.abnamro;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.abnamro.utils.AbnAmroIcsCredentials;
import se.tink.backend.common.dao.AccountDao;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.StatisticMode;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.system.api.ProcessService;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.uuid.UUIDUtils;

/**
 * This command will search for duplicate ICS credit cards accounts. Idea is to change this command later so that it
 * will fix and merge credit cards. The command will go through all ICS accounts and check for duplicate transactions
 * between the accounts.
 * - Too many mutual/duplicate transactions (based on the `mutualTransactionThreshold` parameter) will indicate if
 * two accounts are duplicates.
 * - The duplicate accounts will be printed as well as their unique transactions and the transactions that are mutual/
 * shared between them.
 */
public class IcsDuplicateCreditCardCommand extends ServiceContextCommand<ServiceConfiguration> {

    public IcsDuplicateCreditCardCommand() {
        super("abnamro-duplicate-credit-cards",
                "Command that checks for duplicate credit cards accounts and transactions.");
    }

    private static final LogUtils log = new LogUtils(IcsDuplicateCreditCardCommand.class);

    private AccountDao accountDao;
    private AccountRepository accountRepository;
    private CredentialsRepository credentialsRepository;
    private TransactionDao transactionDao;
    private UserRepository userRepository;
    private ProcessService processService;

    private boolean shouldDeduplicate;

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        accountDao = serviceContext.getDao(AccountDao.class);
        accountRepository = serviceContext.getRepository(AccountRepository.class);
        credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        transactionDao = serviceContext.getDao(TransactionDao.class);
        userRepository = serviceContext.getRepository(UserRepository.class);
        processService = serviceContext.getSystemServiceFactory().getProcessService();

        shouldDeduplicate = Boolean.getBoolean("deduplicate");

        serviceContext.getRepository(UserRepository.class).streamAll()
                .compose(new CommandLineInterfaceUserTraverser(1))
                .forEach(user -> {
                    try {
                        credentialsRepository.findAllByUserId(user.getId()).forEach(c -> process(user, c));

                        if (shouldDeduplicate) {
                            List<String> userFlags = user.getFlags();
                            if (!userFlags.contains(FeatureFlags.ABN_AMRO_ICS_NEW_ACCOUNT_FORMAT)) {
                                userFlags.add(FeatureFlags.ABN_AMRO_ICS_NEW_ACCOUNT_FORMAT);
                                user.setFlags(userFlags);
                                userRepository.save(user);
                            }
                        }
                    } catch (Exception e) {
                        log.error(user.getId(), "Failed to process user.", e);
                    }
                });

    }

    private ImmutableListMultimap<String, Transaction> getTransactionsByAccountId(String userid) {
        return Multimaps.index(transactionDao.findAllByUserId(userid), Transaction::getAccountId);
    }

    private void process(User user, Credentials credentials) {
        if (!AbnAmroIcsCredentials.isAbnAmroIcsCredentials(credentials)) {
            return; // Ignore non ICS credentials
        }

        List<Account> accounts = accountDao.findByUserIdAndCredentialsId(user.getId(), credentials.getId())
                .stream().filter(a -> a.getType() == AccountTypes.CREDIT_CARD).collect(Collectors.toList());

        if (shouldDeduplicate) {
            deduplicate(user, accounts);
        } else {
            if (accounts.size() <= 1) {
                // Only one account, cannot be duplicates
                return;
            }

            findDuplicates(user, accounts);
        }
    }

    private void deduplicate(User user, List<Account> accounts) {
        List<String> newAccounts = accounts.stream()
                .map(Account::getBankId)
                .map(AbnAmroUtils::creditCardIdToAccountId)
                .sorted()
                .distinct()
                .collect(Collectors.toList());

        ImmutableListMultimap<String, Transaction> transactionsByAccountId = getTransactionsByAccountId(user.getId());
        newAccounts.forEach(na -> deduplicateAccount(na, user, accounts, transactionsByAccountId));
    }

    private void deduplicateAccount(String accountId, User user, List<Account> oldAccounts,
            ImmutableListMultimap<String, Transaction> transactionByAccountId) {
        List<Account> filteredAccounts = oldAccounts.stream()
                .filter(a -> Objects.equals(accountId, AbnAmroUtils.creditCardIdToAccountId(a.getBankId())))
                .collect(Collectors.toList());
        List<String> deletedAccountIds = Lists.newArrayList();
        ImmutableListMultimap<String, Transaction> transactions = transactionByAccountId;

        if (filteredAccounts.isEmpty()) {
            return;
        } else if (filteredAccounts.size() == 1) {
            Account account = filteredAccounts.get(0);
            String oldBankId = account.getBankId();
            account.setBankId(AbnAmroUtils.creditCardIdToAccountId(account.getBankId()));
            if (!Objects.equals(account.getBankId(), oldBankId)) {
                accountRepository.save(account);
            }
            return;
        }

        log.info(user.getId(), String.format("Deduplicating %s...", accountId));

        while (filteredAccounts.size() > 1) {
            Account leftAccount = filteredAccounts.get(0);
            filteredAccounts.remove(0);
            deletedAccountIds.add(leftAccount.getId());
            Account rightAccount = filteredAccounts.get(0);
            filteredAccounts.remove(0);
            deletedAccountIds.add(rightAccount.getId());

            AccountWithTransactions accountWithTransactions = mergeAccount(accountId, leftAccount, rightAccount,
                    transactions);
            transactions = ImmutableListMultimap.<String, Transaction>builder()
                    .putAll(transactionByAccountId)
                    .putAll(accountWithTransactions.getAccount().getId(), accountWithTransactions.getTransactions())
                    .build();
            filteredAccounts.add(accountWithTransactions.getAccount());
        }

        processService.generateStatisticsAndActivitiesWithoutNotifications(user.getId(), StatisticMode.FULL);

        Account newAccount = filteredAccounts.get(0);
        accountRepository.save(newAccount);
        transactionDao.saveAndIndex(user, transactions.get(newAccount.getId()), true);
        deletedAccountIds.forEach(deleteId -> transactionDao.delete(transactionByAccountId.get(deleteId)));
        accountDao.deleteByIds(deletedAccountIds);

        log.info(user.getId(), String.format("Deduplicated %s.", accountId));
    }

    protected AccountWithTransactions mergeAccount(String accountId, Account left, Account right,
            ImmutableListMultimap<String, Transaction> transactionByAccountId) {
        Account account;
        try {
            if (left.getCertainDate() == null || right.getCertainDate() == null) {
                account = left.getCertainDate() == null ? right.clone() : left.clone();
            } else if (left.getCertainDate().compareTo(right.getCertainDate()) >= 0) {
                account = left.clone();
            } else {
                account = right.clone();
            }
        } catch (CloneNotSupportedException e) {
            log.error("Could not clone account");
            return null;
        }

        account.setId(UUIDUtils.generateUUID());
        account.setBankId(accountId);

        List<Transaction> leftTransactions = Lists.newArrayList(transactionByAccountId.get(left.getId()));
        leftTransactions.sort(Comparator.comparing(Transaction::getOriginalDate));
        List<Transaction> rightTransactions = Lists.newArrayList(transactionByAccountId.get(right.getId()));
        rightTransactions.sort(Comparator.comparing(Transaction::getOriginalDate));

        List<Transaction> newTransactions = Lists.newArrayListWithExpectedSize(
                leftTransactions.size() + rightTransactions.size());

        while (leftTransactions.size() > 0 && rightTransactions.size() > 0) {
            Transaction leftTransaction = leftTransactions.get(0);
            Transaction rightTransaction = rightTransactions.get(0);

            if (leftTransaction.getOriginalDate().compareTo(rightTransaction.getOriginalDate()) == 0) {
                // Same day, resolve conflict
                leftTransactions.remove(0);
                List<Transaction> filtered = rightTransactions.stream().filter(
                        t -> t.getOriginalDate().compareTo(leftTransaction.getOriginalDate()) == 0
                ).collect(Collectors.toList());

                Transaction transaction = leftTransaction;
                for (Transaction duplicateCandidate : filtered) {
                    if (canDeduplicateTransaction(transaction, duplicateCandidate)) {
                        continue;
                    }

                    if (duplicateCandidate.isUserModified() && !transaction.isUserModified()) {
                        transaction = duplicateCandidate;
                    }

                    rightTransactions.remove(duplicateCandidate);
                    break;
                }
                newTransactions.add(transaction);
            } else if (leftTransaction.getOriginalDate().compareTo(rightTransaction.getOriginalDate()) > 0) {
                // Left side later
                newTransactions.add(rightTransaction);
                rightTransactions.remove(0);
            } else if (leftTransaction.getOriginalDate().compareTo(rightTransaction.getOriginalDate()) < 0) {
                // Right side later
                newTransactions.add(leftTransaction);
                leftTransactions.remove(0);
            }
        }

        if (leftTransactions.size() > 0) {
            newTransactions.addAll(leftTransactions);
        }
        if (rightTransactions.size() > 0) {
            newTransactions.addAll(rightTransactions);
        }

        // Set the account id to the new account for all kept transaction copies
        newTransactions.forEach(transaction -> transaction.setAccountId(account.getId()));

        return new AccountWithTransactions(newTransactions, account);
    }

    private boolean canDeduplicateTransaction(Transaction first, Transaction second) {
        return !Objects.equals(first.getOriginalDate(), second.getOriginalDate())
                && !Objects.equals(first.getOriginalAmount(), second.getOriginalAmount())
                && !Objects.equals(first.getOriginalDescription(), second.getOriginalDescription());
    }

    private void findDuplicates(User user, List<Account> accounts) {
        List<String> accountIds = accounts.stream()
                .map(Account::getBankId)
                .sorted()
                .collect(Collectors.toList());

        Account left = null;
        List<DuplicateAccount> duplicates = Lists.newArrayList();
        ImmutableListMultimap<String, Transaction> transactionsByAccountId = getTransactionsByAccountId(user.getId());
        Map<String, Account> accountByBankId = accounts.stream().collect(Collectors.toMap(Account::getBankId, a -> a));

        for (String rightId : accountIds) {
            Account right = accountByBankId.get(rightId);
            if (left == null) {
                left = right;
                continue;
            }
            if (Objects.equals(AbnAmroUtils.creditCardIdToAccountId(right.getBankId()),
                    AbnAmroUtils.creditCardIdToAccountId(left.getBankId()))) {
                DuplicateAccount duplicateAccount = new DuplicateAccount();
                duplicateAccount.commonName = AbnAmroUtils.creditCardIdToAccountId(left.getBankId());
                duplicateAccount.left = left;
                duplicateAccount.leftTransactions = transactionsByAccountId.get(left.getId());
                duplicateAccount.right = right;
                duplicateAccount.rightTransactions = transactionsByAccountId.get(right.getId());
                duplicateAccount.user = user;
                duplicates.add(duplicateAccount);

            }
            left = right;
        }

        if (duplicates.size() != 0) {
            String duplicateIdsString = String.join(",", duplicates.stream()
                    .map(DuplicateAccount::getCommonName).collect(Collectors.toList()));
            log.info(user.getId(), String.format("Duplicates found: %s [%s]", duplicates.size(), duplicateIdsString));
            duplicates.forEach(this::analyzeTransactions);
        }

    }

    private void analyzeTransactions(DuplicateAccount duplicateAccount) {
        List<Date> leftHistory = duplicateAccount.leftTransactions.stream().map(Transaction::getOriginalDate)
                .sorted().collect(Collectors.toList());
        List<Date> rightHistory = duplicateAccount.rightTransactions.stream().map(Transaction::getOriginalDate)
                .sorted().collect(Collectors.toList());

        log.info(duplicateAccount.user.getId(), String.format("Bank ID length: %d",
                duplicateAccount.left.getBankId().length()));

        log.info(duplicateAccount.user.getId(),
                String.format("Transactions total: [%s %d] [%s %d]",
                        duplicateAccount.left.getBankId(), duplicateAccount.leftTransactions.size(),
                        duplicateAccount.right.getBankId(), duplicateAccount.rightTransactions.size()));

        log.info(duplicateAccount.user.getId(), String.format("Account excluded: [%s %s] [%s %s]",
                duplicateAccount.left.getBankId(), duplicateAccount.left.isExcluded() ? "excluded" : "included",
                duplicateAccount.right.getBankId(), duplicateAccount.right.isExcluded() ? "excluded" : "included"));

        if (leftHistory.size() > 0 && rightHistory.size() > 0) {
            log.info(duplicateAccount.user.getId(),
                    String.format("Transactions creation (old + new): [%s %s %s] [%s %s %s]",
                            duplicateAccount.left.getBankId(), leftHistory.get(0).toString(),
                            leftHistory.get(leftHistory.size() - 1).toString(),
                            duplicateAccount.right.getBankId(), rightHistory.get(0).toString(),
                            rightHistory.get(rightHistory.size() - 1).toString()
                    ));
        } else {
            log.info(duplicateAccount.user.getId(), "Empty ICS account detected");
        }

        int leftModified = duplicateAccount.leftTransactions.stream().filter(Transaction::isUserModified)
                .collect(Collectors.toList()).size();
        int rightModified = duplicateAccount.rightTransactions.stream().filter(Transaction::isUserModified)
                .collect(Collectors.toList()).size();

        log.info(duplicateAccount.user.getId(), String.format("Modified transactions: [%s %d] [%s %d]",
                duplicateAccount.left.getBankId(), leftModified,
                duplicateAccount.right.getBankId(), rightModified));
    }

    protected class AccountWithTransactions {
        private List<Transaction> transactions;
        private Account account;

        public AccountWithTransactions(List<Transaction> transactions, Account account) {
            this.transactions = transactions;
            this.account = account;
        }

        public List<Transaction> getTransactions() {
            return transactions;
        }

        public Account getAccount() {
            return account;
        }
    }

    private class DuplicateAccount {
        String commonName;
        Account left;
        List<Transaction> leftTransactions;
        Account right;
        List<Transaction> rightTransactions;
        User user;

        String getCommonName() {
            return commonName;
        }
    }
}
