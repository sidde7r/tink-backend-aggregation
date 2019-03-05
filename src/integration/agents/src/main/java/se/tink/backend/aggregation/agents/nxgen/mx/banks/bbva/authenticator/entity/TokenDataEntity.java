package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TokenDataEntity {
    private String softwareTokenAuthCode;
    private String id;
    private StatusEntity status;

    public String getSoftwareTokenAuthCode() {
        return softwareTokenAuthCode;
    }

    public String getId() {
        return id;
    }

    public StatusEntity getStatus() {
        return status;
    }
}
