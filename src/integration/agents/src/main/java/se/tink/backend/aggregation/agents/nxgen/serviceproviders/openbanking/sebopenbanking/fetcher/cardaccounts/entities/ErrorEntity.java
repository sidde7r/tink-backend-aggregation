package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.cardaccounts.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorEntity {

    private String correlationId;
    private String developerMessage;
    private String errorAttribute;
    private String errorCode;
    private String moreInformation;
    private String supportID;
    private String userMessage;

    public String getCorrelationId() {
        return correlationId;
    }

    public String getDeveloperMessage() {
        return developerMessage;
    }

    public String getErrorAttribute() {
        return errorAttribute;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMoreInformation() {
        return moreInformation;
    }

    public String getSupportID() {
        return supportID;
    }

    public String getUserMessage() {
        return userMessage;
    }
}
