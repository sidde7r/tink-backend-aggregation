package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Status {
    private boolean success;
    private String reportingCode;
    private String message;

    public String getMessage() {
        return message;
    }

    public String getReportingCode() {
        return reportingCode;
    }

    public boolean isSuccess() {
        return success;
    }
}
