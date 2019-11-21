package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.request.investment;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetInvestmentsBodyEntity {

    public GetInvestmentsBodyEntity(String accountId, String dossierNumber) {
        this.accountId = accountId;
        this.dossierNumber = dossierNumber;
    }

    @JsonProperty("NumeroContaDO")
    String accountId;

    @JsonProperty("NumeroDossier")
    String dossierNumber;
}
