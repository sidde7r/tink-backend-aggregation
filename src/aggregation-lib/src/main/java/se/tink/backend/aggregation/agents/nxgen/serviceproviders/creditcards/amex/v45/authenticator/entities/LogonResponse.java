package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LogonResponse {
    public LogonDataEntity getLogonData() {
        return logonData;
    }

    public SummaryDataEntity getSummaryData() {
        return summaryData;
    }

    private LogonDataEntity logonData;
    private SummaryDataEntity summaryData;
}
