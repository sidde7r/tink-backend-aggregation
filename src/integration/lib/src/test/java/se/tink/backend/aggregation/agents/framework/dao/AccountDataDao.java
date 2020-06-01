package se.tink.backend.aggregation.agents.framework.dao;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountDataDao {
    private Account account;
    private List<Transaction> transactions;
    private List<TransferDestinationPattern> transferDestinationPatterns;

    @JsonCreator
    public AccountDataDao(
            @JsonProperty("account") Account account,
            @JsonProperty("transactions") List<Transaction> transactions,
            @JsonProperty("transferDestinationPatterns")
                    List<TransferDestinationPattern> transferDestinationPatterns) {
        this.account = account;
        this.transactions = transactions;
        this.transferDestinationPatterns = transferDestinationPatterns;
    }

    public Account getAccount() {
        return account;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public List<TransferDestinationPattern> getTransferDestinationPatterns() {
        return transferDestinationPatterns;
    }
}
