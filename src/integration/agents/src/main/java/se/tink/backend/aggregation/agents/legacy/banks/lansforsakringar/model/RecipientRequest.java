package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

public class RecipientRequest {
    private String giroNumber;

    public RecipientRequest() {}

    public RecipientRequest(String giroNumber) {
        this.giroNumber = giroNumber;
    }

    public String getGiroNumber() {
        return giroNumber;
    }

    public void setGiroNumber(String giroNumber) {
        this.giroNumber = giroNumber;
    }
}
