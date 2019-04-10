package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorEntity {
    private String code;

    public String getCode() {
        return code;
    }
}
