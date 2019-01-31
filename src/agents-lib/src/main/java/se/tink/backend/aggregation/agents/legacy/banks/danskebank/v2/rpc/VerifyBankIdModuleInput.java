package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VerifyBankIdModuleInput extends BankIdModuleInput {
    private static final String OPERATION = "verifyAuth";

    @JsonProperty("OrderReference")
    private String orderReference;

    public String getOrderReference() {
        return orderReference;
    }

    public void setOrderReference(String orderReference) {
        this.orderReference = orderReference;
    }

    public VerifyBankIdModuleInput(String orderReference) {
        super(OPERATION);
        this.orderReference = orderReference;
    }

}
