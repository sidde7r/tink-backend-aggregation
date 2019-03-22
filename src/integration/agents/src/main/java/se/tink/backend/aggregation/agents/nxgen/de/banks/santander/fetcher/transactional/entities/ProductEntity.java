package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProductEntity {
    @JsonProperty("EMPRESA")
    private String companyId;

    public String getCompanyId() {
        return companyId;
    }
}
