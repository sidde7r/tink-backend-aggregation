package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CreditCardTransactionsResponse {
    private int page;
    private int pageSize;
    private int size;

    private List<CreditCardTransactionEntity> transactions;

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getSize() {
        return size;
    }

    public List<CreditCardTransactionEntity> getTransactions() {
        return transactions;
    }
}
