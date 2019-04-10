package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.creditcard.entities.CreditCardTransactionEntities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.creditcard.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardTransactionsOutEntity {
    @JsonProperty("cardTransactionsResponse")
    private CreditCardTransactionEntities transactionsEntity;

    public CreditCardTransactionEntities getTransactionsEntity() {
        return transactionsEntity;
    }

    public List<CreditCardTransactionEntity> getTransactions() {
        return transactionsEntity != null
                ? transactionsEntity.getTransactions()
                : Collections.emptyList();
    }

    public String getContinuationKey() {
        return transactionsEntity != null ? transactionsEntity.getContinuationKey() : null;
    }
}
