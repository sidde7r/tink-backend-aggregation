package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.investment;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DossiersEntity {
    @JsonProperty("Numero")
    private String dossierNumber;

    @JsonProperty("Nome")
    private String dossierName;
}
