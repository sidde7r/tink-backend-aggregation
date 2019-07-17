package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.payment.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants.ExceptionMessages;
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

    public PayeeEntity(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    @JsonIgnore
    public static PayeeEntity of(PaymentRequest paymentRequest) {
        return new PayeeEntity(paymentRequest.getPayment().getDebtor().getAccountNumber());
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
                        String.format(ExceptionMessages.UNRECOGNIZED_PAYPAL_ACCOUNT, accountType));
        }
        return new Debtor(accountIdentifier);
    }
}
