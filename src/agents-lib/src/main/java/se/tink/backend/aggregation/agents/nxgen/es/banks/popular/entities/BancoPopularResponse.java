package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BancoPopularResponse {
    private boolean faultIndicator;
    private String nextPage;
    private String faultMessage;
    private String faultCode;

    public boolean isFaultIndicator() {
        return faultIndicator;
    }

    public void setFaultIndicator(boolean faultIndicator) {
        this.faultIndicator = faultIndicator;
    }

    public String getNextPage() {
        return nextPage;
    }

    public void setNextPage(String nextPage) {
        this.nextPage = nextPage;
    }

    public String getFaultMessage() {
        return faultMessage;
    }

    public void setFaultMessage(String faultMessage) {
        this.faultMessage = faultMessage;
    }

    public String getFaultCode() {
        return faultCode;
    }

    public void setFaultCode(String faultCode) {
        this.faultCode = faultCode;
    }
}
