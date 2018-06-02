package se.tink.backend.common.dao;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.cassandra.AccountBalanceHistoryRepository;
import se.tink.backend.common.repository.cassandra.DAO.LoanDAO;
import se.tink.backend.common.repository.cassandra.TransferDestinationPatternRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.FollowItemRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.follow.FollowItem;
import se.tink.backend.core.follow.FollowTypes;
import se.tink.backend.core.follow.SavingsFollowCriteria;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AccountDao {
    private final AccountBalanceHistoryRepository accountBalanceHistoryRepository;
    private final AccountRepository accountRepository;
    private final FollowItemRepository followRepository;
    private final TransferDestinationPatternRepository transferDestinationPatternRepository;

    private final InvestmentDao investmentDao;
    private final LoanDAO loanDAO;
    private final TransactionDao transactionDao;

    @Inject
    public AccountDao(
            AccountBalanceHistoryRepository accountBalanceHistoryRepository,
            AccountRepository accountRepository,
            FollowItemRepository followRepository,
            TransferDestinationPatternRepository transferDestinationPatternRepository,
            InvestmentDao investmentDao, LoanDAO loanDAO,
            TransactionDao transactionDao) {
        this.accountBalanceHistoryRepository = accountBalanceHistoryRepository;
        this.accountRepository = accountRepository;
        this.followRepository = followRepository;
        this.transferDestinationPatternRepository = transferDestinationPatternRepository;
        this.investmentDao = investmentDao;
        this.loanDAO = loanDAO;
        this.transactionDao = transactionDao;
    }

    public void delete(Account account) {
        deleteByUserIdAndAccountIds(account.getUserId(), Collections.singletonList(account.getId()));
    }

    public void deleteByIds(List<String> accountIds) {
        accountRepository.findAll(accountIds).stream()
                .collect(Collectors
                        .groupingBy(Account::getUserId, Collectors.mapping(Account::getId, Collectors.toList())))
                .forEach(this::deleteByUserIdAndAccountIds);
    }

    public void deleteByUserIdAndCredentialId(String userId, String credentialId) {
        List<Account> accounts = accountRepository.findByUserIdAndCredentialsId(userId, credentialId);
        deleteByUserIdAndAccountIds(userId, accountsToIds(accounts));
    }

    public void deleteByUserId(String userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);
        deleteByUserIdAndAccountIds(userId, accountsToIds(accounts));
    }

    public void deleteByUserIdAndAccountIds(String userId, List<String> accountIds) {
        if (accountIds == null || accountIds.isEmpty()) {
            return;
        }
        accountIds.forEach(accountId -> cleanLinkedData(userId, accountId));
        updateFollowItems(userId, Sets.newHashSet(accountIds));
        accountRepository.deleteByIds(accountIds);
    }

    private List<String> accountsToIds(List<Account> accounts) {
        return accounts.stream().map(Account::getId).collect(Collectors.toList());
    }

    private void cleanLinkedData(String userId, String accountId) {
        transactionDao.deleteByUserIdAndAccountId(userId, accountId);
        transferDestinationPatternRepository.deleteByUserIdAndAccountId(userId, accountId);
        accountBalanceHistoryRepository.deleteByUserIdAndAccountId(userId, accountId);
        loanDAO.deleteByAccountId(accountId);
        investmentDao.deleteByUserIdAndAccountId(userId, accountId);
    }

    private void updateFollowItems(String userId, Set<String> accountIdsToDelete) {
        List<FollowItem> followItems = followRepository.findByUserId(userId);

        for (FollowItem followItem : followItems) {
            if (followItem.getType() != FollowTypes.SAVINGS) {
                continue;
            }

            SavingsFollowCriteria criteria = SerializationUtils.deserializeFromString(
                    followItem.getCriteria(), SavingsFollowCriteria.class);

            List<String> remainingAccountIds = criteria.getAccountIds().stream()
                    .filter(s -> (!accountIdsToDelete.contains(s)))
                    .collect(Collectors.toList());

            if (Iterables.isEmpty(remainingAccountIds)) {
                followRepository.delete(followItem.getId());
            } else if (criteria.getAccountIds().size() > remainingAccountIds.size()) {
                criteria.setAccountIds(Lists.newArrayList(remainingAccountIds));
                followItem.setCriteria(SerializationUtils.serializeToString(criteria));
                followRepository.save(followItem);
            }
        }
    }

    public List<Account> findByUserIdAndCredentialsId(String userId, String credentialId) {
        return accountRepository.findByUserIdAndCredentialsId(userId, credentialId);
    }

    public Account save(Account account) {
        return accountRepository.save(account);
    }

    public List<Account> save(Iterable<Account> accounts) {
        return accountRepository.save(accounts);
    }

    public List<Account> findByUserId(String userId) {
        return accountRepository.findByUserId(userId);
    }
}
