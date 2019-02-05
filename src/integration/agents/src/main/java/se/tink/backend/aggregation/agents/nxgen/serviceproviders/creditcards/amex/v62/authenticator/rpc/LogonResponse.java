package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.entities.LogonDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.entities.Status;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.entities.SummaryDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LogonResponse {

    private Status status;
    private LogonDataEntity logonData;
    private SummaryDataEntity summaryData;

    public LogonDataEntity getLogonData() {
        return logonData;
    }

    public SummaryDataEntity getSummaryData() {
        return summaryData;
    }

    public Status getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return status.isSuccess();
    }
}
