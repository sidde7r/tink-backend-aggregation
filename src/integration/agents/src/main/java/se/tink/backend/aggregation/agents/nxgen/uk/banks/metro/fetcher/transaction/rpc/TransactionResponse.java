package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.transaction.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.transaction.model.TransactionDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.transaction.model.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionResponse {
    private TransactionDetailsEntity transactionDetails;

    public List<TransactionEntity> getTransactions() {
        return Optional.ofNullable(transactionDetails)
                .map(TransactionDetailsEntity::getTransactions)
                .orElse(Collections.emptyList());
    }
}
