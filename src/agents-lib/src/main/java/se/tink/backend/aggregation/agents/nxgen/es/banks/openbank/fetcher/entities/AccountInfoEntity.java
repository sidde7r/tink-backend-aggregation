package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountInfoEntity {
    @JsonAlias({"producto", "subgrupo"})
    private String productCode;

    @JsonProperty("numerodecontrato")
    private String contractNumber;

    public String getContractNumber() {
        return contractNumber;
    }

    public String getProductCode() {
        return productCode;
    }
}
