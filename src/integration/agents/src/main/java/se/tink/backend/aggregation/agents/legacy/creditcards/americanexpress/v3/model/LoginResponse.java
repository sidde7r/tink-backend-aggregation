package se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginResponse extends AbstractResponse {
    private LogonDataEntity logonData;
    private SummaryDataEntity summaryData;

    public LogonDataEntity getLogonData() {
        return logonData;
    }

    public void setLoginData(LogonDataEntity logonData) {
        this.logonData = logonData;
    }

    public SummaryDataEntity getSummaryData() {
        return summaryData;
    }

    public void setSummaryData(SummaryDataEntity summaryData) {
        this.summaryData = summaryData;
    }
}
