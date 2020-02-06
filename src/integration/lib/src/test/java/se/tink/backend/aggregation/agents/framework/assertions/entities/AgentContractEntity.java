package se.tink.backend.aggregation.agents.framework.assertions.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.framework.assertions.deserializers.AccountDeserializer;
import se.tink.backend.aggregation.agents.models.Transaction;

public class AgentContractEntity {

    @JsonDeserialize(using = AccountDeserializer.class)
    private List<Account> accounts;

    private List<Transaction> transactions;

    public List<Account> getAccounts() {
        return accounts;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
}
