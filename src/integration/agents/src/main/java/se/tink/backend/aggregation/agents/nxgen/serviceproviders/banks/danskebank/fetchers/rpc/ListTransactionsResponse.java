package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc.AbstractResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ListTransactionsResponse extends AbstractResponse {
    private int totalPages;
    private int totalTransactions;
    @Setter private boolean endOfList;
    private List<TransactionEntity> transactions;
    private String repositionKey;
}
