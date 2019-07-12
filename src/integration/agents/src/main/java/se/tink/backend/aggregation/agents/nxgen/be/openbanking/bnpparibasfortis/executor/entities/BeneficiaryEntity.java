package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BeneficiaryEntity {
    private CreditorEntity creditor;
    private AccountEntity creditorAccount;

    public BeneficiaryEntity(String creditorName, AccountEntity creditorAccount) {
        creditor = new CreditorEntity(creditorName);
        this.creditorAccount = creditorAccount;
    }

    public BeneficiaryEntity() {}

    public AccountEntity getCreditorAccount() {
        return creditorAccount;
    }
}
