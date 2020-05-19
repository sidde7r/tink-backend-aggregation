package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BeneficiaryEntity {

    private PartyIdentificationEntity creditor;
    private CreditorAccountEntity creditorAccount;
    private CreditorAgentEntity creditorAgent;

    @JsonIgnore
    public static BeneficiaryEntity of(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();
        return new BeneficiaryEntity.Builder()
                .withCreditor(
                        new PartyIdentificationEntity(
                                SocieteGeneraleConstants.FormValues.PAYMENT_CREDITOR_DEFAULT_NAME))
                .withCreditorAccount(
                        new CreditorAccountEntity(payment.getCreditor().getAccountNumber()))
                .withCreditorAgent(
                        new CreditorAgentEntity(
                                SocieteGeneraleConstants.FormValues.BANK_BICFI_CODE))
                .build();
    }

    @JsonIgnore
    private BeneficiaryEntity(Builder builder) {
        this.creditor = builder.creditor;
        this.creditorAccount = builder.creditorAccount;
        this.creditorAgent = builder.creditorAgent;
    }

    public static class Builder {
        private PartyIdentificationEntity creditor;
        private CreditorAccountEntity creditorAccount;
        private CreditorAgentEntity creditorAgent;

        public Builder withCreditor(PartyIdentificationEntity creditor) {
            this.creditor = creditor;
            return this;
        }

        public Builder withCreditorAccount(CreditorAccountEntity creditorAccount) {
            this.creditorAccount = creditorAccount;
            return this;
        }

        public Builder withCreditorAgent(CreditorAgentEntity creditorAgent) {
            this.creditorAgent = creditorAgent;
            return this;
        }

        public BeneficiaryEntity build() {
            return new BeneficiaryEntity(this);
        }
    }
}
