package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc;

import java.util.List;
import lombok.Data;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class TransactionsResponse {
    private String originGroupId;
    private List<TransactionEntity> transactions;

    public List<TransactionEntity> getTransactions() {
        return ListUtils.emptyIfNull(transactions);
    }
}
