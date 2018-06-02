package se.tink.backend.connector.util.handler;

import java.util.List;
import java.util.Map;
import se.tink.backend.core.Account;
import se.tink.backend.core.Transaction;

public interface BalanceHandler {

    void calculateAndUpdateBalance(Account account, CRUDType crudType, List<Transaction> transactions);

    void setNewBalance(Account account, Double reservedAmountObject, Double balanceObject, Map<String, Object> payload);
}
