package se.tink.backend.connector.util.helper;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.cassandra.TransactionExternalIdRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.MarketRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Market;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionExternalId;
import se.tink.backend.core.User;
import se.tink.backend.core.UserState;
import se.tink.libraries.uuid.UUIDUtils;

public class RepositoryHelper {

    private static final int BATCH_SIZE = 20;
    private final UserRepository userRepository;
    private AccountRepository accountRepository;
    private CredentialsRepository credentialsRepository;
    private MarketRepository marketRepository;
    private UserStateRepository userStateRepository;
    private TransactionExternalIdRepository transactionExternalIdRepository;
    private TransactionDao transactionDao;

    @Inject
    public RepositoryHelper(UserRepository userRepository, AccountRepository accountRepository,
            CredentialsRepository credentialsRepository, MarketRepository marketRepository,
            UserStateRepository userStateRepository,
            TransactionExternalIdRepository transactionExternalIdRepository,
            TransactionDao transactionDao) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.credentialsRepository = credentialsRepository;
        this.marketRepository = marketRepository;
        this.userStateRepository = userStateRepository;
        this.transactionExternalIdRepository = transactionExternalIdRepository;
        this.transactionDao = transactionDao;
    }

    public Optional<User> getUser(String externalUserId) {
        return Optional.ofNullable(userRepository.findOneByUsername(externalUserId));
    }

    public List<Account> getAccounts(User user, Credentials credentials) {
        List<Account> accounts = accountRepository.findByUserIdAndCredentialsId(user.getId(), credentials.getId());
        return accounts == null ? Lists.newArrayList() : accounts;
    }

    /**
     * Fetch an account belonging to a user using its primary ID
     */
    public Account getAccount(User user, String accountId) {
        Account account = accountRepository.findOne(accountId);
        if (account == null) {
            return null;
        }
        if (Objects.equals(user.getId(), account.getUserId())) {
            return account;
        }
        return null;
    }

    public List<Credentials> getCredentials(User user, String providerName) {
        List<Credentials> credentials = credentialsRepository
                .findAllByUserIdAndProviderName(user.getId(), providerName);
        return credentials == null ? Lists.newArrayList() : credentials;
    }

    public List<Market> getAllMarkets() {
        return marketRepository.findAll();
    }

    public void createUser(User user) {
        saveUser(user);
        userStateRepository.save(new UserState(user.getId()));
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public void saveCredentials(Credentials credentials) {
        credentialsRepository.save(credentials);
    }

    public List<TransactionExternalId> getListOfExternalTransactionIds(String accountId, String userId,
            List<String> transactionIds) {
        return transactionExternalIdRepository.findAllByAccountIdUserIdAndExternalTransactionIds(accountId,
                userId, transactionIds);
    }

    public Optional<String> getTinkTransactionIdFromExternalId(String accountId, String userId, String externalId) {
        TransactionExternalId externalTransaction = transactionExternalIdRepository
                .findByAccountIdUserIdAndExternalTransactionId(accountId, userId, externalId);
        if (externalTransaction == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(UUIDUtils.toTinkUUID(externalTransaction.getTransactionId()));
    }

    public void saveTransactionExternalIds(List<TransactionExternalId> transactionExternalIds) {
        transactionExternalIds.forEach(TransactionExternalId::setUpdatedToNow);

        Iterable<List<TransactionExternalId>> partition = Iterables.partition(transactionExternalIds, BATCH_SIZE);
        for (List<TransactionExternalId> externalIds : partition) {
            transactionExternalIdRepository.save(externalIds);
        }
    }

    public Optional<Transaction> getTransactionFromId(String accountId, String userId, String identifier,
            boolean usesExternalId) {
        if (usesExternalId) {
            identifier = getTinkTransactionIdFromExternalId(accountId, userId, identifier).orElse(null);
        }
        return getTransactionByUserAndId(userId, identifier);
    }

    public Optional<Transaction> getTransactionByUserAndId(String userId, String transactionId) {
        if (transactionId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(transactionDao.findOneByUserIdAndId(userId, transactionId, Optional.empty()));
    }
}
