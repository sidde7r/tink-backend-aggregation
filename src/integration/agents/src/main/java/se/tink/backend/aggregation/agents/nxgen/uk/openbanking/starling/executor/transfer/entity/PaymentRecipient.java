package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.transfer.entity;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.identifiers.SortCodeIdentifier;

@JsonObject
public class PaymentRecipient {

    private String payeeName;
    private String payeeType;
    private String countryCode;
    private String accountIdentifier;
    private String bankIdentifier;
    private String bankIdentifierType;

    private PaymentRecipient(Builder builder) {
        this.payeeName = builder.getPayeeName();
        this.payeeType = builder.getPayeeType();
        this.countryCode = builder.getCountryCode();
        this.accountIdentifier = builder.getAccountIdentifier();
        this.bankIdentifier = builder.getBankIdentifier();
        this.bankIdentifierType = builder.getBankIdentifierType();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String payeeName;
        private String payeeType;
        private String countryCode;
        private String accountIdentifier;
        private String bankIdentifier;
        private String bankIdentifierType;

        public PaymentRecipient build() {
            return new PaymentRecipient(this);
        }

        public Builder setPayeeName(String payeeName) {
            this.payeeName = payeeName;
            return this;
        }

        public Builder setPayeeType(String payeeType) {
            this.payeeType = payeeType;
            return this;
        }

        public Builder setCountryCode(String countryCode) {
            this.countryCode = countryCode;
            return this;
        }

        public Builder setDestinationAccount(SortCodeIdentifier identifier) {
            this.bankIdentifier= identifier.getSortCode();
            this.accountIdentifier = identifier.getAccountNumber();
            this.bankIdentifierType = "SORT_CODE";
            return this;
        }

        public String getPayeeName() {
            return payeeName;
        }

        public String getPayeeType() {
            return payeeType;
        }

        public String getCountryCode() {
            return countryCode;
        }

        public String getAccountIdentifier() {
            return accountIdentifier;
        }

        public String getBankIdentifier() {
            return bankIdentifier;
        }

        public String getBankIdentifierType() {
            return bankIdentifierType;
        }
    }
}
