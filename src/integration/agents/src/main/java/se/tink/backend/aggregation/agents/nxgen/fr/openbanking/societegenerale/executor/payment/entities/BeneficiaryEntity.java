package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class BeneficiaryEntity {

    private PartyIdentificationEntity creditor;
    private CreditorAccountEntity creditorAccount;

    @JsonIgnore
    public static BeneficiaryEntity of(PaymentRequest paymentRequest){
        Payment payment = paymentRequest.getPayment();
    return  new BeneficiaryEntity.Builder().withCreditor(new PartyIdentificationEntity.Builder().withName(SocieteGeneraleConstants.FormValues.PAYMENT_CREDITOR_DEFAULT_NAME).build())
          .withCreditorAccount(new CreditorAccountEntity(payment.getCreditor().getAccountNumber())).build();

    }

    @JsonIgnore
    private BeneficiaryEntity(Builder builder){
        this.creditor = builder.creditor;
        this.creditorAccount = builder.creditorAccount;
    }
    public static class Builder {
        private PartyIdentificationEntity creditor;
        private CreditorAccountEntity creditorAccount;

        public Builder withCreditor(PartyIdentificationEntity creditor){
            this.creditor = creditor;
            return this;
        }

        public Builder withCreditorAccount(CreditorAccountEntity creditorAccount){
            this.creditorAccount = creditorAccount;
            return this;
        }

        public BeneficiaryEntity build(){return new BeneficiaryEntity(this);}
    }


}
