package se.tink.backend.connector.seb;

import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.RandomStringUtils;
import se.tink.backend.common.config.FlagsConfiguration;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.connector.rpc.TransactionContainerType;
import se.tink.backend.connector.rpc.seb.AccountEntity;
import se.tink.backend.connector.rpc.seb.AccountListEntity;
import se.tink.backend.connector.rpc.seb.TransactionAccountContainer;
import se.tink.backend.connector.rpc.seb.TransactionAccountEntity;
import se.tink.backend.connector.rpc.seb.TransactionEntity;
import se.tink.backend.connector.rpc.seb.UserEntity;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Market;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.TransactionTypes;
import se.tink.backend.core.User;
import se.tink.backend.seb.utils.SEBUtils;
import se.tink.backend.utils.Doubles;

public class SebConnectorTestBase {

    private static final Market SE_MARKET = new Market("SE", "sv_SE");

    static boolean transactionsEqual(TransactionEntity transactionEntity, Transaction transaction) {
        return Doubles.fuzzyEquals(transactionEntity.getAmount(), transaction.getAmount(), 0.001) &&
                Objects.equals(transactionEntity.getDescription(), transaction.getDescription()) &&
                Objects.equals(transactionEntity.getExternalId(),
                        transaction.getPayload().get(TransactionPayloadTypes.EXTERNAL_ID)) &&
                Objects.equals(transactionEntity.getDate(), transaction.getDate()) &&
                Objects.equals(transactionEntity.getType(), transaction.getType());
    }

    static boolean usersEqual(UserEntity userEntity, User user) {
        return Objects.equals(userEntity.getExternalId(), user.getUsername());
    }

    static boolean accountsEqual(AccountEntity accountEntity, Account account) {
        return Objects.equals(accountEntity.getName(), account.getName()) &&
                Objects.equals(accountEntity.getType(), account.getType()) &&
                Objects.equals(accountEntity.getName(), account.getName()) &&
                Objects.equals(accountEntity.getExternalId(), account.getBankId()) &&
                (accountEntity.getAvailableCredit() == null || Doubles
                        .fuzzyEquals(accountEntity.getAvailableCredit(), account.getAvailableCredit(), 0.001)) &&
                (accountEntity.getDisposableAmount() != null ?
                        Doubles.fuzzyEquals(accountEntity.getDisposableAmount(), account.getBalance(), 0.001) :
                        Doubles.fuzzyEquals(accountEntity.getBalance(), account.getBalance(), 0.001));
    }

    static TransactionAccountEntity createTransactionAccount(String accountName, Double balance,
            TransactionEntity transaction, Double disposableAmount) {
        return createTransactionAccount(accountName, balance, Lists.newArrayList(transaction), disposableAmount);
    }

    static TransactionAccountEntity createTransactionAccount(String accountName, Double balance,
            List<TransactionEntity> transactions, Double disposableAmount) {
        TransactionAccountEntity transactionAccountEntity = new TransactionAccountEntity();
        transactionAccountEntity.setTransactions(transactions);
        transactionAccountEntity.setBalance(balance);
        transactionAccountEntity.setDisposableAmount(disposableAmount);
        transactionAccountEntity.setExternalId(accountName);
        return transactionAccountEntity;
    }

    static TransactionEntity createTransaction(Double amount, TransactionTypes type, String description) {
        return createTransaction(amount, type, description, new Date());
    }

    static TransactionEntity createTransaction(Double amount, TransactionTypes type, String description, Date date) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setAmount(amount);
        transaction.setExternalId(RandomStringUtils.random(10));
        transaction.setType(type);
        transaction.setDescription(description);
        transaction.setDate(date);
        return transaction;
    }

    static Account createAccount(Double balance, String name, String accountNumber, User user,
            Credentials credentials) {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setBalance(balance);
        accountEntity.setExternalId(name);
        accountEntity.setNumber(accountNumber);

        return SEBUtils.createAccount(accountEntity, user, credentials);
    }

    static User createUser(UserEntity userEntity) {
        return SEBUtils.createUser(userEntity, SE_MARKET, new FlagsConfiguration());
    }

    static UserEntity createUserEntity(Map<String, Object> payload) {
        UserEntity userEntity = new UserEntity();
        userEntity.setExternalId(RandomStringUtils.randomAlphanumeric(10));
        userEntity.setPayload(payload);
        return userEntity;
    }

    static AccountListEntity createAccountListEntity(AccountEntity accountEntity) {
        return createAccountListEntity(Lists.newArrayList(accountEntity));
    }

    static AccountListEntity createAccountListEntity(List<AccountEntity> accountEntities) {
        AccountListEntity accountListEntity = new AccountListEntity();
        accountListEntity.setAccounts(Lists.newArrayList(accountEntities));
        return accountListEntity;
    }

    static AccountEntity createAccountEntity(Double balance, AccountTypes type, String number, String name) {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setBalance(balance);
        accountEntity.setExternalId(RandomStringUtils.random(10));
        accountEntity.setType(type);
        accountEntity.setNumber(number);
        accountEntity.setName(name);
        return accountEntity;
    }

    static class TestUser {

        private final User user;
        private final Credentials credentials;
        private List<Account> accounts = Lists.newArrayList();
        private List<TransactionAccountContainer> transactionAccountContainers = Lists.newArrayList();

        TestUser(User user, Credentials credentials) {
            this.user = user;
            this.credentials = credentials;
        }

        void addAccount(Double balance, String name, String accountNumber) {
            accounts.add(createAccount(balance, name, accountNumber, user, credentials));
        }

        void addAccountAndSaveToDB(Double balance, String name, String accountNumber,
                AccountRepository accountRepository) {
            Account account = createAccount(balance, name, accountNumber, user, credentials);
            accounts.add(account);
            accountRepository.save(account);
        }

        List<Account> getAccounts() {
            return accounts;
        }

        void addRealTimeTransactionAccountContainer(TransactionAccountEntity... transactionAccount) {
            addRealTimeTransactionAccountContainer(Lists.newArrayList(transactionAccount));
        }

        void addRealTimeTransactionAccountContainer(List<TransactionAccountEntity> transactionAccounts) {
            TransactionAccountContainer transactionAccountContainer = new TransactionAccountContainer();
            transactionAccountContainer.setTransactionAccounts(transactionAccounts);
            transactionAccountContainer.setType(TransactionContainerType.REAL_TIME);
            transactionAccountContainers.add(transactionAccountContainer);
        }

        List<TransactionAccountContainer> getTransactionAccountContainers() {
            return transactionAccountContainers;
        }
    }
}
