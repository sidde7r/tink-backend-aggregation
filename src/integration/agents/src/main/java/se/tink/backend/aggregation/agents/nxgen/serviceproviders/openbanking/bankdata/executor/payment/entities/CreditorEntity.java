package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Creditor;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class CreditorEntity {
    private String bban;
    private String iban;

    public CreditorEntity() {}

    private CreditorEntity(Creditor creditor, PaymentType paymentType) {
        switch (paymentType) {
            case SEPA:
                setUpIban(creditor);
                break;
            default:
                setUpBban(creditor);
        }
    }

    @JsonIgnore
    private void setUpIban(Creditor creditor) {
        this.iban = creditor.getAccountNumber();
    }

    @JsonIgnore
    private void setUpBban(Creditor creditor) {
        if (creditor.getAccountIdentifierType() == AccountIdentifierType.IBAN) {
            this.bban = creditor.getAccountNumber().substring(4);
        } else {
            this.bban = creditor.getAccountNumber();
        }
    }

    @JsonIgnore
    public static CreditorEntity of(PaymentRequest paymentRequest, PaymentType type) {
        return new CreditorEntity(paymentRequest.getPayment().getCreditor(), type);
    }

    @JsonIgnore
    public Creditor toTinkCreditor(PaymentType paymentType) {
        if (paymentType == PaymentType.SEPA) {
            return new Creditor(AccountIdentifier.create(AccountIdentifierType.IBAN, iban));
        }

        return new Creditor(AccountIdentifier.create(AccountIdentifierType.DK, bban));
    }
}
