package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DataEntity {

    @JsonProperty("FR")
    private String fr;

    @JsonProperty("NL")
    private String nl;

    @Override
    public String toString() {
        return "DataEntity{" + "fr='" + fr + '\'' + ", nl='" + nl + '\'' + '}';
    }
}
