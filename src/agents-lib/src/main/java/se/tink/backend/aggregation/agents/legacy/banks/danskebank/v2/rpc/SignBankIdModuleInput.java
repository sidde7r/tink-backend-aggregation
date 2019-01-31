package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SignBankIdModuleInput extends BankIdModuleInput {
    public static final String OPERATION = "verifySign";

    @JsonProperty("OrderReference")
    private String orderReference;

    public String getOrderReference() {
        return orderReference;
    }

    public void setOrderReference(String orderReference) {
        this.orderReference = orderReference;
    }

    public SignBankIdModuleInput(String orderReference) {
        super(OPERATION);
        this.orderReference = orderReference;
    }
}
