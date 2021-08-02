package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
@Getter
@Setter
public class BeneficiaryEntity {
    @JsonProperty("id")
    private String id;

    @JsonProperty("isTrusted")
    private Boolean isTrusted;

    @JsonProperty("creditorAgent")
    private FinancialInstitutionIdentificationEntity creditorAgent;

    @JsonProperty("creditor")
    private PartyIdentificationEntity creditor;

    @JsonProperty("creditorAccount")
    private AccountIdentificationEntity creditorAccount;

    @JsonCreator
    private BeneficiaryEntity(
            @JsonProperty("id") String id,
            @JsonProperty("isTrusted") Boolean isTrusted,
            @JsonProperty("creditorAgent") FinancialInstitutionIdentificationEntity creditorAgent,
            @JsonProperty("creditor") PartyIdentificationEntity creditor,
            @JsonProperty("creditorAccount") AccountIdentificationEntity creditorAccount) {
        this.id = id;
        this.isTrusted = isTrusted;
        this.creditorAgent = creditorAgent;
        this.creditor = creditor;
        this.creditorAccount = creditorAccount;
    }

    @JsonIgnore
    public static BeneficiaryEntityBuilder builder() {
        return new BeneficiaryEntityBuilder();
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
