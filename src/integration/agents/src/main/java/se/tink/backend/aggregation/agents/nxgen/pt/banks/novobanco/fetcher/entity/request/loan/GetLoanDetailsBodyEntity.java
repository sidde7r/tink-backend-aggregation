package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.request.loan;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetLoanDetailsBodyEntity {
    @JsonProperty("Contrato")
    private String contractId;

    public GetLoanDetailsBodyEntity(String contractId) {
        this.contractId = contractId;
    }
}
