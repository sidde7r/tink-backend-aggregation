package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class ActivityListEntity {
    private List<TransactionEntity> transactionList;
    private String billingIndex;

    public List<TransactionEntity> getTransactionList() {
        return transactionList;
    }

    public String getBillingIndex() {
        return billingIndex;
    }

    @JsonIgnore
    public List<Transaction> getTransactions(
            final AmericanExpressV62Configuration configuration,
            final boolean isPending,
            final String suppIndex) {

        return Optional.ofNullable(transactionList).orElse(Collections.emptyList()).stream()
                .filter(t -> suppIndex.equalsIgnoreCase(t.getSuppIndex()))
                .map(t -> t.toTransaction(configuration, isPending))
                .collect(Collectors.toList());
    }
}
