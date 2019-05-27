package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.cardaccounts.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Error {

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

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getDeveloperMessage() {
        return developerMessage;
    }

    public void setDeveloperMessage(String developerMessage) {
        this.developerMessage = developerMessage;
    }

    public String getErrorAttribute() {
        return errorAttribute;
    }

    public void setErrorAttribute(String errorAttribute) {
        this.errorAttribute = errorAttribute;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMoreInformation() {
        return moreInformation;
    }

    public void setMoreInformation(String moreInformation) {
        this.moreInformation = moreInformation;
    }

    public String getSupportID() {
        return supportID;
    }

    public void setSupportID(String supportID) {
        this.supportID = supportID;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }
}
