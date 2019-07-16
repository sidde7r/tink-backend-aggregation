package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.enums.SparebankPaymentProduct;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.payment.rpc.Creditor;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class CreditorAccountEntity {
    private String iban;
    private String bban;

    public CreditorAccountEntity() {}

    public CreditorAccountEntity(Creditor creditor, SparebankPaymentProduct paymentProduct) {
        switch (paymentProduct) {
            case SEPA_CREDIT_TRANSFER:
                setUpIban(creditor);
                break;
            default:
                setUpBban(creditor);
        }
    }

    public static CreditorAccountEntity of(
            PaymentRequest paymentRequest, SparebankPaymentProduct paymentProduct) {
        Creditor creditor = paymentRequest.getPayment().getCreditor();
        return new CreditorAccountEntity(creditor, paymentProduct);
    }

    public Creditor toTinkCreditor() {
        return iban != null
                ? new Creditor(AccountIdentifier.create(Type.IBAN, iban))
                : new Creditor(AccountIdentifier.create(Type.NO, bban));
    }

    private void setUpBban(Creditor creditor) {
        if (creditor.getAccountIdentifierType() == Type.IBAN) {
            this.bban = creditor.getAccountNumber().substring(4);
        } else {
            this.bban = creditor.getAccountNumber();
        }
    }

    private void setUpIban(Creditor creditor) {
        this.iban = creditor.getAccountNumber();
    }
}
