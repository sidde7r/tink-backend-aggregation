package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@Getter
@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class FetchAccountTransactionResponse {
    @JsonProperty("result")
    private List<TransactionEntity> transactions;

    private String continuationKey;

    @JsonIgnore
    public List<Transaction> toTinkTransactions(
            NordeaConfiguration nordeaConfiguration,
            boolean skipPendingTransactions,
            Set<String> transactionIdsSeen) {
        return getTransactions().stream()
                .filter(transaction -> !transaction.hasSeenTransactionBefore(transactionIdsSeen))
                .map(te -> te.toTinkTransaction(nordeaConfiguration))
                .filter(transaction -> !(skipPendingTransactions && transaction.isPending()))
                .collect(Collectors.toList());
    }
}
