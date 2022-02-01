package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.executor.payment.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.UpperCamelCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.RequestConstants;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
@JsonInclude(Include.NON_NULL)
@JsonNaming(UpperCamelCaseStrategy.class)
public class InitiationEntity {

    private String instructionIdentification;
    private String endToEndIdentification;
    private String currencyOfTransfer;
    private InstructedAmountEntity instructedAmount;
    private PersonAccountEntity debtorAccount;
    private PersonAccountEntity creditorAccount;
    private String localInstrument;

    public InitiationEntity() {}

    @JsonIgnore
    private InitiationEntity(Builder builder) {
        this.instructionIdentification = builder.instructionIdentification;
        this.endToEndIdentification = builder.endToEndIdentification;
        this.currencyOfTransfer = builder.currencyOfTransfer;

        this.instructedAmount = builder.instructedAmount;
        this.creditorAccount = builder.creditorAccount;
        this.debtorAccount = builder.debtorAccount;

        this.localInstrument = builder.localInstrument;
    }

    @JsonIgnore
    public static InitiationEntity of(PaymentRequest paymentRequest) {
        InstructedAmountEntity instructedAmount = InstructedAmountEntity.of(paymentRequest);
        PersonAccountEntity creditorAccount = PersonAccountEntity.creditorOf(paymentRequest);
        PersonAccountEntity debtorAccount = PersonAccountEntity.debtorOf(paymentRequest);

        return new InitiationEntity.Builder()
                .withInstructionIdentification(
                        paymentRequest.getPayment().getRemittanceInformation().getValue())
                .withEndToEndIdentification(randomStringNumber())
                .withCurrencyOfTransfer(paymentRequest.getPayment().getCurrency())
                .withInstructedAmount(instructedAmount)
                .withCreditorAccount(creditorAccount)
                .withDebtorAccount(debtorAccount)
                .withLocalInstrument(RequestConstants.SEPA_CREDIT_TRANSFER)
                .build();
    }

    public String getInstructionIdentification() {
        return instructionIdentification;
    }

    public void setInstructionIdentification(String instructionIdentification) {
        this.instructionIdentification = instructionIdentification;
    }

    public String getEndToEndIdentification() {
        return endToEndIdentification;
    }

    public void setEndToEndIdentification(String endToEndIdentification) {
        this.endToEndIdentification = endToEndIdentification;
    }

    public String getCurrencyOfTransfer() {
        return currencyOfTransfer;
    }

    public InstructedAmountEntity getInstructedAmount() {
        return instructedAmount;
    }

    public PersonAccountEntity getCreditorAccount() {
        return creditorAccount;
    }

    public PersonAccountEntity getDebtorAccount() {
        return debtorAccount;
    }

    private static String randomStringNumber() {
        return Integer.toString(RandomUtils.randomInt(Integer.MAX_VALUE - 1));
    }

    public static class Builder {
        private String instructionIdentification;
        private String endToEndIdentification;
        private String currencyOfTransfer;
        private InstructedAmountEntity instructedAmount;
        private PersonAccountEntity creditorAccount;
        private PersonAccountEntity debtorAccount;
        private String localInstrument;

        public Builder withInstructionIdentification(String instructionIdentification) {
            this.instructionIdentification = instructionIdentification;
            return this;
        }

        public Builder withEndToEndIdentification(String endToEndIdentification) {
            this.endToEndIdentification = endToEndIdentification;
            return this;
        }

        public Builder withCurrencyOfTransfer(String currencyOfTransfer) {
            this.currencyOfTransfer = currencyOfTransfer;
            return this;
        }

        public Builder withInstructedAmount(InstructedAmountEntity instructedAmount) {
            this.instructedAmount = instructedAmount;
            return this;
        }

        public Builder withCreditorAccount(PersonAccountEntity creditorAccount) {
            this.creditorAccount = creditorAccount;
            return this;
        }

        public Builder withDebtorAccount(PersonAccountEntity debtorAccount) {
            this.debtorAccount = debtorAccount;
            return this;
        }

        public Builder withLocalInstrument(String localInstrument) {
            this.localInstrument = localInstrument;
            return this;
        }

        public InitiationEntity build() {
            return new InitiationEntity(this);
        }
    }
}
