package se.tink.backend.connector.util.handler;

import java.util.List;
import se.tink.backend.connector.exception.RequestException;
import se.tink.backend.connector.rpc.AccountEntity;
import se.tink.backend.connector.rpc.TransactionAccountEntity;
import se.tink.backend.connector.rpc.TransactionContainerType;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;

public interface AccountHandler {

    List<Account> findAccounts(User user, Credentials credentials) throws RequestException;

    Account mapToTinkModel(AccountEntity accountEntity, User user, Credentials credentials) throws RequestException;

    void updateAccount(TransactionContainerType type, Account account, List<Transaction> transactions,
            TransactionAccountEntity transactionAccount, CRUDType crudType);

    void storeAccount(Account account);

    void deleteAccount(Account account, Credentials credentials, User user);
}
