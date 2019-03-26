package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Info {

    @JsonProperty("errorDesc")
    private String errorDesc;

    @JsonProperty("errorCode")
    private String errorCode;

    @JsonProperty("codError")
    private String codError;
}
