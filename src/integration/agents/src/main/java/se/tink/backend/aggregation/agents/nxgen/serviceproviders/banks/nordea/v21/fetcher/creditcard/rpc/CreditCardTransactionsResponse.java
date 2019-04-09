package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.creditcard.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.creditcard.entities.CreditCardTransactionsOutEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.rpc.NordeaResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardTransactionsResponse extends NordeaResponse {
    @JsonProperty("getCreditCardTransactionsOut")
    private CreditCardTransactionsOutEntity transactionsEntity;

    public CreditCardTransactionsOutEntity getTransactionsEntity() {
        return transactionsEntity;
    }

    public List<CreditCardTransactionEntity> getTransactions() {
        if (transactionsEntity == null) {
            return Collections.emptyList();
        }

        // Apparently some transactions don't have either amount or date, but since Nordea doesn't
        // display those
        // transactions in their app, we ill just filter them out
        return transactionsEntity.getTransactions().stream()
                .filter(tx -> tx.getDate() != null || tx.getAmount() != null)
                .collect(Collectors.toList());
    }

    public String getContinuationKey() {
        return transactionsEntity != null ? transactionsEntity.getContinuationKey() : null;
    }
}
