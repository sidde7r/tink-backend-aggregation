package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountNumberEntity {
    @JsonProperty("TIPO_CONTRATO_LOCAL")
    private String localContractType;
    @JsonProperty("DETALLE_CONTRATO_LOCAL")
    private String localContractDetail;

    public String getLocalContractType() {
        return localContractType;
    }

    public String getLocalContractDetail() {
        return localContractDetail;
    }
}
