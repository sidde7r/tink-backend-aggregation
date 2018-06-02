package se.tink.backend.connector;

import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.connector.exception.RequestException;
import se.tink.backend.connector.rpc.CreateTransactionAccountContainer;
import se.tink.backend.connector.rpc.CreateTransactionAccountEntity;
import se.tink.backend.connector.rpc.TransactionContainerType;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.User;

public class TestUser {

    private final User user;
    private final Credentials credentials;
    private TestBase testBase;
    private List<Account> accounts = Lists.newArrayList();
    private List<CreateTransactionAccountContainer> accountTransactionEntities = Lists.newArrayList();

    public TestUser(User user, Credentials credentials, TestBase testBase) {
        this.user = user;
        this.credentials = credentials;
        this.testBase = testBase;
    }

    public void addAccount(Double balance, String name, String accountNumber) throws RequestException {
        accounts.add(testBase.createAccount(balance, name, accountNumber, user, credentials));
    }

    public void addAccountAndSaveToDB(Double balance, String name, String accountNumber,
            AccountRepository accountRepository) throws RequestException {
        Account account = testBase.createAccount(balance, name, accountNumber, user, credentials);
        accounts.add(account);
        accountRepository.save(account);
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void addRealTimeAccountTransactionEntity(CreateTransactionAccountEntity... transactionAccount) {
        addRealTimeAccountTransactionEntity(Lists.newArrayList(transactionAccount));
    }

    public void addRealTimeAccountTransactionEntity(List<CreateTransactionAccountEntity> transactionAccounts) {
        CreateTransactionAccountContainer transactionAccountContainer = new CreateTransactionAccountContainer();
        transactionAccountContainer.setTransactionAccounts(transactionAccounts);
        transactionAccountContainer.setType(TransactionContainerType.REAL_TIME);
        accountTransactionEntities.add(transactionAccountContainer);
    }

    public List<CreateTransactionAccountContainer> getAccountTransactionEntities() {
        return accountTransactionEntities;
    }
}
