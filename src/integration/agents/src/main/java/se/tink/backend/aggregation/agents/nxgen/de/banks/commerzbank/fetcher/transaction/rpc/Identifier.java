package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.rpc;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class Identifier {
    private String productType;
    private String currency;
    private String identifier;
    private String productBranch;
}
