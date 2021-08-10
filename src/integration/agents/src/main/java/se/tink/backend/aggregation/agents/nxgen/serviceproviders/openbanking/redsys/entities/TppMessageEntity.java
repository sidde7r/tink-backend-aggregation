package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TppMessageEntity {
    @JsonProperty private String category;

    @JsonProperty private String code;

    @JsonProperty private String path;

    @JsonProperty private String text;

    @JsonIgnore
    public String getCode() {
        return code;
    }

    @JsonIgnore
    public String getText() {
        return text;
    }

    @JsonIgnore
    public boolean isValidMessageEntity() {
        return !Strings.isNullOrEmpty(category) && !Strings.isNullOrEmpty(code);
    }
}
