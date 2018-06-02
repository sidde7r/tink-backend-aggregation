package se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.transaction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import se.tink.backend.aggregation.agents.banks.uk.barclays.entities.transaction.TransactionEntity;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.Response;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionListResponse extends Response {

    private String pendingTransactionsSum;
    private List<TransactionEntity> transactions;
    private boolean eligibleForFeeBasedOverdraft;
    private boolean eligibleForOverdraft;
    private int pendingTransactionsCount;
    private String alchemyTransitionDate;

    public String getPendingTransactionsSum() {
        return pendingTransactionsSum;
    }

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public boolean isEligibleForFeeBasedOverdraft() {
        return eligibleForFeeBasedOverdraft;
    }

    public boolean isEligibleForOverdraft() {
        return eligibleForOverdraft;
    }

    public int getPendingTransactionsCount() {
        return pendingTransactionsCount;
    }

    public String getAlchemyTransitionDate() {
        return alchemyTransitionDate;
    }
}
