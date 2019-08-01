package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
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

    @JsonCreator
    private BeneficiaryEntity(
            String id,
            Boolean isTrusted,
            FinancialInstitutionIdentificationEntity creditorAgent,
            PartyIdentificationEntity creditor,
            AccountIdentificationEntity creditorAccount) {
        this.id = id;
        this.isTrusted = isTrusted;
        this.creditorAgent = creditorAgent;
        this.creditor = creditor;
        this.creditorAccount = creditorAccount;
    }

    public static BeneficiaryEntityBuilder builder() {
        return new BeneficiaryEntityBuilder();
    }

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

    public static class BeneficiaryEntityBuilder {

        private String id;
        private Boolean isTrusted;
        private FinancialInstitutionIdentificationEntity creditorAgent;
        private PartyIdentificationEntity creditor;
        private AccountIdentificationEntity creditorAccount;

        BeneficiaryEntityBuilder() {}

        public BeneficiaryEntityBuilder id(String id) {
            this.id = id;
            return this;
        }

        public BeneficiaryEntityBuilder isTrusted(Boolean isTrusted) {
            this.isTrusted = isTrusted;
            return this;
        }

        public BeneficiaryEntityBuilder creditorAgent(
                FinancialInstitutionIdentificationEntity creditorAgent) {
            this.creditorAgent = creditorAgent;
            return this;
        }

        public BeneficiaryEntityBuilder creditor(PartyIdentificationEntity creditor) {
            this.creditor = creditor;
            return this;
        }

        public BeneficiaryEntityBuilder creditorAccount(
                AccountIdentificationEntity creditorAccount) {
            this.creditorAccount = creditorAccount;
            return this;
        }

        public BeneficiaryEntity build() {
            return new BeneficiaryEntity(id, isTrusted, creditorAgent, creditor, creditorAccount);
        }

        public String toString() {
            return "BeneficiaryEntity.BeneficiaryEntityBuilder(id="
                    + this.id
                    + ", isTrusted="
                    + this.isTrusted
                    + ", creditorAgent="
                    + this.creditorAgent
                    + ", creditor="
                    + this.creditor
                    + ", creditorAccount="
                    + this.creditorAccount
                    + ")";
        }
    }
}
