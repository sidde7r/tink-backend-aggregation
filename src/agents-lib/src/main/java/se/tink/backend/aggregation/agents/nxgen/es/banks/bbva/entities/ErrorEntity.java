package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorEntity {
    private String code;
    private String description;

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @JsonIgnore
    public BbvaConstants.Error getError() {
        return BbvaConstants.Error.find(code);
    }
}
