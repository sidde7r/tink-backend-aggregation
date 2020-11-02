package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.entity.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class CardTransactionResponse {
    private TransactionEntity cardTransactions;
}
