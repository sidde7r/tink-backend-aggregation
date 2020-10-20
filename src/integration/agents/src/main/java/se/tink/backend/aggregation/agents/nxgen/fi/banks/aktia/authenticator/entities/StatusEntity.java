package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StatusEntity {

    private String statusCode;
    private String localizedMessage;
    private String technicalMessage;

    public String getStatusCode() {
        return statusCode;
    }

    public String getLocalizedMessage() {
        return localizedMessage;
    }

    public String getTechnicalMessage() {
        return technicalMessage;
    }

    public boolean isSuccess() {
        return "SUCCESS".equals(statusCode);
    }
}
