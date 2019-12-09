package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BeneficiaryEntity {
    private CreditorEntity creditor;
    private AccountEntity creditorAccount;

    @JsonCreator
    public BeneficiaryEntity(
            @JsonProperty("creditorName") String creditorName,
            @JsonProperty("creditorAccount") AccountEntity creditorAccount) {
        creditor = new CreditorEntity(creditorName);
        this.creditorAccount = creditorAccount;
    }

    public AccountEntity getCreditorAccount() {
        return creditorAccount;
    }
}
