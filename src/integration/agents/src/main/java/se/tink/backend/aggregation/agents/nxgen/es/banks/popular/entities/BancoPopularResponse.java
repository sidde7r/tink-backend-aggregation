package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BancoPopularResponse {
    private boolean faultIndicator;
    private String nextPage;
    private String faultMessage;
    private String faultCode;

    public boolean isFaultIndicator() {
        return faultIndicator;
    }

    public String getNextPage() {
        return nextPage;
    }

    public String getFaultMessage() {
        return faultMessage;
    }

    public String getFaultCode() {
        return faultCode;
    }
}
