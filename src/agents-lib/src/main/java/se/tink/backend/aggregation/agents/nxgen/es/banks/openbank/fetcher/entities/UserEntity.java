package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserEntity {
    @JsonProperty("tipodepersona")
    private String userType;

    @JsonProperty("codigodepersona")
    private int userCode;

    public String getUserType() {
        return userType;
    }

    public int getUserCode() {
        return userCode;
    }
}
