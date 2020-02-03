package se.tink.backend.aggregation.agents.framework.assertions.entities;

import java.util.List;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.Transaction;

public class AgentContractEntity {

    private List<Account> accounts;
    private List<Transaction> transactions;

    public List<Account> getAccounts() {
        return accounts;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
}
