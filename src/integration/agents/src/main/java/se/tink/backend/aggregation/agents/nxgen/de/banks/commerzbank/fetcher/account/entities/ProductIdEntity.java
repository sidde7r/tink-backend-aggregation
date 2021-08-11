package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ProductIdEntity {
    private String identifier;
    private String productBranch;
    private String currency;
    private String productType;
}
