package se.tink.backend.connector;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.RandomStringUtils;
import se.tink.backend.connector.exception.RequestException;
import se.tink.backend.connector.rpc.AccountEntity;
import se.tink.backend.connector.rpc.AccountListEntity;
import se.tink.backend.connector.rpc.CreateTransactionAccountEntity;
import se.tink.backend.connector.rpc.CreateTransactionEntity;
import se.tink.backend.connector.rpc.UserEntity;
import se.tink.backend.connector.util.handler.AccountHandler;
import se.tink.backend.connector.util.handler.UserHandler;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Market;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.TransactionTypes;
import se.tink.backend.core.User;
import se.tink.backend.utils.Doubles;

public class TestBase {

    private static final Market SE_MARKET = new Market("SE", "sv_SE");
    private AccountHandler accountHandler;
    private UserHandler userHandler;

    @Inject
    public TestBase(AccountHandler accountHandler, UserHandler userHandler) {
        this.accountHandler = accountHandler;
        this.userHandler = userHandler;
    }

    public static boolean transactionsEqual(CreateTransactionEntity transactionEntity, Transaction transaction) {
        return Doubles.fuzzyEquals(transactionEntity.getAmount(), transaction.getAmount(), 0.001) &&
                Objects.equals(transactionEntity.getDescription(), transaction.getDescription()) &&
                Objects.equals(transactionEntity.getExternalId(),
                        transaction.getPayload().get(TransactionPayloadTypes.EXTERNAL_ID)) &&
                Objects.equals(transactionEntity.getDate(), transaction.getDate()) &&
                Objects.equals(transactionEntity.getType(), transaction.getType());
    }

    public static boolean usersEqual(UserEntity userEntity, User user) {
        return Objects.equals(userEntity.getExternalId(), user.getUsername());
    }

    public static boolean accountsEqual(AccountEntity accountEntity, Account account) {
        return Objects.equals(accountEntity.getName(), account.getName()) &&
                Objects.equals(accountEntity.getType(), account.getType()) &&
                Objects.equals(accountEntity.getName(), account.getName()) &&
                Objects.equals(accountEntity.getExternalId(), account.getBankId()) &&
                (accountEntity.getAvailableCredit() == null || Doubles
                        .fuzzyEquals(accountEntity.getAvailableCredit(), account.getAvailableCredit(), 0.001)) &&
                (accountEntity.getReservedAmount() != null ?
                        Doubles.fuzzyEquals(accountEntity.getBalance() - accountEntity.getReservedAmount(), account.getBalance(), 0.001) :
                        Doubles.fuzzyEquals(accountEntity.getBalance(), account.getBalance(), 0.001));
    }

    public static CreateTransactionAccountEntity createTransactionAccount(String accountName, Double balance,
            CreateTransactionEntity transaction, Double reservedAmount) {
        return createTransactionAccount(accountName, balance, Lists.newArrayList(transaction), reservedAmount);
    }

    public static CreateTransactionAccountEntity createTransactionAccount(String accountName, Double balance,
            List<CreateTransactionEntity> transactions, Double reservedAmount) {
        CreateTransactionAccountEntity transactionAccountEntity = new CreateTransactionAccountEntity();
        transactionAccountEntity.setTransactions(transactions);
        transactionAccountEntity.setBalance(balance);
        transactionAccountEntity.setReservedAmount(reservedAmount);
        transactionAccountEntity.setExternalId(accountName);
        return transactionAccountEntity;
    }

    public static CreateTransactionEntity createTransaction(Double amount, TransactionTypes type, String description) {
        return createTransaction(amount, type, description, new Date());
    }

    public static CreateTransactionEntity createTransaction(Double amount, TransactionTypes type, String description, Date date) {
        CreateTransactionEntity transaction = new CreateTransactionEntity();
        transaction.setAmount(amount);
        transaction.setExternalId(RandomStringUtils.random(10));
        transaction.setType(type);
        transaction.setDescription(description);
        transaction.setDate(date);
        return transaction;
    }

    public Account createAccount(Double balance, String name, String accountNumber, User user,
            Credentials credentials) throws RequestException {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setBalance(balance);
        accountEntity.setExternalId(name);
        accountEntity.setNumber(accountNumber);

        return accountHandler.mapToTinkModel(accountEntity, user, credentials);
    }

    public User createUser(UserEntity userEntity) throws RequestException {
        return userHandler.mapToTinkModel(userEntity, SE_MARKET);
    }

    public static UserEntity createUserEntity(Map<String, Object> payload) {
        UserEntity userEntity = new UserEntity();
        userEntity.setExternalId(RandomStringUtils.randomAlphanumeric(10));
        userEntity.setToken(RandomStringUtils.randomAlphanumeric(10));
        userEntity.setPayload(payload);
        return userEntity;
    }

    public static AccountListEntity createAccountListEntity(AccountEntity accountEntity) {
        return createAccountListEntity(Lists.newArrayList(accountEntity));
    }

    public static AccountListEntity createAccountListEntity(List<AccountEntity> accountEntities) {
        AccountListEntity accountListEntity = new AccountListEntity();
        accountListEntity.setAccounts(Lists.newArrayList(accountEntities));
        return accountListEntity;
    }

    public static AccountEntity createAccountEntity(Double balance, AccountTypes type, String number, String name) {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setBalance(balance);
        accountEntity.setExternalId(RandomStringUtils.random(10));
        accountEntity.setType(type);
        accountEntity.setNumber(number);
        accountEntity.setName(name);
        return accountEntity;
    }
}
