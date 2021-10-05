package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ExpenseControlEntity {
    private String status;
    private boolean viewCategorizations;
    private LinksEntity links;
}
