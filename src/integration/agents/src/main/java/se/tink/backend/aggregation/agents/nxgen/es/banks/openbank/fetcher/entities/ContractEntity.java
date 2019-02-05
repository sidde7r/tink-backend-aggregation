package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ContractEntity {
    @JsonProperty("numerodecontrato")
    private String contractNumber;

    @JsonProperty("producto")
    private String productCode;

    public String getContractNumber() {
        return contractNumber;
    }

    public String getProductCode() {
        return productCode;
    }
}
