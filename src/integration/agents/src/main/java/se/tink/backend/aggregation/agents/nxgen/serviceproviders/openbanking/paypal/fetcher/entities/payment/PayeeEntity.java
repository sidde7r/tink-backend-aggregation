package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.payment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.executor.payment.enums.PayPalAccountType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.TinkIdentifier;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
public class PayeeEntity {
    private String id;

    private String type;

    public PayeeEntity() {}

    private PayeeEntity(Builder builder) {
        this.id = builder.id;
        this.type = builder.type;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    @JsonIgnore
    public static PayeeEntity of(PaymentRequest paymentRequest) {
        String type =
                PayPalAccountType.mapToPayPalAccountType(
                                paymentRequest.getPayment().getDebtor().getAccountIdentifierType())
                        .toString();
        String id = paymentRequest.getPayment().getDebtor().getAccountNumber();

        return new Builder().withId(id).withType(type).build();
    }

    public Debtor toTinkDebtor() {
        AccountIdentifier accountIdentifier;
        PayPalAccountType payPalAccountType = PayPalAccountType.fromString(type);
        AccountIdentifier.Type tinkType = payPalAccountType.mapToTinkAccountType();
        switch (tinkType) {
            case TINK:
                accountIdentifier = new TinkIdentifier(id);
                break;
            default:
                throw new IllegalArgumentException("Unrecognized Tink account type " + type);
        }
        return new Debtor(accountIdentifier);
    }

    public static class Builder {
        private String id;

        private String type;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public PayeeEntity build() {
            return new PayeeEntity(this);
        }
    }
}
