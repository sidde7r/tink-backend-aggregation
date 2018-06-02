package se.tink.backend.aggregation.agents.brokers.lysa.rpc;

public class StartBankIdRequest {
    private String identificationNumber;

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public void setIdentificationNumber(String identificationNumber) {
        this.identificationNumber = identificationNumber;
    }
}
