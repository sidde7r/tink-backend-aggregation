package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities.authentication;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MaintenanceMessageEntity {
    private String header;
    private String message;

    public String getHeader() {
        return header;
    }

    public String getMessage() {
        return message;
    }
}
