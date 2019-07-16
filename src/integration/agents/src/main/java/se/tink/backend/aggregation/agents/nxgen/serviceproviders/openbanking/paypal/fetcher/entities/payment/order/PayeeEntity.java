package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.payment.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.executor.payment.enums.PayPalAccountType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.TinkIdentifier;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
public class PayeeEntity {

    @JsonProperty("email_address")
    private String emailAddress;

    public PayeeEntity() {}

    private PayeeEntity(Builder builder) {
        this.emailAddress = builder.emailAddress;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    @JsonIgnore
    public static PayeeEntity of(PaymentRequest paymentRequest) {
        return new Builder()
                .withEmailAddress(paymentRequest.getPayment().getDebtor().getAccountNumber())
                .build();
    }

    @JsonIgnore
    public Debtor toTinkDebtor() {
        PayPalAccountType accountType = PayPalAccountType.EMAIL;
        AccountIdentifier accountIdentifier;
        switch (accountType.mapToTinkAccountType()) {
            case TINK:
                accountIdentifier = new TinkIdentifier(emailAddress);
                break;
            default:
                throw new IllegalArgumentException(
                        "Unrecognized PayPal account type " + accountType);
        }
        return new Debtor(accountIdentifier);
    }

    public static class Builder {
        private String emailAddress;

        public Builder withEmailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
            return this;
        }

        public PayeeEntity build() {
            return new PayeeEntity(this);
        }
    }
}
