package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class ResponseCodeEntity {

    private String code;
    private List<LinkEntity> links;
    private String state;

    public String getCode() {
        return code;
    }
}
