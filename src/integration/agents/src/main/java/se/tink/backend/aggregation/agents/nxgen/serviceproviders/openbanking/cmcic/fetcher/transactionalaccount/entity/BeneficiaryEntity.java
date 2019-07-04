package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BeneficiaryEntity {
    @JsonProperty("id")
    private String id = null;

    @JsonProperty("isTrusted")
    private Boolean isTrusted = null;

    @JsonProperty("creditorAgent")
    private FinancialInstitutionIdentificationEntity creditorAgent = null;

    @JsonProperty("creditor")
    private PartyIdentificationEntity creditor = null;

    @JsonProperty("creditorAccount")
    private AccountIdentificationEntity creditorAccount = null;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getTrusted() {
        return isTrusted;
    }

    public void setTrusted(Boolean trusted) {
        isTrusted = trusted;
    }

    public FinancialInstitutionIdentificationEntity getCreditorAgent() {
        return creditorAgent;
    }

    public void setCreditorAgent(FinancialInstitutionIdentificationEntity creditorAgent) {
        this.creditorAgent = creditorAgent;
    }

    public PartyIdentificationEntity getCreditor() {
        return creditor;
    }

    public void setCreditor(PartyIdentificationEntity creditor) {
        this.creditor = creditor;
    }

    public AccountIdentificationEntity getCreditorAccount() {
        return creditorAccount;
    }

    public void setCreditorAccount(AccountIdentificationEntity creditorAccount) {
        this.creditorAccount = creditorAccount;
    }
}
