package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class TransactionsResponsePayload {

    @JsonProperty("operazioni")
    private List<TransactionEntity> transactions;
}
