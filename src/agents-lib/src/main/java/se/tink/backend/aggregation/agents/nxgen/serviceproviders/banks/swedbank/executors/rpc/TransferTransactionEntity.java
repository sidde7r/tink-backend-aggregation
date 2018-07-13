package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransferTransactionEntity {
    private String currencyCode;
    private String amount;
    private List<TransactionEntity> transactions;
    private FromAccountEntity fromAccount;

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getAmount() {
        return amount;
    }

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public FromAccountEntity getFromAccount() {
        return fromAccount;
    }
}
