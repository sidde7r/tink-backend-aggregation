package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class TransactionsResponseEntity {

    private List<TransactionEntity> transactions;

    @JsonProperty("_links")
    private List<LinkEntity> links;
}
