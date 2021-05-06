package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.transaction.model;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class TransactionDetailsEntity {
    private List<TransactionEntity> transactions;
}
