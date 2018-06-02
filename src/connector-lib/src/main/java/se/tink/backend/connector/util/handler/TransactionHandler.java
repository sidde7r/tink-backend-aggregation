package se.tink.backend.connector.util.handler;

import java.util.List;
import java.util.concurrent.ExecutionException;
import se.tink.backend.connector.rpc.CreateTransactionEntity;
import se.tink.backend.connector.rpc.TransactionContainerType;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.system.rpc.TransactionToDelete;

public interface TransactionHandler {

    Transaction mapToTinkModel(User user, Credentials credentials, Account account,
            CreateTransactionEntity transactionEntity);

    Transaction mapUpdateToTinkModel(Transaction transaction, CreateTransactionEntity updatedTransactionEntity);

    void updateTransaction(Transaction transaction);

    void ingestTransactions(User user, Credentials credentials, List<Transaction> transactions,
            TransactionContainerType type) throws ExecutionException;

    void deleteTransactions(List<TransactionToDelete> transactionsToDelete, User user);
}
