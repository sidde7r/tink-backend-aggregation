package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {

    private String detail;

    public String getDetail() {
        return detail;
    }
}
