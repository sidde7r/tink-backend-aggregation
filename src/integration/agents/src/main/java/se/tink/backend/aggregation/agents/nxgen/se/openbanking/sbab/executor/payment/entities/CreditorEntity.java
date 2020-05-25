package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditorEntity {
    private String accountNumber;

    public CreditorEntity(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public CreditorEntity() {}

    public String getAccountNumber() {
        return accountNumber;
    }
}
