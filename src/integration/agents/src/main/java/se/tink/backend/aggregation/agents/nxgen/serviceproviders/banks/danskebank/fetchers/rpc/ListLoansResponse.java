package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.entities.LoanEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ListLoansResponse {
    private List<LoanEntity> loans;
    private String lastUpdated;
    private String sessionId;
    private int responseCode;

    public List<LoanEntity> getLoans() {
        return loans;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
