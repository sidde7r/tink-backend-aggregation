package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorEntity {
    @JsonProperty("error_description")
    private String errorDescription;

    @JsonProperty("error_description_translated")
    private String errorDescriptionTranslated;

    public String getErrorDescription() {
        return errorDescription;
    }

    public String getErrorDescriptionTranslated() {
        return errorDescriptionTranslated;
    }
}
